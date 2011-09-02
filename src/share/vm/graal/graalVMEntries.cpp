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
#include "c1/c1_Runtime1.hpp"
#include "ci/ciMethodData.hpp"
#include "graal/graalVMEntries.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalVMExits.hpp"
#include "graal/graalVmIds.hpp"
#include "memory/oopFactory.hpp"
#include "oops/generateOopMap.hpp"

methodOop getMethodFromHotSpotMethod(jobject hotspot_method) {
  return getMethodFromHotSpotMethod(JNIHandles::resolve(hotspot_method));
}

methodOop getMethodFromHotSpotMethod(oop hotspot_method) {
  oop reflected = HotSpotMethodResolved::javaMirror(hotspot_method);
  assert(reflected != NULL, "NULL not expected");
  return (methodOop)reflected;

  // (tw) Cannot use reflection code, because then the compiler can dead lock with the user application (test using -Xcomp).
  /*
  // method is a handle to a java.lang.reflect.Method object
  oop mirror     = NULL;
  int slot       = 0;

  if (reflected->klass() == SystemDictionary::reflect_Constructor_klass()) {
    mirror = java_lang_reflect_Constructor::clazz(reflected);
    slot   = java_lang_reflect_Constructor::slot(reflected);
  } else {
    assert(reflected->klass() == SystemDictionary::reflect_Method_klass(), "wrong type");
    mirror = java_lang_reflect_Method::clazz(reflected);
    slot   = java_lang_reflect_Method::slot(reflected);
  }
  klassOop k     = java_lang_Class::as_klassOop(mirror);

  // Make sure class is initialized before handing id's out to methods
//  assert(instanceKlass::cast(k)->is_initialized(), "only initialized classes expected");
  methodOop m = instanceKlass::cast(k)->method_with_idnum(slot);
  assert(m != NULL, "deleted method?");
  return m;*/
}

oop getReflectedMethod(methodOop method, TRAPS) {
  assert(method != NULL, "NULL not expected");

  if (!method->is_initializer() || method->is_static()) {
    return Reflection::new_method(method, true, true, CHECK_NULL);
  } else {
    return Reflection::new_constructor(method, CHECK_NULL);
  }
}

// public byte[] RiMethod_code(long vmId);
JNIEXPORT jbyteArray JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_code");
  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  int code_size = method->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *) method->code_base());
  return result;
}

// public String RiMethod_signature(long vmId);
JNIEXPORT jstring JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_signature");
  VM_ENTRY_MARK
  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  assert(method != NULL && method->signature() != NULL, "signature required");
  return VmIds::toString<jstring>(method->signature(), THREAD);
}

// public RiExceptionHandler[] RiMethod_exceptionHandlers(long vmId);
JNIEXPORT jobjectArray JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1exceptionHandlers(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_exceptionHandlers");
  VM_ENTRY_MARK
  methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
  typeArrayHandle handlers = method->exception_table();
  int handler_count = handlers.is_null() ? 0 : handlers->length() / 4;

  instanceKlass::cast(HotSpotExceptionHandler::klass())->initialize(CHECK_NULL);
  objArrayHandle array = oopFactory::new_objArray(SystemDictionary::RiExceptionHandler_klass(), handler_count, CHECK_NULL);

  for (int i = 0; i < handler_count; i++) {
    // exception handlers are stored as four integers: start bci, end bci, handler bci, catch class constant pool index
    int base = i * 4;
    Handle entry = instanceKlass::cast(HotSpotExceptionHandler::klass())->allocate_instance(CHECK_NULL);
    HotSpotExceptionHandler::set_startBci(entry, handlers->int_at(base + 0));
    HotSpotExceptionHandler::set_endBci(entry, handlers->int_at(base + 1));
    HotSpotExceptionHandler::set_handlerBci(entry, handlers->int_at(base + 2));
    int catch_class_index = handlers->int_at(base + 3);
    HotSpotExceptionHandler::set_catchClassIndex(entry, catch_class_index);

    if (catch_class_index == 0) {
      HotSpotExceptionHandler::set_catchClass(entry, NULL);
    } else {
      constantPoolOop cp = instanceKlass::cast(method->method_holder())->constants();
      ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(method->method_holder());
      bool is_accessible = false;
      ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, catch_class_index, is_accessible, loading_klass);
      oop catch_class = GraalCompiler::get_RiType(klass, method->method_holder(), CHECK_NULL);

      HotSpotExceptionHandler::set_catchClass(entry, catch_class);
    }
    array->obj_at_put(i, entry());
  }

  return (jobjectArray) JNIHandles::make_local(array());
}

// public boolean RiMethod_hasBalancedMonitors(long vmId);
JNIEXPORT jint JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1hasBalancedMonitors(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_hasBalancedMonitors");

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

// public RiMethod getRiMethod(java.lang.reflect.Method reflectionMethod);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_getRiMethod(JNIEnv *, jobject, jobject reflection_method_handle) {
  TRACE_graal_3("VMEntries::getRiMethod");
  VM_ENTRY_MARK;
  oop reflection_method = JNIHandles::resolve(reflection_method_handle);
  oop reflection_holder = java_lang_reflect_Method::clazz(reflection_method);
  int slot = java_lang_reflect_Method::slot(reflection_method);
  klassOop holder = java_lang_Class::as_klassOop(reflection_holder);
  methodOop method = instanceKlass::cast(holder)->method_with_idnum(slot);
  oop ret = GraalCompiler::createHotSpotMethodResolved(method, CHECK_NULL);
  return JNIHandles::make_local(THREAD, ret);
}

