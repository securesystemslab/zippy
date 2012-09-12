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
#include "c1/c1_Runtime1.hpp"
#include "ci/ciMethodData.hpp"
#include "compiler/compileBroker.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "graal/graalVmIds.hpp"
#include "memory/oopFactory.hpp"
#include "oops/generateOopMap.hpp"

methodOop getMethodFromHotSpotMethod(jobject hotspot_method) {
  return getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
}

methodOop getMethodFromHotSpotMethod(oop hotspot_method) {
  return (methodOop)HotSpotResolvedJavaMethod::javaMirror(hotspot_method);
}

methodDataOop getMethodDataFromHotSpotMethodData(jobject hotspot_method_data) {
  return (methodDataOop)HotSpotMethodData::hotspotMirror(JNIHandles::resolve(hotspot_method_data));
}

// public byte[] JavaMethod_code(HotSpotResolvedMethod method);
JNIEXPORT jbyteArray JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1code(JNIEnv *env, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_code");
  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  
  // copy all bytecodes
  int code_size = method->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *) method->code_base());
  
  // iterate over all bytecodes and replace non-Java bytecodes
  if (RewriteBytecodes || RewriteFrequentPairs) {
    BytecodeStream s(method);
    while(!s.is_last_bytecode()) {
      s.next();
      Bytecodes::Code code = s.raw_code();
      if (!Bytecodes::is_java_code(code)) {
        jbyte original_code = Bytecodes::java_code(code);
        env->SetByteArrayRegion(result, s.bci(), 1, &original_code);
      }
    }
  }  

  // replace all breakpoints
  if (method->number_of_breakpoints() > 0) {
    BreakpointInfo* bp = instanceKlass::cast(method->method_holder())->breakpoints();
    for (; bp != NULL; bp = bp->next()) {
      if (bp->match(method())) {
        jbyte code = bp->orig_bytecode();
        env->SetByteArrayRegion(result, bp->bci(), 1, &code);
      }
    }
  }

  return result;
}

// public String JavaMethod_signature(HotSpotResolvedMethod method);
JNIEXPORT jstring JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1signature(JNIEnv *env, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_signature");
  VM_ENTRY_MARK
  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  assert(method != NULL && method->signature() != NULL, "signature required");
  return VmIds::toString<jstring>(method->signature(), THREAD);
}

// public ExceptionHandler[] JavaMethod_exceptionHandlers(long vmId);
JNIEXPORT jobjectArray JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1exceptionHandlers(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_exceptionHandlers");
  VM_ENTRY_MARK
  ResourceMark rm;
  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  int handler_count = method->exception_table_length();
  ExceptionTableElement* handlers = handler_count == 0 ? NULL : method->exception_table_start();

  instanceKlass::cast(ExceptionHandler::klass())->initialize(CHECK_NULL);
  objArrayHandle array = oopFactory::new_objArray(SystemDictionary::ExceptionHandler_klass(), handler_count, CHECK_NULL);

  for (int i = 0; i < handler_count; i++) {
    ExceptionTableElement* handler = handlers + i;
    Handle entry = instanceKlass::cast(ExceptionHandler::klass())->allocate_instance(CHECK_NULL);
    ExceptionHandler::set_startBCI(entry, handler->start_pc);
    ExceptionHandler::set_endBCI(entry, handler->end_pc);
    ExceptionHandler::set_handlerBCI(entry, handler->handler_pc);
    int catch_class_index = handler->catch_type_index;
    ExceptionHandler::set_catchTypeCPI(entry, catch_class_index);

    if (catch_class_index == 0) {
      ExceptionHandler::set_catchType(entry, NULL);
    } else {
      constantPoolOop cp = instanceKlass::cast(method->method_holder())->constants();
      KlassHandle loading_klass = method->method_holder();
      Handle catch_class = GraalCompiler::get_JavaType(cp, catch_class_index, loading_klass, CHECK_NULL);
      if (catch_class->klass() == HotSpotResolvedJavaType::klass() && java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(catch_class)) == SystemDictionary::Throwable_klass()) {
        ExceptionHandler::set_catchType(entry, NULL);
        ExceptionHandler::set_catchTypeCPI(entry, 0);
      } else {
        ExceptionHandler::set_catchType(entry, catch_class());
      }
    }
    array->obj_at_put(i, entry());
  }

  return (jobjectArray) JNIHandles::make_local(array());
}

// public boolean JavaMethod_hasBalancedMonitors(long vmId);
JNIEXPORT jint JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1hasBalancedMonitors(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_hasBalancedMonitors");

  VM_ENTRY_MARK;

  // Analyze the method to see if monitors are used properly.
  methodHandle method(THREAD, getMethodFromHotSpotMethod(hotspot_method));
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
}

// public JavaMethod getJavaMethod(java.lang.reflect.Method reflectionMethod);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_getJavaMethod(JNIEnv *, jobject, jobject reflection_method_handle) {
  TRACE_graal_3("CompilerToVM::getJavaMethod");
  VM_ENTRY_MARK;
  oop reflection_method = JNIHandles::resolve(reflection_method_handle);
  oop reflection_holder = java_lang_reflect_Method::clazz(reflection_method);
  int slot = java_lang_reflect_Method::slot(reflection_method);
  klassOop holder = java_lang_Class::as_klassOop(reflection_holder);
  methodOop method = instanceKlass::cast(holder)->method_with_idnum(slot);
  Handle ret = GraalCompiler::createHotSpotResolvedJavaMethod(method, CHECK_NULL);
  return JNIHandles::make_local(THREAD, ret());
}

// public boolean JavaMethod_uniqueConcreteMethod(long vmId);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1uniqueConcreteMethod(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_uniqueConcreteMethod");

  VM_ENTRY_MARK;
  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  KlassHandle holder = method->method_holder();
  if (holder->is_interface()) {
    // Cannot trust interfaces. Because of:
    // interface I { void foo(); }
    // class A { public void foo() {} }
    // class B extends A implements I { }
    // class C extends B { public void foo() { } }
    // class D extends B { }
    // Would lead to identify C.foo() as the unique concrete method for I.foo() without seeing A.foo().
    return NULL;
  }
  methodHandle unique_concrete;
  {
    ResourceMark rm;
    MutexLocker locker(Compile_lock);
    unique_concrete = Dependencies::find_unique_concrete_method(holder(), method());
  }
  if (unique_concrete.is_null()) {
    return NULL;
  } else {
    Handle method_resolved = GraalCompiler::createHotSpotResolvedJavaMethod(unique_concrete, CHECK_NULL);
    return JNIHandles::make_local(THREAD, method_resolved());
  }
}

