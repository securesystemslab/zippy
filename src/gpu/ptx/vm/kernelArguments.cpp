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
#include "kernelArguments.hpp"
#include "runtime/javaCalls.hpp"

gpu::Ptx::cuda_cu_memalloc_func_t gpu::Ptx::_cuda_cu_memalloc;
gpu::Ptx::cuda_cu_memcpy_htod_func_t gpu::Ptx::_cuda_cu_memcpy_htod;

// Get next java argument
oop PTXKernelArguments::next_arg(BasicType expectedType) {
  assert(_index < _args->length(), "out of bounds");
  oop arg=((objArrayOop) (_args))->obj_at(_index++);
  assert(expectedType == T_OBJECT || java_lang_boxing_object::is_instance(arg, expectedType), "arg type mismatch");
  return arg;
}

void PTXKernelArguments::do_int()    { 
  // If the parameter is a return value, 
  if (is_return_type()) {
    // Allocate device memory for T_INT return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_return_value_ptr, T_INT_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Push _return_value_ptr to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _return_value_ptr;
    _bufferOffset += sizeof(_return_value_ptr);
  }
  else {
    // Get the next java argument and its value which should be a T_INT
    oop arg = next_arg(T_INT);
    // Copy the java argument value to kernelArgBuffer
    jvalue intval;
    if (java_lang_boxing_object::get_value(arg, &intval) != T_INT) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_INT");
      _success = false;
      return;
    }
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = intval.i;
    _bufferOffset += sizeof(intval.i);
  }
  return;
}

void PTXKernelArguments::do_long()    { 
  // If the parameter is a return value, 
  if (is_return_type()) {
    // Allocate device memory for T_LONG return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_return_value_ptr, T_LONG_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Push _return_value_ptr to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _return_value_ptr;
    _bufferOffset += sizeof(_return_value_ptr);
  }
  else {
    // Get the next java argument and its value which should be a T_LONG
    oop arg = next_arg(T_LONG);
    // Copy the java argument value to kernelArgBuffer
    jvalue val;
    if (java_lang_boxing_object::get_value(arg, &val) != T_LONG) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_LONG");
      _success = false;
      return;
    }
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = val.j;
    _bufferOffset += sizeof(val.j);
  }
  return;
}

void PTXKernelArguments::do_byte()    { 
  // If the parameter is a return value, 
  if (is_return_type()) {
    // Allocate device memory for T_BYTE return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_return_value_ptr, T_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Push _return_value_ptr to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _return_value_ptr;
    _bufferOffset += sizeof(_return_value_ptr);
  }
  else {
    // Get the next java argument and its value which should be a T_BYTE
    oop arg = next_arg(T_BYTE);
    // Copy the java argument value to kernelArgBuffer
    jvalue val;
    if (java_lang_boxing_object::get_value(arg, &val) != T_BYTE) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_BYTE");
      _success = false;
      return;
    }
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = val.b;
    _bufferOffset += sizeof(val.b);
  }
  return;
}

// TODO implement other do_*
