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
#include "graal/graalVMExits.hpp"

// this is a *global* handle
jobject VMExits::_compilerPermObject;
jobject VMExits::_vmExitsPermObject;
jobject VMExits::_vmExitsPermKlass;

KlassHandle VMExits::vmExitsKlass() {
  if (JNIHandles::resolve(_vmExitsPermKlass) == NULL) {
    klassOop result = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_VMExits(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    check_not_null(result, "Couldn't find class com.sun.hotspot.graal.VMExits");
    _vmExitsPermKlass = JNIHandles::make_global(result);
  }
  return KlassHandle((klassOop)JNIHandles::resolve_non_null(_vmExitsPermKlass));
}

Handle VMExits::compilerInstance() {
  if (JNIHandles::resolve(_compilerPermObject) == NULL) {
    KlassHandle compilerImplKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_CompilerImpl(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    check_not_null(compilerImplKlass(), "Couldn't find class com.sun.hotspot.graal.CompilerImpl");

    JavaValue result(T_OBJECT);
    JavaCalls::call_static(&result, compilerImplKlass, vmSymbols::getInstance_name(), vmSymbols::getInstance_signature(), Thread::current());
    check_pending_exception("Couldn't get Compiler");
    _compilerPermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_compilerPermObject));
}

Handle VMExits::instance() {
  if (JNIHandles::resolve(_vmExitsPermObject) == NULL) {
    KlassHandle compilerKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_Compiler(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    check_not_null(compilerKlass(), "Couldn't find class com.sun.hotspot.graal.Compiler");

    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    args.set_receiver(compilerInstance());
    JavaCalls::call_interface(&result, compilerKlass, vmSymbols::getVMExits_name(), vmSymbols::getVMExits_signature(), &args, Thread::current());
    check_pending_exception("Couldn't get VMExits");
    _vmExitsPermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_vmExitsPermObject));
}

void VMExits::initializeCompiler() {
  KlassHandle compilerImplKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_CompilerImpl(), SystemDictionary::java_system_loader(), NULL, Thread::current());
  check_not_null(compilerImplKlass(), "Couldn't find class com.sun.hotspot.graal.CompilerImpl");

  JavaValue result(T_VOID);
  JavaCalls::call_static(&result, compilerImplKlass, vmSymbols::initialize_name(), vmSymbols::void_method_signature(), Thread::current());
  check_pending_exception("Couldn't initialize compiler");
}

jboolean VMExits::setOption(Handle option) {
  assert(!option.is_null(), "");
  KlassHandle compilerKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_HotSpotOptions(), SystemDictionary::java_system_loader(), NULL, Thread::current());
  check_not_null(compilerKlass(), "Couldn't find class com.sun.hotspot.graal.HotSpotOptions");

  Thread* THREAD = Thread::current();
  JavaValue result(T_BOOLEAN);
  JavaCalls::call_static(&result, compilerKlass, vmSymbols::setOption_name(), vmSymbols::setOption_signature(), option, THREAD);
  check_pending_exception("Error while calling setOption");
  return result.get_jboolean();
}

void VMExits::setDefaultOptions() {
  KlassHandle compilerKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_graal_HotSpotOptions(), SystemDictionary::java_system_loader(), NULL, Thread::current());
  check_not_null(compilerKlass(), "Couldn't find class com.sun.hotspot.graal.HotSpotOptions");

  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCalls::call_static(&result, compilerKlass, vmSymbols::setDefaultOptions_name(), vmSymbols::void_method_signature(), THREAD);
  check_pending_exception("Error while calling setDefaultOptions");
}

void VMExits::compileMethod(jlong methodVmId, Handle name, int entry_bci) {
  assert(!name.is_null(), "just checking");
  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(methodVmId);
  args.push_oop(name);
  args.push_int(entry_bci);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileMethod");
}

oop VMExits::createRiMethodResolved(jlong vmId, Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(vmId);
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiMethodResolved_name(), vmSymbols::createRiMethodResolved_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiMethodResolved");
  return (oop) result.get_jobject();
}

oop VMExits::createRiMethodUnresolved(Handle name, Handle signature, Handle holder, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_oop(signature);
  args.push_oop(holder);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiMethodUnresolved_name(), vmSymbols::createRiMethodUnresolved_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiMethodUnresolved");
  return (oop) result.get_jobject();
}

oop VMExits::createRiField(Handle holder, Handle name, Handle type, int index, int flags, TRAPS) {
  assert(!holder.is_null(), "just checking");
  assert(!name.is_null(), "just checking");
  assert(!type.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(holder);
  args.push_oop(name);
  args.push_oop(type);
  args.push_int(index);
  args.push_int(flags);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiField_name(), vmSymbols::createRiField_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiField");
  assert(result.get_type() == T_OBJECT, "just checking");
  return (oop) result.get_jobject();
}

oop VMExits::createRiType(jlong vmId, Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(vmId);
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiType_name(), vmSymbols::createRiType_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiType");
  return (oop) result.get_jobject();
}

oop VMExits::createRiTypePrimitive(int basic_type, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_int(basic_type);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiTypePrimitive_name(), vmSymbols::createRiTypePrimitive_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiTypePrimitive");
  return (oop) result.get_jobject();
}

oop VMExits::createRiTypeUnresolved(Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiTypeUnresolved_name(), vmSymbols::createRiTypeUnresolved_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiTypeUnresolved");
  return (oop) result.get_jobject();
}

oop VMExits::createRiConstantPool(jlong vmId, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(vmId);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiConstantPool_name(), vmSymbols::createRiConstantPool_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiConstantPool");
  return (oop) result.get_jobject();
}

oop VMExits::createRiSignature(Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiSignature_name(), vmSymbols::createRiSignature_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiSignature");
  return (oop) result.get_jobject();
}

oop VMExits::createCiConstant(Handle kind, jlong value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(kind());
  args.push_long(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstant_name(), vmSymbols::createCiConstant_signature(), &args, THREAD);
  check_pending_exception("Error while calling createCiConstantFloat");
  return (oop) result.get_jobject();

}

oop VMExits::createCiConstantFloat(jfloat value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_float(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantFloat_name(), vmSymbols::createCiConstantFloat_signature(), &args, THREAD);
  check_pending_exception("Error while calling createCiConstantFloat");
  return (oop) result.get_jobject();

}

oop VMExits::createCiConstantDouble(jdouble value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_double(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantDouble_name(), vmSymbols::createCiConstantDouble_signature(), &args, THREAD);
  check_pending_exception("Error while calling createCiConstantDouble");
  return (oop) result.get_jobject();
}

oop VMExits::createCiConstantObject(Handle object, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  /*
  args.push_oop(instance());
  args.push_oop(object);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantObject_name(), vmSymbols::createCiConstantObject_signature(), &args, THREAD);
  check_pending_exception("Error while calling createCiConstantObject");
  */


  KlassHandle klass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_cri_ci_CiConstant(), SystemDictionary::java_system_loader(), NULL, Thread::current());
  JavaCalls::call_static(&result, klass(), vmSymbols::forObject_name(), vmSymbols::createCiConstantObject_signature(), object, THREAD);
  check_pending_exception("Error while calling CiConstant.forObject");
  return (oop) result.get_jobject();
}