// public native int JavaMethod_invocationCount(long vmId);
JNIEXPORT jint JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1invocationCount(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_invocationCount");
  return getMethodFromHotSpotMethod(hotspot_method)->invocation_count();
}

// public native HotSpotMethodData JavaMethod_methodData(HotSpotResolvedJavaMethod method);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1methodData(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_methodData");
  VM_ENTRY_MARK;

  methodDataHandle method_data = getMethodFromHotSpotMethod(hotspot_method)->method_data();
  if(method_data.is_null()) {
    return NULL;
  } else {
    Handle graalMethodData = GraalCompiler::createHotSpotMethodData(method_data, CHECK_NULL);
    return JNIHandles::make_local(THREAD, graalMethodData());
  }
}

// ------------------------------------------------------------------
// Adjust a CounterData count to be commensurate with
// interpreter_invocation_count.  If the MDO exists for
// only 25% of the time the method exists, then the
// counts in the MDO should be scaled by 4X, so that
// they can be usefully and stably compared against the
// invocation counts in methods.
int scale_count(methodDataOop method_data, int count) {
  if (count > 0) {
    int counter_life;
    int method_life = method_data->method()->interpreter_invocation_count();
    int current_mileage = methodDataOopDesc::mileage_of(method_data->method());
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

// public native boolean JavaMethod_hasCompiledCode(HotSpotResolvedJavaMethod method);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1hasCompiledCode(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_hasCompiledCode");
  return getMethodFromHotSpotMethod(hotspot_method)->has_compiled_code();
}

// public native int JavaMethod_getCompiledCodeSize(HotSpotResolvedJavaMethod method);
JNIEXPORT jint JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1getCompiledCodeSize(JNIEnv *env, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_getCompiledCodeSize");
  nmethod* code = getMethodFromHotSpotMethod(hotspot_method)->code();
  return code == NULL ? 0 : code->insts_size();
}

// public JavaType Signature_lookupType(String returnType, HotSpotResolvedJavaType accessingClass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_Signature_1lookupType(JNIEnv *env, jobject, jstring jname, jobject accessingClass, jboolean eagerResolve) {
  TRACE_graal_3("CompilerToVM::Signature_lookupType");
  VM_ENTRY_MARK;
  ResourceMark rm;

  Symbol* nameSymbol = VmIds::toSymbol(jname);
  Handle name = JNIHandles::resolve(jname);

  oop result;
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
    klassOop resolved_type = NULL;
    // if the name isn't in the symbol table then the class isn't loaded anyway...
    if (nameSymbol != NULL) {
      Handle classloader;
      Handle protectionDomain;
      if (JNIHandles::resolve(accessingClass) != NULL) {
        classloader = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(accessingClass))->klass_part()->class_loader();
        protectionDomain = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(accessingClass))->klass_part()->protection_domain();
      }
      if (eagerResolve) {
        resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
      } else {
        if (FieldType::is_obj(nameSymbol)) {
          ResourceMark rm(THREAD);
          // Ignore wrapping L and ;.
          TempNewSymbol tmp_name = SymbolTable::new_symbol(nameSymbol->as_C_string() + 1,
                                         nameSymbol->utf8_length() - 2, CHECK_NULL);
          resolved_type = SystemDictionary::find_instance_or_array_klass(tmp_name, classloader, protectionDomain, THREAD);
        } else {
          resolved_type = SystemDictionary::find_instance_or_array_klass(nameSymbol, classloader, protectionDomain, THREAD);
        }
      }
      if (HAS_PENDING_EXCEPTION) {
        CLEAR_PENDING_EXCEPTION;
        resolved_type = NULL;
      }
    }
    if (resolved_type != NULL) {
      Handle type = GraalCompiler::createHotSpotResolvedJavaType(resolved_type, name, CHECK_NULL);
      result = type();
    } else {
      Handle type = VMToCompiler::createJavaType(name, THREAD);
      result = type();
    }
  }

  return JNIHandles::make_local(THREAD, result);
}

// public Object ConstantPool_lookupConstant(HotSpotResolvedJavaType type, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_ConstantPool_1lookupConstant(JNIEnv *env, jobject, jobject type, jint index) {
  TRACE_graal_3("CompilerToVM::ConstantPool_lookupConstant");
  VM_ENTRY_MARK;

  constantPoolOop cp = instanceKlass::cast(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(type)))->constants();

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
  } else if (tag.is_string() || tag.is_unresolved_string()) {
    oop string = NULL;
    if (cp->is_pseudo_string_at(index)) {
      string = cp->pseudo_string_at(index);
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
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

// public JavaMethod ConstantPool_lookupMethod(long vmId, int cpi, byte byteCode);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_ConstantPool_1lookupMethod(JNIEnv *env, jobject, jobject type, jint index, jbyte byteCode) {
  TRACE_graal_3("CompilerToVM::ConstantPool_lookupMethod");
  VM_ENTRY_MARK;
  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = instanceKlass::cast(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(type)))->constants();
  instanceKlassHandle pool_holder(cp->pool_holder());

  Bytecodes::Code bc = (Bytecodes::Code) (((int) byteCode) & 0xFF);
  methodHandle method = GraalEnv::get_method_by_index(cp, index, bc, pool_holder);
  if (!method.is_null()) {
    Handle ret = GraalCompiler::createHotSpotResolvedJavaMethod(method, CHECK_NULL);
    return JNIHandles::make_local(THREAD, ret());
  } else {
    // Get the method's name and signature.
    Handle name = VmIds::toString<Handle>(cp->name_ref_at(index), CHECK_NULL);
    Handle signature  = VmIds::toString<Handle>(cp->signature_ref_at(index), CHECK_NULL);
    int holder_index = cp->klass_ref_index_at(index);
    Handle type = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
    return JNIHandles::make_local(THREAD, VMToCompiler::createJavaMethod(name, signature, type, THREAD));
  }
}