// public boolean RiMethod_uniqueConcreteMethod(long vmId);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1uniqueConcreteMethod(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_uniqueConcreteMethod");

  VM_ENTRY_MARK;
  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  klassOop holder = method->method_holder();
  if (holder->klass_part()->is_interface()) {
    // Cannot trust interfaces. Because of:
    // interface I { void foo(); }
    // class A { public void foo() {} }
    // class B extends A implements I { }
    // class C extends B { public void foo() { } }
    // class D extends B { }
    // Would lead to identify C.foo() as the unique concrete method for I.foo() without seeing A.foo().
    return false;
  }
  methodOop unique_concrete;
  {
    ResourceMark rm;
    MutexLocker locker(Compile_lock);
    unique_concrete = Dependencies::find_unique_concrete_method(holder, method);
  }
  if (unique_concrete == NULL) {
    return NULL;
  } else {
    oop method_resolved = GraalCompiler::createHotSpotMethodResolved(unique_concrete, CHECK_NULL);
    return JNIHandles::make_local(THREAD, method_resolved);
  }
}

// public native int RiMethod_invocationCount(long vmId);
JNIEXPORT jint JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1invocationCount(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_invocationCount");
  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  return method->invocation_count();
}

// public native int RiMethod_exceptionProbability(long vmId, int bci);
JNIEXPORT jint JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_2exceptionProbability(JNIEnv *, jobject, jobject hotspot_method, jint bci) {
  TRACE_graal_3("VMEntries::RiMethod_exceptionProbability");
  {
    VM_ENTRY_MARK;
    methodOop method = getMethodFromHotSpotMethod(hotspot_method);
    methodDataOop method_data = method->method_data();
    if (method_data == NULL || !method_data->is_mature()) {
      return -1;
    }
    ProfileData* data = method_data->bci_to_data(bci);
    if (data == NULL) {
      return 0;
    }
    uint trap = Deoptimization::trap_state_is_recompiled(data->trap_state())? 1: 0;
    if (trap > 0) {
      return 100;
    } else {
      return trap;
    }
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

// public native RiTypeProfile RiMethod_typeProfile(long vmId, int bci);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_2typeProfile(JNIEnv *, jobject, jobject hotspot_method, jint bci) {
  TRACE_graal_3("VMEntries::RiMethod_typeProfile");
  Handle obj;
  {
    VM_ENTRY_MARK;
    methodHandle method = getMethodFromHotSpotMethod(hotspot_method);
    if (strstr(method->name_and_sig_as_C_string(), "factor") != NULL) {
//      tty->print_cr("here");
    }
    if (bci == 123) {
//      tty->print_cr("here2");
    }
    methodDataHandle method_data = method->method_data();
    if (method_data == NULL || !method_data->is_mature()) {
      return NULL;
    }
    ProfileData* data = method_data->bci_to_data(bci);
    if (data != NULL && data->is_ReceiverTypeData()) {
      ReceiverTypeData* recv = data->as_ReceiverTypeData();
      // determine morphism
      int morphism = 0;
      uint total_count = 0;
      for (uint i = 0; i < recv->row_limit(); i++) {
        klassOop receiver = recv->receiver(i);
        if (receiver == NULL)  continue;
        morphism++;
        total_count += recv->receiver_count(i);
      }

        instanceKlass::cast(RiTypeProfile::klass())->initialize(CHECK_NULL);
        obj = instanceKlass::cast(RiTypeProfile::klass())->allocate_instance(CHECK_NULL);
        assert(obj() != NULL, "must succeed in allocating instance");

        int count = MAX2(total_count, recv->count());
        RiTypeProfile::set_count(obj, scale_count(method_data(), count));
        RiTypeProfile::set_morphism(obj, morphism);

      if (morphism > 0) {
        typeArrayHandle probabilities = oopFactory::new_typeArray(T_FLOAT, morphism, CHECK_NULL);
        objArrayHandle types = oopFactory::new_objArray(SystemDictionary::RiType_klass(), morphism, CHECK_NULL);
        int pos = 0;
        for (uint i = 0; i < recv->row_limit(); i++) {
          KlassHandle receiver = recv->receiver(i);
          if (receiver.is_null())  continue;

          float prob = recv->receiver_count(i) / (float) total_count;
          oop type = GraalCompiler::get_RiType(receiver, KlassHandle(), CHECK_NULL);

          probabilities->float_at_put(pos, prob);
          types->obj_at_put(pos, type);

          pos++;
        }

        RiTypeProfile::set_probabilities(obj, probabilities());
        RiTypeProfile::set_types(obj, types());
      } else {
        RiTypeProfile::set_probabilities(obj, NULL);
        RiTypeProfile::set_types(obj, NULL);
      }
    }
  }

  return JNIHandles::make_local(obj());
}

JNIEXPORT jdouble JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_2branchProbability(JNIEnv *, jobject, jobject hotspot_method, jint bci) {
  TRACE_graal_3("VMEntries::RiMethod_typeProfile");
  ciMethodData* method_data;
  ciMethod* cimethod;
  {
    VM_ENTRY_MARK;
    methodOop method = getMethodFromHotSpotMethod(hotspot_method);
    cimethod = (ciMethod*)CURRENT_ENV->get_object(method);
  }
  method_data = cimethod->method_data();

  jfloat probability = -1;

  if (method_data == NULL || !method_data->is_mature()) return -1;

  ciProfileData* data = method_data->bci_to_data(bci);
  if (data == NULL || !data->is_JumpData())  return -1;

  // get taken and not taken values
  int     taken = data->as_JumpData()->taken();
  int not_taken = 0;
  if (data->is_BranchData()) {
    not_taken = data->as_BranchData()->not_taken();
  }

  // Give up if too few (or too many, in which case the sum will overflow) counts to be meaningful.
  // We also check that individual counters are positive first, otherwise the sum can become positive.
  if (taken < 0 || not_taken < 0 || taken + not_taken < 40) return -1;

  // Pin probability to sane limits
  if (taken == 0)
    return 0;
  else if (not_taken == 0)
    return 1;
  else {                         // Compute probability of true path
    return (jdouble)(taken) / (taken + not_taken);
  }
}

JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_2switchProbability(JNIEnv *, jobject, jobject hotspot_method, jint bci) {
  TRACE_graal_3("VMEntries::RiMethod_typeProfile");
  ciMethodData* method_data;
  ciMethod* cimethod;
  {
    VM_ENTRY_MARK;
    methodOop method = getMethodFromHotSpotMethod(hotspot_method);
    cimethod = (ciMethod*)CURRENT_ENV->get_object(method);
  }
  method_data = cimethod->method_data();

  jfloat probability = -1;

  if (method_data == NULL || !method_data->is_mature()) return NULL;

  ciProfileData* data = method_data->bci_to_data(bci);
  if (data == NULL || !data->is_MultiBranchData())  return NULL;

  MultiBranchData* branch_data = data->as_MultiBranchData();

  long sum = 0;
  int cases = branch_data->number_of_cases();
  GrowableArray<uint>* counts = new GrowableArray<uint>(cases + 1);

  for (int i = 0; i < cases; i++) {
    uint value = branch_data->count_at(i);
    sum += value;
    counts->append(value);
  }
  uint value = branch_data->default_count();
  sum += value;
  counts->append(value);

  // Give up if too few (or too many, in which case the sum will overflow) counts to be meaningful.
  // We also check that individual counters are positive first, otherwise the sum can become positive.
  if (sum < 10 * (cases + 3)) return NULL;

  {
    VM_ENTRY_MARK;
    typeArrayOop probability = oopFactory::new_typeArray(T_DOUBLE, cases + 1, CHECK_NULL);
    for (int i = 0; i < cases + 1; i++) {
      probability->double_at_put(i, counts->at(i) / (double) sum);
    }
    return JNIHandles::make_local(probability);
  }
}

// public native boolean RiMethod_hasCompiledCode(HotSpotMethodResolved method);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1hasCompiledCode(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_hasCompiledCode");
  methodOop method = getMethodFromHotSpotMethod(hotspot_method);
  return method->has_compiled_code();
}

