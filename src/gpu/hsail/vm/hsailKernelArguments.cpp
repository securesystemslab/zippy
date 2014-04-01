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
#include "hsailKernelArguments.hpp"
#include "runtime/javaCalls.hpp"


// Get next java argument
oop HSAILKernelArguments::next_arg(BasicType expectedType) {
  assert(_index < _args->length(), "out of bounds");

  oop arg = ((objArrayOop) (_args))->obj_at(_index++);
  assert(expectedType == T_OBJECT ||
         java_lang_boxing_object::is_instance(arg, expectedType), "arg type mismatch");

  return arg;
}

void HSAILKernelArguments::do_bool() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_BOOLEAN), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  
  bool pushed = Hsail::_okra_push_boolean(_kernel, jValue.z);
  assert(pushed == true, "arg push failed");
}

void HSAILKernelArguments::do_byte() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_BYTE), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  
  bool pushed = Hsail::_okra_push_byte(_kernel, jValue.b);
  assert(pushed == true, "arg push failed");
}

void HSAILKernelArguments::do_double() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_DOUBLE), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_double, _index=%d, value = %e", _index - 1, jValue.d);
  }  
  bool pushed = Hsail::_okra_push_double(_kernel, jValue.d);
  assert(pushed == true, "arg push failed");
}

void HSAILKernelArguments::do_float() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_FLOAT), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_float, _index=%d, value = %f", _index - 1, jValue.f);
  }    
  bool pushed = Hsail::_okra_push_float(_kernel, jValue.f);
  assert(pushed == true, "float push failed");
}

void HSAILKernelArguments::do_int() {
  // The last int is the iteration variable in an IntStream, but we don't pass it
  // since we use the HSAIL workitemid in place of that int value
  if (isLastParameter()) {
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] HSAILKernelArguments::not pushing trailing int");
    }
    return;
  }

  // Get the boxed int
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_INT), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_int, _index=%d, value = %d", _index - 1, jValue.i);
  }    
  
  bool pushed = Hsail::_okra_push_int(_kernel, jValue.i);
  assert(pushed == true, "arg push failed");
}

void HSAILKernelArguments::do_long() {
  // Get the boxed value
  oop arg = _args->obj_at(_index++);
  assert(java_lang_boxing_object::is_instance(arg, T_LONG), "arg type mismatch");
  
  jvalue jValue;
  java_lang_boxing_object::get_value(arg, &jValue);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_long, _index=%d, value = %d", _index - 1, jValue.j);
  }    
  
  bool pushed = Hsail::_okra_push_long(_kernel, jValue.j);
  assert(pushed == true, "arg push failed");  
}

void HSAILKernelArguments::do_array(int begin, int end) {
  oop arg = _args->obj_at(_index++);
  assert(arg->is_array(), "arg type mismatch");
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_array, _index=%d, 0x%08x, is a %s", _index - 1, (address) arg, arg->klass()->external_name());
  }
    
  bool pushed = Hsail::_okra_push_object(_kernel, arg);
  assert(pushed == true, "arg push failed");  
}

void HSAILKernelArguments::do_object() {
  
  bool isLastParam = isLastParameter();  // determine this before incrementing _index

  oop arg = _args->obj_at(_index++);

  // check if this is last arg in signature
  // an object as last parameter requires an object stream source array to be passed
  if (isLastParam) {
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] HSAILKernelArguments::trailing object ref should be object source array ref");
    }
    assert(arg->is_objArray(), "arg type mismatch");
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] HSAILKernelArguments::do_object, _index=%d, 0x%08x is a %s", _index - 1, (address) arg, arg->klass()->external_name());
  }
    
  bool pushed = Hsail::_okra_push_object(_kernel, arg);
  assert(pushed == true, "arg push failed");  
}

void HSAILKernelArguments::do_object(int begin, int end) {
  if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] HSAILKernelArguments::do_object(int begin, int end), begin=%d, end=%d.", begin, end);
  }
  do_object();
}

void HSAILKernelArguments::do_void() {
    return;
}

// TODO implement other do_*
