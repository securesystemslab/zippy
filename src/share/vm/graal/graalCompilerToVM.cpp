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
#include "runtime/fieldDescriptor.hpp"
#include "memory/oopFactory.hpp"
#include "oops/generateOopMap.hpp"
#include "oops/fieldStreams.hpp"
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
  jbyte* reconstituted_code = NULL;

  // replace all breakpoints - must be done before undoing any rewriting
  if (method->number_of_breakpoints() > 0) {
    reconstituted_code = NEW_RESOURCE_ARRAY(jbyte, code_size);
    memcpy(reconstituted_code, (jbyte *) method->code_base(), code_size);
    BreakpointInfo* bp = InstanceKlass::cast(method->method_holder())->breakpoints();
    for (; bp != NULL; bp = bp->next()) {
      if (bp->match(method())) {
        jbyte code = bp->orig_bytecode();
        reconstituted_code[bp->bci()] = code;
      }
    }
  }

  // iterate over all bytecodes and replace non-Java bytecodes
  if (RewriteBytecodes || RewriteFrequentPairs || InstanceKlass::cast(method->method_holder())->is_rewritten()) {
    if (reconstituted_code == NULL) {
      reconstituted_code = NEW_RESOURCE_ARRAY(jbyte, code_size);
      memcpy(reconstituted_code, (jbyte *) method->code_base(), code_size);
    }
    BytecodeStream s(method);
    while (!s.is_last_bytecode()) {
      s.next();
      Bytecodes::Code opcode = s.raw_code();
      if (!Bytecodes::is_java_code(opcode)) {
        jbyte original_opcode = Bytecodes::java_code(opcode);
        int bci = s.bci();
        reconstituted_code[bci] = original_opcode;
        if (opcode == Bytecodes::_fast_aldc_w) {
          int cpci = Bytes::get_native_u2((address) reconstituted_code + bci + 1);
          int i = method->constants()->object_to_cp_index(cpci);
          assert(i < method->constants()->length(), "sanity check");
          Bytes::put_Java_u2((address) reconstituted_code + bci + 1, (u2)i);
        } else if (opcode == Bytecodes::_fast_aldc) {
          int cpci = reconstituted_code[bci + 1] & 0xff;
          int i = method->constants()->object_to_cp_index(cpci);
          assert(i < method->constants()->length(), "sanity check");
          reconstituted_code[bci + 1] = (jbyte)i;
        }
      }
    }
  }

  if (reconstituted_code == NULL) {
    env->SetByteArrayRegion(result, 0, code_size, (jbyte *) method->code_base());
  } else {
    env->SetByteArrayRegion(result, 0, code_size, reconstituted_code);
  }

  return result;
C2V_END

C2V_VMENTRY(jstring, getSignature, (JNIEnv *env, jobject, jlong metaspace_method))
  Method* method = asMethod(metaspace_method);
  assert(method != NULL && method->signature() != NULL, "signature required");
  return (jstring)JNIHandles::make_local(java_lang_String::create_from_symbol(method->signature(), THREAD)());
C2V_END

C2V_VMENTRY(jobjectArray, initializeExceptionHandlers, (JNIEnv *, jobject, jlong metaspace_method, jobjectArray java_handlers))
  ResourceMark rm;
  methodHandle method = asMethod(metaspace_method);
  int handler_count = method->exception_table_length();
  objArrayHandle array = (objArrayOop) JNIHandles::resolve(java_handlers);
  assert(array->length() == handler_count, "wrong length");
  ExceptionTableElement* handlers = handler_count == 0 ? NULL : method->exception_table_start();

  for (int i = 0; i < handler_count; i++) {
    ExceptionTableElement* handler = handlers + i;
    Handle entry = array->obj_at(i);
    assert(!entry.is_null(), "entry should not be null");
    ExceptionHandler::set_startBCI(entry, handler->start_pc);
    ExceptionHandler::set_endBCI(entry, handler->end_pc);
    ExceptionHandler::set_handlerBCI(entry, handler->handler_pc);
    int catch_class_index = handler->catch_type_index;
    ExceptionHandler::set_catchTypeCPI(entry, catch_class_index);

    if (catch_class_index == 0) {
      ExceptionHandler::set_catchType(entry, NULL);
    } else {
      ConstantPool* cp = InstanceKlass::cast(method->method_holder())->constants();
      KlassHandle loading_klass = method->method_holder();
      Handle catch_class = GraalCompiler::get_JavaType(cp, catch_class_index, loading_klass, CHECK_NULL);
      if (catch_class->klass() == HotSpotResolvedObjectType::klass() && java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(catch_class)) == SystemDictionary::Throwable_klass()) {
        ExceptionHandler::set_catchType(entry, NULL);
        ExceptionHandler::set_catchTypeCPI(entry, 0);
      } else {
        ExceptionHandler::set_catchType(entry, catch_class());
      }
    }
    array->obj_at_put(i, entry());
  }

  return (jobjectArray) JNIHandles::make_local(array());
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

C2V_VMENTRY(jlong, getMetaspaceMethod, (JNIEnv *, jobject, jobject reflection_method_handle, jobject resultHolder))
  oop reflection_method = JNIHandles::resolve(reflection_method_handle);
  oop reflection_holder = java_lang_reflect_Method::clazz(reflection_method);
  int slot = java_lang_reflect_Method::slot(reflection_method);
  Klass* holder = java_lang_Class::as_Klass(reflection_holder);
  methodHandle method = InstanceKlass::cast(holder)->method_with_idnum(slot);
  Handle type = GraalCompiler::createHotSpotResolvedObjectType(method, CHECK_0);
  objArrayOop(JNIHandles::resolve(resultHolder))->obj_at_put(0, type());
  return (jlong) (address) method();
}

C2V_VMENTRY(jlong, getMetaspaceConstructor, (JNIEnv *, jobject, jobject reflection_ctor_handle, jobject resultHolder))
  oop reflection_ctor = JNIHandles::resolve(reflection_ctor_handle);
  oop reflection_holder = java_lang_reflect_Constructor::clazz(reflection_ctor);
  int slot = java_lang_reflect_Constructor::slot(reflection_ctor);
  Klass* holder = java_lang_Class::as_Klass(reflection_holder);
  methodHandle method = InstanceKlass::cast(holder)->method_with_idnum(slot);
  Handle type = GraalCompiler::createHotSpotResolvedObjectType(method, CHECK_0);
  objArrayOop(JNIHandles::resolve(resultHolder))->obj_at_put(0, type());
  return (jlong) (address) method();
}

C2V_VMENTRY(jobject, getJavaField, (JNIEnv *, jobject, jobject reflection_field_handle))
  oop reflection_field = JNIHandles::resolve(reflection_field_handle);
  oop reflection_holder = java_lang_reflect_Field::clazz(reflection_field);
  int slot = java_lang_reflect_Field::slot(reflection_field);
  InstanceKlass* holder = InstanceKlass::cast(java_lang_Class::as_Klass(reflection_holder));

  int offset = holder->field_offset(slot);
  int flags = holder->field_access_flags(slot);
  Symbol* field_name = holder->field_name(slot);
  Handle field_holder = GraalCompiler::get_JavaTypeFromClass(reflection_holder, CHECK_NULL);
  Handle field_type = GraalCompiler::get_JavaTypeFromClass(java_lang_reflect_Field::type(reflection_field), CHECK_NULL);

  Handle ret = GraalCompiler::get_JavaField(offset, flags, field_name, field_holder, field_type, CHECK_NULL);
  return JNIHandles::make_local(THREAD, ret());
}

