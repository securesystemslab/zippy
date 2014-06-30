/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

void HSAILKernelArguments::collectArgs() {
  int index = 0;
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] %s::collectArgs, args length=%d", argsBuilderName(), length());
  }

  // Manually iterate over the actual args array without looking at method signature
  while (index < length()) {
    oop arg = args()->obj_at(index++);
    jvalue jValue;
    if (arg == NULL) {
      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] %s::collectArgs object, _index=%d, value = " PTR_FORMAT " is a %s", argsBuilderName(), index, (void*) arg, "null");
      }
      recordNullObjectParameter();
      pushObject(arg);
    } else {
      java_lang_boxing_object::get_value(arg, &jValue);
      BasicType basic_type = java_lang_boxing_object::basic_type(arg);
      if (basic_type == T_ILLEGAL && (!(arg->is_array()))) {
        if (TraceGPUInteraction) {
          tty->print_cr("[HSAIL] %s::collectArgs object, _index=%d, value = " PTR_FORMAT " is a %s", argsBuilderName(), index, (void*) arg, arg == NULL ? "null" : arg->klass()->external_name());
        }
        pushObject(arg);
      } else if (arg->is_array()) {
        if (TraceGPUInteraction) {
          int array_length = ((objArrayOop) arg)->length();
          tty->print_cr("[HSAIL] %s::collectArgs array, length=%d, _index=%d, value = " PTR_FORMAT, argsBuilderName(), array_length, index, (void*) arg);
        }
        pushObject(arg);
      } else {
        switch (basic_type) {
          case T_INT:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_INT _index=%d, value = %d", argsBuilderName(), index, jValue.i);
            }
            pushInt(jValue.i);
            break;
          case T_LONG:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_LONG _index=%d, value = %d", argsBuilderName(), index, jValue.j);
            }
            pushLong(jValue.j);
            break;
          case T_FLOAT:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_FLOAT _index=%d, value = %d", argsBuilderName(), index, jValue.f);
            }
            pushFloat(jValue.f);
            break;
          case T_DOUBLE:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_DOUBLE _index=%d, value = %d", argsBuilderName(), index, jValue.d);
            }
            pushDouble(jValue.d);
            break;
          case T_BYTE:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_BYTE _index=%d, value = %d", argsBuilderName(), index, jValue.b);
            }
            pushByte(jValue.b);
            break;
          case T_BOOLEAN:
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] %s::collectArgs, T_BOOLEAN _index=%d, value = %d", argsBuilderName(), index, jValue.z);
            }
            pushBool(jValue.z);
            break;
        }
      }
    }
  }

  pushTrailingArgs();
}