// public JavaType ConstantPool_lookupType(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_ConstantPool_1lookupType(JNIEnv *env, jobject, jobject type, jint index) {
  TRACE_graal_3("CompilerToVM::ConstantPool_lookupType");
  VM_ENTRY_MARK;

  constantPoolOop cp = instanceKlass::cast(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(type)))->constants();
  Handle result = GraalCompiler::get_JavaType(cp, index, cp->pool_holder(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, result());
}

// public void ConstantPool_loadReferencedType(long vmId, int cpi);
JNIEXPORT void JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_ConstantPool_1loadReferencedType(JNIEnv *env, jobject, jobject type, jint index, jbyte op) {
  TRACE_graal_3("CompilerToVM::ConstantPool_loadReferencedType");
  VM_ENTRY_MARK;
  
  constantPoolOop cp = instanceKlass::cast(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(type)))->constants();
  int byteCode = (op & 0xFF);
  if (byteCode != Bytecodes::_checkcast && byteCode != Bytecodes::_instanceof && byteCode != Bytecodes::_new && byteCode != Bytecodes::_anewarray
      && byteCode != Bytecodes::_multianewarray && byteCode != Bytecodes::_ldc && byteCode != Bytecodes::_ldc_w && byteCode != Bytecodes::_ldc2_w)
  {
    index = cp->remap_instruction_operand_from_cache(GraalCompiler::to_cp_index_u2(index));
  }
  constantTag tag = cp->tag_at(index);
  if (tag.is_field_or_method()) {
    index = cp->uncached_klass_ref_index_at(index);
    tag = cp->tag_at(index);
  }

  if (tag.is_unresolved_klass() || tag.is_klass()) {
    klassOop klass = cp->klass_at(index, CHECK);
    if (klass->klass_part()->oop_is_instance()) {
      instanceKlass::cast(klass)->initialize(CHECK);
    }
  }
}

// public JavaField ConstantPool_lookupField(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_ConstantPool_1lookupField(JNIEnv *env, jobject, jobject constantPoolHolder, jint index, jbyte byteCode) {
  TRACE_graal_3("CompilerToVM::ConstantPool_lookupField");
  VM_ENTRY_MARK;
  ResourceMark rm;

  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = instanceKlass::cast(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(constantPoolHolder)))->constants();

  int nt_index = cp->name_and_type_ref_index_at(index);
  int sig_index = cp->signature_ref_index_at(nt_index);
  Symbol* signature = cp->symbol_at(sig_index);
  int name_index = cp->name_ref_index_at(nt_index);
  Symbol* name = cp->symbol_at(name_index);
  int holder_index = cp->klass_ref_index_at(index);
  Handle holder = GraalCompiler::get_JavaType(cp, holder_index, cp->pool_holder(), CHECK_NULL);
  instanceKlassHandle holder_klass;
  
  Bytecodes::Code code = (Bytecodes::Code)(((int) byteCode) & 0xFF);
  int offset = -1;
  AccessFlags flags;
  BasicType basic_type;
  if (holder->klass() == SystemDictionary::HotSpotResolvedJavaType_klass()) {
    FieldAccessInfo result;
    LinkResolver::resolve_field(result, cp, index,
                                Bytecodes::java_code(code),
                                true, false, Thread::current());
    if (HAS_PENDING_EXCEPTION) {
      CLEAR_PENDING_EXCEPTION;
    } else {
      offset = result.field_offset();
      flags = result.access_flags();
      holder_klass = result.klass()->as_klassOop();
      basic_type = result.field_type();
      holder = GraalCompiler::get_JavaType(holder_klass, CHECK_NULL);
    }
  }
  
  Handle type = GraalCompiler::get_JavaTypeFromSignature(cp, sig_index, cp->pool_holder(), CHECK_NULL);
  Handle field_handle = GraalCompiler::get_JavaField(offset, flags.as_int(), name, holder, type, code, THREAD);

  return JNIHandles::make_local(THREAD, field_handle());
}

// public JavaMethod JavaType_resolveMethodImpl(HotSpotResolvedJavaType klass, String name, String signature);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_3resolveMethodImpl(JNIEnv *, jobject, jobject resolved_type, jstring name, jstring signature) {
  TRACE_graal_3("CompilerToVM::JavaType_resolveMethodImpl");
  VM_ENTRY_MARK;

  assert(JNIHandles::resolve(resolved_type) != NULL, "");
  klassOop klass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(resolved_type));
  Symbol* name_symbol = VmIds::toSymbol(name);
  Symbol* signature_symbol = VmIds::toSymbol(signature);
  methodHandle method = klass->klass_part()->lookup_method(name_symbol, signature_symbol);
  if (method == NULL) {
    if (TraceGraal >= 3) {
      ResourceMark rm;
      tty->print_cr("Could not resolve method %s %s on klass %s", name_symbol->as_C_string(), signature_symbol->as_C_string(), klass->klass_part()->name()->as_C_string());
    }
    return NULL;
  }
  Handle ret = GraalCompiler::createHotSpotResolvedJavaMethod(method, CHECK_NULL);
  return JNIHandles::make_local(THREAD, ret());
}

// public boolean JavaType_isSubtypeOf(HotSpotResolvedJavaType klass, JavaType other);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_2isSubtypeOf(JNIEnv *, jobject, jobject klass, jobject jother) {
  TRACE_graal_3("CompilerToVM::JavaType_isSubtypeOf");
  VM_ENTRY_MARK;
  
  oop other = JNIHandles::resolve(jother);
  assert(other->is_a(HotSpotResolvedJavaType::klass()), "resolved hotspot type expected");
  assert(JNIHandles::resolve(klass) != NULL, "");
  klassOop thisKlass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass));
  klassOop otherKlass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(other));
  if (thisKlass->klass_part()->oop_is_instance_slow()) {
    return instanceKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else if (thisKlass->klass_part()->oop_is_array()) {
    return arrayKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else {
    fatal("unexpected class type");
    return false;
  }
}

// public JavaType JavaType_leastCommonAncestor(HotSpotResolvedJavaType thisType, HotSpotResolvedJavaType otherType);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_2leastCommonAncestor(JNIEnv *, jobject, jobject this_type, jobject other_type) {
  TRACE_graal_3("CompilerToVM::JavaType_leastCommonAncestor");
  VM_ENTRY_MARK;

  Klass* this_klass  = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(this_type))->klass_part();
  Klass* other_klass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(other_type))->klass_part();
  Klass* lca         = this_klass->LCA(other_klass);

  return JNIHandles::make_local(GraalCompiler::get_JavaType(lca, THREAD)());
}

