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
/*
jobject         OopCache::handles = NULL;
jobject         OopCache::mirrors = NULL;
int             OopCache::capacity = 0;
int             OopCache::used = 0;

void initialize() {

}
  static Handle mirror(oop internal_object);
  static Handle resolve(oop mirror);

*/
KlassHandle     VMExits::_vmExitsKlass;
Handle          VMExits::_vmExitsObject;

KlassHandle &VMExits::vmExitsKlass() {
  if (_vmExitsKlass.is_null()) {
    _vmExitsKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_c1x_VMExits(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    if (_vmExitsKlass.is_null()) {
      fatal("Could not find class com.sun.hotspot.c1x.VMExits");
    }
  }
  return _vmExitsKlass;
}

Handle &VMExits::instance() {
  if (_vmExitsObject.is_null()) {
    KlassHandle compiler_klass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_c1x_Compiler(), SystemDictionary::java_system_loader(), NULL, Thread::current());
    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    JavaCalls::call_static(&result, compiler_klass(), vmSymbols::getVMExits_name(), vmSymbols::getVMExits_signature(), &args, Thread::current());
    check_pending_exception("Couldn't get VMExits");
    oop res = (oop)result.get_jobject();
    _vmExitsObject = res;
  }
  return _vmExitsObject;
}

void VMExits::compileMethod(oop method, int entry_bci) {
  assert(method != NULL, "just checking");
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(method);
  args.push_int(entry_bci);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, Thread::current());
  check_pending_exception("Error while calling compileMethod");
}

oop VMExits::createRiMethod(methodOop m) {
  assert(m != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(m);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiMethod_name(), vmSymbols::createRiMethod_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiMethod");
  return (oop)result.get_jobject();
}

oop VMExits::createRiField(oop field_holder, symbolOop field_name, oop field_type, int index) {
  assert(field_holder != NULL && field_name != NULL && field_type != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(field_holder);
  args.push_oop(field_name);
  args.push_oop(field_type);
  args.push_int(index);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiField_name(), vmSymbols::createRiField_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiField");
  return (oop)result.get_jobject();
}

oop VMExits::createRiType(klassOop k) {
  assert(k != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(k);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiType_name(), vmSymbols::createRiType_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiType");
  return (oop)result.get_jobject();
}

oop VMExits::createRiTypePrimitive(int basic_type) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_int(basic_type);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiTypePrimitive_name(), vmSymbols::createRiTypePrimitive_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiTypePrimitive");
  return (oop)result.get_jobject();
}

oop VMExits::createRiTypeUnresolved(symbolOop name, klassOop accessor) {
//  assert(name != NULL && accessor != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_oop(accessor);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiTypeUnresolved_name(), vmSymbols::createRiTypeUnresolved_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiTypeUnresolved");
  return (oop)result.get_jobject();
}

oop VMExits::createRiConstantPool(constantPoolOop cp) {
  assert(cp != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(cp);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiConstantPool_name(), vmSymbols::createRiConstantPool_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiConstantPool");
  return (oop)result.get_jobject();
}

oop VMExits::createRiSignature(symbolOop symbol) {
  assert(symbol != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(symbol);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createRiSignature_name(), vmSymbols::createRiSignature_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createRiSignature");
  return (oop)result.get_jobject();
}

oop VMExits::createCiConstantInt(jint value) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_int(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantInt_name(), vmSymbols::createCiConstantInt_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createCiConstantInt");
  return (oop)result.get_jobject();

}

oop VMExits::createCiConstantLong(jlong value) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantLong_name(), vmSymbols::createCiConstantLong_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createCiConstantFloat");
  return (oop)result.get_jobject();

}

oop VMExits::createCiConstantFloat(jfloat value) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_float(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantFloat_name(), vmSymbols::createCiConstantFloat_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createCiConstantFloat");
  return (oop)result.get_jobject();

}

oop VMExits::createCiConstantDouble(jdouble value) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_double(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantDouble_name(), vmSymbols::createCiConstantDouble_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createCiConstantDouble");
  return (oop)result.get_jobject();
}

oop VMExits::createCiConstantObject(oop value) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(value);
  JavaCalls::call_interface(&result, vmExitsKlass(), vmSymbols::createCiConstantObject_name(), vmSymbols::createCiConstantObject_signature(), &args, Thread::current());
  check_pending_exception("Error while calling createCiConstantObject");
  return (oop)result.get_jobject();
}