C2V_VMENTRY(jlong, getUniqueConcreteMethod, (JNIEnv *, jobject, jlong metaspace_method, jobject resultHolder))
  methodHandle method = asMethod(metaspace_method);
  KlassHandle holder = method->method_holder();
  if (holder->is_interface()) {
    // Cannot trust interfaces. Because of:
    // interface I { void foo(); }
    // class A { public void foo() {} }
    // class B extends A implements I { }
    // class C extends B { public void foo() { } }
    // class D extends B { }
    // Would lead to identify C.foo() as the unique concrete method for I.foo() without seeing A.foo().
    return 0L;
  }
  methodHandle ucm;
  {
    ResourceMark rm;
    MutexLocker locker(Compile_lock);
    ucm = Dependencies::find_unique_concrete_method(holder(), method());
  }

  if (ucm.is_null()) {
    return 0L;
  }

  Handle type = GraalCompiler::createHotSpotResolvedObjectType(ucm(), CHECK_0);
  objArrayOop(JNIHandles::resolve(resultHolder))->obj_at_put(0, type());
  return (jlong) (address) ucm();
C2V_END

C2V_VMENTRY(jobject, getUniqueImplementor, (JNIEnv *, jobject, jobject interface_type))
  InstanceKlass* klass = (InstanceKlass*) asKlass(HotSpotResolvedObjectType::metaspaceKlass(interface_type));
  assert(klass->is_interface(), "must be");
  if (klass->nof_implementors() == 1) {
    InstanceKlass* implementor = (InstanceKlass*) klass->implementor();
    if (!implementor->is_abstract() && !implementor->is_interface() && implementor->is_leaf_class()) {
      Handle type = GraalCompiler::get_JavaType(implementor, CHECK_NULL);
      return JNIHandles::make_local(THREAD, type());
    }
  }
  return NULL;
C2V_END

C2V_ENTRY(jint, getInvocationCount, (JNIEnv *, jobject, jlong metaspace_method))
  Method* method = asMethod(metaspace_method);
  return method->invocation_count();
C2V_END

C2V_VMENTRY(void, initializeMethod,(JNIEnv *, jobject, jlong metaspace_method, jobject hotspot_method))
  methodHandle method = asMethod(metaspace_method);
  Handle name = java_lang_String::create_from_symbol(method->name(), CHECK);
  InstanceKlass::cast(HotSpotResolvedJavaMethod::klass())->initialize(CHECK);
  HotSpotResolvedJavaMethod::set_name(hotspot_method, name());
  HotSpotResolvedJavaMethod::set_codeSize(hotspot_method, method->code_size());
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

C2V_VMENTRY(void, initializeMethodData,(JNIEnv *, jobject, jlong metaspace_method_data, jobject hotspot_method_data))
  MethodData* method_data = asMethodData(metaspace_method_data);
  HotSpotMethodData::set_normalDataSize(hotspot_method_data, method_data->data_size());
  HotSpotMethodData::set_extraDataSize(hotspot_method_data, method_data->extra_data_size());
C2V_END

// ------------------------------------------------------------------
// Adjust a CounterData count to be commensurate with
// interpreter_invocation_count.  If the MDO exists for
// only 25% of the time the method exists, then the
// counts in the MDO should be scaled by 4X, so that
// they can be usefully and stably compared against the
// invocation counts in methods.
int scale_count(MethodData* method_data, int count) {
  if (count > 0) {
    int counter_life;
    int method_life = method_data->method()->interpreter_invocation_count();
    int current_mileage = MethodData::mileage_of(method_data->method());
    int creation_mileage = method_data->creation_mileage();
    counter_life = current_mileage - creation_mileage;

    // counter_life due to backedge_counter could be > method_life
    if (counter_life > method_life)
      counter_life = method_life;
    if (0 < counter_life && counter_life <= method_life) {
      count = (int)((double)count * method_life / counter_life + 0.5);
      count = (count > 0) ? count : 1;
    }
  }
  return count;
}

C2V_ENTRY(jint, getCompiledCodeSize, (JNIEnv *env, jobject, jlong metaspace_method))
  nmethod* code = (asMethod(metaspace_method))->code();
  return code == NULL ? 0 : code->insts_size();
C2V_END

C2V_VMENTRY(jint, constantPoolLength, (JNIEnv *env, jobject, jobject type))
  ConstantPool* cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();
  return cp->length();
C2V_END

C2V_VMENTRY(jobject, lookupType, (JNIEnv *env, jobject, jstring jname, jobject accessingClass, jboolean eagerResolve))
  ResourceMark rm;

  Handle name = JNIHandles::resolve(jname);
  Symbol* nameSymbol = java_lang_String::as_symbol(name, THREAD);
  assert(nameSymbol != NULL, "name to symbol creation failed");

  oop result = NULL;
  if (nameSymbol == vmSymbols::int_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_INT, THREAD);
  } else if (nameSymbol == vmSymbols::long_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_LONG, THREAD);
  } else if (nameSymbol == vmSymbols::bool_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_BOOLEAN, THREAD);
  } else if (nameSymbol == vmSymbols::char_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_CHAR, THREAD);
  } else if (nameSymbol == vmSymbols::short_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_SHORT, THREAD);
  } else if (nameSymbol == vmSymbols::byte_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_BYTE, THREAD);
  } else if (nameSymbol == vmSymbols::double_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_DOUBLE, THREAD);
  } else if (nameSymbol == vmSymbols::float_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_FLOAT, THREAD);
  } else if (nameSymbol == vmSymbols::void_signature()) {
    result = VMToCompiler::createPrimitiveJavaType((int) T_VOID, THREAD);
  } else {
    Klass* resolved_type = NULL;
    Handle classloader;
    Handle protectionDomain;
    if (JNIHandles::resolve(accessingClass) != NULL) {
      classloader = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(accessingClass))->class_loader();
      protectionDomain = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(accessingClass))->protection_domain();
    }

    if (eagerResolve) {
      resolved_type = SystemDictionary::resolve_or_fail(nameSymbol, classloader, protectionDomain, true, THREAD);
    } else {
      resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
    }

    if (!HAS_PENDING_EXCEPTION) {
      if (resolved_type == NULL) {
        assert(!eagerResolve, "failed eager resolution should have caused an exception");
        Handle type = VMToCompiler::createUnresolvedJavaType(name, THREAD);
        result = type();
      } else {
        Handle type = GraalCompiler::createHotSpotResolvedObjectType(resolved_type, name, CHECK_NULL);
        result = type();
      }
    }
  }

  return JNIHandles::make_local(THREAD, result);
C2V_END

