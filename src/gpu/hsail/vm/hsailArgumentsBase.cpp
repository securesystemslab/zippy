/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#include "precompiled.hpp"
#include "hsailArgumentsBase.hpp"
#include "runtime/javaCalls.hpp"


// Get next java argument
oop HSAILArgumentsBase::next_arg(BasicType expectedType) {
  assert(_index < _args->length(), "out of bounds");

  oop arg = ((objArrayOop) (_args))->obj_at(_index++);
  assert(expectedType == T_OBJECT ||
         java_lang_boxing_object::is_instance(arg, expectedType), "arg type mismatch");

  return arg;
}

void HSAILArgumentsBase::do_bool() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_BOOLEAN), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  
  pushBool(jValue.z);
}

void HSAILArgumentsBase::do_byte() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_BYTE), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);

  pushByte(jValue.b);
}

void HSAILArgumentsBase::do_double() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_DOUBLE), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_double, _index=%d, value = %e", argsBuilderName(), _index - 1, jValue.d);
  }  
  pushDouble(jValue.d);
}

void HSAILArgumentsBase::do_float() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_FLOAT), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_float, _index=%d, value = %f", argsBuilderName(), _index - 1, jValue.f);
  }  
  pushFloat(jValue.f);
}

void HSAILArgumentsBase::do_int() {
  // If the last parameter is an int, it is handled in a special way
  // For kernel arguments we don't pass it since we use the HSAIL workitemid in place of that int value
  // For javaCall arguments we pass the actual workitemid
  if (isLastParameter()) {
    handleFinalIntParameter();
    return;
  }

  // not the final int parameter, Get the boxed int
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_INT), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_int, _index=%d, value = %d", argsBuilderName(), _index - 1, jValue.i);
  }    
  
  pushInt(jValue.i);
}

void HSAILArgumentsBase::do_long() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_LONG), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_long, _index=%d, value = %d", argsBuilderName(), _index - 1, jValue.j);
  }    

  pushLong(jValue.j);
}

void HSAILArgumentsBase::do_array(int begin, int end) {
  oop arg = _args->obj_at(_index++);
  if (arg == NULL) {
      recordNullObjectParameter();
  } else {
      assert(arg->is_array(), "arg type mismatch");
  }
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_array, _index=%d, 0x%08x, is a %s", argsBuilderName(), _index - 1, (address) arg,
                  arg == NULL ? "null" : arg->klass()->external_name());
  }
    
  pushObject(arg);
}

void HSAILArgumentsBase::do_object() {
  bool isLastParam = isLastParameter();  // determine this before incrementing _index

  oop arg = _args->obj_at(_index++);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::do_object, _index=%d, 0x%08x is a %s", argsBuilderName(), _index - 1, (address) arg,
                  arg == NULL ? "null" : arg->klass()->external_name());
  }
  if (arg == NULL) {
      recordNullObjectParameter();
  }

  // check if this is last arg in signature
  // an object as last parameter requires special handling
  if (isLastParam) {
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] %s, trailing object ref should be object source array ref", argsBuilderName());
    }
    assert(arg->is_objArray(), "arg type mismatch");
    handleFinalObjParameter(arg);
  } else {
    // not the final parameter, just push
    pushObject(arg);
  }
}

void HSAILArgumentsBase::do_object(int begin, int end) {
  do_object();
}

void HSAILArgumentsBase::do_void() {
    return;
}

// TODO implement other do_*

