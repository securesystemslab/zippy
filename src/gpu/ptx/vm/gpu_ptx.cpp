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
#include "runtime/javaCalls.hpp"
#include "runtime/gpu.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/ostream.hpp"
#include "memory/allocation.hpp"
#include "memory/allocation.inline.hpp"
#include "ptxKernelArguments.hpp"

void * gpu::Ptx::_device_context;
int    gpu::Ptx::_cu_device = 0;

gpu::Ptx::cuda_cu_init_func_t gpu::Ptx::_cuda_cu_init;
gpu::Ptx::cuda_cu_ctx_create_func_t gpu::Ptx::_cuda_cu_ctx_create;
gpu::Ptx::cuda_cu_ctx_destroy_func_t gpu::Ptx::_cuda_cu_ctx_destroy;
gpu::Ptx::cuda_cu_ctx_synchronize_func_t gpu::Ptx::_cuda_cu_ctx_synchronize;
gpu::Ptx::cuda_cu_ctx_set_current_func_t gpu::Ptx::_cuda_cu_ctx_set_current;
gpu::Ptx::cuda_cu_device_get_count_func_t gpu::Ptx::_cuda_cu_device_get_count;
gpu::Ptx::cuda_cu_device_get_name_func_t gpu::Ptx::_cuda_cu_device_get_name;
gpu::Ptx::cuda_cu_device_get_func_t gpu::Ptx::_cuda_cu_device_get;
gpu::Ptx::cuda_cu_device_compute_capability_func_t gpu::Ptx::_cuda_cu_device_compute_capability;
gpu::Ptx::cuda_cu_device_get_attribute_func_t gpu::Ptx::_cuda_cu_device_get_attribute;
gpu::Ptx::cuda_cu_launch_kernel_func_t gpu::Ptx::_cuda_cu_launch_kernel;
gpu::Ptx::cuda_cu_module_get_function_func_t gpu::Ptx::_cuda_cu_module_get_function;
gpu::Ptx::cuda_cu_module_load_data_ex_func_t gpu::Ptx::_cuda_cu_module_load_data_ex;
gpu::Ptx::cuda_cu_memcpy_dtoh_func_t gpu::Ptx::_cuda_cu_memcpy_dtoh;
gpu::Ptx::cuda_cu_memfree_func_t gpu::Ptx::_cuda_cu_memfree;

bool gpu::Ptx::initialize_gpu() {

  /* Initialize CUDA driver API */
  int status = _cuda_cu_init(0);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("Failed to initialize CUDA device");
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("CUDA driver initialization: Success");
  }

  /* Get the number of compute-capable device count */
  int device_count = 0;
  status = _cuda_cu_device_get_count(&device_count);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to get compute-capable device count");
    return false;
  }

  if (device_count == 0) {
    tty->print_cr("[CUDA] Found no device supporting CUDA");
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Number of compute-capable devices found: %d", device_count);
  }

  /* Get the handle to the first compute device */
  int device_id = 0;
  /* Compute-capable device handle */
  status = _cuda_cu_device_get(&_cu_device, device_id);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to get handle of first compute-capable device i.e., the one at ordinal: %d", device_id);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Got the handle of first compute-device");
  }

  /* Get device attributes */
  int minor, major, unified_addressing;
  status = _cuda_cu_device_get_attribute(&minor, GRAAL_CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MINOR, _cu_device);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to get minor attribute of device: %d", _cu_device);
    return false;
  }

  status = _cuda_cu_device_get_attribute(&major, GRAAL_CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MAJOR, _cu_device);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to get major attribute of device: %d", _cu_device);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Compatibility version of device %d: %d.%d", _cu_device, major, minor);
  }

  status = _cuda_cu_device_get_attribute(&unified_addressing, GRAAL_CU_DEVICE_ATTRIBUTE_UNIFIED_ADDRESSING, _cu_device);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to query unified addressing mode of device: %d", _cu_device);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Unified addressing support on device %d: %d", _cu_device, unified_addressing);
  }


  /* Get device name */
  char device_name[256];
  status = _cuda_cu_device_get_name(device_name, 256, _cu_device);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to get name of device: %d", _cu_device);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Using %s", device_name);
  }

  return true;
}

