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
#include "ptxKernelArguments.hpp"
#include "runtime/javaCalls.hpp"

gpu::Ptx::cuda_cu_memalloc_func_t gpu::Ptx::_cuda_cu_memalloc;
gpu::Ptx::cuda_cu_memcpy_htod_func_t gpu::Ptx::_cuda_cu_memcpy_htod;

// Get next java argument
oop PTXKernelArguments::next_arg(BasicType expectedType) {
  assert(_index < _args->length(), "out of bounds");
  oop arg = ((objArrayOop) (_args))->obj_at(_index++);
  assert(expectedType == T_OBJECT ||
         java_lang_boxing_object::is_instance(arg, expectedType), "arg type mismatch");
  return arg;
}

/*
 * Pad kernel argument buffer to naturally align for given size.
 */
void PTXKernelArguments::pad_kernel_argument_buffer(size_t dataSz) {
  while ((_bufferOffset % dataSz) != 0) {
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = (char) 0;
    _bufferOffset += sizeof(char);
  }
  return;
}
void PTXKernelArguments::do_int() {
  // If the parameter is a return value,
  if (is_return_type()) {
    // Allocate device memory for T_INT return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_INT_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }

    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;
    _bufferOffset += sizeof(_dev_return_value);
  } else {
    // Get the next java argument and its value which should be a T_INT
    oop arg = next_arg(T_INT);
    // Copy the java argument value to kernelArgBuffer
    jvalue intval;
    if (java_lang_boxing_object::get_value(arg, &intval) != T_INT) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_INT");
      _success = false;
      return;
    }

    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(intval.i));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = intval.i;

    // Advance _bufferOffset
    _bufferOffset += sizeof(intval.i);
  }
  return;
}

void PTXKernelArguments::do_float() {
  // If the parameter is a return value,
  if (is_return_type()) {
    // Allocate device memory for T_FLOAT return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_FLOAT_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;
    // Advance _bufferOffset
    _bufferOffset += sizeof(_dev_return_value);
  } else {
    // Get the next java argument and its value which should be a T_FLOAT
    oop arg = next_arg(T_FLOAT);
    // Copy the java argument value to kernelArgBuffer
    jvalue floatval;
    if (java_lang_boxing_object::get_value(arg, &floatval) != T_FLOAT) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_FLOAT");
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(floatval.f));
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = (gpu::Ptx::CUdeviceptr) floatval.f;

    // Advance _bufferOffset
    _bufferOffset += sizeof(floatval.f);
  }
  return;
}

void PTXKernelArguments::do_double() {
  // If the parameter is a return value,
  jvalue doubleval;
  if (is_return_type()) {
    // Allocate device memory for T_DOUBLE return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_DOUBLE_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;
    // Advance _bufferOffset.
    _bufferOffset += sizeof(doubleval.d);
  } else {
    // Get the next java argument and its value which should be a T_INT
    oop arg = next_arg(T_FLOAT);
    // Copy the java argument value to kernelArgBuffer
    if (java_lang_boxing_object::get_value(arg, &doubleval) != T_DOUBLE) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_INT");
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(doubleval.d));
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = (gpu::Ptx::CUdeviceptr) doubleval.d;

    // Advance _bufferOffset
    _bufferOffset += sizeof(doubleval.d);
    // For a 64-bit host, since size of double is 8, there is no need
    // to pad the kernel argument buffer to ensure 8-byte alignment of
    // the next potential argument to be pushed.
  }
  return;
}

void PTXKernelArguments::do_long() {
  // If the parameter is a return value,
  if (is_return_type()) {
    // Allocate device memory for T_LONG return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_LONG_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;
    // Advance _bufferOffset
    _bufferOffset += sizeof(_dev_return_value);
  } else {
    // Get the next java argument and its value which should be a T_LONG
    oop arg = next_arg(T_LONG);
    // Copy the java argument value to kernelArgBuffer
    jvalue val;
    if (java_lang_boxing_object::get_value(arg, &val) != T_LONG) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_LONG");
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(val.j));
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = val.j;

    // Advance _bufferOffset
    _bufferOffset += sizeof(val.j);
    // For a 64-bit host, since size of long is 8, there is no need
    // to pad the kernel argument buffer to ensure 8-byte alignment of
    // the next potential argument to be pushed.
  }
  return;
}