C2V_VMENTRY(jobject, lookupConstantInPool, (JNIEnv *env, jobject, jobject type, jint index))

  ConstantPool* cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();

  oop result = NULL;
  constantTag tag = cp->tag_at(index);

  switch (tag.value()) {
  case JVM_CONSTANT_Integer:
    result = VMToCompiler::createConstant(Kind::Int(), cp->int_at(index), CHECK_NULL);
    break;

  case JVM_CONSTANT_Long:
    result = VMToCompiler::createConstant(Kind::Long(), cp->long_at(index), CHECK_NULL);
    break;

  case JVM_CONSTANT_Float:
    result = VMToCompiler::createConstantFloat(cp->float_at(index), CHECK_NULL);
    break;

  case JVM_CONSTANT_Double:
    result = VMToCompiler::createConstantDouble(cp->double_at(index), CHECK_NULL);
    break;

  case JVM_CONSTANT_Class:
  case JVM_CONSTANT_UnresolvedClass:
  case JVM_CONSTANT_UnresolvedClassInError:
    {
      Handle type = GraalCompiler::get_JavaType(cp, index, cp->pool_holder(), CHECK_NULL);
      result = type();
      break;
    }

  case JVM_CONSTANT_String:
    {
      oop result_oop = cp->resolve_possibly_cached_constant_at(index, CHECK_NULL);
      result = VMToCompiler::createConstantObject(result_oop, CHECK_NULL);
      break;
    }

  case JVM_CONSTANT_MethodHandle:
  case JVM_CONSTANT_MethodHandleInError:
  case JVM_CONSTANT_MethodType:
  case JVM_CONSTANT_MethodTypeInError:
    {
      oop result_oop = cp->resolve_constant_at(index, CHECK_NULL);
      result = VMToCompiler::createConstantObject(result_oop, CHECK_NULL);
      break;
    }

  default:
    fatal(err_msg_res("unknown constant pool tag %s at cpi %d in %s", tag.internal_name(), index, cp->pool_holder()->name()->as_C_string()));
  }

  return JNIHandles::make_local(THREAD, result);
C2V_END

C2V_VMENTRY(jobject, lookupAppendixInPool, (JNIEnv *env, jobject, jobject type, jint index, jbyte opcode))
  Bytecodes::Code bc = (Bytecodes::Code) (((int) opcode) & 0xFF);
  index = GraalCompiler::to_cp_index(index, bc);
  constantPoolHandle cpool(InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants());
  oop appendix_oop = ConstantPool::appendix_at_if_loaded(cpool, index);

  return JNIHandles::make_local(THREAD, appendix_oop);
C2V_END

C2V_VMENTRY(jobject, lookupMethodInPool, (JNIEnv *env, jobject, jobject type, jint index, jbyte opcode))
  constantPoolHandle cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();
  instanceKlassHandle pool_holder(cp->pool_holder());

  Bytecodes::Code bc = (Bytecodes::Code) (((int) opcode) & 0xFF);
  index = GraalCompiler::to_cp_index(index, bc);

  methodHandle method = GraalEnv::get_method_by_index(cp, index, bc, pool_holder);
  if (!method.is_null()) {
    Handle holder = GraalCompiler::get_JavaType(method->method_holder(), CHECK_NULL);
    return JNIHandles::make_local(THREAD, VMToCompiler::createResolvedJavaMethod(holder, method(), THREAD));
  } else {
    // Get the method's name and signature.
    Handle name = java_lang_String::create_from_symbol(cp->name_ref_at(index), CHECK_NULL);
    Handle signature  = java_lang_String::create_from_symbol(cp->signature_ref_at(index), CHECK_NULL);
    Handle type;
    if (bc != Bytecodes::_invokedynamic) {
      int holder_index = cp->klass_ref_index_at(index);
      type = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
    } else {
      type = Handle(SystemDictionary::MethodHandle_klass()->java_mirror());
    }
    return JNIHandles::make_local(THREAD, VMToCompiler::createUnresolvedJavaMethod(name, signature, type, THREAD));
  }
C2V_END

C2V_VMENTRY(jobject, lookupTypeInPool, (JNIEnv *env, jobject, jobject type, jint index))

  ConstantPool* cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();
  Handle result = GraalCompiler::get_JavaType(cp, index, cp->pool_holder(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, result());
C2V_END

C2V_VMENTRY(void, lookupReferencedTypeInPool, (JNIEnv *env, jobject, jobject type, jint index, jbyte op))
  ConstantPool* cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();
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

C2V_VMENTRY(jobject, lookupFieldInPool, (JNIEnv *env, jobject, jobject constantPoolHolder, jint index, jbyte opcode))
  ResourceMark rm;

  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(constantPoolHolder)))->constants();

  int nt_index = cp->name_and_type_ref_index_at(index);
  int sig_index = cp->signature_ref_index_at(nt_index);
  Symbol* signature = cp->symbol_at(sig_index);
  int name_index = cp->name_ref_index_at(nt_index);
  Symbol* name = cp->symbol_at(name_index);
  int holder_index = cp->klass_ref_index_at(index);
  Handle holder = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
  instanceKlassHandle holder_klass;

  Bytecodes::Code code = (Bytecodes::Code)(((int) opcode) & 0xFF);
  int offset = -1;
  AccessFlags flags;
  BasicType basic_type;
  if (holder->klass() == SystemDictionary::HotSpotResolvedObjectType_klass()) {
    FieldAccessInfo result;
    LinkResolver::resolve_field(result, cp, index,
                                Bytecodes::java_code(code),
                                true, false, Thread::current());
    if (HAS_PENDING_EXCEPTION) {
      CLEAR_PENDING_EXCEPTION;
    } else {
      offset = result.field_offset();
      flags = result.access_flags();
      holder_klass = result.klass()();
      basic_type = result.field_type();
      holder = GraalCompiler::get_JavaType(holder_klass, CHECK_NULL);
    }
  }

  Handle type = GraalCompiler::get_JavaTypeFromSignature(signature, cp->pool_holder(), CHECK_NULL);
  Handle field_handle = GraalCompiler::get_JavaField(offset, flags.as_int(), name, holder, type, THREAD);

  return JNIHandles::make_local(THREAD, field_handle());
C2V_END

C2V_VMENTRY(jobject, resolveMethod, (JNIEnv *, jobject, jobject resolved_type, jstring name, jstring signature))

  assert(JNIHandles::resolve(resolved_type) != NULL, "");
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(resolved_type));
  Symbol* name_symbol = java_lang_String::as_symbol(JNIHandles::resolve(name), THREAD);
  Symbol* signature_symbol = java_lang_String::as_symbol(JNIHandles::resolve(signature), THREAD);
  methodHandle method = klass->lookup_method(name_symbol, signature_symbol);
  if (method.is_null()) {
    if (TraceGraal >= 3) {
      ResourceMark rm;
      tty->print_cr("Could not resolve method %s %s on klass %s", name_symbol->as_C_string(), signature_symbol->as_C_string(), klass->name()->as_C_string());
    }
    return NULL;
  }
  Handle holder = GraalCompiler::get_JavaType(method->method_holder(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, VMToCompiler::createResolvedJavaMethod(holder, method(), THREAD));
C2V_END

C2V_VMENTRY(jboolean, isTypeInitialized,(JNIEnv *, jobject, jobject hotspot_klass))
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(hotspot_klass));
  assert(klass != NULL, "method must not be called for primitive types");
  return InstanceKlass::cast(klass)->is_initialized();
C2V_END