// public native boolean RiMethod_compiledCodeSize(HotSpotMethodResolved method);
JNIEXPORT jint JNICALL Java_com_oracle_graal_runtime_VMEntries_RiMethod_1compiledCodeSize(JNIEnv *, jobject, jobject hotspot_method) {
  TRACE_graal_3("VMEntries::RiMethod_compiledCodeSize");
  nmethod* code = getMethodFromHotSpotMethod(hotspot_method)->code();
  if (code != NULL) {
    return code->insts_end() - code->verified_entry_point();
  }
  return -1;
}

// public RiType RiSignature_lookupType(String returnType, HotSpotTypeResolved accessingClass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiSignature_1lookupType(JNIEnv *env, jobject, jstring jname, jobject accessingClass) {
  TRACE_graal_3("VMEntries::RiSignature_lookupType");
  VM_ENTRY_MARK;

  Symbol* nameSymbol = VmIds::toSymbol(jname);
  Handle name = JNIHandles::resolve(jname);

  oop result;
  if (nameSymbol == vmSymbols::int_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_INT, THREAD);
  } else if (nameSymbol == vmSymbols::long_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_LONG, THREAD);
  } else if (nameSymbol == vmSymbols::bool_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_BOOLEAN, THREAD);
  } else if (nameSymbol == vmSymbols::char_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_CHAR, THREAD);
  } else if (nameSymbol == vmSymbols::short_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_SHORT, THREAD);
  } else if (nameSymbol == vmSymbols::byte_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_BYTE, THREAD);
  } else if (nameSymbol == vmSymbols::double_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_DOUBLE, THREAD);
  } else if (nameSymbol == vmSymbols::float_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_FLOAT, THREAD);
  } else {
    klassOop resolved_type = NULL;
    // if the name isn't in the symbol table then the class isn't loaded anyway...
    if (nameSymbol != NULL) {
      Handle classloader;
      Handle protectionDomain;
      if (JNIHandles::resolve(accessingClass) != NULL) {
        classloader = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(accessingClass))->klass_part()->class_loader();
        protectionDomain = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(accessingClass))->klass_part()->protection_domain();
      }
      resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
      if (HAS_PENDING_EXCEPTION) {
        CLEAR_PENDING_EXCEPTION;
        resolved_type = NULL;
      }
    }
    if (resolved_type != NULL) {
      result = GraalCompiler::createHotSpotTypeResolved(resolved_type, name, CHECK_NULL);
    } else {
      result = VMExits::createRiTypeUnresolved(name, THREAD);
    }
  }

  return JNIHandles::make_local(THREAD, result);
}

