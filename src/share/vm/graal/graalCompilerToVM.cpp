/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include "precompiled.hpp"
#include "memory/oopFactory.hpp"
#include "oops/generateOopMap.hpp"
#include "oops/fieldStreams.hpp"
#include "runtime/fieldDescriptor.hpp"
#include "runtime/javaCalls.hpp"
#include "graal/graalRuntime.hpp"
#include "compiler/compileBroker.hpp"
#include "compiler/compilerOracle.hpp"
#include "compiler/disassembler.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "gc_implementation/g1/heapRegion.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/vmStructs.hpp"
#include "runtime/gpu.hpp"


Method* getMethodFromHotSpotMethod(oop hotspot_method) {
  assert(hotspot_method != NULL && hotspot_method->is_a(HotSpotResolvedJavaMethod::klass()), "sanity");
  return asMethod(HotSpotResolvedJavaMethod::metaspaceMethod(hotspot_method));
}

// Entry to native method implementation that transitions current thread to '_thread_in_vm'.
#define C2V_VMENTRY(result_type, name, signature) \
  JNIEXPORT result_type JNICALL c2v_ ## name signature { \
  TRACE_graal_3("CompilerToVM::" #name); \
  GRAAL_VM_ENTRY_MARK; \

// Entry to native method implementation that calls a JNI function
// and hence cannot transition current thread to '_thread_in_vm'.
#define C2V_ENTRY(result_type, name, signature) \
  JNIEXPORT result_type JNICALL c2v_ ## name signature { \
  TRACE_graal_3("CompilerToVM::" #name); \

#define C2V_END }

C2V_ENTRY(jbyteArray, initializeBytecode, (JNIEnv *env, jobject, jlong metaspace_method, jbyteArray result))
  methodHandle method = asMethod(metaspace_method);
  ResourceMark rm;

  int code_size = method->code_size();
  jbyte* reconstituted_code = NEW_RESOURCE_ARRAY(jbyte, code_size);

  guarantee(method->method_holder()->is_rewritten(), "Method's holder should be rewritten");
  // iterate over all bytecodes and replace non-Java bytecodes

  for (BytecodeStream s(method); s.next() != Bytecodes::_illegal; ) {
    Bytecodes::Code code = s.code();
    Bytecodes::Code raw_code = s.raw_code();
    int bci = s.bci();
    int len = s.instruction_size();

    // Restore original byte code.
    reconstituted_code[bci] = (jbyte) (s.is_wide()? Bytecodes::_wide : code);
    if (len > 1) {
      memcpy(&reconstituted_code[bci+1], s.bcp()+1, len-1);
    }

    if (len > 1) {
      // Restore the big-endian constant pool indexes.
      // Cf. Rewriter::scan_method
      switch (code) {
        case Bytecodes::_getstatic:
        case Bytecodes::_putstatic:
        case Bytecodes::_getfield:
        case Bytecodes::_putfield:
        case Bytecodes::_invokevirtual:
        case Bytecodes::_invokespecial:
        case Bytecodes::_invokestatic:
        case Bytecodes::_invokeinterface:
        case Bytecodes::_invokehandle: {
          int cp_index = Bytes::get_native_u2((address) &reconstituted_code[bci + 1]);
          Bytes::put_Java_u2((address) &reconstituted_code[bci + 1], (u2) cp_index);
          break;
        }

        case Bytecodes::_invokedynamic:
          int cp_index = Bytes::get_native_u4((address) &reconstituted_code[bci + 1]);
          Bytes::put_Java_u4((address) &reconstituted_code[bci + 1], (u4) cp_index);
          break;
      }

      // Not all ldc byte code are rewritten.
      switch (raw_code) {
        case Bytecodes::_fast_aldc: {
          int cpc_index = reconstituted_code[bci + 1] & 0xff;
          int cp_index = method->constants()->object_to_cp_index(cpc_index);
          assert(cp_index < method->constants()->length(), "sanity check");
          reconstituted_code[bci + 1] = (jbyte) cp_index;
          break;
        }

        case Bytecodes::_fast_aldc_w: {
          int cpc_index = Bytes::get_native_u2((address) &reconstituted_code[bci + 1]);
          int cp_index = method->constants()->object_to_cp_index(cpc_index);
          assert(cp_index < method->constants()->length(), "sanity check");
          Bytes::put_Java_u2((address) &reconstituted_code[bci + 1], (u2) cp_index);
          break;
        }
      }
    }
  }

  env->SetByteArrayRegion(result, 0, code_size, reconstituted_code);

  return result;
C2V_END

C2V_VMENTRY(jlong, exceptionTableStart, (JNIEnv *, jobject, jlong metaspace_method))
  ResourceMark rm;
  methodHandle method = asMethod(metaspace_method);
  assert(method->exception_table_length() != 0, "should be handled in Java code");
  return (jlong) (address) method->exception_table_start();
C2V_END

C2V_VMENTRY(jint, hasBalancedMonitors, (JNIEnv *, jobject, jlong metaspace_method))

  // Analyze the method to see if monitors are used properly.
  methodHandle method(THREAD, asMethod(metaspace_method));
  assert(method->has_monitor_bytecodes(), "should have checked this");

  // Check to see if a previous compilation computed the monitor-matching analysis.
  if (method->guaranteed_monitor_matching()) {
    return true;
  }

  {
    EXCEPTION_MARK;
    ResourceMark rm(THREAD);
    GeneratePairingInfo gpi(method);
    gpi.compute_map(CATCH);
    if (!gpi.monitor_safe()) {
      return false;
    }
    method->set_guaranteed_monitor_matching();
  }
  return true;
C2V_END

C2V_VMENTRY(jlong, getMetaspaceMethod, (JNIEnv *, jobject, jclass holder_handle, jint slot))
  oop java_class = JNIHandles::resolve(holder_handle);
  Klass* holder = java_lang_Class::as_Klass(java_class);
  methodHandle method = InstanceKlass::cast(holder)->method_with_idnum(slot);
  return (jlong) (address) method();
}

C2V_VMENTRY(jlong, findUniqueConcreteMethod, (JNIEnv *, jobject, jlong metaspace_method))
  methodHandle method = asMethod(metaspace_method);
  KlassHandle holder = method->method_holder();
  assert(!holder->is_interface(), "should be handled in Java code");
  ResourceMark rm;
  MutexLocker locker(Compile_lock);
  Method* ucm = Dependencies::find_unique_concrete_method(holder(), method());
  return (jlong) (address) ucm;
C2V_END

C2V_VMENTRY(jlong, getKlassImplementor, (JNIEnv *, jobject, jlong metaspace_klass))
  InstanceKlass* klass = (InstanceKlass*) asKlass(metaspace_klass);
  return (jlong) (address) klass->implementor();
C2V_END

C2V_VMENTRY(void, initializeMethod,(JNIEnv *, jobject, jlong metaspace_method, jobject hotspot_method))
  methodHandle method = asMethod(metaspace_method);
  InstanceKlass::cast(HotSpotResolvedJavaMethod::klass())->initialize(CHECK);
  HotSpotResolvedJavaMethod::set_exceptionHandlerCount(hotspot_method, method->exception_table_length());
  HotSpotResolvedJavaMethod::set_callerSensitive(hotspot_method, method->caller_sensitive());
  HotSpotResolvedJavaMethod::set_forceInline(hotspot_method, method->force_inline());
  HotSpotResolvedJavaMethod::set_dontInline(hotspot_method, method->dont_inline());
  HotSpotResolvedJavaMethod::set_ignoredBySecurityStackWalk(hotspot_method, method->is_ignored_by_security_stack_walk());
C2V_END

C2V_VMENTRY(jboolean, isMethodCompilable,(JNIEnv *, jobject, jlong metaspace_method))
  methodHandle method = asMethod(metaspace_method);
  return !method->is_not_compilable() && !CompilerOracle::should_not_inline(method);
C2V_END

C2V_ENTRY(jint, getCompiledCodeSize, (JNIEnv *env, jobject, jlong metaspace_method))
  nmethod* code = (asMethod(metaspace_method))->code();
  return code == NULL ? 0 : code->insts_size();
C2V_END

C2V_VMENTRY(jlong, lookupType, (JNIEnv *env, jobject, jstring jname, jclass accessing_class, jboolean eagerResolve))
  ResourceMark rm;
  Handle name = JNIHandles::resolve(jname);
  Symbol* class_name = java_lang_String::as_symbol(name, THREAD);
  assert(class_name != NULL, "name to symbol creation failed");
  assert(class_name->size() > 1, "primitive types should be handled in Java code");

  Klass* resolved_klass = NULL;
  Handle class_loader;
  Handle protection_domain;
  if (JNIHandles::resolve(accessing_class) != NULL) {
    Klass* accessing_klass = java_lang_Class::as_Klass(JNIHandles::resolve(accessing_class));
    class_loader = accessing_klass->class_loader();
    protection_domain = accessing_klass->protection_domain();
  }

  if (eagerResolve) {
    resolved_klass = SystemDictionary::resolve_or_fail(class_name, class_loader, protection_domain, true, THREAD);
  } else {
    resolved_klass = SystemDictionary::resolve_or_null(class_name, class_loader, protection_domain, THREAD);
  }

  return (jlong) (address) resolved_klass;
C2V_END

C2V_VMENTRY(jobject, lookupConstantInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index))
  ConstantPool* cp = (ConstantPool*) metaspace_constant_pool;
  oop result = NULL;
  constantTag tag = cp->tag_at(index);
  switch (tag.value()) {
  case JVM_CONSTANT_String:
      result = cp->resolve_possibly_cached_constant_at(index, CHECK_NULL);
      break;
  case JVM_CONSTANT_MethodHandle:
  case JVM_CONSTANT_MethodHandleInError:
  case JVM_CONSTANT_MethodType:
  case JVM_CONSTANT_MethodTypeInError:
      result = cp->resolve_constant_at(index, CHECK_NULL);
      break;
  default:
    fatal(err_msg_res("unknown constant pool tag %s at cpi %d in %s", tag.internal_name(), index, cp->pool_holder()->name()->as_C_string()));
  }
  return JNIHandles::make_local(THREAD, result);
