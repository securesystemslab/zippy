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
#include "runtime/gpu.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/ostream.hpp"

void * gpu::Ptx::_device_context;

gpu::Ptx::cuda_cu_init_func_t gpu::Ptx::_cuda_cu_init;
gpu::Ptx::cuda_cu_ctx_create_func_t gpu::Ptx::_cuda_cu_ctx_create;
gpu::Ptx::cuda_cu_ctx_detach_func_t gpu::Ptx::_cuda_cu_ctx_detach;
gpu::Ptx::cuda_cu_ctx_synchronize_func_t gpu::Ptx::_cuda_cu_ctx_synchronize;
gpu::Ptx::cuda_cu_device_get_count_func_t gpu::Ptx::_cuda_cu_device_get_count;
gpu::Ptx::cuda_cu_device_get_name_func_t gpu::Ptx::_cuda_cu_device_get_name;
gpu::Ptx::cuda_cu_device_get_func_t gpu::Ptx::_cuda_cu_device_get;
gpu::Ptx::cuda_cu_device_compute_capability_func_t gpu::Ptx::_cuda_cu_device_compute_capability;
gpu::Ptx::cuda_cu_launch_kernel_func_t gpu::Ptx::_cuda_cu_launch_kernel;
gpu::Ptx::cuda_cu_module_get_function_func_t gpu::Ptx::_cuda_cu_module_get_function;
gpu::Ptx::cuda_cu_module_load_data_ex_func_t gpu::Ptx::_cuda_cu_module_load_data_ex;

void gpu::probe_linkage() {
#ifdef __APPLE__
  set_gpu_linkage(gpu::Ptx::probe_linkage_apple());
#else
  set_gpu_linkage(false);
#endif
}

void gpu::initialize_gpu() {
  if (gpu::has_gpu_linkage()) {
    set_initialized(gpu::Ptx::initialize_gpu());
  }
}

void * gpu::generate_kernel(unsigned char *code, int code_len, const char *name) {
  if (gpu::has_gpu_linkage()) {
    return (gpu::Ptx::generate_kernel(code, code_len, name));
  } else {
    return NULL;
  }
}

bool gpu::execute_kernel(address kernel) {
  if (gpu::has_gpu_linkage()) {
    return (gpu::Ptx::execute_kernel(kernel));
  } else {
    return false;
  }
}

#define __CUDA_API_VERSION 5000

bool gpu::Ptx::initialize_gpu() {
  int status = _cuda_cu_init(0, __CUDA_API_VERSION);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_init: %d", status);
  }

  int device_count = 0;
  status = _cuda_cu_device_get_count(&device_count);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_device_get_count(%d): %d", device_count, status);
  }

  int device_id = 0, cu_device = 0;
  status = _cuda_cu_device_get(&cu_device, device_id);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_device_get(%d): %d", cu_device, status);
  }

  int major, minor;
  status = _cuda_cu_device_compute_capability(&major, &minor, cu_device);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_device_compute_capability(major %d, minor %d): %d",
                  major, minor, status);
  }

  char device_name[256];
  status = _cuda_cu_device_get_name(device_name, 256, cu_device);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_device_get_name(%s): %d", device_name, status);
  }

  status = _cuda_cu_ctx_create(&_device_context, 0, cu_device);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_ctx_create(%x): %d", _device_context, status);
  }

  return status == 0;  // CUDA_SUCCESS
}

