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

KlassHandle VMExits::_vmExitsKlass;

KlassHandle &VMExits::vmExitsKlass() {

  if (_vmExitsKlass.is_null()) {
    Handle nullh;
    _vmExitsKlass = SystemDictionary::resolve_or_null(vmSymbols::com_sun_hotspot_c1x_VMExits(), nullh, nullh, Thread::current());
    if (_vmExitsKlass.is_null()) {
      fatal("Could not find class com.sun.hotspot.c1x.VMExits");
    }
  }
  return _vmExitsKlass;
}

void VMExits::compileMethod(oop method, int entry_bci) {
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(method);
  args.push_int(entry_bci);
  JavaCalls::call_static(&result, vmExitsKlass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, Thread::current());
}

oop VMExits::createRiMethod(methodOop m) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(m);
  JavaCalls::call_static(&result, vmExitsKlass(), vmSymbols::createRiMethod_name(), vmSymbols::createRiMethod_signature(), &args, Thread::current());
  if(Thread::current()->pending_exception() != NULL) {
    tty->print_cr("exc pending");
  }
  return (oop)result.get_jobject();
}

oop VMExits::createRiField(klassOop k, int index) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(k);
  args.push_int(index);
  JavaCalls::call_static(&result, vmExitsKlass(), vmSymbols::createRiField_name(), vmSymbols::createRiField_signature(), &args, Thread::current());
  return (oop)result.get_jobject();
}

oop VMExits::createRiType(klassOop k) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(k);
  JavaCalls::call_static(&result, vmExitsKlass(), vmSymbols::createRiType_name(), vmSymbols::createRiType_signature(), &args, Thread::current());
  return (oop)result.get_jobject();
}

oop VMExits::createRiConstantPool(constantPoolOop cp) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(cp);
  JavaCalls::call_static(&result, vmExitsKlass(), vmSymbols::createRiConstantPool_name(), vmSymbols::createRiConstantPool_signature(), &args, Thread::current());
  return (oop)result.get_jobject();
}