C2V_END

C2V_VMENTRY(jobject, lookupAppendixInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index, jbyte opcode))
  Bytecodes::Code bc = (Bytecodes::Code) (((int) opcode) & 0xFF);
  index = GraalCompiler::to_cp_index(index, bc);
  constantPoolHandle cp = (ConstantPool*) metaspace_constant_pool;
  oop appendix_oop = ConstantPool::appendix_at_if_loaded(cp, index);
  return JNIHandles::make_local(THREAD, appendix_oop);
C2V_END

C2V_VMENTRY(jobject, lookupMethodInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index, jbyte opcode))
  constantPoolHandle cp = (ConstantPool*) metaspace_constant_pool;
  instanceKlassHandle pool_holder(cp->pool_holder());

  Bytecodes::Code bc = (Bytecodes::Code) (((int) opcode) & 0xFF);
  int cp_index = GraalCompiler::to_cp_index(index, bc);

  methodHandle method = GraalEnv::get_method_by_index(cp, cp_index, bc, pool_holder);
  if (!method.is_null()) {
    Handle holder = VMToCompiler::createResolvedJavaType(method->method_holder()->java_mirror(), CHECK_NULL);
    return JNIHandles::make_local(THREAD, VMToCompiler::createResolvedJavaMethod(holder, method(), THREAD));
  } else {
    // Get the method's name and signature.
    Handle name = java_lang_String::create_from_symbol(cp->name_ref_at(cp_index), CHECK_NULL);
    Handle signature  = java_lang_String::create_from_symbol(cp->signature_ref_at(cp_index), CHECK_NULL);
    Handle type;
    if (bc != Bytecodes::_invokedynamic) {
      int holder_index = cp->klass_ref_index_at(cp_index);
      type = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
    } else {
      type = Handle(SystemDictionary::MethodHandle_klass()->java_mirror());
    }
    return JNIHandles::make_local(THREAD, VMToCompiler::createUnresolvedJavaMethod(name, signature, type, THREAD));
  }
