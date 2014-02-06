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

#ifndef KERNEL_ARGUMENTS_HSAIL_HPP
#define KERNEL_ARGUMENTS_HSAIL_HPP

#include "runtime/gpu.hpp"
#include "hsail/vm/gpu_hsail.hpp"
#include "runtime/signature.hpp"

class HSAILKernelArguments : public SignatureIterator {
  friend class Hsail;

public:

private:
  // Array of java argument oops
  objArrayOop _args;
  // Length of args array
  int   _length;
  // Current index into _args
  int _index;
  // Kernel to push into
  address _kernel;
  // number of parameters in the signature
  int _parameter_count;

  bool _is_static;
  
  // Get next java argument
  oop next_arg(BasicType expectedType);

 public:
  HSAILKernelArguments(address kernel, Symbol* signature, objArrayOop args, bool is_static) : SignatureIterator(signature) {
    this->_return_type = T_ILLEGAL;
    _index = 0;
    _args = args;
    _kernel = kernel;
    _is_static = is_static;

    _length = args->length();
    _parameter_count = ArgumentCount(signature).size();

    if (TraceGPUInteraction) {
      ResourceMark rm;
      tty->print_cr("[HSAIL] sig:%s  args length=%d, _parameter_count=%d", signature->as_C_string(), _length, _parameter_count);
    }    
    if (!_is_static) {      
      // First object in args should be 'this'
      oop arg = args->obj_at(_index++);
      assert(arg->is_instance() && (! arg->is_array()), "First arg should be 'this'");
      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] instance method, this 0x%08x, is a %s", (address) arg, arg->klass()->external_name());
      }
      bool pushed = Hsail::_okra_push_object(kernel, arg);
      assert(pushed == true, "'this' push failed");
    } else {
      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] static method");
      }
    }
    // Iterate over the entire signature
    iterate();
  }

  void do_bool();
  void do_byte();
  void do_double();
  void do_float();
  void do_int();
  void do_long();
  void do_array(int begin, int end);
  void do_object();
  void do_object(int begin, int end);

  void do_void();

  inline void do_char()   {
    /* TODO : To be implemented */
    guarantee(false, "do_char:NYI");
  }
  inline void do_short()  {
    /* TODO : To be implemented */
    guarantee(false, "do_short:NYI");
  }

  bool isLastParameter() {
      return  (_index == (_is_static ?  _parameter_count - 1 : _parameter_count));
  }

};

#endif  // KERNEL_ARGUMENTS_HPP