C2V_VMENTRY(jboolean, hasFinalizableSubclass,(JNIEnv *, jobject, jobject hotspot_klass))
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(hotspot_klass));
  assert(klass != NULL, "method must not be called for primitive types");
  return Dependencies::find_finalizable_subclass(klass) != NULL;
C2V_END

C2V_VMENTRY(void, initializeType, (JNIEnv *, jobject, jobject hotspot_klass))
  Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(hotspot_klass));
  assert(klass != NULL, "method must not be called for primitive types");
  InstanceKlass::cast(klass)->initialize(JavaThread::current());
C2V_END

C2V_VMENTRY(jobject, getInstanceFields, (JNIEnv *, jobject, jobject klass))
  ResourceMark rm;

  instanceKlassHandle k = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(klass));
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

C2V_VMENTRY(jobject, getMethods, (JNIEnv *, jobject, jobject klass))
  ResourceMark rm;

  instanceKlassHandle k(THREAD, java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(klass)));
  Array<Method*>* methods = k->methods();
  int methods_length = methods->length();

  // Allocate result
  objArrayOop r = oopFactory::new_objArray(SystemDictionary::HotSpotResolvedJavaMethod_klass(), methods_length, CHECK_NULL);
  objArrayHandle result (THREAD, r);

  for (int i = 0; i < methods_length; i++) {
    methodHandle method(THREAD, methods->at(i));
    Handle holder = JNIHandles::resolve(klass);
    oop m = VMToCompiler::createResolvedJavaMethod(holder, method(), THREAD);
    result->obj_at_put(i, m);
  }

  return JNIHandles::make_local(THREAD, result());
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

C2V_VMENTRY(jobject, getResolvedType, (JNIEnv *env, jobject, jobject javaClass))
  oop java_mirror = JNIHandles::resolve(javaClass);
  assert(java_mirror != NULL, "argument to CompilerToVM.getResolvedType must not be NULL");
  Handle type = GraalCompiler::get_JavaTypeFromClass(java_mirror, CHECK_NULL);
  return JNIHandles::make_local(THREAD, type());
C2V_END


// helpers used to set fields in the HotSpotVMConfig object
jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig) {
  jfieldID id = env->GetFieldID(env->GetObjectClass(obj), name, sig);
  if (id == NULL) {
    fatal(err_msg("field not found: %s (%s)", name, sig));
  }
  return id;
}

C2V_ENTRY(void, initializeConfiguration, (JNIEnv *env, jobject, jobject config))

#define set_boolean(name, value) do { env->SetBooleanField(config, getFieldID(env, config, name, "Z"), value); } while (0)
#define set_int(name, value) do { env->SetIntField(config, getFieldID(env, config, name, "I"), value); } while (0)
#define set_long(name, value) do { env->SetLongField(config, getFieldID(env, config, name, "J"), value); } while (0)
#define set_address(name, value) do { set_long(name, (jlong) value); } while (0)
#define set_object(name, value) do { env->SetObjectField(config, getFieldID(env, config, name, "Ljava/lang/Object;"), value); } while (0)
#define set_int_array(name, value) do { env->SetObjectField(config, getFieldID(env, config, name, "[I"), value); } while (0)

  guarantee(HeapWordSize == sizeof(char*), "Graal assumption that HeadWordSize == machine word size is wrong");

  set_boolean("cAssertions", DEBUG_ONLY(true) NOT_DEBUG(false));
  set_boolean("verifyOops", VerifyOops);
  set_boolean("ciTime", CITime);
  set_int("compileThreshold", CompileThreshold);
  set_boolean("compileTheWorld", CompileTheWorld);
  set_int("compileTheWorldStartAt", CompileTheWorldStartAt);
  set_int("compileTheWorldStopAt", CompileTheWorldStopAt);
  set_boolean("printCompilation", PrintCompilation);
  set_boolean("printInlining", PrintInlining);
  set_boolean("useFastLocking", GraalUseFastLocking);
  set_boolean("useBiasedLocking", UseBiasedLocking);
  set_boolean("usePopCountInstruction", UsePopCountInstruction);
  set_boolean("useAESIntrinsics", UseAESIntrinsics);
  set_boolean("useTLAB", UseTLAB);
  set_boolean("useG1GC", UseG1GC);
#ifdef TARGET_ARCH_x86
  set_int("useSSE", UseSSE);
  set_int("useAVX", UseAVX);