// public JavaType JavaType_componentType(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1componentType(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("CompilerToVM::JavaType_componentType");
  VM_ENTRY_MARK;
  KlassHandle array_klass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass));
  if(array_klass->oop_is_typeArray()) {
    BasicType t = typeArrayKlass::cast(array_klass())->element_type();
    oop primitive_type = VMToCompiler::createPrimitiveJavaType((int) t, CHECK_NULL);
    return JNIHandles::make_local(primitive_type);
  }
  assert(array_klass->oop_is_objArray(), "just checking");
  klassOop element_type = objArrayKlass::cast(array_klass())->element_klass();
  assert(JNIHandles::resolve(klass) != NULL, "");
  return JNIHandles::make_local(GraalCompiler::get_JavaType(element_type, THREAD)());
}

// public JavaType JavaType_superType(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1superType(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("CompilerToVM::JavaType_superType");
  VM_ENTRY_MARK;
  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass)));
  klassOop k;

  if (klass_handle->oop_is_array()) {
    k = SystemDictionary::Object_klass();
  } else {
    guarantee(klass_handle->oop_is_instance(), "must be instance klass");  
    k = klass_handle->super();
  }

  if (k != NULL) {
    return JNIHandles::make_local(GraalCompiler::get_JavaType(k, THREAD)());
  } else {
    return NULL;
  }
}

// public JavaType JavaType_uniqueConcreteSubtype(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1uniqueConcreteSubtype(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("CompilerToVM::JavaType_uniqueConcreteSubtype");
  VM_ENTRY_MARK;
  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass)));
  Klass *up_cast = klass_handle->up_cast_abstract();
  if (!up_cast->is_interface() && up_cast->subklass() == NULL) {
    return JNIHandles::make_local(GraalCompiler::get_JavaType(up_cast, THREAD)());
  }
  return NULL;
}

// public bool JavaType_isInitialized(HotSpotResolvedType klass);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1isInitialized(JNIEnv *, jobject, jobject hotspot_klass) {
  TRACE_graal_3("CompilerToVM::JavaType_isInitialized");
  klassOop klass = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(hotspot_klass));
  assert(klass != NULL, "method must not be called for primitive types");
  return instanceKlass::cast(klass)->is_initialized();
}

// public JavaType JavaType_arrayOf(HotSpotResolvedJavaType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1arrayOf(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("CompilerToVM::JavaType_arrayOf");
  VM_ENTRY_MARK;

  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass)));
  KlassHandle arr = klass_handle->array_klass(THREAD);
  Handle name = VmIds::toString<Handle>(arr->name(), CHECK_NULL);
  assert(arr->oop_is_array(), "");
  return JNIHandles::make_local(THREAD, GraalCompiler::createHotSpotResolvedJavaType(arr, name, THREAD)());
}

// public ResolvedJavaField[] JavaType_fields(HotSpotResolvedJavaType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaType_1fields(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("CompilerToVM::JavaType_fields");
  VM_ENTRY_MARK;
  ResourceMark rm;

  instanceKlassHandle k = java_lang_Class::as_klassOop(HotSpotResolvedJavaType::javaMirror(klass));
  class MyFieldClosure : public FieldClosure {
   public:
    instanceKlassHandle _holder;
    Handle _resolved_type_holder;
    GrowableArray<Handle> _field_array;

    MyFieldClosure(instanceKlassHandle& holder, Handle resolved_type_holder) : _holder(holder), _resolved_type_holder(resolved_type_holder) { }
    
    virtual void do_field(fieldDescriptor* fd) {
      if (!Thread::current()->has_pending_exception()) {
        if (fd->field_holder() == _holder()) {
          Handle type = GraalCompiler::get_JavaTypeFromSignature(fd->constants(), fd->signature_index(), fd->field_holder(), Thread::current());
          Handle field = VMToCompiler::createJavaField(_resolved_type_holder, VmIds::toString<Handle>(fd->name(), Thread::current()), type, fd->offset(), fd->access_flags().as_int(), Thread::current());
          _field_array.append(field());
        }
      }
    }
  };
  MyFieldClosure closure(k, JNIHandles::resolve(klass));
  k->do_nonstatic_fields(&closure);
  objArrayHandle field_array = oopFactory::new_objArray(SystemDictionary::ResolvedJavaField_klass(), closure._field_array.length(), CHECK_NULL);
  for (int i=0; i<closure._field_array.length(); ++i) {
    field_array->obj_at_put(i, closure._field_array.at(i)());
  }
  return JNIHandles::make_local(field_array());
}

// public JavaType getPrimitiveArrayType(Kind kind);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_getPrimitiveArrayType(JNIEnv *env, jobject, jobject kind) {
  TRACE_graal_3("CompilerToVM::getPrimitiveArrayType");
  VM_ENTRY_MARK;
  BasicType type = GraalCompiler::kindToBasicType(Kind::typeChar(kind));
  assert(type != T_OBJECT, "primitive type expecteds");
  Handle result = GraalCompiler::get_JavaType(Universe::typeArrayKlassObj(type), CHECK_NULL);
  return JNIHandles::make_local(THREAD, result());
}

// public long getMaxCallTargetOffset(RuntimeCall rtcall);
JNIEXPORT jlong JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_getMaxCallTargetOffset(JNIEnv *env, jobject, jobject rtcall) {
  TRACE_graal_3("CompilerToVM::getMaxCallTargetOffset");
  VM_ENTRY_MARK;
  oop call = JNIHandles::resolve(rtcall);
  address target_addr = CodeInstaller::runtime_call_target_address(call);
  if (target_addr != 0x0) {
    int64_t off_low = (int64_t)target_addr - ((int64_t)CodeCache::low_bound() + sizeof(int));
    int64_t off_high = (int64_t)target_addr - ((int64_t)CodeCache::high_bound() + sizeof(int));
    return MAX2(ABS(off_low), ABS(off_high));
  }
  return -1;
}