// public Object RiConstantPool_lookupConstant(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jobject, jlong vmId, jint index) {
  TRACE_graal_3("VMEntries::RiConstantPool_lookupConstant");
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  oop result = NULL;
  constantTag tag = cp->tag_at(index);
  if (tag.is_int()) {
    result = VMExits::createCiConstant(CiKind::Int(), cp->int_at(index), CHECK_0);
  } else if (tag.is_long()) {
    result = VMExits::createCiConstant(CiKind::Long(), cp->long_at(index), CHECK_0);
  } else if (tag.is_float()) {
    result = VMExits::createCiConstantFloat(cp->float_at(index), CHECK_0);
  } else if (tag.is_double()) {
    result = VMExits::createCiConstantDouble(cp->double_at(index), CHECK_0);
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
    result = VMExits::createCiConstantObject(string, CHECK_0);
  } else if (tag.is_klass() || tag.is_unresolved_klass()) {
    bool ignore;
    ciInstanceKlass* accessor = (ciInstanceKlass*) ciEnv::current()->get_object(cp->pool_holder());
    ciKlass* klass = ciEnv::current()->get_klass_by_index(cp, index, ignore, accessor);
    result = GraalCompiler::get_RiType(klass, cp->pool_holder(), CHECK_NULL);
  } else if (tag.is_object()) {
    oop obj = cp->object_at(index);
    assert(obj->is_instance(), "must be an instance");
    result = VMExits::createCiConstantObject(obj, CHECK_NULL);
  } else {
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

// public RiMethod RiConstantPool_lookupMethod(long vmId, int cpi, byte byteCode);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  TRACE_graal_3("VMEntries::RiConstantPool_lookupMethod");
  VM_ENTRY_MARK;
  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = VmIds::get<constantPoolOop>(vmId);

  Bytecodes::Code bc = (Bytecodes::Code) (((int) byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *cimethod = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  if (cimethod->is_loaded()) {
    methodOop method = (methodOop) cimethod->get_oop();
    oop ret = GraalCompiler::createHotSpotMethodResolved(method, CHECK_NULL);
    return JNIHandles::make_local(THREAD, ret);
  } else {
    Handle name = VmIds::toString<Handle>(cimethod->name()->get_symbol(), CHECK_NULL);
    Handle signature = VmIds::toString<Handle>(cimethod->signature()->as_symbol()->get_symbol(), CHECK_NULL);
    Handle holder = GraalCompiler::get_RiType(cimethod->holder(), cp->klass(), THREAD);
    return JNIHandles::make_local(THREAD, VMExits::createRiMethodUnresolved(name, signature, holder, THREAD));
  }
}

// public RiSignature RiConstantPool_lookupSignature(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupSignature(JNIEnv *env, jobject, jlong vmId, jint index) {
  fatal("currently unsupported");
  return NULL;
}

// public RiType RiConstantPool_lookupType(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupType(JNIEnv *env, jobject, jlong vmId, jint index) {
  TRACE_graal_3("VMEntries::RiConstantPool_lookupType");
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, GraalCompiler::get_RiType(klass, cp->klass(), THREAD));
}

// public RiField RiConstantPool_lookupField(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  TRACE_graal_3("VMEntries::RiConstantPool_lookupField");
  VM_ENTRY_MARK;

  index = GraalCompiler::to_cp_index_u2(index);
  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  
  Bytecodes::Code code = (Bytecodes::Code)(((int) byteCode) & 0xFF);
  Handle field_handle = GraalCompiler::get_RiField(field, loading_klass, cp->pool_holder(), code, THREAD);
  bool is_constant = field->is_constant();
  if (is_constant && field->is_static()) {
    ciConstant constant = field->constant_value();
    oop constant_object = NULL;
    switch (constant.basic_type()) {
      case T_OBJECT:
      case T_ARRAY:
        {
          ciObject* obj = constant.as_object();
          if (obj->is_null_object()) {
            constant_object = VMExits::createCiConstantObject(NULL, CHECK_0);
          } else if (obj->can_be_constant()) {
            constant_object = VMExits::createCiConstantObject(constant.as_object()->get_oop(), CHECK_0);
          }
        }
        break;
      case T_DOUBLE:
        constant_object = VMExits::createCiConstantDouble(constant.as_double(), CHECK_0);
        break;
      case T_FLOAT:
        constant_object = VMExits::createCiConstantFloat(constant.as_float(), CHECK_0);
        break;
      case T_LONG:
        constant_object = VMExits::createCiConstant(CiKind::Long(), constant.as_long(), CHECK_0);
        break;
      case T_INT:
        constant_object = VMExits::createCiConstant(CiKind::Int(), constant.as_int(), CHECK_0);
        break;
      case T_SHORT:
        constant_object = VMExits::createCiConstant(CiKind::Short(), constant.as_int(), CHECK_0);
        break;
      case T_CHAR:
        constant_object = VMExits::createCiConstant(CiKind::Char(), constant.as_int(), CHECK_0);
        break;
      case T_BYTE:
        constant_object = VMExits::createCiConstant(CiKind::Byte(), constant.as_int(), CHECK_0);
        break;
      case T_BOOLEAN:
        constant_object = VMExits::createCiConstant(CiKind::Boolean(), constant.as_int(), CHECK_0);
        break;
      default:
        constant.print();
        fatal("Unhandled constant");
        break;
    }
    if (constant_object != NULL) {
      HotSpotField::set_constant(field_handle, constant_object);
    }
  }
  return JNIHandles::make_local(THREAD, field_handle());
}

// public RiConstantPool RiType_constantPool(HotSpotTypeResolved klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1constantPool(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_constantPool");
  VM_ENTRY_MARK;

  assert(JNIHandles::resolve(klass) != NULL, "");
  constantPoolOop constantPool = ((instanceKlass*)java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass))->klass_part())->constants();
  return JNIHandles::make_local(VMExits::createRiConstantPool(VmIds::add<constantPoolOop>(constantPool), THREAD));
}