void *gpu::Ptx::generate_kernel(unsigned char *code, int code_len, const char *name) {

  void *cu_module;
  const unsigned int jit_num_options = 3;
  int *jit_options = new int[jit_num_options];
  void **jit_option_values = new void *[jit_num_options];

  jit_options[0] = 4; // CU_JIT_INFO_LOG_BUFFER_SIZE_BYTES
  int jit_log_buffer_size = 1024;
  jit_option_values[0] = (void *)(size_t)jit_log_buffer_size;

  jit_options[1] = 3; // CU_JIT_INFO_LOG_BUFFER
  char *jit_log_buffer = new char[jit_log_buffer_size];
  jit_option_values[1] = jit_log_buffer;

  jit_options[2] = 0; // CU_JIT_MAX_REGISTERS
  int jit_register_count = 32;
  jit_option_values[2] = (void *)(size_t)jit_register_count;
  
  int status = _cuda_cu_module_load_data_ex(&cu_module, code,
                                            jit_num_options, jit_options, (void **)jit_option_values);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_module_load_data_ex(%x): %d", cu_module, status);
    tty->print_cr("gpu_ptx::jit_log_buffer\n%s", jit_log_buffer);
  }

  void *cu_function;

  status = _cuda_cu_module_get_function(&cu_function, cu_module, name);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_module_get_function(%s):%x %d", name, cu_function, status);
  }
  return cu_function;
}

bool gpu::Ptx::execute_kernel(address kernel) {
  // grid dimensionality
  unsigned int gridX = 1;
  unsigned int gridY = 1;
  unsigned int gridZ = 1;

  // thread dimensionality
  unsigned int blockX = 1;
  unsigned int blockY = 1;
  unsigned int blockZ = 1;
  
  int *cu_function = (int *)kernel;

  int status = _cuda_cu_launch_kernel(cu_function,
                                      gridX, gridY, gridZ,
                                      blockX, blockY, blockZ,
                                      0, NULL, NULL, NULL);
  if (TraceWarpLoading) {
    tty->print_cr("gpu_ptx::_cuda_cu_launch_kernel(%x): %d", kernel, status);
  }
  return status == 0;  // CUDA_SUCCESS
}

#ifdef __APPLE__
bool gpu::Ptx::probe_linkage_apple() {
  void *handle = dlopen("/usr/local/cuda/lib/libcuda.dylib", RTLD_LAZY);
  if (handle != NULL) {
    _cuda_cu_init =
        CAST_TO_FN_PTR(cuda_cu_init_func_t, dlsym(handle, "cuInit"));
    _cuda_cu_ctx_create =
        CAST_TO_FN_PTR(cuda_cu_ctx_create_func_t, dlsym(handle, "cuCtxCreate"));
    _cuda_cu_ctx_detach =
        CAST_TO_FN_PTR(cuda_cu_ctx_detach_func_t, dlsym(handle, "cuCtxDetach"));
    _cuda_cu_ctx_synchronize =
        CAST_TO_FN_PTR(cuda_cu_ctx_synchronize_func_t, dlsym(handle, "cuCtxSynchronize"));
    _cuda_cu_device_get_count =
        CAST_TO_FN_PTR(cuda_cu_device_get_count_func_t, dlsym(handle, "cuDeviceGetCount"));
    _cuda_cu_device_get_name =
        CAST_TO_FN_PTR(cuda_cu_device_get_name_func_t, dlsym(handle, "cuDeviceGetName"));
    _cuda_cu_device_get =
        CAST_TO_FN_PTR(cuda_cu_device_get_func_t, dlsym(handle, "cuDeviceGet"));
    _cuda_cu_device_compute_capability =
        CAST_TO_FN_PTR(cuda_cu_device_compute_capability_func_t, dlsym(handle, "cuDeviceComputeCapability"));
    _cuda_cu_module_get_function =
        CAST_TO_FN_PTR(cuda_cu_module_get_function_func_t, dlsym(handle, "cuModuleGetFunction"));
    _cuda_cu_module_load_data_ex =
        CAST_TO_FN_PTR(cuda_cu_module_load_data_ex_func_t, dlsym(handle, "cuModuleLoadDataEx"));
    _cuda_cu_launch_kernel =
        CAST_TO_FN_PTR(cuda_cu_launch_kernel_func_t, dlsym(handle, "cuLaunchKernel"));
    return true;
  }
  return false;
}
#endif