// public JavaType getType(Class<?> javaClass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_getType(JNIEnv *env, jobject, jobject javaClass) {
  TRACE_graal_3("CompilerToVM::getType");
  VM_ENTRY_MARK;
  oop javaClassOop = JNIHandles::resolve(javaClass);
  if (javaClassOop == NULL) {
    fatal("argument to CompilerToVM.getType must not be NULL");
    return NULL;
  } else if (java_lang_Class::is_primitive(javaClassOop)) {
    BasicType basicType = java_lang_Class::primitive_type(javaClassOop);
    return JNIHandles::make_local(THREAD, VMToCompiler::createPrimitiveJavaType((int) basicType, THREAD));
  } else {
    KlassHandle klass = java_lang_Class::as_klassOop(javaClassOop);
    Handle name = java_lang_String::create_from_symbol(klass->name(), CHECK_NULL);

    Handle type = GraalCompiler::createHotSpotResolvedJavaType(klass, name, CHECK_NULL);
    return JNIHandles::make_local(THREAD, type());
  }
}


// helpers used to set fields in the HotSpotVMConfig object
jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig) {
  jfieldID id = env->GetFieldID(env->GetObjectClass(obj), name, sig);
  if (id == NULL) {
    fatal(err_msg("field not found: %s (%s)", name, sig));
  }
  return id;
}

void set_boolean(JNIEnv* env, jobject obj, const char* name, bool value) { env->SetBooleanField(obj, getFieldID(env, obj, name, "Z"), value); }
void set_int(JNIEnv* env, jobject obj, const char* name, int value) { env->SetIntField(obj, getFieldID(env, obj, name, "I"), value); }
void set_long(JNIEnv* env, jobject obj, const char* name, jlong value) { env->SetLongField(obj, getFieldID(env, obj, name, "J"), value); }
void set_object(JNIEnv* env, jobject obj, const char* name, jobject value) { env->SetObjectField(obj, getFieldID(env, obj, name, "Ljava/lang/Object;"), value); }
void set_int_array(JNIEnv* env, jobject obj, const char* name, jarray value) { env->SetObjectField(obj, getFieldID(env, obj, name, "[I"), value); }