void *gpu::Ptx::generate_kernel(unsigned char *code, int code_len, const char *name) {

  struct CUmod_st * cu_module;
  // Use three JIT compiler options
  const unsigned int jit_num_options = 3;
  int *jit_options = NEW_C_HEAP_ARRAY(int, jit_num_options, mtCompiler);
  void **jit_option_values = NEW_C_HEAP_ARRAY(void *, jit_num_options, mtCompiler);

  // Set up PTX JIT compiler options
  // 1. set size of compilation log buffer
  int jit_log_buffer_size = 1024;
  jit_options[0] = GRAAL_CU_JIT_INFO_LOG_BUFFER_SIZE_BYTES;
  jit_option_values[0] = (void *)(size_t)jit_log_buffer_size;

  // 2. set pointer to compilation log buffer
  char *jit_log_buffer = NEW_C_HEAP_ARRAY(char, jit_log_buffer_size, mtCompiler);
  jit_options[1] = GRAAL_CU_JIT_INFO_LOG_BUFFER;
  jit_option_values[1] = jit_log_buffer;

  // 3. set pointer to set the Maximum # of registers (32) for the kernel
  int jit_register_count = 32;
  jit_options[2] = GRAAL_CU_JIT_MAX_REGISTERS;
  jit_option_values[2] = (void *)(size_t)jit_register_count;

  /* Create CUDA context to compile and execute the kernel */
  int status = _cuda_cu_ctx_create(&_device_context, 0, _cu_device);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to create CUDA context for device(%d): %d", _cu_device, status);
    return NULL;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Created context for device: %d", _cu_device);
  }

  status = _cuda_cu_ctx_set_current(_device_context);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to set current context for device: %d", _cu_device);
    return NULL;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Set current context for device: %d", _cu_device);
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] PTX Kernel\n%s", code);
    tty->print_cr("[CUDA] Function name : %s", name);

  }

  /* Load module's data with compiler options */
  status = _cuda_cu_module_load_data_ex(&cu_module, (void*) code, jit_num_options,
                                            jit_options, (void **)jit_option_values);
  if (status != GRAAL_CUDA_SUCCESS) {
    if (status == GRAAL_CUDA_ERROR_NO_BINARY_FOR_GPU) {
      tty->print_cr("[CUDA] Check for malformed PTX kernel or incorrect PTX compilation options");
    }
    tty->print_cr("[CUDA] *** Error (%d) Failed to load module data with online compiler options for method %s",
                  status, name);
    return NULL;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Loaded data for PTX Kernel");
  }

  struct CUfunc_st * cu_function;

  status = _cuda_cu_module_get_function(&cu_function, cu_module, name);

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] *** Error: Failed to get function %s", name);
    return NULL;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Got function handle for %s", name);
  }

  return cu_function;
}

bool gpu::Ptx::execute_kernel(address kernel, PTXKernelArguments &ptxka, JavaValue &ret) {
  // grid dimensionality
  unsigned int gridX = 1;
  unsigned int gridY = 1;
  unsigned int gridZ = 1;

  // thread dimensionality
  unsigned int blockX = 1;
  unsigned int blockY = 1;
  unsigned int blockZ = 1;

  struct CUfunc_st* cu_function = (struct CUfunc_st*) kernel;

  void * config[5] = {
    GRAAL_CU_LAUNCH_PARAM_BUFFER_POINTER, ptxka._kernelArgBuffer,
    GRAAL_CU_LAUNCH_PARAM_BUFFER_SIZE, &(ptxka._bufferOffset),
    GRAAL_CU_LAUNCH_PARAM_END
  };

  if (kernel == NULL) {
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] launching kernel");
  }

  int status = _cuda_cu_launch_kernel(cu_function,
                                      gridX, gridY, gridZ,
                                      blockX, blockY, blockZ,
                                      0, NULL, NULL, (void **) &config);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to launch kernel");
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Kernel Launch");
  }

  status = _cuda_cu_ctx_synchronize();

  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] Failed to synchronize launched kernel (%d)", status);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Synchronized launch kernel");
  }


  // Get the result. TODO: Move this code to get_return_oop()
  BasicType return_type = ptxka.get_ret_type();
  switch (return_type) {
     case T_INT :
       {
         int return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._return_value_ptr, T_INT_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jint(return_val);
       }
       break;
     case T_LONG :
       {
         long return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._return_value_ptr, T_LONG_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jlong(return_val);
       }
       break;
     default:
       tty->print_cr("[CUDA] TODO *** Unhandled return type");
  }


  // Free device memory allocated for result
  status = gpu::Ptx::_cuda_cu_memfree(ptxka._return_value_ptr);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] *** Error (%d) Failed to free device memory of return value", status);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Freed device memory of return value");
  }

  // Destroy context
  status = gpu::Ptx::_cuda_cu_ctx_destroy(_device_context);
  if (status != GRAAL_CUDA_SUCCESS) {
    tty->print_cr("[CUDA] *** Error (%d) Failed to destroy context", status);
    return false;
  }

  if (TraceGPUInteraction) {
    tty->print_cr("[CUDA] Success: Destroy context");
  }

  return (status == GRAAL_CUDA_SUCCESS);
}