#endif
  set_int("codeEntryAlignment", CodeEntryAlignment);
  set_int("stackShadowPages", StackShadowPages);
  set_int("hubOffset", oopDesc::klass_offset_in_bytes());
  set_int("markOffset", oopDesc::mark_offset_in_bytes());
  set_int("prototypeMarkWordOffset", in_bytes(Klass::prototype_header_offset()));
  set_int("superCheckOffsetOffset", in_bytes(Klass::super_check_offset_offset()));
  set_int("secondarySuperCacheOffset", in_bytes(Klass::secondary_super_cache_offset()));
  set_int("secondarySupersOffset", in_bytes(Klass::secondary_supers_offset()));
  set_int("subklassOffset", in_bytes(Klass::subklass_offset()));
  set_int("nextSiblingOffset", in_bytes(Klass::next_sibling_offset()));
  set_int("arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());
  set_int("klassStateOffset", in_bytes(InstanceKlass::init_state_offset()));
  set_int("klassStateFullyInitialized", (int)InstanceKlass::fully_initialized);
  set_int("threadTlabTopOffset", in_bytes(JavaThread::tlab_top_offset()));
  set_int("threadTlabEndOffset", in_bytes(JavaThread::tlab_end_offset()));
  set_int("threadObjectOffset", in_bytes(JavaThread::threadObj_offset()));
  set_int("threadIsMethodHandleReturnOffset", in_bytes(JavaThread::is_method_handle_return_offset()));
  set_int("osThreadOffset", in_bytes(JavaThread::osthread_offset()));
  set_int("osThreadInterruptedOffset", in_bytes(OSThread::interrupted_offset()));
  set_int("unlockedMask", (int) markOopDesc::unlocked_value);
  set_int("biasedLockMaskInPlace", (int) markOopDesc::biased_lock_mask_in_place);
  set_int("ageMaskInPlace", (int) markOopDesc::age_mask_in_place);
  set_int("epochMaskInPlace", (int) markOopDesc::epoch_mask_in_place);
  set_int("biasedLockPattern", (int) markOopDesc::biased_lock_pattern);
  set_int("methodMaxLocalsOffset", in_bytes(ConstMethod::size_of_locals_offset()));
  set_int("methodConstMethodOffset", in_bytes(Method::const_offset()));
  set_int("constMethodMaxStackOffset", in_bytes(ConstMethod::max_stack_offset()));
  set_int("constMethodConstantsOffset", in_bytes(ConstMethod::constants_offset()));
  set_int("constantPoolHolderOffset", ConstantPool::pool_holder_offset_in_bytes());
  set_int("extraStackEntries", Method::extra_stack_entries());
  set_int("methodAccessFlagsOffset", in_bytes(Method::access_flags_offset()));
  set_int("methodQueuedForCompilationBit", (int) JVM_ACC_QUEUED);
  set_int("methodIntrinsicIdOffset", Method::intrinsic_id_offset_in_bytes());
  set_int("klassHasFinalizerFlag", JVM_ACC_HAS_FINALIZER);
  set_int("threadExceptionOopOffset", in_bytes(JavaThread::exception_oop_offset()));
  set_int("threadExceptionPcOffset", in_bytes(JavaThread::exception_pc_offset()));
#ifdef TARGET_ARCH_x86
  set_boolean("isPollingPageFar", Assembler::is_polling_page_far());
  set_int("runtimeCallStackSize", (jint)frame::arg_reg_save_area_bytes);
#endif
  set_int("classMirrorOffset", in_bytes(Klass::java_mirror_offset()));
  set_int("klassModifierFlagsOffset", in_bytes(Klass::modifier_flags_offset()));
  set_int("klassAccessFlagsOffset", in_bytes(Klass::access_flags_offset()));
  set_int("klassOffset", java_lang_Class::klass_offset_in_bytes());
  set_int("arrayKlassOffset", java_lang_Class::array_klass_offset_in_bytes());
  set_int("graalMirrorInClassOffset", java_lang_Class::graal_mirror_offset_in_bytes());
  set_int("klassLayoutHelperOffset", in_bytes(Klass::layout_helper_offset()));
  set_int("klassSuperKlassOffset", in_bytes(Klass::super_offset()));
  set_int("methodDataOffset", in_bytes(Method::method_data_offset()));
  set_int("nmethodEntryOffset", nmethod::verified_entry_point_offset());
  set_int("methodCompiledEntryOffset", in_bytes(Method::from_compiled_offset()));
  set_int("basicLockSize", sizeof(BasicLock));
  set_int("basicLockDisplacedHeaderOffset", BasicLock::displaced_header_offset_in_bytes());
  set_int("uninitializedIdentityHashCodeValue", markOopDesc::no_hash);
  set_int("identityHashCodeShift", markOopDesc::hash_shift);

  set_int("arrayKlassLayoutHelperIdentifier", 0x80000000);
  assert((Klass::_lh_array_tag_obj_value & Klass::_lh_array_tag_type_value & 0x80000000) != 0, "obj_array and type_array must have first bit set");
  set_int("arrayKlassComponentMirrorOffset", in_bytes(ArrayKlass::component_mirror_offset()));


  set_int("pendingExceptionOffset", in_bytes(ThreadShadow::pending_exception_offset()));
  set_int("pendingDeoptimizationOffset", in_bytes(ThreadShadow::pending_deoptimization_offset()));

  set_int("metaspaceArrayLengthOffset", Array<Klass*>::length_offset_in_bytes());
  set_int("metaspaceArrayBaseOffset", Array<Klass*>::base_offset_in_bytes());
  set_int("methodDataOopDataOffset", in_bytes(MethodData::data_offset()));
  set_int("methodDataOopTrapHistoryOffset", in_bytes(MethodData::trap_history_offset()));
  set_int("dataLayoutHeaderSize", DataLayout::header_size_in_bytes());
  set_int("dataLayoutTagOffset", in_bytes(DataLayout::tag_offset()));
  set_int("dataLayoutFlagsOffset", in_bytes(DataLayout::flags_offset()));
  set_int("dataLayoutBCIOffset", in_bytes(DataLayout::bci_offset()));
  set_int("dataLayoutCellsOffset", in_bytes(DataLayout::cell_offset(0)));
  set_int("dataLayoutCellSize", DataLayout::cell_size);
  set_int("bciProfileWidth", BciProfileWidth);
  set_int("typeProfileWidth", TypeProfileWidth);
  set_int("methodProfileWidth", MethodProfileWidth);

  set_int("tlabAlignmentReserve", (int32_t)ThreadLocalAllocBuffer::alignment_reserve());
  set_long("tlabIntArrayMarkWord", (intptr_t)markOopDesc::prototype()->copy_set_hash(0x2));
  set_long("heapTopAddress", (jlong)(address) Universe::heap()->top_addr());
  set_long("heapEndAddress", (jlong)(address) Universe::heap()->end_addr());
  set_int("threadTlabStartOffset", in_bytes(JavaThread::tlab_start_offset()));
  set_int("threadTlabSizeOffset", in_bytes(JavaThread::tlab_size_offset()));
  set_int("threadAllocatedBytesOffset", in_bytes(JavaThread::allocated_bytes_offset()));
  set_int("threadLastJavaSpOffset", in_bytes(JavaThread::last_Java_sp_offset()));
#ifdef TARGET_ARCH_x86
  set_int("threadLastJavaFpOffset", in_bytes(JavaThread::last_Java_fp_offset()));
#endif
  set_int("threadLastJavaPcOffset", in_bytes(JavaThread::last_Java_pc_offset()));
  set_int("threadObjectResultOffset", in_bytes(JavaThread::vm_result_offset()));
  set_int("tlabSlowAllocationsOffset", in_bytes(JavaThread::tlab_slow_allocations_offset()));
  set_int("tlabFastRefillWasteOffset", in_bytes(JavaThread::tlab_fast_refill_waste_offset()));
  set_int("tlabNumberOfRefillsOffset", in_bytes(JavaThread::tlab_number_of_refills_offset()));
  set_int("tlabRefillWasteLimitOffset", in_bytes(JavaThread::tlab_refill_waste_limit_offset()));
  set_int("tlabRefillWasteIncrement", (int32_t) ThreadLocalAllocBuffer::refill_waste_limit_increment());
  set_int("klassInstanceSizeOffset", in_bytes(Klass::layout_helper_offset()));
  set_boolean("tlabStats", TLABStats);
  set_boolean("inlineContiguousAllocationSupported", !CMSIncrementalMode && Universe::heap()->supports_inline_contig_alloc());

  set_long("verifyOopCounterAddress", (jlong)(address) StubRoutines::verify_oop_count_addr());
  set_long("verifyOopMask", Universe::verify_oop_mask());
  set_long("verifyOopBits", Universe::verify_oop_bits());

  set_long("arrayPrototypeMarkWord", (intptr_t)markOopDesc::prototype());
  set_int("layoutHelperLog2ElementSizeShift", Klass::_lh_log2_element_size_shift);
  set_int("layoutHelperLog2ElementSizeMask", Klass::_lh_log2_element_size_mask);
  set_int("layoutHelperElementTypeShift", Klass::_lh_element_type_shift);
  set_int("layoutHelperElementTypeMask", Klass::_lh_element_type_mask);
  // this filters out the bit that differentiates a type array from an object array
  set_int("layoutHelperElementTypePrimitiveInPlace", (Klass::_lh_array_tag_type_value & ~Klass::_lh_array_tag_obj_value) << Klass::_lh_array_tag_shift);
  set_int("layoutHelperHeaderSizeShift", Klass::_lh_header_size_shift);
  set_int("layoutHelperHeaderSizeMask", Klass::_lh_header_size_mask);
  set_int("layoutHelperOffset", in_bytes(Klass::layout_helper_offset()));

  set_address("inlineCacheMissStub", SharedRuntime::get_ic_miss_stub());
  set_address("handleDeoptStub", SharedRuntime::deopt_blob()->unpack());
  set_address("aescryptEncryptBlockStub", StubRoutines::aescrypt_encryptBlock());
  set_address("aescryptDecryptBlockStub", StubRoutines::aescrypt_decryptBlock());
  set_address("cipherBlockChainingEncryptAESCryptStub", StubRoutines::cipherBlockChaining_encryptAESCrypt());
  set_address("cipherBlockChainingDecryptAESCryptStub", StubRoutines::cipherBlockChaining_decryptAESCrypt());

  set_address("newInstanceAddress", GraalRuntime::new_instance);
  set_address("newArrayAddress", GraalRuntime::new_array);
  set_address("newMultiArrayAddress", GraalRuntime::new_multi_array);
  set_address("registerFinalizerAddress", SharedRuntime::register_finalizer);
  set_address("threadIsInterruptedAddress", GraalRuntime::thread_is_interrupted);
  set_address("uncommonTrapStub", SharedRuntime::deopt_blob()->uncommon_trap());
  set_address("vmMessageAddress", GraalRuntime::vm_message);
  set_address("identityHashCodeAddress", GraalRuntime::identity_hash_code);
  set_address("exceptionHandlerForPcAddress", GraalRuntime::exception_handler_for_pc);
  set_address("exceptionHandlerForReturnAddressAddress", SharedRuntime::exception_handler_for_return_address);
  set_address("osrMigrationEndAddress", SharedRuntime::OSR_migration_end);
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
  set_address("javaTimeMillisAddress", CAST_FROM_FN_PTR(address, os::javaTimeMillis));
  set_address("javaTimeNanosAddress", CAST_FROM_FN_PTR(address, os::javaTimeNanos));
  set_address("arithmeticSinAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dsin));
  set_address("arithmeticCosAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dcos));
  set_address("arithmeticTanAddress", CAST_FROM_FN_PTR(address, SharedRuntime::dtan));

  set_int("deoptReasonNone", Deoptimization::Reason_none);
  set_int("deoptReasonNullCheck", Deoptimization::Reason_null_check);
  set_int("deoptReasonRangeCheck", Deoptimization::Reason_range_check);
  set_int("deoptReasonClassCheck", Deoptimization::Reason_class_check);
  set_int("deoptReasonArrayCheck", Deoptimization::Reason_array_check);
  set_int("deoptReasonUnreached0", Deoptimization::Reason_unreached0);
  set_int("deoptReasonTypeCheckInlining", Deoptimization::Reason_type_checked_inlining);
  set_int("deoptReasonOptimizedTypeCheck", Deoptimization::Reason_optimized_type_check);
  set_int("deoptReasonNotCompiledExceptionHandler", Deoptimization::Reason_not_compiled_exception_handler);
  set_int("deoptReasonUnresolved", Deoptimization::Reason_unresolved);
  set_int("deoptReasonJsrMismatch", Deoptimization::Reason_jsr_mismatch);
  set_int("deoptReasonDiv0Check", Deoptimization::Reason_div0_check);
  set_int("deoptReasonConstraint", Deoptimization::Reason_constraint);
  set_int("deoptReasonLoopLimitCheck", Deoptimization::Reason_loop_limit_check);

  set_int("deoptActionNone", Deoptimization::Action_none);
  set_int("deoptActionMaybeRecompile", Deoptimization::Action_maybe_recompile);
  set_int("deoptActionReinterpret", Deoptimization::Action_reinterpret);
  set_int("deoptActionMakeNotEntrant", Deoptimization::Action_make_not_entrant);
  set_int("deoptActionMakeNotCompilable", Deoptimization::Action_make_not_compilable);

  set_int("vmIntrinsicInvokeBasic", vmIntrinsics::_invokeBasic);
  set_int("vmIntrinsicLinkToVirtual", vmIntrinsics::_linkToVirtual);
  set_int("vmIntrinsicLinkToStatic", vmIntrinsics::_linkToStatic);
  set_int("vmIntrinsicLinkToSpecial", vmIntrinsics::_linkToSpecial);
  set_int("vmIntrinsicLinkToInterface", vmIntrinsics::_linkToInterface);

  set_boolean("useCompressedOops", UseCompressedOops);
  set_boolean("useCompressedKlassPointers", UseCompressedKlassPointers);
  set_address("narrowOopBase", Universe::narrow_oop_base());
  set_int("narrowOopShift", Universe::narrow_oop_shift());
  set_int("logMinObjAlignment", LogMinObjAlignmentInBytes);

  set_int("g1CardQueueIndexOffset", in_bytes(JavaThread::dirty_card_queue_offset() + PtrQueue::byte_offset_of_index()));
  set_int("g1CardQueueBufferOffset", in_bytes(JavaThread::dirty_card_queue_offset() + PtrQueue::byte_offset_of_buf()));
  set_int("logOfHRGrainBytes", HeapRegion::LogOfHRGrainBytes);
  set_int("g1SATBQueueMarkingOffset", in_bytes(JavaThread::satb_mark_queue_offset() + PtrQueue::byte_offset_of_active()));
  set_int("g1SATBQueueIndexOffset", in_bytes(JavaThread::satb_mark_queue_offset() +  PtrQueue::byte_offset_of_index()));
  set_int("g1SATBQueueBufferOffset", in_bytes(JavaThread::satb_mark_queue_offset() + PtrQueue::byte_offset_of_buf()));

  BarrierSet* bs = Universe::heap()->barrier_set();
  switch (bs->kind()) {
    case BarrierSet::CardTableModRef:
    case BarrierSet::CardTableExtension:
    case BarrierSet::G1SATBCT:
    case BarrierSet::G1SATBCTLogging:{
      jlong base = (jlong)((CardTableModRefBS*)bs)->byte_map_base;
      assert(base != 0, "unexpected byte_map_base");
      set_long("cardtableStartAddress", base);
      set_int("cardtableShift", CardTableModRefBS::card_shift);
      break;
    }
    case BarrierSet::ModRef:
    case BarrierSet::Other:
      set_long("cardtableStartAddress", 0);
      set_int("cardtableShift", 0);
      // No post barriers
      break;
    default:
      ShouldNotReachHere();
      break;
    }

  set_int("arrayClassElementOffset", in_bytes(ObjArrayKlass::element_klass_offset()));

#undef set_boolean
#undef set_int
#undef set_long
#undef set_object
#undef set_int_array

C2V_END

C2V_VMENTRY(jint, installCode0, (JNIEnv *jniEnv, jobject, jobject compiled_code, jobject installed_code, jobject triggered_deoptimizations))
  ResourceMark rm;
  HandleMark hm;
  Handle compiled_code_handle = JNIHandles::resolve(compiled_code);
  CodeBlob* cb = NULL;
  Handle installed_code_handle = JNIHandles::resolve(installed_code);
  Handle triggered_deoptimizations_handle = JNIHandles::resolve(triggered_deoptimizations);
  GraalEnv::CodeInstallResult result;

  CodeInstaller installer(compiled_code_handle, result, cb, installed_code_handle, triggered_deoptimizations_handle);

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
      HotSpotInstalledCode::set_start(installed_code_handle, (jlong) cb->code_begin());
      nmethod* nm = cb->as_nmethod_or_null();
      assert(nm == NULL || !installed_code_handle->is_scavengable() || nm->on_scavenge_root_list(), "nm should be scavengable if installed_code is scavengable");
    }
  }
  return result;
C2V_END

C2V_VMENTRY(jobject, getCode, (JNIEnv *jniEnv, jobject,  jlong codeBlob))
  ResourceMark rm;
  HandleMark hm;

  CodeBlob* cb = (CodeBlob*) (address) codeBlob;
  if (cb == NULL) {
    return NULL;
  }
  if (cb->is_nmethod()) {
    nmethod* nm = (nmethod*) cb;
    if (!nm->is_alive()) {
      return NULL;
    }
  }
  int length = cb->code_size();
  arrayOop codeCopy = oopFactory::new_byteArray(length, CHECK_0);
  memcpy(codeCopy->base(T_BYTE), cb->code_begin(), length);
  return JNIHandles::make_local(codeCopy);
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
  // sized to 20x code size should be sufficient.
  int bufferSize = cb->code_size() * 20;
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

C2V_VMENTRY(jobject, executeCompiledMethodVarargs, (JNIEnv *env, jobject, jobject args, jlong nmethodValue))
  ResourceMark rm;
  HandleMark hm;

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

C2V_VMENTRY(jint, getVtableEntryOffset, (JNIEnv *, jobject, jlong metaspace_method))

  Method* method = asMethod(metaspace_method);
  assert(!InstanceKlass::cast(method->method_holder())->is_interface(), "vtableEntryOffset cannot be called for interface methods");
  assert(InstanceKlass::cast(method->method_holder())->is_linked(), "vtableEntryOffset cannot be called is holder is not linked");
  assert(method->vtable_index() >= 0, "vtable entry offset should not be used");

  // get entry offset in words
  int vtable_entry_offset = InstanceKlass::vtable_start_offset() + method->vtable_index() * vtableEntry::size();
  // convert to bytes
  vtable_entry_offset = vtable_entry_offset * wordSize + vtableEntry::method_offset_in_bytes();

  return vtable_entry_offset;
C2V_END

C2V_VMENTRY(jboolean, hasVtableEntry, (JNIEnv *, jobject, jlong metaspace_method))
  Method* method = asMethod(metaspace_method);
  return method->vtable_index() >= 0;
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

C2V_VMENTRY(jobject, getLocalVariableTable, (JNIEnv *, jobject, jobject hotspot_method))
  ResourceMark rm;

  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  if (!method->has_localvariable_table()) {
    return NULL;
  }
  int localvariable_table_length = method->localvariable_table_length();

  objArrayHandle local_array = oopFactory::new_objArray(SystemDictionary::LocalImpl_klass(), localvariable_table_length, CHECK_NULL);
  LocalVariableTableElement* table = method->localvariable_table_start();
  for (int i = 0; i < localvariable_table_length; i++) {
    u2 start_bci = table[i].start_bci;
    u4 end_bci = (u4)(start_bci + table[i].length);
    u2 nameCPIdx = table[i].name_cp_index;
    u2 typeCPIdx = table[i].descriptor_cp_index;
    u2 slot = table[i].slot;

    char* name = method->constants()->symbol_at(nameCPIdx)->as_C_string();
    Handle nameHandle = java_lang_String::create_from_str(name, CHECK_NULL);

    char* typeInfo = method->constants()->symbol_at(typeCPIdx)->as_C_string();
    Handle typeHandle = java_lang_String::create_from_str(typeInfo, CHECK_NULL);

    Handle holderHandle = GraalCompiler::createHotSpotResolvedObjectType(method, CHECK_0);
    Handle local = VMToCompiler::createLocal(nameHandle, typeHandle, (int) start_bci, (int) end_bci, (int) slot, holderHandle, Thread::current());
    local_array->obj_at_put(i, local());
  }

  return JNIHandles::make_local(local_array());
C2V_END


C2V_VMENTRY(jobject, getFileName, (JNIEnv *, jobject, jobject klass))
  ResourceMark rm;
  InstanceKlass* k = (InstanceKlass*) asKlass(HotSpotResolvedObjectType::metaspaceKlass(klass));
  Symbol *s = k->source_file_name();
  int length;
  jchar *name = s->as_unicode(length);

  Handle result = java_lang_String::create_from_unicode(name, length, CHECK_NULL);
  return JNIHandles::make_local(result());

C2V_END


C2V_VMENTRY(void, reprofile, (JNIEnv *env, jobject, jlong metaspace_method))
  Method* method = asMethod(metaspace_method);
  method->reset_counters();

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
    method_data->initialize();
  }