// public RiMethod RiType_resolveMethodImpl(HotSpotTypeResolved klass, String name, String signature);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_3resolveMethodImpl(JNIEnv *, jobject, jobject resolved_type, jstring name, jstring signature) {
  TRACE_graal_3("VMEntries::RiType_resolveMethodImpl");
  VM_ENTRY_MARK;

  assert(JNIHandles::resolve(resolved_type) != NULL, "");
  klassOop klass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(resolved_type));
  Symbol* name_symbol = VmIds::toSymbol(name);
  Symbol* signature_symbol = VmIds::toSymbol(signature);
  methodOop method = klass->klass_part()->lookup_method(name_symbol, signature_symbol);
  if (method == NULL) {
    if (TraceGraal >= 3) {
      ResourceMark rm;
      tty->print_cr("Could not resolve method %s %s on klass %s", name_symbol->as_C_string(), signature_symbol->as_C_string(), klass->klass_part()->name()->as_C_string());
    }
    return NULL;
  }
  oop ret = GraalCompiler::createHotSpotMethodResolved(method, CHECK_NULL);
  return JNIHandles::make_local(THREAD, ret);
}

// public boolean RiType_isSubtypeOf(HotSpotTypeResolved klass, RiType other);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_2isSubtypeOf(JNIEnv *, jobject, jobject klass, jobject jother) {
  TRACE_graal_3("VMEntries::RiType_isSubtypeOf");
  oop other = JNIHandles::resolve(jother);
  assert(other->is_a(HotSpotTypeResolved::klass()), "resolved hotspot type expected");
  assert(JNIHandles::resolve(klass) != NULL, "");
  klassOop thisKlass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass));
  klassOop otherKlass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(other));
  if (thisKlass->klass_part()->oop_is_instance_slow()) {
    return instanceKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else if (thisKlass->klass_part()->oop_is_array()) {
    return arrayKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else {
    fatal("unexpected class type");
    return false;
  }
}

// public RiType RiType_componentType(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1componentType(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_componentType");
  ciArrayKlass* array_klass;
  {
    VM_ENTRY_MARK;
    assert(JNIHandles::resolve(klass) != NULL, "");
    array_klass = (ciArrayKlass *) CURRENT_ENV->get_object(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  }
  ciType* element_type = array_klass->element_type();

  VM_ENTRY_MARK;
  assert(JNIHandles::resolve(klass) != NULL, "");
  return JNIHandles::make_local(GraalCompiler::get_RiType(element_type, java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)), THREAD));
}

// public RiType RiType_superType(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1superType(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_superType");
  VM_ENTRY_MARK;
  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  ciInstanceKlass* k = NULL;

  if (klass_handle->oop_is_array()) {
    k = (ciInstanceKlass *) CURRENT_ENV->get_object(SystemDictionary::Object_klass());
  } else {
    guarantee(klass_handle->oop_is_instance(), "must be instance klass");  
    k = (ciInstanceKlass *) CURRENT_ENV->get_object(klass_handle());
    k = k->super();
  }

  if (k != NULL) {
    return JNIHandles::make_local(GraalCompiler::get_RiType(k, klass_handle, THREAD));
  } else {
    return NULL;
  }
}

// public RiType RiType_uniqueConcreteSubtype(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1uniqueConcreteSubtype(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_uniqueConcreteSubtype");
  Thread* THREAD = Thread::current();
  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  ciInstanceKlass* k = NULL;
  {
    VM_ENTRY_MARK;
    k = (ciInstanceKlass *) CURRENT_ENV->get_object(klass_handle());
  }

  if (k->is_abstract()) {
    ciInstanceKlass* sub = k->unique_concrete_subklass();
    if (sub != NULL && sub->is_leaf_type()) {
      VM_ENTRY_MARK;
      return JNIHandles::make_local(GraalCompiler::get_RiType(sub, klass_handle, THREAD));
    }
  } else if (k->is_leaf_type()) {
    assert(!k->is_interface(), "");
    return klass;
  }

  return NULL;
}

// public bool RiType_isInitialized(HotSpotResolvedType klass);
JNIEXPORT jboolean JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1isInitialized(JNIEnv *, jobject, jobject hotspot_klass) {
  TRACE_graal_3("VMEntries::RiType_isInitialized");
  klassOop klass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(hotspot_klass));
  return instanceKlass::cast(klass)->is_initialized();
}

// public RiType RiType_arrayOf(HotSpotTypeResolved klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1arrayOf(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_arrayOf");
  VM_ENTRY_MARK;

  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  KlassHandle array = klass_handle->array_klass(THREAD);
  Handle name = VmIds::toString<Handle>(array->name(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, GraalCompiler::createHotSpotTypeResolved(array, name, THREAD));
}

// public RiField[] RiType_fields(HotSpotTypeResolved klass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_RiType_1fields(JNIEnv *, jobject, jobject klass) {
  TRACE_graal_3("VMEntries::RiType_fields");
  KlassHandle klass_handle;
  ciInstanceKlass* instance_klass;
  objArrayHandle fieldsArray;
  HandleMark hm;
  {
    VM_ENTRY_MARK;
    klass_handle = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass));
    instance_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(klass_handle());
  }
  GrowableArray<ciField*>* fields = instance_klass->non_static_fields();
  {
    VM_ENTRY_MARK;
    fieldsArray = oopFactory::new_objArray(SystemDictionary::RiField_klass(), fields->length(), CHECK_NULL);
    for (int i = 0; i < fields->length(); i++) {
      ciField* field = fields->at(i);
      Handle field_handle = GraalCompiler::get_RiField(field, instance_klass, klass_handle, Bytecodes::_illegal, CHECK_NULL);
      fieldsArray->obj_at_put(i, field_handle());
    }
  }
  return JNIHandles::make_local(fieldsArray());
}