C2V_END

C2V_VMENTRY(jobject, lookupTypeInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index))
  ConstantPool* cp = (ConstantPool*) metaspace_constant_pool;
  Handle result = GraalCompiler::get_JavaType(cp, index, cp->pool_holder(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, result());
C2V_END

C2V_VMENTRY(void, lookupReferencedTypeInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index, jbyte op))
  ConstantPool* cp = (ConstantPool*) metaspace_constant_pool;
  Bytecodes::Code bc = (Bytecodes::Code) (((int) op) & 0xFF);
  if (bc != Bytecodes::_checkcast && bc != Bytecodes::_instanceof && bc != Bytecodes::_new && bc != Bytecodes::_anewarray
      && bc != Bytecodes::_multianewarray && bc != Bytecodes::_ldc && bc != Bytecodes::_ldc_w && bc != Bytecodes::_ldc2_w)
  {
    index = cp->remap_instruction_operand_from_cache(GraalCompiler::to_cp_index(index, bc));
  }
  constantTag tag = cp->tag_at(index);
  if (tag.is_field_or_method()) {
    index = cp->uncached_klass_ref_index_at(index);
    tag = cp->tag_at(index);
  }

  if (tag.is_unresolved_klass() || tag.is_klass()) {
    Klass* klass = cp->klass_at(index, CHECK);
    if (klass->oop_is_instance()) {
      InstanceKlass::cast(klass)->initialize(CHECK);
    }
  }
C2V_END

C2V_VMENTRY(jobject, lookupFieldInPool, (JNIEnv *env, jobject, jlong metaspace_constant_pool, jint index, jbyte opcode))
  ResourceMark rm;

  int cp_index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = (ConstantPool*) metaspace_constant_pool;

  int nt_index = cp->name_and_type_ref_index_at(cp_index);
  int sig_index = cp->signature_ref_index_at(nt_index);
  Symbol* signature = cp->symbol_at(sig_index);
  int name_index = cp->name_ref_index_at(nt_index);
  Symbol* name = cp->symbol_at(name_index);
  int holder_index = cp->klass_ref_index_at(cp_index);
  Handle holder = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
  instanceKlassHandle holder_klass;

  Bytecodes::Code code = (Bytecodes::Code)(((int) opcode) & 0xFF);
  int offset = -1;
  AccessFlags flags;
  if (holder->klass() == SystemDictionary::HotSpotResolvedObjectType_klass()) {
    fieldDescriptor result;
    LinkResolver::resolve_field_access(result, cp, cp_index, Bytecodes::java_code(code), true, false, Thread::current());

    if (HAS_PENDING_EXCEPTION) {
      CLEAR_PENDING_EXCEPTION;
    } else {
      offset = result.offset();
      flags = result.access_flags();
      holder_klass = result.field_holder();
      holder = VMToCompiler::createResolvedJavaType(holder_klass->java_mirror(), CHECK_NULL);
    }
  }

  Handle type = GraalCompiler::get_JavaTypeFromSignature(signature, cp->pool_holder(), CHECK_NULL);
  Handle field_handle = GraalCompiler::get_JavaField(offset, flags.as_int(), name, holder, type, THREAD);

  return JNIHandles::make_local(THREAD, field_handle());
C2V_END

C2V_VMENTRY(jlong, resolveMethod, (JNIEnv *, jobject, jobject resolved_type, jstring name, jstring signature))
  assert(JNIHandles::resolve(resolved_type) != NULL, "");
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(resolved_type));
  Symbol* name_symbol = java_lang_String::as_symbol(JNIHandles::resolve(name), THREAD);
  Symbol* signature_symbol = java_lang_String::as_symbol(JNIHandles::resolve(signature), THREAD);
  Method* method = klass->lookup_method(name_symbol, signature_symbol);
  if (method == NULL) {
    if (TraceGraal >= 3) {
      ResourceMark rm;
      tty->print_cr("Could not resolve method %s %s on klass %s", name_symbol->as_C_string(), signature_symbol->as_C_string(), klass->name()->as_C_string());
    }
    return 0;
  }
  return (jlong) (address) method;
C2V_END

C2V_VMENTRY(jboolean, hasFinalizableSubclass,(JNIEnv *, jobject, jobject hotspot_klass))
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(hotspot_klass));
  assert(klass != NULL, "method must not be called for primitive types");
  return Dependencies::find_finalizable_subclass(klass) != NULL;
C2V_END

C2V_VMENTRY(jobject, getInstanceFields, (JNIEnv *, jobject, jobject klass))
  ResourceMark rm;

  instanceKlassHandle k = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(klass));
  GrowableArray<Handle> fields(k->java_fields_count());

  for (AllFieldStream fs(k()); !fs.done(); fs.next()) {
    if (!fs.access_flags().is_static()) {
      Handle type = GraalCompiler::get_JavaTypeFromSignature(fs.signature(), k, THREAD);
      int flags = fs.access_flags().as_int();
      bool internal = fs.access_flags().is_internal();
      Handle name = java_lang_String::create_from_symbol(fs.name(), THREAD);
      Handle field = VMToCompiler::createJavaField(JNIHandles::resolve(klass), name, type, fs.offset(), flags, internal, THREAD);
      fields.append(field());
    }
  }
  objArrayHandle field_array = oopFactory::new_objArray(SystemDictionary::HotSpotResolvedJavaField_klass(), fields.length(), CHECK_NULL);
  for (int i = 0; i < fields.length(); ++i) {
    field_array->obj_at_put(i, fields.at(i)());
  }
  return JNIHandles::make_local(THREAD, field_array());
C2V_END

C2V_VMENTRY(jlong, getClassInitializer, (JNIEnv *, jobject, jobject klass))
  instanceKlassHandle k(THREAD, java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(klass)));
  Method* clinit = k->class_initializer();
  return (jlong) (address) clinit;
C2V_END

