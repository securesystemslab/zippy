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

#ifndef KERNEL_ARGUMENTS_PTX_HPP
#define KERNEL_ARGUMENTS_PTX_HPP

#include "runtime/gpu.hpp"
#include "runtime/signature.hpp"

#define T_BYTE_SIZE        1
#define T_BOOLEAN_SIZE     4
#define T_INT_BYTE_SIZE    4
#define T_FLOAT_BYTE_SIZE  4
#define T_DOUBLE_BYTE_SIZE 8
#define T_LONG_BYTE_SIZE   8
#define T_OBJECT_BYTE_SIZE 8
#define T_ARRAY_BYTE_SIZE  8

class PTXKernelArguments : public SignatureIterator {
public:
  // Buffer holding CUdeviceptr values that represent the kernel arguments
  char _kernelArgBuffer[1024];
  // Current offset into _kernelArgBuffer
  size_t _bufferOffset;
  // Device pointer holding return value
  gpu::Ptx::CUdeviceptr _dev_return_value;

private:
  // Array of java argument oops
  arrayOop _args;
  // Current index into _args
  int _index;
  // Flag to indicate successful creation of kernel argument buffer
  bool _success;

  // Get next java argument
  oop next_arg(BasicType expectedType);

 public:
  PTXKernelArguments(Symbol* signature, arrayOop args, bool is_static) : SignatureIterator(signature) {
    this->_return_type = T_ILLEGAL;
    _index = 0;
    _args = args;
    _success = true;
    _bufferOffset = 0;
    _dev_return_value = 0;
    if (!is_static) {
      // TODO : Create a device argument for receiver object and add it to _kernelBuffer
      tty->print_cr("{CUDA] ****** TODO: Support for execution of non-static java methods not implemented yet.");
    }
    // Iterate over the entire signature
    iterate();
    assert((_success && (_index == args->length())), "arg count mismatch with signature");
  }

  inline char* device_argument_buffer() {
    return _kernelArgBuffer;
  }

  inline size_t device_argument_buffer_size() {
    return _bufferOffset;
  }

  // Get the return oop value
  oop get_return_oop();

  // get device return value ptr
  gpu::Ptx::CUdeviceptr get_dev_return_value() {
      return _dev_return_value;
  }

  /*
   * Pad kernel argument buffer to naturally align for given size.
   */
  void pad_kernel_argument_buffer(size_t);

  void do_byte();
  void do_bool();
  void do_int();
  void do_float();
  void do_double();
  void do_long();
  void do_array(int begin, int end);
  void do_void();

  inline void do_char()   {
    /* TODO : To be implemented */
    guarantee(false, "do_char:NYI");
  }
  inline void do_short()  {
    /* TODO : To be implemented */
    guarantee(false, "do_short:NYI");
  }
  inline void do_object() {
    /* TODO : To be implemented */
    guarantee(false, "do_object:NYI");
  }
    
  inline void do_object(int begin, int end) {
    /* TODO : To be implemented */
    guarantee(false, "do_object(II):NYI");
  }

};

#endif  // KERNEL_ARGUMENTS_HPP