// public RiType getPrimitiveArrayType(CiKind kind);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_getPrimitiveArrayType(JNIEnv *env, jobject, jobject kind) {
  TRACE_graal_3("VMEntries::VMEntries_getPrimitiveArrayType");
  VM_ENTRY_MARK;
  BasicType type = GraalCompiler::kindToBasicType(CiKind::typeChar(kind));
  assert(type != T_OBJECT, "primitive type expecteds");
  ciKlass* klass = ciTypeArrayKlass::make(type);
  return JNIHandles::make_local(THREAD, GraalCompiler::get_RiType(klass, KlassHandle(), THREAD));
}

// public long getMaxCallTargetOffset(CiRuntimeCall rtcall);
JNIEXPORT jlong JNICALL Java_com_oracle_graal_runtime_VMEntries_getMaxCallTargetOffset(JNIEnv *env, jobject, jobject rtcall) {
  TRACE_graal_3("VMEntries::VMEntries_getMaxCallTargetOffset");
  VM_ENTRY_MARK;
  oop call = JNIHandles::resolve(rtcall);
  address target_addr = 0x0;
  if (call == CiRuntimeCall::ArithmeticSin()) {
    target_addr = CAST_FROM_FN_PTR(address, SharedRuntime::dsin);
  } else if (call == CiRuntimeCall::ArithmeticCos()) {
    target_addr = CAST_FROM_FN_PTR(address, SharedRuntime::dcos);
  } else if (call == CiRuntimeCall::ArithmeticTan()) {
    target_addr = CAST_FROM_FN_PTR(address, SharedRuntime::dtan);
  } else if (call == CiRuntimeCall::JavaTimeMillis()) {
    target_addr = CAST_FROM_FN_PTR(address, os::javaTimeMillis);
  } else if (call == CiRuntimeCall::JavaTimeNanos()) {
    target_addr = CAST_FROM_FN_PTR(address, os::javaTimeNanos);
  }
  if (target_addr != 0x0) {
    int64_t off_low = (int64_t)target_addr - ((int64_t)CodeCache::low_bound() + sizeof(int));
    int64_t off_high = (int64_t)target_addr - ((int64_t)CodeCache::high_bound() + sizeof(int));
    return MAX2(ABS(off_low), ABS(off_high));
  }
  return -1;
}