void PTXKernelArguments::do_byte() {
  // If the parameter is a return value,
  if (is_return_type()) {
    // Allocate device memory for T_BYTE return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_BYTE_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;

    // Advance _bufferOffset
    _bufferOffset += sizeof(_dev_return_value);
  } else {
    // Get the next java argument and its value which should be a T_BYTE
    oop arg = next_arg(T_BYTE);
    // Copy the java argument value to kernelArgBuffer
    jvalue val;
    if (java_lang_boxing_object::get_value(arg, &val) != T_BYTE) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_BYTE");
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(val.b));
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = val.b;

    // Advance _bufferOffset
    _bufferOffset += sizeof(val.b);
    // For a 64-bit host, since size of T_BYTE is 8, there is no need
    // to pad the kernel argument buffer to ensure 8-byte alignment of
    // the next potential argument to be pushed.
  }
  return;
}

void PTXKernelArguments::do_bool() {
  // If the parameter is a return value,
  if (is_return_type()) {
    // Allocate device memory for T_BYTE return value pointer on device. Size in bytes
    int status = gpu::Ptx::_cuda_cu_memalloc(&_dev_return_value, T_BOOLEAN_SIZE);
    if (status != GRAAL_CUDA_SUCCESS) {
      tty->print_cr("[CUDA] *** Error (%d) Failed to allocate memory for return value pointer on device", status);
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(_dev_return_value));
    // Push _dev_return_value to _kernelBuffer
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = _dev_return_value;
    _bufferOffset += sizeof(_dev_return_value);
  } else {
    // Get the next java argument and its value which should be a T_BOOLEAN
    oop arg = next_arg(T_BOOLEAN);
    // Copy the java argument value to kernelArgBuffer
    jvalue val;
    if (java_lang_boxing_object::get_value(arg, &val) != T_BOOLEAN) {
      tty->print_cr("[CUDA] *** Error: Unexpected argument type; expecting T_BOOLEAN");
      _success = false;
      return;
    }
    // Kernel arguments are expected to be naturally aligned.
    // Insert padding into kernel argument buffer, if needed.
    pad_kernel_argument_buffer(sizeof(val.z));
    *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = val.z;

    // Advance _bufferOffset
    _bufferOffset += sizeof(val.z);
  }
  return;
}

void PTXKernelArguments::do_array(int begin, int end) {
  // Get the next java argument and its value which should be a T_ARRAY
  oop arg = next_arg(T_OBJECT);
  assert(arg->is_array(), "argument value not an array");
  // Size of array argument
  int argSize = arg->size() * HeapWordSize;
  // Device pointer to array argument.
  gpu::Ptx::CUdeviceptr arrayArgOnDev;
  int status;

  // Register host memory for use by the device. Size in bytes
  status = gpu::Ptx::_cuda_cu_mem_host_register(arg, argSize, GRAAL_CU_MEMHOSTREGISTER_DEVICEMAP);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] *** Error (%d) Failed to register host memory for array argument on device",
                  status);
    _success = false;
    return;
  }
  // Get device pointer
  status = gpu::Ptx::_cuda_cu_mem_host_get_device_pointer(&arrayArgOnDev, arg, 0);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] *** Error (%d) Failed to get device pointer of mapped pinned memory of array argument.",
                  status);
    _success = false;
    return;
  }

  // Kernel arguments are expected to be naturally aligned.
  // Insert padding into kernel argument buffer, if needed.
  pad_kernel_argument_buffer(sizeof(arrayArgOnDev));
  // Push device array argument to _kernelBuffer
  *((gpu::Ptx::CUdeviceptr*) &_kernelArgBuffer[_bufferOffset]) = arrayArgOnDev;

  // Advance _bufferOffset
  _bufferOffset += sizeof(arrayArgOnDev);
  return;
}

void PTXKernelArguments::do_void() {
  return;
}

// TODO implement other do_*