C2V_VMENTRY(jlong, getMaxCallTargetOffset, (JNIEnv *env, jobject, jlong addr))
  address target_addr = (address) addr;
  if (target_addr != 0x0) {
    int64_t off_low = (int64_t)target_addr - ((int64_t)CodeCache::low_bound() + sizeof(int));
    int64_t off_high = (int64_t)target_addr - ((int64_t)CodeCache::high_bound() + sizeof(int));
    return MAX2(ABS(off_low), ABS(off_high));
  }
  return -1;
C2V_END


// helpers used to set fields in the HotSpotVMConfig object
jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig) {
  jfieldID id = env->GetFieldID(env->GetObjectClass(obj), name, sig);
  if (id == NULL) {
    fatal(err_msg("field not found: %s (%s)", name, sig));
  }
  return id;
}

C2V_VMENTRY(void, doNotInlineOrCompile,(JNIEnv *, jobject,  jlong metaspace_method))
  methodHandle method = asMethod(metaspace_method);
  method->set_not_c1_compilable();
  method->set_not_c2_compilable();
  method->set_dont_inline(true);
C2V_END

extern "C" {
extern VMStructEntry* gHotSpotVMStructs;
extern uint64_t gHotSpotVMStructEntryTypeNameOffset;
extern uint64_t gHotSpotVMStructEntryFieldNameOffset;
extern uint64_t gHotSpotVMStructEntryTypeStringOffset;
extern uint64_t gHotSpotVMStructEntryIsStaticOffset;
extern uint64_t gHotSpotVMStructEntryOffsetOffset;
extern uint64_t gHotSpotVMStructEntryAddressOffset;
extern uint64_t gHotSpotVMStructEntryArrayStride;

extern VMTypeEntry* gHotSpotVMTypes;
extern uint64_t gHotSpotVMTypeEntryTypeNameOffset;
extern uint64_t gHotSpotVMTypeEntrySuperclassNameOffset;
extern uint64_t gHotSpotVMTypeEntryIsOopTypeOffset;
extern uint64_t gHotSpotVMTypeEntryIsIntegerTypeOffset;
extern uint64_t gHotSpotVMTypeEntryIsUnsignedOffset;
extern uint64_t gHotSpotVMTypeEntrySizeOffset;
extern uint64_t gHotSpotVMTypeEntryArrayStride;

extern VMIntConstantEntry* gHotSpotVMIntConstants;
extern uint64_t gHotSpotVMIntConstantEntryNameOffset;
extern uint64_t gHotSpotVMIntConstantEntryValueOffset;
extern uint64_t gHotSpotVMIntConstantEntryArrayStride;

extern VMLongConstantEntry* gHotSpotVMLongConstants;
extern uint64_t gHotSpotVMLongConstantEntryNameOffset;
extern uint64_t gHotSpotVMLongConstantEntryValueOffset;
extern uint64_t gHotSpotVMLongConstantEntryArrayStride;
}

C2V_ENTRY(void, initializeConfiguration, (JNIEnv *env, jobject, jobject config))