// public RiType getType(Class<?> javaClass);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_getType(JNIEnv *env, jobject, jobject javaClass) {
  TRACE_graal_3("VMEntries::VMEntries_getType");
  VM_ENTRY_MARK;
  oop javaClassOop = JNIHandles::resolve(javaClass);
  if (javaClassOop == NULL) {
    fatal("argument to VMEntries.getType must not be NULL");
    return NULL;
  } else if (java_lang_Class::is_primitive(javaClassOop)) {
    BasicType basicType = java_lang_Class::primitive_type(javaClassOop);
    return JNIHandles::make_local(THREAD, VMExits::createRiTypePrimitive((int) basicType, THREAD));
  } else {
    KlassHandle klass = java_lang_Class::as_klassOop(javaClassOop);
    Handle name = java_lang_String::create_from_symbol(klass->name(), CHECK_NULL);

    oop type = GraalCompiler::createHotSpotTypeResolved(klass, name, CHECK_NULL);
    return JNIHandles::make_local(THREAD, type);
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

// public HotSpotVMConfig getConfiguration();
JNIEXPORT jobject JNICALL Java_com_oracle_graal_runtime_VMEntries_getConfiguration(JNIEnv *env, jobject) {
  jclass klass = env->FindClass("com/oracle/max/graal/runtime/HotSpotVMConfig");
  assert(klass != NULL, "HotSpot vm config class not found");
  jobject config = env->AllocObject(klass);
#ifdef _WIN64
  set_boolean(env, config, "windowsOs", true);
#else
  set_boolean(env, config, "windowsOs", false);
#endif
  set_boolean(env, config, "verifyPointers", VerifyOops);
  set_boolean(env, config, "useFastLocking", UseFastLocking);
  set_boolean(env, config, "useFastNewObjectArray", UseFastNewObjectArray);
  set_boolean(env, config, "useFastNewTypeArray", UseFastNewTypeArray);
  set_int(env, config, "codeEntryAlignment", CodeEntryAlignment);
  set_int(env, config, "vmPageSize", os::vm_page_size());
  set_int(env, config, "stackShadowPages", StackShadowPages);
  set_int(env, config, "hubOffset", oopDesc::klass_offset_in_bytes());
  set_int(env, config, "arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());
  set_int(env, config, "klassStateOffset", instanceKlass::init_state_offset_in_bytes() + sizeof(oopDesc));
  set_int(env, config, "klassStateFullyInitialized", (int)instanceKlass::fully_initialized);
  set_int(env, config, "threadTlabTopOffset", in_bytes(JavaThread::tlab_top_offset()));
  set_int(env, config, "threadTlabEndOffset", in_bytes(JavaThread::tlab_end_offset()));
  set_int(env, config, "threadObjectOffset", in_bytes(JavaThread::threadObj_offset()));
  set_int(env, config, "instanceHeaderPrototypeOffset", Klass::prototype_header_offset_in_bytes() + klassOopDesc::klass_part_offset_in_bytes());
  set_int(env, config, "threadExceptionOopOffset", in_bytes(JavaThread::exception_oop_offset()));
  set_int(env, config, "threadExceptionPcOffset", in_bytes(JavaThread::exception_pc_offset()));
  set_int(env, config, "threadMultiNewArrayStorage", in_bytes(JavaThread::graal_multinewarray_storage_offset()));
  set_int(env, config, "classMirrorOffset", klassOopDesc::klass_part_offset_in_bytes() + Klass::java_mirror_offset_in_bytes());

  set_long(env, config, "debugStub", VmIds::addStub((address)warning));
  set_long(env, config, "instanceofStub", VmIds::addStub(Runtime1::entry_for(Runtime1::slow_subtype_check_id)));
  set_long(env, config, "verifyPointerStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_verify_pointer_id)));
  set_long(env, config, "newInstanceStub", VmIds::addStub(Runtime1::entry_for(Runtime1::fast_new_instance_init_check_id)));
  set_long(env, config, "unresolvedNewInstanceStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_instance_id)));
  set_long(env, config, "newTypeArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_type_array_id)));
  set_long(env, config, "newObjectArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_object_array_id)));
  set_long(env, config, "newMultiArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_multi_array_id)));
  set_long(env, config, "loadKlassStub", VmIds::addStub(Runtime1::entry_for(Runtime1::load_klass_patching_id)));
  set_long(env, config, "accessFieldStub", VmIds::addStub(Runtime1::entry_for(Runtime1::access_field_patching_id)));
  set_long(env, config, "resolveStaticCallStub", VmIds::addStub(SharedRuntime::get_resolve_static_call_stub()));
  set_long(env, config, "inlineCacheMissStub", VmIds::addStub(SharedRuntime::get_ic_miss_stub()));
  set_long(env, config, "unwindExceptionStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_unwind_exception_call_id)));
  set_long(env, config, "handleExceptionStub", VmIds::addStub(Runtime1::entry_for(Runtime1::handle_exception_nofpu_id)));
  set_long(env, config, "handleDeoptStub", VmIds::addStub(SharedRuntime::deopt_blob()->unpack()));
  set_long(env, config, "monitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorenter_id)));
  set_long(env, config, "monitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorexit_id)));
  set_long(env, config, "fastMonitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_monitorenter_id)));
  set_long(env, config, "fastMonitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::graal_monitorexit_id)));
  set_long(env, config, "safepointPollingAddress", (jlong)(os::get_polling_page() + (SafepointPollOffset % os::vm_page_size())));
  set_int(env, config, "runtimeCallStackSize", (jint)frame::arg_reg_save_area_bytes);
  set_boolean(env, config, "isPollingPageFar", Assembler::is_polling_page_far());

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

  jintArray arrayOffsets = env->NewIntArray(basicTypeCount);
  for (int i=0; i<basicTypeCount; i++) {
    jint offset = arrayOopDesc::base_offset_in_bytes(basicTypes[i]);
    env->SetIntArrayRegion(arrayOffsets, i, 1, &offset);
  }
  set_int_array(env, config, "arrayOffsets", arrayOffsets);
  set_int(env, config, "arrayClassElementOffset", objArrayKlass::element_klass_offset_in_bytes() + sizeof(oopDesc));
  return config;
}

// public void installMethod(HotSpotTargetMethod targetMethod);
JNIEXPORT void JNICALL Java_com_oracle_graal_runtime_VMEntries_installMethod(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  VM_ENTRY_MARK;
  if (CURRENT_ENV == NULL) {
    Arena arena;
    ciEnv env(&arena);
    CodeInstaller installer(JNIHandles::resolve(targetMethod));
  } else {
    CodeInstaller installer(JNIHandles::resolve(targetMethod));
  }
}

// public HotSpotProxy installStub(HotSpotTargetMethod targetMethod, String name);
JNIEXPORT jlong JNICALL Java_com_oracle_graal_runtime_VMEntries_installStub(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  VM_ENTRY_MARK;
  jlong id;
  if (CURRENT_ENV == NULL) {
    Arena arena;
    ciEnv env(&arena);
    CodeInstaller installer(JNIHandles::resolve(targetMethod), id);
  } else {
    CodeInstaller installer(JNIHandles::resolve(targetMethod), id);
  }
  return id;
}

// public void recordBailout(String reason);
JNIEXPORT void JNICALL Java_com_oracle_graal_runtime_VMEntries_recordBailout(JNIEnv *jniEnv, jobject, jobject message) {
  if (GraalBailoutIsFatal) {
    Handle msg = JNIHandles::resolve(message);
    if (!msg.is_null()) {
      java_lang_String::print(msg, tty);
    }
    vm_abort(false);
  }
}




#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

#define PROXY           "J"
#define TYPE            "Lcom/sun/cri/ri/RiType;"
#define RESOLVED_TYPE   "Lcom/oracle/max/graal/runtime/HotSpotTypeResolved;"
#define METHOD          "Lcom/sun/cri/ri/RiMethod;"
#define RESOLVED_METHOD "Lcom/oracle/max/graal/runtime/HotSpotMethodResolved;"
#define REFLECT_METHOD  "Ljava/lang/reflect/Method;"
#define TYPE_PROFILE    "Lcom/sun/cri/ri/RiTypeProfile;"
#define SIGNATURE       "Lcom/sun/cri/ri/RiSignature;"
#define FIELD           "Lcom/sun/cri/ri/RiField;"
#define CONSTANT_POOL   "Lcom/sun/cri/ri/RiConstantPool;"
#define EXCEPTION_HANDLERS "[Lcom/sun/cri/ri/RiExceptionHandler;"
#define TARGET_METHOD   "Lcom/oracle/max/graal/runtime/HotSpotTargetMethod;"
#define CONFIG          "Lcom/oracle/max/graal/runtime/HotSpotVMConfig;"
#define HS_METHOD       "Lcom/oracle/max/graal/runtime/HotSpotMethod;"
#define CI_CONSTANT     "Lcom/sun/cri/ci/CiConstant;"
#define CI_KIND         "Lcom/sun/cri/ci/CiKind;"
#define CI_RUNTIME_CALL "Lcom/sun/cri/ci/CiRuntimeCall;"
#define STRING          "Ljava/lang/String;"
#define OBJECT          "Ljava/lang/Object;"
#define CLASS           "Ljava/lang/Class;"

JNINativeMethod VMEntries_methods[] = {
  {CC"RiMethod_code",                   CC"("RESOLVED_METHOD")[B",                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1code)},
  {CC"RiMethod_signature",              CC"("RESOLVED_METHOD")"STRING,              FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1signature)},
  {CC"RiMethod_exceptionHandlers",      CC"("RESOLVED_METHOD")"EXCEPTION_HANDLERS,  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1exceptionHandlers)},
  {CC"RiMethod_hasBalancedMonitors",    CC"("RESOLVED_METHOD")Z",                   FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1hasBalancedMonitors)},
  {CC"RiMethod_uniqueConcreteMethod",   CC"("RESOLVED_METHOD")"METHOD,              FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1uniqueConcreteMethod)},
  {CC"getRiMethod",                     CC"("REFLECT_METHOD")"METHOD,               FN_PTR(Java_com_oracle_graal_runtime_VMEntries_getRiMethod)},
  {CC"RiMethod_typeProfile",            CC"("RESOLVED_METHOD"I)"TYPE_PROFILE,       FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_2typeProfile)},
  {CC"RiMethod_branchProbability",      CC"("RESOLVED_METHOD"I)D",                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_2branchProbability)},
  {CC"RiMethod_switchProbability",      CC"("RESOLVED_METHOD"I)[D",                 FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_2switchProbability)},
  {CC"RiMethod_invocationCount",        CC"("RESOLVED_METHOD")I",                   FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1invocationCount)},
  {CC"RiMethod_exceptionProbability",   CC"("RESOLVED_METHOD"I)I",                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_2exceptionProbability)},
  {CC"RiMethod_hasCompiledCode",        CC"("RESOLVED_METHOD")Z",                   FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1hasCompiledCode)},
  {CC"RiMethod_compiledCodeSize",       CC"("RESOLVED_METHOD")I",                   FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiMethod_1compiledCodeSize)},
  {CC"RiSignature_lookupType",          CC"("STRING RESOLVED_TYPE")"TYPE,           FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiSignature_1lookupType)},
  {CC"RiConstantPool_lookupConstant",   CC"("PROXY"I)"OBJECT,                       FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupConstant)},
  {CC"RiConstantPool_lookupMethod",     CC"("PROXY"IB)"METHOD,                      FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupMethod)},
  {CC"RiConstantPool_lookupSignature",  CC"("PROXY"I)"SIGNATURE,                    FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupSignature)},
  {CC"RiConstantPool_lookupType",       CC"("PROXY"I)"TYPE,                         FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupType)},
  {CC"RiConstantPool_lookupField",      CC"("PROXY"IB)"FIELD,                       FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiConstantPool_1lookupField)},
  {CC"RiType_constantPool",             CC"("RESOLVED_TYPE")"CONSTANT_POOL,         FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1constantPool)},
  {CC"RiType_resolveMethodImpl",        CC"("RESOLVED_TYPE STRING STRING")"METHOD,  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_3resolveMethodImpl)},
  {CC"RiType_isSubtypeOf",              CC"("RESOLVED_TYPE TYPE")Z",                FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_2isSubtypeOf)},
  {CC"RiType_componentType",            CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1componentType)},
  {CC"RiType_uniqueConcreteSubtype",    CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1uniqueConcreteSubtype)},
  {CC"RiType_superType",                CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1superType)},
  {CC"RiType_arrayOf",                  CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1arrayOf)},
  {CC"RiType_fields",                   CC"("RESOLVED_TYPE")["FIELD,                FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1fields)},
  {CC"RiType_isInitialized",            CC"("RESOLVED_TYPE")Z",                     FN_PTR(Java_com_oracle_graal_runtime_VMEntries_RiType_1isInitialized)},
  {CC"getPrimitiveArrayType",           CC"("CI_KIND")"TYPE,                        FN_PTR(Java_com_oracle_graal_runtime_VMEntries_getPrimitiveArrayType)},
  {CC"getMaxCallTargetOffset",          CC"("CI_RUNTIME_CALL")J",                   FN_PTR(Java_com_oracle_graal_runtime_VMEntries_getMaxCallTargetOffset)},
  {CC"getType",                         CC"("CLASS")"TYPE,                          FN_PTR(Java_com_oracle_graal_runtime_VMEntries_getType)},
  {CC"getConfiguration",                CC"()"CONFIG,                               FN_PTR(Java_com_oracle_graal_runtime_VMEntries_getConfiguration)},
  {CC"installMethod",                   CC"("TARGET_METHOD")V",                     FN_PTR(Java_com_oracle_graal_runtime_VMEntries_installMethod)},
  {CC"installStub",                     CC"("TARGET_METHOD")"PROXY,                 FN_PTR(Java_com_oracle_graal_runtime_VMEntries_installStub)},
  {CC"recordBailout",                   CC"("STRING")V",                            FN_PTR(Java_com_oracle_graal_runtime_VMEntries_recordBailout)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
