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
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "graal/graalVmIds.hpp"


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
    while(!s.is_last_bytecode()) {
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
  return VmIds::toString<jstring>(method->signature(), THREAD);
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
  Handle name = VmIds::toString<Handle>(method->name(), CHECK);
  InstanceKlass::cast(HotSpotResolvedJavaMethod::klass())->initialize(CHECK);
  HotSpotResolvedJavaMethod::set_name(hotspot_method, name());
  HotSpotResolvedJavaMethod::set_codeSize(hotspot_method, method->code_size());
  HotSpotResolvedJavaMethod::set_exceptionHandlerCount(hotspot_method, method->exception_table_length());
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

C2V_VMENTRY(jobject, lookupType, (JNIEnv *env, jobject, jstring jname, jobject accessingClass, jboolean eagerResolve))
  ResourceMark rm;

  Symbol* nameSymbol = VmIds::toSymbol(jname);
  Handle name = JNIHandles::resolve(jname);
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
  if (tag.is_int()) {
    result = VMToCompiler::createConstant(Kind::Int(), cp->int_at(index), CHECK_0);
  } else if (tag.is_long()) {
    result = VMToCompiler::createConstant(Kind::Long(), cp->long_at(index), CHECK_0);
  } else if (tag.is_float()) {
    result = VMToCompiler::createConstantFloat(cp->float_at(index), CHECK_0);
  } else if (tag.is_double()) {
    result = VMToCompiler::createConstantDouble(cp->double_at(index), CHECK_0);
  } else if (tag.is_string()) {
    oop string = NULL;
    if (cp->is_pseudo_string_at(index)) {
      int obj_index = cp->cp_to_object_index(index);
      string = cp->pseudo_string_at(index, obj_index);
    } else {
      string = cp->string_at(index, THREAD);
      if (HAS_PENDING_EXCEPTION) {
        CLEAR_PENDING_EXCEPTION;
        // TODO: Gracefully exit compilation.
        fatal("out of memory during compilation!");
        return NULL;
      }
    }
    result = VMToCompiler::createConstantObject(string, CHECK_0);
  } else if (tag.is_klass() || tag.is_unresolved_klass()) {
    Handle type = GraalCompiler::get_JavaType(cp, index, cp->pool_holder(), CHECK_NULL);
    result = type();
  } else if (tag.is_object()) {
    oop obj = cp->object_at(index);
    assert(obj->is_instance(), "must be an instance");
    result = VMToCompiler::createConstantObject(obj, CHECK_NULL);
  } else {
    tty->print("unknown constant pool tag (%s) at cpi %d in %s: ", tag.internal_name(), index, cp->pool_holder()->name()->as_C_string());
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
C2V_END

C2V_VMENTRY(jobject, lookupMethodInPool, (JNIEnv *env, jobject, jobject type, jint index, jbyte opcode))
  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = InstanceKlass::cast(java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaMirror(type)))->constants();
  instanceKlassHandle pool_holder(cp->pool_holder());

  Bytecodes::Code bc = (Bytecodes::Code) (((int) opcode) & 0xFF);
  methodHandle method = GraalEnv::get_method_by_index(cp, index, bc, pool_holder);
  if (!method.is_null()) {
    Handle holder = GraalCompiler::get_JavaType(method->method_holder(), CHECK_NULL);
    return JNIHandles::make_local(THREAD, VMToCompiler::createResolvedJavaMethod(holder, method(), THREAD));
  } else {
    // Get the method's name and signature.
    Handle name = VmIds::toString<Handle>(cp->name_ref_at(index), CHECK_NULL);
    Handle signature  = VmIds::toString<Handle>(cp->signature_ref_at(index), CHECK_NULL);
    int holder_index = cp->klass_ref_index_at(index);
    Handle type = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
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
  int opcode = (op & 0xFF);
  if (opcode != Bytecodes::_checkcast && opcode != Bytecodes::_instanceof && opcode != Bytecodes::_new && opcode != Bytecodes::_anewarray
      && opcode != Bytecodes::_multianewarray && opcode != Bytecodes::_ldc && opcode != Bytecodes::_ldc_w && opcode != Bytecodes::_ldc2_w)
  {
    index = cp->remap_instruction_operand_from_cache(GraalCompiler::to_cp_index_u2(index));
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
  Symbol* name_symbol = VmIds::toSymbol(name);
  Symbol* signature_symbol = VmIds::toSymbol(signature);
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
      Handle type = GraalCompiler::get_JavaTypeFromSignature(fs.signature(), k, Thread::current());
      int flags = fs.access_flags().as_int();
      bool internal = fs.access_flags().is_internal();
      Handle name = VmIds::toString<Handle>(fs.name(), Thread::current());
      Handle field = VMToCompiler::createJavaField(JNIHandles::resolve(klass), name, type, fs.offset(), flags, internal, Thread::current());
      fields.append(field());
    }
  }
  objArrayHandle field_array = oopFactory::new_objArray(SystemDictionary::HotSpotResolvedJavaField_klass(), fields.length(), CHECK_NULL);
  for (int i = 0; i < fields.length(); ++i) {
    field_array->obj_at_put(i, fields.at(i)());
  }
  return JNIHandles::make_local(field_array());
C2V_END

C2V_VMENTRY(jlong, getMaxCallTargetOffset, (JNIEnv *env, jobject, jlong stub))
  address target_addr = (address) stub;
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

BasicType basicTypes[] = { T_BOOLEAN, T_BYTE, T_SHORT, T_CHAR, T_INT, T_FLOAT, T_LONG, T_DOUBLE, T_OBJECT };
int basicTypeCount = sizeof(basicTypes) / sizeof(BasicType);

C2V_ENTRY(void, initializeConfiguration, (JNIEnv *env, jobject, jobject config))

#define set_boolean(name, value) do { env->SetBooleanField(config, getFieldID(env, config, name, "Z"), value); } while (0)
#define set_int(name, value) do { env->SetIntField(config, getFieldID(env, config, name, "I"), value); } while (0)
#define set_long(name, value) do { env->SetLongField(config, getFieldID(env, config, name, "J"), value); } while (0)
#define set_object(name, value) do { env->SetObjectField(config, getFieldID(env, config, name, "Ljava/lang/Object;"), value); } while (0)
#define set_int_array(name, value) do { env->SetObjectField(config, getFieldID(env, config, name, "[I"), value); } while (0)

  guarantee(HeapWordSize == sizeof(char*), "Graal assumption that HeadWordSize == machine word size is wrong");
#ifdef _WIN64
  set_boolean("windowsOs", true);
#else
  set_boolean("windowsOs", false);
#endif
  set_boolean("verifyOops", VerifyOops);
  set_boolean("useFastLocking", GraalUseFastLocking);
  set_boolean("useBiasedLocking", UseBiasedLocking);
  set_boolean("usePopCountInstruction", UsePopCountInstruction);
  set_boolean("useAESIntrinsics", UseAESIntrinsics);
  set_boolean("useTLAB", UseTLAB);
  set_int("codeEntryAlignment", CodeEntryAlignment);
  set_int("vmPageSize", os::vm_page_size());
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
  set_int("extraStackEntries", Method::extra_stack_entries());
  set_int("methodAccessFlagsOffset", in_bytes(Method::access_flags_offset()));
  set_int("klassHasFinalizerFlag", JVM_ACC_HAS_FINALIZER);
  set_int("threadExceptionOopOffset", in_bytes(JavaThread::exception_oop_offset()));
  set_int("threadExceptionPcOffset", in_bytes(JavaThread::exception_pc_offset()));
  set_boolean("isPollingPageFar", Assembler::is_polling_page_far());
  set_int("classMirrorOffset", in_bytes(Klass::java_mirror_offset()));
  set_int("runtimeCallStackSize", (jint)frame::arg_reg_save_area_bytes);
  set_int("klassModifierFlagsOffset", in_bytes(Klass::modifier_flags_offset()));
  set_int("klassAccessFlagsOffset", in_bytes(Klass::access_flags_offset()));
  set_int("klassOffset", java_lang_Class::klass_offset_in_bytes());
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

  set_int("tlabAlignmentReserve", (int32_t)ThreadLocalAllocBuffer::alignment_reserve());
  set_long("tlabIntArrayMarkWord", (intptr_t)markOopDesc::prototype()->copy_set_hash(0x2));
  set_long("heapTopAddress", (jlong)(address) Universe::heap()->top_addr());
  set_long("heapEndAddress", (jlong)(address) Universe::heap()->end_addr());
  set_int("threadTlabStartOffset", in_bytes(JavaThread::tlab_start_offset()));
  set_int("threadTlabSizeOffset", in_bytes(JavaThread::tlab_size_offset()));
  set_int("threadAllocatedBytesOffset", in_bytes(JavaThread::allocated_bytes_offset()));
  set_int("tlabSlowAllocationsOffset", in_bytes(JavaThread::tlab_slow_allocations_offset()));
  set_int("tlabFastRefillWasteOffset", in_bytes(JavaThread::tlab_fast_refill_waste_offset()));
  set_int("tlabNumberOfRefillsOffset", in_bytes(JavaThread::tlab_number_of_refills_offset()));
  set_int("tlabRefillWasteLimitOffset", in_bytes(JavaThread::tlab_refill_waste_limit_offset()));
  set_int("tlabRefillWasteIncrement", (int32_t) ThreadLocalAllocBuffer::refill_waste_limit_increment());
  set_int("klassInstanceSizeOffset", in_bytes(Klass::layout_helper_offset()));
  set_boolean("tlabStats", TLABStats);
  set_boolean("inlineContiguousAllocationSupported", !CMSIncrementalMode && Universe::heap()->supports_inline_contig_alloc());

  set_long("arrayPrototypeMarkWord", (intptr_t)markOopDesc::prototype());
  set_int("layoutHelperLog2ElementSizeShift", Klass::_lh_log2_element_size_shift);
  set_int("layoutHelperLog2ElementSizeMask", Klass::_lh_log2_element_size_mask);
  set_int("layoutHelperElementTypeShift", Klass::_lh_element_type_shift);
  set_int("layoutHelperElementTypeMask", Klass::_lh_element_type_mask);
  set_int("layoutHelperHeaderSizeShift", Klass::_lh_header_size_shift);
  set_int("layoutHelperHeaderSizeMask", Klass::_lh_header_size_mask);
  set_int("layoutHelperOffset", in_bytes(Klass::layout_helper_offset()));

  set_long("debugStub", VmIds::addStub((address)warning));
  set_long("instanceofStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_slow_subtype_check_id)));
  set_long("newInstanceStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_new_instance_id)));
  set_long("newArrayStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_new_array_id)));
  set_long("newMultiArrayStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_new_multi_array_id)));
  set_long("identityHashCodeStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_identity_hash_code_id)));
  set_long("threadIsInterruptedStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_thread_is_interrupted_id)));
  set_long("inlineCacheMissStub", VmIds::addStub(SharedRuntime::get_ic_miss_stub()));
  set_long("handleExceptionStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_handle_exception_nofpu_id)));
  set_long("handleDeoptStub", VmIds::addStub(SharedRuntime::deopt_blob()->unpack()));
  set_long("monitorEnterStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_monitorenter_id)));
  set_long("monitorExitStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_monitorexit_id)));
  set_long("verifyOopStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_verify_oop_id)));
  set_long("vmErrorStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_vm_error_id)));
  set_long("deoptimizeStub", VmIds::addStub(SharedRuntime::deopt_blob()->uncommon_trap()));
  set_long("unwindExceptionStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_unwind_exception_call_id)));
  set_long("osrMigrationEndStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_OSR_migration_end_id)));
  set_long("registerFinalizerStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_register_finalizer_id)));
  set_long("setDeoptInfoStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_set_deopt_info_id)));
  set_long("createNullPointerExceptionStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_create_null_pointer_exception_id)));
  set_long("createOutOfBoundsExceptionStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_create_out_of_bounds_exception_id)));
  set_long("javaTimeMillisStub", VmIds::addStub(CAST_FROM_FN_PTR(address, os::javaTimeMillis)));
  set_long("javaTimeNanosStub", VmIds::addStub(CAST_FROM_FN_PTR(address, os::javaTimeNanos)));
  set_long("arithmeticFremStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_arithmetic_frem_id)));
  set_long("arithmeticDremStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_arithmetic_drem_id)));
  set_long("arithmeticSinStub", VmIds::addStub(CAST_FROM_FN_PTR(address, SharedRuntime::dsin)));
  set_long("arithmeticCosStub", VmIds::addStub(CAST_FROM_FN_PTR(address, SharedRuntime::dcos)));
  set_long("arithmeticTanStub", VmIds::addStub(CAST_FROM_FN_PTR(address, SharedRuntime::dtan)));
  set_long("logPrimitiveStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_log_primitive_id)));
  set_long("logObjectStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_log_object_id)));
  set_long("logPrintfStub", VmIds::addStub(GraalRuntime::entry_for(GraalRuntime::graal_log_printf_id)));
  set_long("aescryptEncryptBlockStub", VmIds::addStub(StubRoutines::aescrypt_encryptBlock()));
  set_long("aescryptDecryptBlockStub", VmIds::addStub(StubRoutines::aescrypt_decryptBlock()));

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

  set_int("deoptActionNone", Deoptimization::Action_none);
  set_int("deoptActionMaybeRecompile", Deoptimization::Action_maybe_recompile);
  set_int("deoptActionReinterpret", Deoptimization::Action_reinterpret);
  set_int("deoptActionMakeNotEntrant", Deoptimization::Action_make_not_entrant);
  set_int("deoptActionMakeNotCompilable", Deoptimization::Action_make_not_compilable);


  BarrierSet* bs = Universe::heap()->barrier_set();
  switch (bs->kind()) {
    case BarrierSet::CardTableModRef:
    case BarrierSet::CardTableExtension: {
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
#ifndef SERIALGC
    case BarrierSet::G1SATBCT:
    case BarrierSet::G1SATBCTLogging:
#endif // SERIALGC
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

C2V_VMENTRY(jint, installCode0, (JNIEnv *jniEnv, jobject, jobject compResult, jobject installed_code, jobject info))
  ResourceMark rm;
  HandleMark hm;
  Handle compResultHandle = JNIHandles::resolve(compResult);
  nmethod* nm = NULL;
  methodHandle method = getMethodFromHotSpotMethod(HotSpotCompilationResult::method(compResult));
  Handle installed_code_handle = JNIHandles::resolve(installed_code);
  GraalEnv::CodeInstallResult result;
  CodeInstaller installer(compResultHandle, method, result, nm, installed_code_handle);

  if (result != GraalEnv::ok) {
    assert(nm == NULL, "should be");
  } else {
    if (info != NULL) {
      arrayOop codeCopy = oopFactory::new_byteArray(nm->code_size(), CHECK_0);
      memcpy(codeCopy->base(T_BYTE), nm->code_begin(), nm->code_size());
      HotSpotCodeInfo::set_code(info, codeCopy);
      HotSpotCodeInfo::set_start(info, (jlong) nm->code_begin());
    }

    if (!installed_code_handle.is_null()) {
      assert(installed_code_handle->is_a(HotSpotInstalledCode::klass()), "wrong type");
      HotSpotInstalledCode::set_nmethod(installed_code_handle, (jlong) nm);
      HotSpotInstalledCode::set_method(installed_code_handle, HotSpotCompilationResult::method(compResult));
      assert(nm == NULL || !installed_code_handle->is_scavengable() || nm->on_scavenge_root_list(), "nm should be scavengable if installed_code is scavengable");
    }
  }
  return result;
C2V_END

C2V_VMENTRY(jobject, disassembleNative, (JNIEnv *jniEnv, jobject, jbyteArray code, jlong start_address))
  ResourceMark rm;
  HandleMark hm;

  stringStream(st);
  arrayOop code_oop = (arrayOop) JNIHandles::resolve(code);
  int len = code_oop->length();
  address begin = (address) code_oop->base(T_BYTE);
  address end = begin + len;
  Disassembler::decode(begin, end, &st);

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

C2V_VMENTRY(jobject, executeCompiledMethodVarargs, (JNIEnv *env, jobject, jlong metaspace_method, jlong metaspace_nmethod, jobject args))
  ResourceMark rm;
  HandleMark hm;

  assert(metaspace_method != 0, "just checking");
  methodHandle mh = asMethod(metaspace_method);
  Symbol* signature = mh->signature();
  JavaCallArguments jca(mh->size_of_parameters());

  JavaArgumentUnboxer jap(signature, &jca, (arrayOop) JNIHandles::resolve(args), mh->is_static());
  JavaValue result(jap.get_ret_type());

  nmethod* nm = (nmethod*) (address) metaspace_nmethod;
  if (nm == NULL || !nm->is_alive()) {
    THROW_0(vmSymbols::MethodInvalidatedException());
  }

  JavaCalls::call(&result, mh, nm, &jca, CHECK_NULL);

  if (jap.get_ret_type() == T_VOID) {
    return NULL;
  } else if (jap.get_ret_type() == T_OBJECT || jap.get_ret_type() == T_ARRAY) {
    return JNIHandles::make_local((oop) result.get_jobject());
  } else {
    oop o = java_lang_boxing_object::create(jap.get_ret_type(), (jvalue *) result.get_value_addr(), CHECK_NULL);
    return JNIHandles::make_local(o);
  }
C2V_END

C2V_VMENTRY(jobject, executeCompiledMethod, (JNIEnv *env, jobject, jlong metaspace_method, jlong metaspace_nmethod, jobject arg1, jobject arg2, jobject arg3))
  ResourceMark rm;
  HandleMark hm;

  methodHandle method = asMethod(metaspace_method);
  assert(!method.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(JNIHandles::resolve(arg1));
  args.push_oop(JNIHandles::resolve(arg2));
  args.push_oop(JNIHandles::resolve(arg3));

  nmethod* nm = (nmethod*) (address) metaspace_nmethod;
  if (nm == NULL || !nm->is_alive()) {
    THROW_0(vmSymbols::MethodInvalidatedException());
  }

  JavaCalls::call(&result, method, nm, &args, CHECK_NULL);

  return JNIHandles::make_local((oop) result.get_jobject());
C2V_END

C2V_VMENTRY(jint, getVtableEntryOffset, (JNIEnv *, jobject, jlong metaspace_method))

  Method* method = asMethod(metaspace_method);
  assert(!InstanceKlass::cast(method->method_holder())->is_interface(), "vtableEntryOffset cannot be called for interface methods");
  assert(InstanceKlass::cast(method->method_holder())->is_linked(), "vtableEntryOffset cannot be called is holder is not linked");

  // get entry offset in words
  int vtable_entry_offset = InstanceKlass::vtable_start_offset() + method->vtable_index() * vtableEntry::size();
  // convert to bytes
  vtable_entry_offset = vtable_entry_offset * wordSize + vtableEntry::method_offset_in_bytes();

  return vtable_entry_offset;
C2V_END

C2V_VMENTRY(jobject, getDeoptedLeafGraphIds, (JNIEnv *, jobject))

  // the contract for this method is as follows:
  // returning null: no deopted leaf graphs
  // returning array (size > 0): the ids of the deopted leaf graphs
  // returning array (size == 0): there was an overflow, the compiler needs to clear its cache completely

  oop array = GraalCompiler::instance()->dump_deopted_leaf_graphs(CHECK_NULL);
  return JNIHandles::make_local(array);
C2V_END

C2V_VMENTRY(jobject, decodePC, (JNIEnv *, jobject, jlong pc))
  stringStream(st);
  CodeBlob* blob = CodeCache::find_blob_unsafe((void*) pc);
  if (blob == NULL) {
    st.print("[unidentified pc]");
  } else {
    st.print(blob->name());

    nmethod* nm = blob->as_nmethod_or_null();
    if (nm != NULL && nm->method() != NULL) {
      st.print(" %s.", nm->method()->method_holder()->external_name());
      nm->method()->name()->print_symbol_on(&st);
      st.print("  @ %d", pc - (jlong) nm->entry_point());
    }
  }
  Handle result = java_lang_String::create_from_platform_dependent_str(st.as_string(), CHECK_NULL);
  return JNIHandles::make_local(result());
C2V_END

C2V_ENTRY(jlongArray, getLineNumberTable, (JNIEnv *env, jobject, jobject hotspot_method))
// XXX: Attention: it seEms that the line number table of a method just contains lines that are important, means that
// empty lines are left out or lines that can't have a breakpoint on it; eg int a; or try {
  Method* method = getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
  if(!method->has_linenumber_table()) {
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


C2V_VMENTRY(jobject, getFileName, (JNIEnv *, jobject, jobject klass))
  ResourceMark rm;
  InstanceKlass* k = (InstanceKlass*) asKlass(HotSpotResolvedObjectType::metaspaceKlass(klass));
  Symbol *s = k->source_file_name();
  int length;
  jchar *name = s->as_unicode(length);

  Handle result = java_lang_String::create_from_unicode(name, length, CHECK_NULL);
  return JNIHandles::make_local(result());

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
#define HS_COMP_RESULT        "Lcom/oracle/graal/hotspot/HotSpotCompilationResult;"
#define HS_CONFIG             "Lcom/oracle/graal/hotspot/HotSpotVMConfig;"
#define HS_METHOD             "Lcom/oracle/graal/hotspot/meta/HotSpotMethod;"
#define HS_INSTALLED_CODE     "Lcom/oracle/graal/hotspot/meta/HotSpotInstalledCode;"
#define HS_CODE_INFO          "Lcom/oracle/graal/hotspot/meta/HotSpotCodeInfo;"
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
  {CC"lookupType",                    CC"("STRING HS_RESOLVED_TYPE"Z)"TYPE,                             FN_PTR(lookupType)},
  {CC"lookupConstantInPool",          CC"("HS_RESOLVED_TYPE"I)"OBJECT,                                  FN_PTR(lookupConstantInPool)},
  {CC"lookupMethodInPool",            CC"("HS_RESOLVED_TYPE"IB)"METHOD,                                 FN_PTR(lookupMethodInPool)},
  {CC"lookupTypeInPool",              CC"("HS_RESOLVED_TYPE"I)"TYPE,                                    FN_PTR(lookupTypeInPool)},
  {CC"lookupReferencedTypeInPool",    CC"("HS_RESOLVED_TYPE"IB)V",                                      FN_PTR(lookupReferencedTypeInPool)},
  {CC"lookupFieldInPool",             CC"("HS_RESOLVED_TYPE"IB)"FIELD,                                  FN_PTR(lookupFieldInPool)},
  {CC"resolveMethod",                 CC"("HS_RESOLVED_TYPE STRING STRING")"METHOD,                     FN_PTR(resolveMethod)},
  {CC"getInstanceFields",             CC"("HS_RESOLVED_TYPE")["HS_RESOLVED_FIELD,                       FN_PTR(getInstanceFields)},
  {CC"isTypeInitialized",             CC"("HS_RESOLVED_TYPE")Z",                                        FN_PTR(isTypeInitialized)},
  {CC"initializeType",                CC"("HS_RESOLVED_TYPE")V",                                        FN_PTR(initializeType)},
  {CC"getMaxCallTargetOffset",        CC"(J)J",                                                         FN_PTR(getMaxCallTargetOffset)},
  {CC"getResolvedType",               CC"("CLASS")"RESOLVED_TYPE,                                       FN_PTR(getResolvedType)},
  {CC"getMetaspaceMethod",            CC"("REFLECT_METHOD"["HS_RESOLVED_TYPE")"METASPACE_METHOD,        FN_PTR(getMetaspaceMethod)},
  {CC"getMetaspaceConstructor",       CC"("REFLECT_CONSTRUCTOR"["HS_RESOLVED_TYPE")"METASPACE_METHOD,   FN_PTR(getMetaspaceConstructor)},
  {CC"getJavaField",                  CC"("REFLECT_FIELD")"HS_RESOLVED_FIELD,                           FN_PTR(getJavaField)},
  {CC"initializeConfiguration",       CC"("HS_CONFIG")V",                                               FN_PTR(initializeConfiguration)},
  {CC"installCode0",                  CC"("HS_COMP_RESULT HS_INSTALLED_CODE HS_CODE_INFO")I",           FN_PTR(installCode0)},
  {CC"disassembleNative",             CC"([BJ)"STRING,                                                  FN_PTR(disassembleNative)},
  {CC"executeCompiledMethod",         CC"("METASPACE_METHOD NMETHOD OBJECT OBJECT OBJECT")"OBJECT,      FN_PTR(executeCompiledMethod)},
  {CC"executeCompiledMethodVarargs",  CC"("METASPACE_METHOD NMETHOD "["OBJECT")"OBJECT,                 FN_PTR(executeCompiledMethodVarargs)},
  {CC"getDeoptedLeafGraphIds",        CC"()[J",                                                         FN_PTR(getDeoptedLeafGraphIds)},
  {CC"decodePC",                      CC"(J)"STRING,                                                    FN_PTR(decodePC)},
  {CC"getLineNumberTable",            CC"("HS_RESOLVED_METHOD")[J",                                     FN_PTR(getLineNumberTable)},
  {CC"getFileName",                   CC"("HS_RESOLVED_JAVA_TYPE")"STRING,                              FN_PTR(getFileName)},
};

int CompilerToVM_methods_count() {
  return sizeof(CompilerToVM_methods) / sizeof(JNINativeMethod);
}