#define set_boolean(name, value) do { env->SetBooleanField(config, getFieldID(env, config, name, "Z"), value); } while (0)
#define set_int(name, value) do { env->SetIntField(config, getFieldID(env, config, name, "I"), value); } while (0)
#define set_long(name, value) do { env->SetLongField(config, getFieldID(env, config, name, "J"), value); } while (0)
#define set_address(name, value) do { set_long(name, (jlong) value); } while (0)

  guarantee(HeapWordSize == sizeof(char*), "Graal assumption that HeadWordSize == machine word size is wrong");

  set_address("gHotSpotVMStructs", gHotSpotVMStructs);
  set_long("gHotSpotVMStructEntryTypeNameOffset",   gHotSpotVMStructEntryTypeNameOffset);
  set_long("gHotSpotVMStructEntryFieldNameOffset",  gHotSpotVMStructEntryFieldNameOffset);
  set_long("gHotSpotVMStructEntryTypeStringOffset", gHotSpotVMStructEntryTypeStringOffset);
  set_long("gHotSpotVMStructEntryIsStaticOffset",   gHotSpotVMStructEntryIsStaticOffset);
  set_long("gHotSpotVMStructEntryOffsetOffset",     gHotSpotVMStructEntryOffsetOffset);
  set_long("gHotSpotVMStructEntryAddressOffset",    gHotSpotVMStructEntryAddressOffset);
  set_long("gHotSpotVMStructEntryArrayStride",      gHotSpotVMStructEntryArrayStride);

  set_address("gHotSpotVMTypes", gHotSpotVMTypes);
  set_long("gHotSpotVMTypeEntryTypeNameOffset",       gHotSpotVMTypeEntryTypeNameOffset);
  set_long("gHotSpotVMTypeEntrySuperclassNameOffset", gHotSpotVMTypeEntrySuperclassNameOffset);
  set_long("gHotSpotVMTypeEntryIsOopTypeOffset",      gHotSpotVMTypeEntryIsOopTypeOffset);
  set_long("gHotSpotVMTypeEntryIsIntegerTypeOffset",  gHotSpotVMTypeEntryIsIntegerTypeOffset);
  set_long("gHotSpotVMTypeEntryIsUnsignedOffset",     gHotSpotVMTypeEntryIsUnsignedOffset);
  set_long("gHotSpotVMTypeEntrySizeOffset",           gHotSpotVMTypeEntrySizeOffset);
  set_long("gHotSpotVMTypeEntryArrayStride",          gHotSpotVMTypeEntryArrayStride);

  set_address("gHotSpotVMIntConstants", gHotSpotVMIntConstants);
  set_long("gHotSpotVMIntConstantEntryNameOffset",  gHotSpotVMIntConstantEntryNameOffset);
  set_long("gHotSpotVMIntConstantEntryValueOffset", gHotSpotVMIntConstantEntryValueOffset);
  set_long("gHotSpotVMIntConstantEntryArrayStride", gHotSpotVMIntConstantEntryArrayStride);

  set_address("gHotSpotVMLongConstants", gHotSpotVMLongConstants);
  set_long("gHotSpotVMLongConstantEntryNameOffset",  gHotSpotVMLongConstantEntryNameOffset);
  set_long("gHotSpotVMLongConstantEntryValueOffset", gHotSpotVMLongConstantEntryValueOffset);
  set_long("gHotSpotVMLongConstantEntryArrayStride", gHotSpotVMLongConstantEntryArrayStride);

  //------------------------------------------------------------------------------------------------

  set_int("arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());

  set_int("extraStackEntries", Method::extra_stack_entries());

  set_int("tlabAlignmentReserve", (int32_t)ThreadLocalAllocBuffer::alignment_reserve());
  set_long("heapTopAddress", (jlong)(address) Universe::heap()->top_addr());
  set_long("heapEndAddress", (jlong)(address) Universe::heap()->end_addr());

  set_boolean("inlineContiguousAllocationSupported", !CMSIncrementalMode && Universe::heap()->supports_inline_contig_alloc());

  set_long("verifyOopMask", Universe::verify_oop_mask());
  set_long("verifyOopBits", Universe::verify_oop_bits());

  set_int("instanceKlassVtableStartOffset", InstanceKlass::vtable_start_offset() * HeapWordSize);

  //------------------------------------------------------------------------------------------------

  set_address("handleDeoptStub", SharedRuntime::deopt_blob()->unpack());
  set_address("uncommonTrapStub", SharedRuntime::deopt_blob()->uncommon_trap());

  set_address("registerFinalizerAddress", SharedRuntime::register_finalizer);
  set_address("exceptionHandlerForReturnAddressAddress", SharedRuntime::exception_handler_for_return_address);
  set_address("osrMigrationEndAddress", SharedRuntime::OSR_migration_end);

  set_address("javaTimeMillisAddress", CAST_FROM_FN_PTR(address, os::javaTimeMillis));
  set_address("javaTimeNanosAddress", CAST_FROM_FN_PTR(address, os::javaTimeNanos));
  set_address("arithmeticSinAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dsin));
  set_address("arithmeticCosAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dcos));
  set_address("arithmeticTanAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dtan));

  set_address("newInstanceAddress", GraalRuntime::new_instance);
  set_address("newArrayAddress", GraalRuntime::new_array);
  set_address("newMultiArrayAddress", GraalRuntime::new_multi_array);
  set_address("dynamicNewArrayAddress", GraalRuntime::dynamic_new_array);
  set_address("dynamicNewInstanceAddress", GraalRuntime::dynamic_new_instance);
  set_address("threadIsInterruptedAddress", GraalRuntime::thread_is_interrupted);
  set_address("vmMessageAddress", GraalRuntime::vm_message);
  set_address("identityHashCodeAddress", GraalRuntime::identity_hash_code);
  set_address("exceptionHandlerForPcAddress", GraalRuntime::exception_handler_for_pc);
  set_address("monitorenterAddress", GraalRuntime::monitorenter);
  set_address("monitorexitAddress", GraalRuntime::monitorexit);
  set_address("createNullPointerExceptionAddress", GraalRuntime::create_null_exception);
  set_address("createOutOfBoundsExceptionAddress", GraalRuntime::create_out_of_bounds_exception);
  set_address("logPrimitiveAddress", GraalRuntime::log_primitive);
  set_address("logObjectAddress", GraalRuntime::log_object);
  set_address("logPrintfAddress", GraalRuntime::log_printf);
  set_address("vmErrorAddress", GraalRuntime::vm_error);
  set_address("loadAndClearExceptionAddress", GraalRuntime::load_and_clear_exception);
  set_address("writeBarrierPreAddress", GraalRuntime::write_barrier_pre);
  set_address("writeBarrierPostAddress", GraalRuntime::write_barrier_post);
  set_address("validateObject", GraalRuntime::validate_object);

  //------------------------------------------------------------------------------------------------

  set_int("graalCountersThreadOffset", in_bytes(JavaThread::graal_counters_offset()));
  set_int("graalCountersSize", (jint) GraalCounterSize);

  //------------------------------------------------------------------------------------------------

  set_long("dllLoad", (jlong) os::dll_load);
  set_long("dllLookup", (jlong) os::dll_lookup);
  #if defined(TARGET_OS_FAMILY_bsd) || defined(TARGET_OS_FAMILY_linux)
  set_long("rtldDefault", (jlong) RTLD_DEFAULT);
  #endif

#undef set_boolean
#undef set_int
#undef set_long

C2V_END

C2V_VMENTRY(jint, installCode0, (JNIEnv *jniEnv, jobject, jobject compiled_code, jobject installed_code, jobject speculation_log))
  ResourceMark rm;
  HandleMark hm;
  Handle compiled_code_handle = JNIHandles::resolve(compiled_code);
  CodeBlob* cb = NULL;
  Handle installed_code_handle = JNIHandles::resolve(installed_code);
  Handle speculation_log_handle = JNIHandles::resolve(speculation_log);

  CodeInstaller installer;
  GraalEnv::CodeInstallResult result = installer.install(compiled_code_handle, cb, installed_code_handle, speculation_log_handle);

  if (PrintCodeCacheOnCompilation) {
    stringStream s;
    // Dump code cache  into a buffer before locking the tty,
    {
      MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
      CodeCache::print_summary(&s, false);
    }
    ttyLocker ttyl;
    tty->print_cr(s.as_string());
  }

  if (result != GraalEnv::ok) {
    assert(cb == NULL, "should be");
  } else {
    if (!installed_code_handle.is_null()) {
      assert(installed_code_handle->is_a(HotSpotInstalledCode::klass()), "wrong type");
      HotSpotInstalledCode::set_codeBlob(installed_code_handle, (jlong) cb);
      oop comp_result = HotSpotCompiledCode::comp(compiled_code_handle);
      if (comp_result->is_a(ExternalCompilationResult::klass())) {
        if (TraceGPUInteraction) {
          tty->print_cr("installCode0: ExternalCompilationResult");
        }
        HotSpotInstalledCode::set_codeStart(installed_code_handle, ExternalCompilationResult::entryPoint(comp_result));
      } else {
        HotSpotInstalledCode::set_size(installed_code_handle, cb->size());
        HotSpotInstalledCode::set_codeStart(installed_code_handle, (jlong) cb->code_begin());
        HotSpotInstalledCode::set_codeSize(installed_code_handle, cb->code_size());
      }
      nmethod* nm = cb->as_nmethod_or_null();
      assert(nm == NULL || !installed_code_handle->is_scavengable() || nm->on_scavenge_root_list(), "nm should be scavengable if installed_code is scavengable");
    }
  }
  return result;
C2V_END

C2V_VMENTRY(void, notifyCompilationStatistics, (JNIEnv *jniEnv, jobject, jint id, jobject hotspot_method, jboolean osr, jint processedBytecodes, jlong time, jlong timeUnitsPerSecond, jobject installed_code))
  CompilerStatistics* stats = GraalCompiler::instance()->stats();

  elapsedTimer timer = elapsedTimer(time, timeUnitsPerSecond);
  if (osr) {
    stats->_osr.update(timer, processedBytecodes);
  } else {
    stats->_standard.update(timer, processedBytecodes);
  }
  Handle installed_code_handle = JNIHandles::resolve(installed_code);
  stats->_nmethods_size += HotSpotInstalledCode::size(installed_code_handle);
  stats->_nmethods_code_size += HotSpotInstalledCode::codeSize(installed_code_handle);

  if (CITimeEach) {
    methodHandle method = asMethod(HotSpotResolvedJavaMethod::metaspaceMethod(hotspot_method));
    float bytes_per_sec = 1.0 * processedBytecodes / timer.seconds();
    tty->print_cr("%3d   seconds: %f bytes/sec: %f (bytes %d)",
                  id, timer.seconds(), bytes_per_sec, processedBytecodes);
  }
C2V_END

C2V_VMENTRY(void, printCompilationStatistics, (JNIEnv *jniEnv, jobject, jboolean per_compiler, jboolean aggregate))
  CompileBroker::print_times(per_compiler == JNI_TRUE, aggregate == JNI_TRUE);
C2V_END

C2V_VMENTRY(void, resetCompilationStatistics, (JNIEnv *jniEnv, jobject))
  CompilerStatistics* stats = GraalCompiler::instance()->stats();
  stats->_standard._time.reset();
  stats->_standard._bytes = 0;
  stats->_standard._count = 0;
  stats->_osr._time.reset();
  stats->_osr._bytes = 0;
  stats->_osr._count = 0;
C2V_END

C2V_VMENTRY(jobject, disassembleCodeBlob, (JNIEnv *jniEnv, jobject, jlong codeBlob))
  ResourceMark rm;
  HandleMark hm;

  CodeBlob* cb = (CodeBlob*) (address) codeBlob;
  if (cb == NULL) {
    return NULL;
  }

  // We don't want the stringStream buffer to resize during disassembly as it
  // uses scoped resource memory. If a nested function called during disassembly uses
  // a ResourceMark and the buffer expands within the scope of the mark,
  // the buffer becomes garbage when that scope is exited. Experience shows that
  // the disassembled code is typically about 10x the code size so a fixed buffer
  // sized to 20x code size plus a fixed amount for header info should be sufficient.
  int bufferSize = cb->code_size() * 20 + 1024;
  char* buffer = NEW_RESOURCE_ARRAY(char, bufferSize);
  stringStream st(buffer, bufferSize);
  if (cb->is_nmethod()) {
    nmethod* nm = (nmethod*) cb;
    if (!nm->is_alive()) {
      return NULL;
    }
    Disassembler::decode(nm, &st);
  } else {
    Disassembler::decode(cb, &st);
  }
  if (st.size() <= 0) {
    return NULL;
  }

  Handle result = java_lang_String::create_from_platform_dependent_str(st.as_string(), CHECK_NULL);
  return JNIHandles::make_local(result());
C2V_END

C2V_VMENTRY(jobject, getStackTraceElement, (JNIEnv *env, jobject, jlong metaspace_method, int bci))
  ResourceMark rm;
  HandleMark hm;

  methodHandle method = asMethod(metaspace_method);
  oop element = java_lang_StackTraceElement::create(method, bci, CHECK_NULL);
  return JNIHandles::make_local(element);
C2V_END

C2V_VMENTRY(jobject, executeCompiledMethodVarargs, (JNIEnv *env, jobject, jobject args, jobject hotspotInstalledCode))
  ResourceMark rm;
  HandleMark hm;

  jlong nmethodValue = HotSpotInstalledCode::codeBlob(hotspotInstalledCode);
  if (nmethodValue == 0L) {
    THROW_(vmSymbols::com_oracle_graal_api_code_InvalidInstalledCodeException(), NULL);
  }
  nmethod* nm = (nmethod*) (address) nmethodValue;
  methodHandle mh = nm->method();
  Symbol* signature = mh->signature();
  JavaCallArguments jca(mh->size_of_parameters());

  JavaArgumentUnboxer jap(signature, &jca, (arrayOop) JNIHandles::resolve(args), mh->is_static());
  JavaValue result(jap.get_ret_type());
  jca.set_alternative_target(nm);
  JavaCalls::call(&result, mh, &jca, CHECK_NULL);

  if (jap.get_ret_type() == T_VOID) {
    return NULL;
  } else if (jap.get_ret_type() == T_OBJECT || jap.get_ret_type() == T_ARRAY) {
    return JNIHandles::make_local((oop) result.get_jobject());
  } else {
    oop o = java_lang_boxing_object::create(jap.get_ret_type(), (jvalue *) result.get_value_addr(), CHECK_NULL);
    return JNIHandles::make_local(o);
  }
C2V_END

C2V_VMENTRY(jobject, getDeoptedLeafGraphIds, (JNIEnv *, jobject))

  // the contract for this method is as follows:
  // returning null: no deopted leaf graphs
  // returning array (size > 0): the ids of the deopted leaf graphs
  // returning array (size == 0): there was an overflow, the compiler needs to clear its cache completely

  oop array = GraalCompiler::instance()->dump_deopted_leaf_graphs(CHECK_NULL);
  return JNIHandles::make_local(array);
C2V_END

C2V_ENTRY(jlongArray, getLineNumberTable, (JNIEnv *env, jobject, jobject hotspot_method))
  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  if (!method->has_linenumber_table()) {
    return NULL;
  }
  u2 num_entries = 0;
  CompressedLineNumberReadStream streamForSize(method->compressed_linenumber_table());
  while (streamForSize.read_pair()) {
    num_entries++;
  }

  CompressedLineNumberReadStream stream(method->compressed_linenumber_table());
  jlongArray result = env->NewLongArray(2 * num_entries);

  int i = 0;
  jlong value;
  while (stream.read_pair()) {
    value = ((long) stream.bci());
    env->SetLongArrayRegion(result,i,1,&value);
    value = ((long) stream.line());
    env->SetLongArrayRegion(result,i + 1,1,&value);
    i += 2;
  }

  return result;
C2V_END

C2V_VMENTRY(jlong, getLocalVariableTableStart, (JNIEnv *, jobject, jobject hotspot_method))
  ResourceMark rm;
  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  if (!method->has_localvariable_table()) {
    return 0;
  }
  return (jlong) (address) method->localvariable_table_start();
C2V_END

C2V_VMENTRY(jint, getLocalVariableTableLength, (JNIEnv *, jobject, jobject hotspot_method))
  ResourceMark rm;
  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  return method->localvariable_table_length();
C2V_END

C2V_VMENTRY(void, reprofile, (JNIEnv *env, jobject, jlong metaspace_method))
  Method* method = asMethod(metaspace_method);
  MethodCounters* mcs = method->method_counters();
  if (mcs != NULL) {
    mcs->clear_counters();
  }
  NOT_PRODUCT(method->set_compiled_invocation_count(0));

  nmethod* code = method->code();
  if (code != NULL) {
    code->make_not_entrant();
  }

  MethodData* method_data = method->method_data();
  if (method_data == NULL) {
    ClassLoaderData* loader_data = method->method_holder()->class_loader_data();
    method_data = MethodData::allocate(loader_data, method, CHECK);
    method->set_method_data(method_data);
  } else {
    method_data->initialize(true);
  }
C2V_END


C2V_VMENTRY(void, invalidateInstalledCode, (JNIEnv *env, jobject, jobject hotspotInstalledCode))
  jlong nativeMethod = HotSpotInstalledCode::codeBlob(hotspotInstalledCode);
  nmethod* m = (nmethod*)nativeMethod;
  if (m != NULL && !m->is_not_entrant()) {
    m->mark_for_deoptimization();
    VM_Deoptimize op;
    VMThread::execute(&op);
  }
  HotSpotInstalledCode::set_codeBlob(hotspotInstalledCode, 0);
C2V_END


C2V_VMENTRY(jobject, readUnsafeUncompressedPointer, (JNIEnv *env, jobject, jobject o, jlong offset))
  oop resolved_o = JNIHandles::resolve(o);
  address addr = ((address)resolved_o) + offset;
  return JNIHandles::make_local(*((oop*)addr));
C2V_END

C2V_VMENTRY(jlong, readUnsafeKlassPointer, (JNIEnv *env, jobject, jobject o))
  oop resolved_o = JNIHandles::resolve(o);
  jlong klass = (jlong)(address)resolved_o->klass();
  return klass;
C2V_END

C2V_VMENTRY(jlongArray, collectCounters, (JNIEnv *env, jobject))
  typeArrayOop arrayOop = oopFactory::new_longArray(GraalCounterSize, CHECK_NULL);
  JavaThread::collect_counters(arrayOop);
  return (jlongArray) JNIHandles::make_local(arrayOop);
C2V_END

C2V_ENTRY(jobject, getGPUs, (JNIEnv *env, jobject))
#if defined(TARGET_OS_FAMILY_bsd) || defined(TARGET_OS_FAMILY_linux) || defined(TARGET_OS_FAMILY_windows)
  return gpu::probe_gpus(env);
#else
  return env->NewStringUTF("");
#endif
C2V_END

C2V_VMENTRY(int, allocateCompileId, (JNIEnv *env, jobject, jobject hotspot_method, int entry_bci))
  HandleMark hm;
  ResourceMark rm;
  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  MutexLocker locker(MethodCompileQueue_lock, thread);
  return CompileBroker::assign_compile_id(method, entry_bci);
C2V_END


C2V_VMENTRY(jboolean, isMature, (JNIEnv *env, jobject, jlong metaspace_method_data))
  MethodData* mdo = asMethodData(metaspace_method_data);
  return mdo != NULL && mdo->is_mature();
C2V_END

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &(c2v_ ## f))

#define TYPE                  "Lcom/oracle/graal/api/meta/JavaType;"
#define METHOD                "Lcom/oracle/graal/api/meta/JavaMethod;"
#define FIELD                 "Lcom/oracle/graal/api/meta/JavaField;"
#define SPECULATION_LOG       "Lcom/oracle/graal/api/code/SpeculationLog;"
#define STRING                "Ljava/lang/String;"
#define OBJECT                "Ljava/lang/Object;"
#define CLASS                 "Ljava/lang/Class;"
#define STACK_TRACE_ELEMENT   "Ljava/lang/StackTraceElement;"
#define HS_RESOLVED_TYPE      "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedObjectType;"
#define HS_RESOLVED_METHOD    "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaMethod;"
#define HS_RESOLVED_FIELD     "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaField;"
#define HS_COMPILED_CODE      "Lcom/oracle/graal/hotspot/HotSpotCompiledCode;"
#define HS_CONFIG             "Lcom/oracle/graal/hotspot/HotSpotVMConfig;"
#define HS_INSTALLED_CODE     "Lcom/oracle/graal/hotspot/meta/HotSpotInstalledCode;"
#define METASPACE_KLASS       "J"
#define METASPACE_METHOD      "J"
#define METASPACE_METHOD_DATA "J"
#define METASPACE_CONSTANT_POOL "J"

JNINativeMethod CompilerToVM_methods[] = {
  {CC"initializeBytecode",            CC"("METASPACE_METHOD"[B)[B",                                     FN_PTR(initializeBytecode)},
  {CC"exceptionTableStart",           CC"("METASPACE_METHOD")J",                                        FN_PTR(exceptionTableStart)},
  {CC"hasBalancedMonitors",           CC"("METASPACE_METHOD")Z",                                        FN_PTR(hasBalancedMonitors)},
  {CC"findUniqueConcreteMethod",      CC"("METASPACE_METHOD")"METASPACE_METHOD,                         FN_PTR(findUniqueConcreteMethod)},
  {CC"getKlassImplementor",           CC"("METASPACE_KLASS")"METASPACE_KLASS,                           FN_PTR(getKlassImplementor)},
  {CC"getStackTraceElement",          CC"("METASPACE_METHOD"I)"STACK_TRACE_ELEMENT,                     FN_PTR(getStackTraceElement)},
  {CC"initializeMethod",              CC"("METASPACE_METHOD HS_RESOLVED_METHOD")V",                     FN_PTR(initializeMethod)},
  {CC"doNotInlineOrCompile",          CC"("METASPACE_METHOD")V",                                        FN_PTR(doNotInlineOrCompile)},
  {CC"isMethodCompilable",            CC"("METASPACE_METHOD")Z",                                        FN_PTR(isMethodCompilable)},
  {CC"getCompiledCodeSize",           CC"("METASPACE_METHOD")I",                                        FN_PTR(getCompiledCodeSize)},
  {CC"lookupType",                    CC"("STRING CLASS"Z)"METASPACE_KLASS,                             FN_PTR(lookupType)},
  {CC"lookupConstantInPool",          CC"("METASPACE_CONSTANT_POOL"I)"OBJECT,                           FN_PTR(lookupConstantInPool)},
  {CC"lookupAppendixInPool",          CC"("METASPACE_CONSTANT_POOL"IB)"OBJECT,                          FN_PTR(lookupAppendixInPool)},
  {CC"lookupMethodInPool",            CC"("METASPACE_CONSTANT_POOL"IB)"METHOD,                          FN_PTR(lookupMethodInPool)},
  {CC"lookupTypeInPool",              CC"("METASPACE_CONSTANT_POOL"I)"TYPE,                             FN_PTR(lookupTypeInPool)},
  {CC"lookupReferencedTypeInPool",    CC"("METASPACE_CONSTANT_POOL"IB)V",                               FN_PTR(lookupReferencedTypeInPool)},
  {CC"lookupFieldInPool",             CC"("METASPACE_CONSTANT_POOL"IB)"FIELD,                           FN_PTR(lookupFieldInPool)},
  {CC"resolveMethod",                 CC"("HS_RESOLVED_TYPE STRING STRING")"METASPACE_METHOD,           FN_PTR(resolveMethod)},
  {CC"getInstanceFields",             CC"("HS_RESOLVED_TYPE")["HS_RESOLVED_FIELD,                       FN_PTR(getInstanceFields)},
  {CC"getClassInitializer",           CC"("HS_RESOLVED_TYPE")"METASPACE_METHOD,                         FN_PTR(getClassInitializer)},
  {CC"hasFinalizableSubclass",        CC"("HS_RESOLVED_TYPE")Z",                                        FN_PTR(hasFinalizableSubclass)},
  {CC"getMaxCallTargetOffset",        CC"(J)J",                                                         FN_PTR(getMaxCallTargetOffset)},
  {CC"getMetaspaceMethod",            CC"("CLASS"I)"METASPACE_METHOD,                                   FN_PTR(getMetaspaceMethod)},
  {CC"initializeConfiguration",       CC"("HS_CONFIG")V",                                               FN_PTR(initializeConfiguration)},
  {CC"installCode0",                  CC"("HS_COMPILED_CODE HS_INSTALLED_CODE SPECULATION_LOG")I",      FN_PTR(installCode0)},
  {CC"notifyCompilationStatistics",   CC"(I"HS_RESOLVED_METHOD"ZIJJ"HS_INSTALLED_CODE")V",              FN_PTR(notifyCompilationStatistics)},
  {CC"printCompilationStatistics",    CC"(ZZ)V",                                                        FN_PTR(printCompilationStatistics)},
  {CC"resetCompilationStatistics",    CC"()V",                                                          FN_PTR(resetCompilationStatistics)},
  {CC"disassembleCodeBlob",           CC"(J)"STRING,                                                    FN_PTR(disassembleCodeBlob)},
  {CC"executeCompiledMethodVarargs",  CC"(["OBJECT HS_INSTALLED_CODE")"OBJECT,                          FN_PTR(executeCompiledMethodVarargs)},
  {CC"getDeoptedLeafGraphIds",        CC"()[J",                                                         FN_PTR(getDeoptedLeafGraphIds)},
  {CC"getLineNumberTable",            CC"("HS_RESOLVED_METHOD")[J",                                     FN_PTR(getLineNumberTable)},
  {CC"getLocalVariableTableStart",    CC"("HS_RESOLVED_METHOD")J",                                      FN_PTR(getLocalVariableTableStart)},
  {CC"getLocalVariableTableLength",   CC"("HS_RESOLVED_METHOD")I",                                      FN_PTR(getLocalVariableTableLength)},
  {CC"reprofile",                     CC"("METASPACE_METHOD")V",                                        FN_PTR(reprofile)},
  {CC"invalidateInstalledCode",       CC"("HS_INSTALLED_CODE")V",                                       FN_PTR(invalidateInstalledCode)},
  {CC"readUnsafeUncompressedPointer", CC"("OBJECT"J)"OBJECT,                                            FN_PTR(readUnsafeUncompressedPointer)},
  {CC"readUnsafeKlassPointer",        CC"("OBJECT")J",                                                  FN_PTR(readUnsafeKlassPointer)},
  {CC"collectCounters",               CC"()[J",                                                         FN_PTR(collectCounters)},
  {CC"getGPUs",                       CC"()"STRING,                                                     FN_PTR(getGPUs)},
  {CC"allocateCompileId",             CC"("HS_RESOLVED_METHOD"I)I",                                     FN_PTR(allocateCompileId)},
  {CC"isMature",                      CC"("METASPACE_METHOD_DATA")Z",                                   FN_PTR(isMature)},
};

int CompilerToVM_methods_count() {
  return sizeof(CompilerToVM_methods) / sizeof(JNINativeMethod);
}

