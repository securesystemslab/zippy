/*
 * Copyright 2000-2010 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

# include "incls/_precompiled.incl"
# include "incls/_c1x_VMExits.cpp.incl"

// this is a *global* handle
jobject VMExits::_vmExitsPermObject;
jobject VMExits::_vmExitsPermKlass;

KlassHandle VMExits::vmExitsKlass() {
  if (JNIHandles::resolve(_vmExitsPermKlass) == NULL) {
    klassOop result = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_c1x_VMExits(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    if (result == NULL) {
      fatal("Could not find class com.sun.hotspot.c1x.VMExits");
    }
    _vmExitsPermKlass = JNIHandles::make_global(result);
  }
  return KlassHandle((klassOop)JNIHandles::resolve_non_null(_vmExitsPermKlass));
}

Handle VMExits::instance() {
  if (JNIHandles::resolve(_vmExitsPermObject) == NULL) {
    KlassHandle compiler_klass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_c1x_Compiler(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    JavaCalls::call_static(&result, compiler_klass(), vmSymbols::getVMExits_name(), vmSymbols::getVMExits_signature(), &args, Thread::current());
    check_pending_exception("Couldn't get VMExits");
    oop res = (oop) result.get_jobject();
    _vmExitsPermObject = JNIHandles::make_global(res);
  }
  return Handle(JNIHandles::resolve_non_null(_vmExitsPermObject));
}

jboolean VMExits::setOption(Handle option) {
  assert(!option.is_null(), "");
  Thread* THREAD = Thread::current();
  JavaValue result(T_BOOLEAN);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(option);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::setOption_name(), vmSymbols::setOption_signature(), &args, THREAD);
  check_pending_exception("Error while calling setOption");
  return result.get_jboolean();
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

oop VMExits::createRiField(Handle holder, Handle name, Handle type, int index, TRAPS) {
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
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiField_name(), vmSymbols::createRiField_signature(), &args, THREAD);
  check_pending_exception("Error while calling createRiField");
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

oop VMExits::createRiTypeUnresolved(Handle name, jlong accessingClassVmId, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_long(accessingClassVmId);
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

oop VMExits::createCiConstantLong(jlong value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantLong_name(), vmSymbols::createCiConstantLong_signature(), &args, THREAD);
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
  args.push_oop(instance());
  args.push_oop(object);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantObject_name(), vmSymbols::createCiConstantObject_signature(), &args, THREAD);
  check_pending_exception("Error while calling createCiConstantObject");
  return (oop) result.get_jobject();
}