C2V_END


C2V_VMENTRY(void, invalidateInstalledCode, (JNIEnv *env, jobject, jlong nativeMethod))
  nmethod* m = (nmethod*)nativeMethod;
  if (!m->is_not_entrant()) {
    m->mark_for_deoptimization();
    VM_Deoptimize op;
    VMThread::execute(&op);
  }
C2V_END


C2V_VMENTRY(jboolean, isInstalledCodeValid, (JNIEnv *env, jobject, jlong nativeMethod))
  nmethod* m = (nmethod*)nativeMethod;
  return m->is_alive() && !m->is_not_entrant();
C2V_END

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &(c2v_ ## f))

#define RESOLVED_TYPE         "Lcom/oracle/graal/api/meta/ResolvedJavaType;"
#define TYPE                  "Lcom/oracle/graal/api/meta/JavaType;"
#define METHOD                "Lcom/oracle/graal/api/meta/JavaMethod;"
#define FIELD                 "Lcom/oracle/graal/api/meta/JavaField;"
#define SIGNATURE             "Lcom/oracle/graal/api/meta/Signature;"
#define CONSTANT_POOL         "Lcom/oracle/graal/api/meta/ConstantPool;"
#define CONSTANT              "Lcom/oracle/graal/api/meta/Constant;"
#define KIND                  "Lcom/oracle/graal/api/meta/Kind;"
#define LOCAL                  "Lcom/oracle/graal/api/meta/Local;"
#define RUNTIME_CALL          "Lcom/oracle/graal/api/code/RuntimeCall;"
#define EXCEPTION_HANDLERS    "[Lcom/oracle/graal/api/meta/ExceptionHandler;"
#define REFLECT_METHOD        "Ljava/lang/reflect/Method;"
#define REFLECT_CONSTRUCTOR   "Ljava/lang/reflect/Constructor;"
#define REFLECT_FIELD         "Ljava/lang/reflect/Field;"
#define STRING                "Ljava/lang/String;"
#define OBJECT                "Ljava/lang/Object;"
#define CLASS                 "Ljava/lang/Class;"
#define STACK_TRACE_ELEMENT   "Ljava/lang/StackTraceElement;"
#define HS_RESOLVED_TYPE      "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedObjectType;"
#define HS_RESOLVED_JAVA_TYPE "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaType;"
#define HS_RESOLVED_METHOD    "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaMethod;"
#define HS_RESOLVED_FIELD     "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaField;"
#define HS_COMPILED_CODE      "Lcom/oracle/graal/hotspot/HotSpotCompiledCode;"
#define HS_CONFIG             "Lcom/oracle/graal/hotspot/HotSpotVMConfig;"
#define HS_METHOD             "Lcom/oracle/graal/hotspot/meta/HotSpotMethod;"
#define HS_INSTALLED_CODE     "Lcom/oracle/graal/hotspot/meta/HotSpotInstalledCode;"
#define METHOD_DATA           "Lcom/oracle/graal/hotspot/meta/HotSpotMethodData;"
#define METASPACE_METHOD      "J"
#define METASPACE_METHOD_DATA "J"
#define NMETHOD               "J"

JNINativeMethod CompilerToVM_methods[] = {
  {CC"initializeBytecode",            CC"("METASPACE_METHOD"[B)[B",                                     FN_PTR(initializeBytecode)},
  {CC"getSignature",                  CC"("METASPACE_METHOD")"STRING,                                   FN_PTR(getSignature)},
  {CC"initializeExceptionHandlers",   CC"("METASPACE_METHOD EXCEPTION_HANDLERS")"EXCEPTION_HANDLERS,    FN_PTR(initializeExceptionHandlers)},
  {CC"hasBalancedMonitors",           CC"("METASPACE_METHOD")Z",                                        FN_PTR(hasBalancedMonitors)},
  {CC"getUniqueConcreteMethod",       CC"("METASPACE_METHOD"["HS_RESOLVED_TYPE")"METASPACE_METHOD,      FN_PTR(getUniqueConcreteMethod)},
  {CC"getUniqueImplementor",          CC"("HS_RESOLVED_TYPE")"RESOLVED_TYPE,                            FN_PTR(getUniqueImplementor)},
  {CC"getStackTraceElement",          CC"("METASPACE_METHOD"I)"STACK_TRACE_ELEMENT,                     FN_PTR(getStackTraceElement)},
  {CC"initializeMethod",              CC"("METASPACE_METHOD HS_RESOLVED_METHOD")V",                     FN_PTR(initializeMethod)},
  {CC"initializeMethodData",          CC"("METASPACE_METHOD_DATA METHOD_DATA")V",                       FN_PTR(initializeMethodData)},
  {CC"isMethodCompilable",            CC"("METASPACE_METHOD")Z",                                        FN_PTR(isMethodCompilable)},
  {CC"getInvocationCount",            CC"("METASPACE_METHOD")I",                                        FN_PTR(getInvocationCount)},
  {CC"getCompiledCodeSize",           CC"("METASPACE_METHOD")I",                                        FN_PTR(getCompiledCodeSize)},
  {CC"getVtableEntryOffset",          CC"("METASPACE_METHOD")I",                                        FN_PTR(getVtableEntryOffset)},
  {CC"hasVtableEntry",                CC"("METASPACE_METHOD")Z",                                        FN_PTR(hasVtableEntry)},
  {CC"constantPoolLength",            CC"("HS_RESOLVED_TYPE")I",                                        FN_PTR(constantPoolLength)},
  {CC"lookupType",                    CC"("STRING HS_RESOLVED_TYPE"Z)"TYPE,                             FN_PTR(lookupType)},
  {CC"lookupConstantInPool",          CC"("HS_RESOLVED_TYPE"I)"OBJECT,                                  FN_PTR(lookupConstantInPool)},
  {CC"lookupAppendixInPool",          CC"("HS_RESOLVED_TYPE"IB)"OBJECT,                                 FN_PTR(lookupAppendixInPool)},
  {CC"lookupMethodInPool",            CC"("HS_RESOLVED_TYPE"IB)"METHOD,                                 FN_PTR(lookupMethodInPool)},
  {CC"lookupTypeInPool",              CC"("HS_RESOLVED_TYPE"I)"TYPE,                                    FN_PTR(lookupTypeInPool)},
  {CC"lookupReferencedTypeInPool",    CC"("HS_RESOLVED_TYPE"IB)V",                                      FN_PTR(lookupReferencedTypeInPool)},
  {CC"lookupFieldInPool",             CC"("HS_RESOLVED_TYPE"IB)"FIELD,                                  FN_PTR(lookupFieldInPool)},
  {CC"resolveMethod",                 CC"("HS_RESOLVED_TYPE STRING STRING")"METHOD,                     FN_PTR(resolveMethod)},
  {CC"getInstanceFields",             CC"("HS_RESOLVED_TYPE")["HS_RESOLVED_FIELD,                       FN_PTR(getInstanceFields)},
  {CC"getMethods",                    CC"("HS_RESOLVED_TYPE")["HS_RESOLVED_METHOD,                      FN_PTR(getMethods)},
  {CC"isTypeInitialized",             CC"("HS_RESOLVED_TYPE")Z",                                        FN_PTR(isTypeInitialized)},
  {CC"hasFinalizableSubclass",        CC"("HS_RESOLVED_TYPE")Z",                                        FN_PTR(hasFinalizableSubclass)},
  {CC"initializeType",                CC"("HS_RESOLVED_TYPE")V",                                        FN_PTR(initializeType)},
  {CC"getMaxCallTargetOffset",        CC"(J)J",                                                         FN_PTR(getMaxCallTargetOffset)},
  {CC"getResolvedType",               CC"("CLASS")"RESOLVED_TYPE,                                       FN_PTR(getResolvedType)},
  {CC"getMetaspaceMethod",            CC"("REFLECT_METHOD"["HS_RESOLVED_TYPE")"METASPACE_METHOD,        FN_PTR(getMetaspaceMethod)},
  {CC"getMetaspaceConstructor",       CC"("REFLECT_CONSTRUCTOR"["HS_RESOLVED_TYPE")"METASPACE_METHOD,   FN_PTR(getMetaspaceConstructor)},
  {CC"getJavaField",                  CC"("REFLECT_FIELD")"HS_RESOLVED_FIELD,                           FN_PTR(getJavaField)},
  {CC"initializeConfiguration",       CC"("HS_CONFIG")V",                                               FN_PTR(initializeConfiguration)},
  {CC"installCode0",                  CC"("HS_COMPILED_CODE HS_INSTALLED_CODE"[Z)I",                    FN_PTR(installCode0)},
  {CC"getCode",                       CC"(J)[B",                                                        FN_PTR(getCode)},
  {CC"disassembleCodeBlob",           CC"(J)"STRING,                                                    FN_PTR(disassembleCodeBlob)},
  {CC"executeCompiledMethodVarargs",  CC"(["OBJECT NMETHOD")"OBJECT,                                    FN_PTR(executeCompiledMethodVarargs)},
  {CC"getDeoptedLeafGraphIds",        CC"()[J",                                                         FN_PTR(getDeoptedLeafGraphIds)},
  {CC"getLineNumberTable",            CC"("HS_RESOLVED_METHOD")[J",                                     FN_PTR(getLineNumberTable)},
  {CC"getLocalVariableTable",         CC"("HS_RESOLVED_METHOD")["LOCAL,                                 FN_PTR(getLocalVariableTable)},
  {CC"getFileName",                   CC"("HS_RESOLVED_JAVA_TYPE")"STRING,                              FN_PTR(getFileName)},
  {CC"reprofile",                     CC"("METASPACE_METHOD")V",                                        FN_PTR(reprofile)},
  {CC"invalidateInstalledCode",       CC"(J)V",                                                         FN_PTR(invalidateInstalledCode)},
  {CC"isInstalledCodeValid",          CC"(J)Z",                                                         FN_PTR(isInstalledCodeValid)},
};

int CompilerToVM_methods_count() {
  return sizeof(CompilerToVM_methods) / sizeof(JNINativeMethod);
}