jboolean get_boolean(JNIEnv* env, jobject obj, const char* name) { return env->GetBooleanField(obj, getFieldID(env, obj, name, "Z")); }
jint get_int(JNIEnv* env, jobject obj, const char* name) { return env->GetIntField(obj, getFieldID(env, obj, name, "I")); }
jlong get_long(JNIEnv* env, jobject obj, const char* name) { return env->GetLongField(obj, getFieldID(env, obj, name, "J")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name) { return env->GetObjectField(obj, getFieldID(env, obj, name, "Ljava/lang/Object;")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name, const char* sig) { return env->GetObjectField(obj, getFieldID(env, obj, name, sig)); }


BasicType basicTypes[] = { T_BOOLEAN, T_BYTE, T_SHORT, T_CHAR, T_INT, T_FLOAT, T_LONG, T_DOUBLE, T_OBJECT };
int basicTypeCount = sizeof(basicTypes) / sizeof(BasicType);

// public void initializeConfiguration(HotSpotVMConfig config);
JNIEXPORT void JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_initializeConfiguration(JNIEnv *env, jobject, jobject config) {
#ifdef _WIN64
  set_boolean(env, config, "windowsOs", true);
#else
  set_boolean(env, config, "windowsOs", false);
#endif
  set_boolean(env, config, "verifyOops", VerifyOops);
  set_boolean(env, config, "useFastLocking", UseFastLocking);
  set_boolean(env, config, "useFastNewObjectArray", UseFastNewObjectArray);
  set_boolean(env, config, "useFastNewTypeArray", UseFastNewTypeArray);
  set_boolean(env, config, "useTLAB", UseTLAB);
  set_int(env, config, "codeEntryAlignment", CodeEntryAlignment);
  set_int(env, config, "vmPageSize", os::vm_page_size());
  set_int(env, config, "stackShadowPages", StackShadowPages);
  set_int(env, config, "hubOffset", oopDesc::klass_offset_in_bytes());
  set_int(env, config, "markOffset", oopDesc::mark_offset_in_bytes());
  set_int(env, config, "superCheckOffsetOffset", in_bytes(Klass::super_check_offset_offset()));
  set_int(env, config, "secondarySuperCacheOffset", in_bytes(Klass::secondary_super_cache_offset()));
  set_int(env, config, "secondarySupersOffset", in_bytes(Klass::secondary_supers_offset()));
  set_int(env, config, "arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());
  set_int(env, config, "klassStateOffset", in_bytes(instanceKlass::init_state_offset()));
  set_int(env, config, "klassStateFullyInitialized", (int)instanceKlass::fully_initialized);
  set_int(env, config, "threadTlabTopOffset", in_bytes(JavaThread::tlab_top_offset()));
  set_int(env, config, "threadTlabEndOffset", in_bytes(JavaThread::tlab_end_offset()));
  set_int(env, config, "threadObjectOffset", in_bytes(JavaThread::threadObj_offset()));
  set_int(env, config, "instanceHeaderPrototypeOffset", in_bytes(Klass::prototype_header_offset()));
  set_int(env, config, "threadExceptionOopOffset", in_bytes(JavaThread::exception_oop_offset()));
  set_int(env, config, "threadExceptionPcOffset", in_bytes(JavaThread::exception_pc_offset()));
  set_int(env, config, "threadMultiNewArrayStorageOffset", in_bytes(JavaThread::graal_multinewarray_storage_offset()));
  set_long(env, config, "safepointPollingAddress", (jlong)(os::get_polling_page() + (SafepointPollOffset % os::vm_page_size())));
  set_boolean(env, config, "isPollingPageFar", Assembler::is_polling_page_far());
  set_int(env, config, "classMirrorOffset", in_bytes(Klass::java_mirror_offset()));
  set_int(env, config, "runtimeCallStackSize", (jint)frame::arg_reg_save_area_bytes);
  set_int(env, config, "klassModifierFlagsOffset", in_bytes(Klass::modifier_flags_offset()));
  set_int(env, config, "klassOopOffset", java_lang_Class::klass_offset_in_bytes());
  set_int(env, config, "graalMirrorKlassOffset", in_bytes(Klass::graal_mirror_offset()));
  set_int(env, config, "nmethodEntryOffset", nmethod::verified_entry_point_offset());
  set_int(env, config, "methodCompiledEntryOffset", in_bytes(methodOopDesc::from_compiled_offset()));
  
  set_int(env, config, "methodDataOopDataOffset", in_bytes(methodDataOopDesc::data_offset()));
  set_int(env, config, "methodDataOopTrapHistoryOffset", in_bytes(methodDataOopDesc::trap_history_offset()));
  set_int(env, config, "dataLayoutHeaderSize", DataLayout::header_size_in_bytes());
  set_int(env, config, "dataLayoutTagOffset", in_bytes(DataLayout::tag_offset()));
  set_int(env, config, "dataLayoutFlagsOffset", in_bytes(DataLayout::flags_offset()));
  set_int(env, config, "dataLayoutBCIOffset", in_bytes(DataLayout::bci_offset()));
  set_int(env, config, "dataLayoutCellsOffset", in_bytes(DataLayout::cell_offset(0)));
  set_int(env, config, "dataLayoutCellSize", DataLayout::cell_size);
  set_int(env, config, "bciProfileWidth", BciProfileWidth);
  set_int(env, config, "typeProfileWidth", TypeProfileWidth);

  // We use the fast path stub so that we get TLAB refills whenever possible instead of
  // unconditionally allocating directly from the heap (which the slow path does).
  // The stub must also do initialization when the compiled check fails.
  Runtime1::StubID newInstanceStub = Runtime1::fast_new_instance_init_check_id;

  set_long(env, config, "debugStub", VmIds::addStub((address)warning));
  set_long(env, config, "instanceofStub", VmIds::addStub(Runtime1::entry_for(Runtime1::slow_subtype_check_id)));
  set_long(env, config, "newInstanceStub", VmIds::addStub(Runtime1::entry_for(newInstanceStub)));
  set_long(env, config, "newTypeArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_type_array_id)));
  set_long(env, config, "newObjectArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_object_array_id)));
  set_long(env, config, "newMultiArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_multi_array_id)));
  set_long(env, config, "inlineCacheMissStub", VmIds::addStub(SharedRuntime::get_ic_miss_stub()));
  set_long(env, config, "handleExceptionStub", VmIds::addStub(Runtime1::entry_for(Runtime1::handle_exception_nofpu_id)));
  set_long(env, config, "handleDeoptStub", VmIds::addStub(SharedRuntime::deopt_blob()->unpack()));
  set_long(env, config, "monitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorenter_id)));
  set_long(env, config, "monitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorexit_id)));
  set_long(env, config, "fastMonitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_monitorenter_id)));
  set_long(env, config, "fastMonitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_monitorexit_id)));
  set_long(env, config, "verifyOopStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_verify_oop_id)));


  BarrierSet* bs = Universe::heap()->barrier_set();
  switch (bs->kind()) {
    case BarrierSet::CardTableModRef:
    case BarrierSet::CardTableExtension: {
      jlong base = (jlong)((CardTableModRefBS*)bs)->byte_map_base;
      assert(base != 0, "unexpected byte_map_base");
      set_long(env, config, "cardtableStartAddress", base);
      set_int(env, config, "cardtableShift", CardTableModRefBS::card_shift);
      break;
    }
    case BarrierSet::ModRef:
    case BarrierSet::Other:
      set_long(env, config, "cardtableStartAddress", 0);
      set_int(env, config, "cardtableShift", 0);
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

  set_int(env, config, "arrayClassElementOffset", in_bytes(objArrayKlass::element_klass_offset()));
}

// public HotSpotCompiledMethod installMethod(HotSpotCompilationResult comp, boolean installCode);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_installMethod(JNIEnv *jniEnv, jobject, jobject compResult, jboolean install_code, jobject info) {
  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;
  Handle compResultHandle = JNIHandles::resolve(compResult);
  nmethod* nm = NULL;
  Arena arena;
  ciEnv env(&arena);
  CodeInstaller installer(compResultHandle, nm, install_code != 0);

  if (info != NULL) {
    arrayOop codeCopy = oopFactory::new_byteArray(nm->code_size(), CHECK_0);
    memcpy(codeCopy->base(T_BYTE), nm->code_begin(), nm->code_size());
    HotSpotCodeInfo::set_code(info, codeCopy);
    HotSpotCodeInfo::set_start(info, (jlong) nm->code_begin());
  }

  // if install_code is true then we installed the code into the given method, no need to return an InstalledCode
  if (!install_code && nm != NULL) {
    instanceKlass::cast(HotSpotCompiledMethod::klass())->initialize(CHECK_NULL);
    Handle obj = instanceKlass::cast(HotSpotCompiledMethod::klass())->allocate_permanent_instance(CHECK_NULL);
    assert(obj() != NULL, "must succeed in allocating instance");
    HotSpotCompiledMethod::set_nmethod(obj, (jlong) nm);
    HotSpotCompiledMethod::set_method(obj, HotSpotCompilationResult::method(compResult));
    nm->set_graal_compiled_method(obj());
    return JNIHandles::make_local(obj());
  } else {
    return NULL;
  }
}

// public String disassembleNative(byte[] code, long address);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_disassembleNative(JNIEnv *jniEnv, jobject, jbyteArray code, jlong start_address) {
  TRACE_graal_3("CompilerToVM::disassembleNative");
  VM_ENTRY_MARK;
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
}

// public StackTraceElement JavaMethod_toStackTraceElement(HotSpotResolvedJavaMethod method, int bci);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_1toStackTraceElement(JNIEnv *env, jobject, jobject hotspot_method, int bci) {
  TRACE_graal_3("CompilerToVM::JavaMethod_toStackTraceElement");

  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;

  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  oop element = java_lang_StackTraceElement::create(method, bci, CHECK_NULL);
  return JNIHandles::make_local(element);
}

// public Object executeCompiledMethodVarargs(HotSpotCompiledMethod method, Object... args);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_executeCompiledMethodVarargs(JNIEnv *env, jobject, jobject method, jobject args) {
  TRACE_graal_3("CompilerToVM::executeCompiledMethod");

  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;

  assert(method != NULL, "just checking");
  methodHandle mh = getMethodFromHotSpotMethod(HotSpotCompiledMethod::method(method));
  Symbol* signature = mh->signature();
  JavaCallArguments jca;

  JavaArgumentUnboxer jap(signature, &jca, (arrayOop) JNIHandles::resolve(args), mh->is_static());
  JavaValue result(jap.get_ret_type());

  nmethod* nm = (nmethod*) HotSpotCompiledMethod::nmethod(method);
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
}

// public Object executeCompiledMethod(HotSpotCompiledMethod method, Object arg1, Object arg2, Object arg3);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_executeCompiledMethod(JNIEnv *env, jobject, jobject method, jobject arg1, jobject arg2, jobject arg3) {
  TRACE_graal_3("CompilerToVM::executeCompiledMethod");

  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;

  methodHandle actualMethod = getMethodFromHotSpotMethod(HotSpotCompiledMethod::method(method));
  assert(method != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(JNIHandles::resolve(arg1));
  args.push_oop(JNIHandles::resolve(arg2));
  args.push_oop(JNIHandles::resolve(arg3));

  nmethod* nm = (nmethod*) HotSpotCompiledMethod::nmethod(method);
  if (nm == NULL || !nm->is_alive()) {
    THROW_0(vmSymbols::MethodInvalidatedException());
  }

  JavaCalls::call(&result, actualMethod, nm, &args, CHECK_NULL);

  return JNIHandles::make_local((oop) result.get_jobject());
}

// public native int JavaMethod_vtableEntryOffset(HotSpotResolvedJavaMethod method);
JNIEXPORT jint JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_JavaMethod_vtableEntryOffset(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("CompilerToVM::JavaMethod_vtableEntryOffset");

  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  assert(!instanceKlass::cast(method->method_holder())->is_interface(), "vtableEntryOffset cannot be called for interface methods");

  // get entry offset in words
  int vtable_entry_offset = instanceKlass::vtable_start_offset() + method->vtable_index() * vtableEntry::size();
  // convert to bytes
  vtable_entry_offset = vtable_entry_offset * wordSize + vtableEntry::method_offset_in_bytes();

  return vtable_entry_offset;
}

// public native long[] getDeoptedLeafGraphIds();
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_getDeoptedLeafGraphIds(JNIEnv *, jobject) {
  TRACE_graal_3("CompilerToVM::getDeoptedLeafGraphIds");

  VM_ENTRY_MARK;

  // the contract for this method is as follows:
  // returning null: no deopted leaf graphs
  // returning array (size > 0): the ids of the deopted leaf graphs
  // returning array (size == 0): there was an overflow, the compiler needs to clear its cache completely

  oop array = GraalCompiler::instance()->dump_deopted_leaf_graphs(CHECK_NULL);
  return JNIHandles::make_local(array);
}

JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_decodePC(JNIEnv *, jobject, jlong pc) {
  TRACE_graal_3("CompilerToVM::decodePC");

  VM_ENTRY_MARK;

  stringStream(st);
  CodeBlob* blob = CodeCache::find_blob_unsafe((void*) pc);
  if (blob == NULL) {
    st.print("[unidentified pc]");
  } else {
    st.print(blob->name());

    nmethod* nm = blob->as_nmethod_or_null();
    if (nm != NULL && nm->method() != NULL) {
      st.print(" %s.", nm->method()->method_holder()->klass_part()->external_name());
      nm->method()->name()->print_symbol_on(&st);
      st.print("  @ %d", pc - (jlong) nm->entry_point());
    }
  }
  Handle result = java_lang_String::create_from_platform_dependent_str(st.as_string(), CHECK_NULL);
  return JNIHandles::make_local(result());

}


#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &(Java_com_oracle_graal_hotspot_bridge_CompilerToVMImpl_##f))

#define PROXY           "J"
#define TYPE            "Lcom/oracle/graal/api/meta/JavaType;"
#define RESOLVED_TYPE   "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaType;"
#define METHOD          "Lcom/oracle/graal/api/meta/JavaMethod;"
#define RESOLVED_METHOD "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaMethod;"
#define REFLECT_METHOD  "Ljava/lang/reflect/Method;"
#define SIGNATURE       "Lcom/oracle/graal/api/meta/Signature;"
#define FIELD           "Lcom/oracle/graal/api/meta/JavaField;"
#define RESOLVED_FIELD  "Lcom/oracle/graal/api/meta/ResolvedJavaField;"
#define CONSTANT_POOL   "Lcom/oracle/graal/api/meta/ConstantPool;"
#define EXCEPTION_HANDLERS "[Lcom/oracle/graal/api/meta/ExceptionHandler;"
#define HS_COMP_RESULT  "Lcom/oracle/graal/hotspot/HotSpotCompilationResult;"
#define CONFIG          "Lcom/oracle/graal/hotspot/HotSpotVMConfig;"
#define HS_METHOD       "Lcom/oracle/graal/hotspot/meta/HotSpotMethod;"
#define HS_COMP_METHOD  "Lcom/oracle/graal/hotspot/meta/HotSpotCompiledMethod;"
#define HS_CODE_INFO    "Lcom/oracle/graal/hotspot/meta/HotSpotCodeInfo;"
#define METHOD_DATA     "Lcom/oracle/graal/hotspot/meta/HotSpotMethodData;"
#define CONSTANT        "Lcom/oracle/graal/api/meta/Constant;"
#define KIND            "Lcom/oracle/graal/api/meta/Kind;"
#define RUNTIME_CALL    "Lcom/oracle/graal/api/code/RuntimeCall;"
#define STRING          "Ljava/lang/String;"
#define OBJECT          "Ljava/lang/Object;"
#define CLASS           "Ljava/lang/Class;"
#define STACK_TRACE_ELEMENT "Ljava/lang/StackTraceElement;"

JNINativeMethod CompilerToVM_methods[] = {
  {CC"JavaMethod_code",                     CC"("RESOLVED_METHOD")[B",                            FN_PTR(JavaMethod_1code)},
  {CC"JavaMethod_signature",                CC"("RESOLVED_METHOD")"STRING,                        FN_PTR(JavaMethod_1signature)},
  {CC"JavaMethod_exceptionHandlers",        CC"("RESOLVED_METHOD")"EXCEPTION_HANDLERS,            FN_PTR(JavaMethod_1exceptionHandlers)},
  {CC"JavaMethod_hasBalancedMonitors",      CC"("RESOLVED_METHOD")Z",                             FN_PTR(JavaMethod_1hasBalancedMonitors)},
  {CC"JavaMethod_uniqueConcreteMethod",     CC"("RESOLVED_METHOD")"METHOD,                        FN_PTR(JavaMethod_1uniqueConcreteMethod)},
  {CC"getJavaMethod",                       CC"("REFLECT_METHOD")"METHOD,                         FN_PTR(getJavaMethod)},
  {CC"JavaMethod_methodData",               CC"("RESOLVED_METHOD")"METHOD_DATA,                   FN_PTR(JavaMethod_1methodData)},
  {CC"JavaMethod_invocationCount",          CC"("RESOLVED_METHOD")I",                             FN_PTR(JavaMethod_1invocationCount)},
  {CC"JavaMethod_hasCompiledCode",          CC"("RESOLVED_METHOD")Z",                             FN_PTR(JavaMethod_1hasCompiledCode)},
  {CC"JavaMethod_getCompiledCodeSize",      CC"("RESOLVED_METHOD")I",                             FN_PTR(JavaMethod_1getCompiledCodeSize)},
  {CC"Signature_lookupType",                CC"("STRING RESOLVED_TYPE"Z)"TYPE,                    FN_PTR(Signature_1lookupType)},
  {CC"ConstantPool_lookupConstant",         CC"("RESOLVED_TYPE"I)"OBJECT,                         FN_PTR(ConstantPool_1lookupConstant)},
  {CC"ConstantPool_lookupMethod",           CC"("RESOLVED_TYPE"IB)"METHOD,                        FN_PTR(ConstantPool_1lookupMethod)},
  {CC"ConstantPool_lookupType",             CC"("RESOLVED_TYPE"I)"TYPE,                           FN_PTR(ConstantPool_1lookupType)},
  {CC"ConstantPool_loadReferencedType",     CC"("RESOLVED_TYPE"IB)V",                             FN_PTR(ConstantPool_1loadReferencedType)},
  {CC"ConstantPool_lookupField",            CC"("RESOLVED_TYPE"IB)"FIELD,                         FN_PTR(ConstantPool_1lookupField)},
  {CC"JavaType_resolveMethodImpl",          CC"("RESOLVED_TYPE STRING STRING")"METHOD,            FN_PTR(JavaType_3resolveMethodImpl)},
  {CC"JavaType_isSubtypeOf",                CC"("RESOLVED_TYPE TYPE")Z",                          FN_PTR(JavaType_2isSubtypeOf)},
  {CC"JavaType_leastCommonAncestor",        CC"("RESOLVED_TYPE RESOLVED_TYPE")"TYPE,              FN_PTR(JavaType_2leastCommonAncestor)},
  {CC"JavaType_componentType",              CC"("RESOLVED_TYPE")"TYPE,                            FN_PTR(JavaType_1componentType)},
  {CC"JavaType_uniqueConcreteSubtype",      CC"("RESOLVED_TYPE")"TYPE,                            FN_PTR(JavaType_1uniqueConcreteSubtype)},
  {CC"JavaType_superType",                  CC"("RESOLVED_TYPE")"TYPE,                            FN_PTR(JavaType_1superType)},
  {CC"JavaType_arrayOf",                    CC"("RESOLVED_TYPE")"TYPE,                            FN_PTR(JavaType_1arrayOf)},
  {CC"JavaType_fields",                     CC"("RESOLVED_TYPE")["RESOLVED_FIELD,                 FN_PTR(JavaType_1fields)},
  {CC"JavaType_isInitialized",              CC"("RESOLVED_TYPE")Z",                               FN_PTR(JavaType_1isInitialized)},
  {CC"getPrimitiveArrayType",               CC"("KIND")"TYPE,                                     FN_PTR(getPrimitiveArrayType)},
  {CC"getMaxCallTargetOffset",              CC"("RUNTIME_CALL")J",                                FN_PTR(getMaxCallTargetOffset)},
  {CC"getType",                             CC"("CLASS")"TYPE,                                    FN_PTR(getType)},
  {CC"initializeConfiguration",             CC"("CONFIG")V",                                      FN_PTR(initializeConfiguration)},
  {CC"installMethod",                       CC"("HS_COMP_RESULT"Z"HS_CODE_INFO")"HS_COMP_METHOD,  FN_PTR(installMethod)},
  {CC"disassembleNative",                   CC"([BJ)"STRING,                                      FN_PTR(disassembleNative)},
  {CC"JavaMethod_toStackTraceElement",      CC"("RESOLVED_METHOD"I)"STACK_TRACE_ELEMENT,          FN_PTR(JavaMethod_1toStackTraceElement)},
  {CC"executeCompiledMethod",               CC"("HS_COMP_METHOD OBJECT OBJECT OBJECT")"OBJECT,    FN_PTR(executeCompiledMethod)},
  {CC"executeCompiledMethodVarargs",        CC"("HS_COMP_METHOD "["OBJECT")"OBJECT,               FN_PTR(executeCompiledMethodVarargs)},
  {CC"JavaMethod_vtableEntryOffset",        CC"("RESOLVED_METHOD")I",                             FN_PTR(JavaMethod_vtableEntryOffset)},
  {CC"getDeoptedLeafGraphIds",              CC"()[J",                                             FN_PTR(getDeoptedLeafGraphIds)},
  {CC"decodePC",                            CC"(J)"STRING,                                        FN_PTR(decodePC)},
};

int CompilerToVM_methods_count() {
  return sizeof(CompilerToVM_methods) / sizeof(JNINativeMethod);
}