#if defined(LINUX)
static const char cuda_library_name[] = "libcuda.so";
#elif defined(__APPLE__)
static char const cuda_library_name[] = "/usr/local/cuda/lib/libcuda.dylib";
#else
static char const cuda_library_name[] = "";
#endif

#define STD_BUFFER_SIZE 1024

bool gpu::Ptx::probe_linkage() {
  if (cuda_library_name != NULL) {
    char *buffer = (char*)malloc(STD_BUFFER_SIZE);
    void *handle = os::dll_load(cuda_library_name, buffer, STD_BUFFER_SIZE);
        free(buffer);
    if (handle != NULL) {
      _cuda_cu_init =
        CAST_TO_FN_PTR(cuda_cu_init_func_t, os::dll_lookup(handle, "cuInit"));
      _cuda_cu_ctx_create =
        CAST_TO_FN_PTR(cuda_cu_ctx_create_func_t, os::dll_lookup(handle, "cuCtxCreate"));
      _cuda_cu_ctx_destroy =
        CAST_TO_FN_PTR(cuda_cu_ctx_destroy_func_t, os::dll_lookup(handle, "cuCtxDestroy"));
      _cuda_cu_ctx_synchronize =
        CAST_TO_FN_PTR(cuda_cu_ctx_synchronize_func_t, os::dll_lookup(handle, "cuCtxSynchronize"));
      _cuda_cu_ctx_set_current =
        CAST_TO_FN_PTR(cuda_cu_ctx_set_current_func_t, os::dll_lookup(handle, "cuCtxSetCurrent"));
      _cuda_cu_device_get_count =
        CAST_TO_FN_PTR(cuda_cu_device_get_count_func_t, os::dll_lookup(handle, "cuDeviceGetCount"));
      _cuda_cu_device_get_name =
        CAST_TO_FN_PTR(cuda_cu_device_get_name_func_t, os::dll_lookup(handle, "cuDeviceGetName"));
      _cuda_cu_device_get =
        CAST_TO_FN_PTR(cuda_cu_device_get_func_t, os::dll_lookup(handle, "cuDeviceGet"));
      _cuda_cu_device_compute_capability =
        CAST_TO_FN_PTR(cuda_cu_device_compute_capability_func_t, os::dll_lookup(handle, "cuDeviceComputeCapability"));
      _cuda_cu_device_get_attribute =
        CAST_TO_FN_PTR(cuda_cu_device_get_attribute_func_t, os::dll_lookup(handle, "cuDeviceGetAttribute"));
      _cuda_cu_module_get_function =
        CAST_TO_FN_PTR(cuda_cu_module_get_function_func_t, os::dll_lookup(handle, "cuModuleGetFunction"));
      _cuda_cu_module_load_data_ex =
        CAST_TO_FN_PTR(cuda_cu_module_load_data_ex_func_t, os::dll_lookup(handle, "cuModuleLoadDataEx"));
      _cuda_cu_launch_kernel =
        CAST_TO_FN_PTR(cuda_cu_launch_kernel_func_t, os::dll_lookup(handle, "cuLaunchKernel"));
      _cuda_cu_memalloc =
        CAST_TO_FN_PTR(cuda_cu_memalloc_func_t, os::dll_lookup(handle, "cuMemAlloc"));
      _cuda_cu_memfree =
        CAST_TO_FN_PTR(cuda_cu_memfree_func_t, os::dll_lookup(handle, "cuMemFree"));
      _cuda_cu_memcpy_htod =
        CAST_TO_FN_PTR(cuda_cu_memcpy_htod_func_t, os::dll_lookup(handle, "cuMemcpyHtoD"));
      _cuda_cu_memcpy_dtoh =
        CAST_TO_FN_PTR(cuda_cu_memcpy_dtoh_func_t, os::dll_lookup(handle, "cuMemcpyDtoH"));

      if (TraceGPUInteraction) {
        tty->print_cr("[CUDA] Success: library linkage");
      }
      return true;
    } else {
      // Unable to dlopen libcuda
      return false;
    }
  } else {
    tty->print_cr("Unsupported CUDA platform");
    return false;
  }
  tty->print_cr("Failed to find CUDA linkage");
  return false;
}
