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
#include "runtime/interfaceSupport.hpp"
#include "ptxKernelArguments.hpp"

void * gpu::Ptx::_device_context;
int    gpu::Ptx::_cu_device = 0;

gpu::Ptx::cuda_cu_init_func_t gpu::Ptx::_cuda_cu_init;
gpu::Ptx::cuda_cu_ctx_create_func_t gpu::Ptx::_cuda_cu_ctx_create;
gpu::Ptx::cuda_cu_ctx_destroy_func_t gpu::Ptx::_cuda_cu_ctx_destroy;
gpu::Ptx::cuda_cu_ctx_synchronize_func_t gpu::Ptx::_cuda_cu_ctx_synchronize;
gpu::Ptx::cuda_cu_ctx_get_current_func_t gpu::Ptx::_cuda_cu_ctx_get_current;
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
gpu::Ptx::cuda_cu_mem_host_register_func_t gpu::Ptx::_cuda_cu_mem_host_register;
gpu::Ptx::cuda_cu_mem_host_get_device_pointer_func_t gpu::Ptx::_cuda_cu_mem_host_get_device_pointer;
gpu::Ptx::cuda_cu_mem_host_unregister_func_t gpu::Ptx::_cuda_cu_mem_host_unregister;

#define STRINGIFY(x)     #x

#define LOOKUP_CUDA_FUNCTION(name, alias)  \
  _##alias =                               \
    CAST_TO_FN_PTR(alias##_func_t, os::dll_lookup(handle, STRINGIFY(name))); \
  if (_##alias == NULL) {      \
  tty->print_cr("[CUDA] ***** Error: Failed to lookup %s", STRINGIFY(name)); \
        return 0; \
  } \

#define LOOKUP_CUDA_V2_FUNCTION(name, alias)  LOOKUP_CUDA_FUNCTION(name##_v2, alias)

/*
 * see http://en.wikipedia.org/wiki/CUDA#Supported_GPUs
 */
int ncores(int major, int minor) {
    int device_type = (major << 4) + minor;

    switch (device_type) {
        case 0x10: return 8;
        case 0x11: return 8;
        case 0x12: return 8;
        case 0x13: return 8;
        case 0x20: return 32;
        case 0x21: return 48;
        case 0x30: return 192;
        case 0x35: return 192;
    default:
        tty->print_cr("[CUDA] Warning: Unhandled device %x", device_type);
        return 0;
    }
}

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
  int unified_addressing;

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

unsigned int gpu::Ptx::total_cores() {

    int minor, major, nmp;
    int status = _cuda_cu_device_get_attribute(&minor,
                                               GRAAL_CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MINOR,
                                               _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get minor attribute of device: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&major,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MAJOR,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get major attribute of device: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&nmp,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get numberof MPs on device: %d", _cu_device);
        return 0;
    }

    int total = nmp * ncores(major, minor);

    int max_threads_per_block, warp_size, async_engines, can_map_host_memory, concurrent_kernels;

    status = _cuda_cu_device_get_attribute(&max_threads_per_block,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_BLOCK,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get GRAAL_CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_BLOCK: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&warp_size,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_WARP_SIZE,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get GRAAL_CU_DEVICE_ATTRIBUTE_WARP_SIZE: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&async_engines,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_ASYNC_ENGINE_COUNT,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get GRAAL_CU_DEVICE_ATTRIBUTE_WARP_SIZE: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&can_map_host_memory,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_CAN_MAP_HOST_MEMORY,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get GRAAL_CU_DEVICE_ATTRIBUTE_CAN_MAP_HOST_MEMORY: %d", _cu_device);
        return 0;
    }

    status = _cuda_cu_device_get_attribute(&concurrent_kernels,
                                           GRAAL_CU_DEVICE_ATTRIBUTE_CONCURRENT_KERNELS,
                                           _cu_device);

    if (status != GRAAL_CUDA_SUCCESS) {
        tty->print_cr("[CUDA] Failed to get GRAAL_CU_DEVICE_ATTRIBUTE_CONCURRENT_KERNELS: %d", _cu_device);
        return 0;
    }

    if (TraceGPUInteraction) {
        tty->print_cr("[CUDA] Compatibility version of device %d: %d.%d", _cu_device, major, minor);
        tty->print_cr("[CUDA] Number of cores: %d async engines: %d can map host mem: %d concurrent kernels: %d",
                      total, async_engines, can_map_host_memory, concurrent_kernels);
        tty->print_cr("[CUDA] Max threads per block: %d warp size: %d", max_threads_per_block, warp_size);
    }
    return (total);

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
  int status = _cuda_cu_ctx_create(&_device_context, GRAAL_CU_CTX_MAP_HOST, _cu_device);

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
    tty->print_cr("[CUDA] Got function handle for %s kernel address %p", name, cu_function);
  }

  return cu_function;
}

// A PtxCall is used to manage executing a GPU kernel. In addition to launching
// the kernel, this class releases resources allocated for the execution.
class PtxCall: StackObj {
 private:
  JavaThread*  _thread;        // the thread on which this call is made
  address      _buffer;        // buffer containing parameters and _return_value
  int          _buffer_size;   // size (in bytes) of _buffer
  oop*         _pinned;        // objects that have been pinned with cuMemHostRegister
  int          _pinned_length; // length of _pinned
  gpu::Ptx::CUdeviceptr  _ret_value;     // pointer to slot in GPU memory holding the return value
  int          _ret_type_size; // size of the return type value
  bool         _ret_is_object; // specifies if the return type is Object

  bool check(int status, const char *action) {
    if (status != GRAAL_CUDA_SUCCESS) {
      Thread* THREAD = _thread;
      char* message = NEW_RESOURCE_ARRAY_IN_THREAD(THREAD, char, O_BUFLEN + 1);
      jio_snprintf(message, O_BUFLEN, "[CUDA] *** Error (status=%d): %s", status, action);
      if (TraceGPUInteraction || HAS_PENDING_EXCEPTION) {
        tty->print_cr(message);
      }
      if (!HAS_PENDING_EXCEPTION) {
        SharedRuntime::throw_and_post_jvmti_exception(_thread, vmSymbols::java_lang_RuntimeException(), message);
      }
      return false;
    }
    if (TraceGPUInteraction) {
      tty->print_cr("[CUDA] Success: %s", action);
    }
    return true;
  }

 public:
  PtxCall(JavaThread* thread, address buffer, int buffer_size, oop* pinned, int encodedReturnTypeSize) : _thread(thread),
      _buffer(buffer), _buffer_size(buffer_size), _pinned(pinned), _pinned_length(0), _ret_value(0), _ret_is_object(encodedReturnTypeSize < 0) {
    _ret_type_size = _ret_is_object ? -encodedReturnTypeSize : encodedReturnTypeSize;
  }

  bool is_object_return() { return _ret_is_object; }

  void alloc_return_value() {
    if (_ret_type_size != 0) {
      if (check(gpu::Ptx::_cuda_cu_memalloc(&_ret_value, _ret_type_size), "Allocate device memory for return value")) {
        gpu::Ptx::CUdeviceptr* retValuePtr = (gpu::Ptx::CUdeviceptr*) ((_buffer + _buffer_size) - sizeof(_ret_value));
        *retValuePtr = _ret_value;
      }
    }
  }

  void pin_objects(int count, int* objectOffsets) {
    if (count == 0) {
      return;
    }
    for (int i = 0; i < count; i++) {
      int offset = objectOffsets[i];
      oop* argPtr = (oop*) (_buffer + offset);
      oop obj = *argPtr;
      if (obj != NULL) {
        // Size (in bytes) of object
        int objSize = obj->size() * HeapWordSize;
        //tty->print_cr("Pinning object %d at offset %d: %p", i, offset, obj);
        if (!check(gpu::Ptx::_cuda_cu_mem_host_register(obj, objSize, GRAAL_CU_MEMHOSTREGISTER_DEVICEMAP), "Pin object")) {
          return;
        }

        // Record original oop so that its memory can be unpinned
        _pinned[_pinned_length++] = obj;

        // Replace host pointer to object with device pointer
        // to object in kernel parameters buffer
        if (!check(gpu::Ptx::_cuda_cu_mem_host_get_device_pointer((gpu::Ptx::CUdeviceptr*) argPtr, obj, 0), "Get device pointer for pinned object")) {
          return;
        }
      }
    }
  }

  void launch(address kernel, jint dimX, jint dimY, jint dimZ) {
    // grid dimensionality
    unsigned int gridX = 1;
    unsigned int gridY = 1;
    unsigned int gridZ = 1;
    void * config[] = {
      GRAAL_CU_LAUNCH_PARAM_BUFFER_POINTER, (char*) (address) _buffer,
      GRAAL_CU_LAUNCH_PARAM_BUFFER_SIZE, &_buffer_size,
      GRAAL_CU_LAUNCH_PARAM_END
    };
    if (check(gpu::Ptx::_cuda_cu_launch_kernel((struct CUfunc_st*) (address) kernel,
                                      gridX, gridY, gridZ,
                                      dimX, dimY, dimZ,
                                      0, NULL, NULL, (void**) &config), "Launch kernel")) {
    }
  }

  void synchronize() {
    check(gpu::Ptx::_cuda_cu_ctx_synchronize(), "Synchronize kernel");
  }

  void unpin_objects() {
    while (_pinned_length > 0) {
      oop obj = _pinned[--_pinned_length];
      assert(obj != NULL, "npe");
      //tty->print_cr("Unpinning object %d: %p", _pinned_length, obj);
      if (!check(gpu::Ptx::_cuda_cu_mem_host_unregister(obj), "Unpin object")) {
        return;
      }
    }
  }

  oop get_object_return_value() {
    oop return_val;
    check(gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, _ret_value, T_OBJECT_BYTE_SIZE), "Copy return value from device");
    return return_val;
  }

  jlong get_primitive_return_value() {
    jlong return_val;
    check(gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, _ret_value, _ret_type_size), "Copy return value from device");
    return return_val;
  }

  void free_return_value() {
    if (_ret_value != 0) {
      check(gpu::Ptx::_cuda_cu_memfree(_ret_value), "Free device memory");
      _ret_value = 0;
    }
  }

  void destroy_context() {
    if (gpu::Ptx::_device_context != NULL) {
      check(gpu::Ptx::_cuda_cu_ctx_destroy(gpu::Ptx::_device_context), "Destroy context");
      gpu::Ptx::_device_context = NULL;
    }
  }

  ~PtxCall() {
    unpin_objects();
    free_return_value();
    destroy_context();
  }
};


JRT_ENTRY(jlong, gpu::Ptx::execute_kernel_from_vm(JavaThread* thread, jlong kernel, jint dimX, jint dimY, jint dimZ,
                                                  jlong buffer,
                                                  jint bufferSize,
                                                  jint objectParametersCount,
                                                  jlong objectParametersOffsets,
                                                  jlong pinnedObjects,
                                                  int encodedReturnTypeSize))
  if (kernel == 0L) {
    SharedRuntime::throw_and_post_jvmti_exception(thread, vmSymbols::java_lang_NullPointerException(), NULL);
    return 0L;
  }

  PtxCall call(thread, (address) buffer, bufferSize, (oop*) (address) pinnedObjects, encodedReturnTypeSize);

#define TRY(action) do { \
  action; \
  if (HAS_PENDING_EXCEPTION) return 0L; \
} while (0)

  TRY(call.alloc_return_value());

  TRY(call.pin_objects(objectParametersCount, (int*) (address) objectParametersOffsets));

  TRY(call.launch((address) kernel, dimX, dimY, dimZ));

  TRY(call.synchronize());

  if (call.is_object_return()) {
    oop return_val;
    TRY(return_val = call.get_object_return_value());
    thread->set_vm_result(return_val);
    return 0L;
  }

  jlong return_val;
  TRY(return_val = call.get_primitive_return_value());
  return return_val;

#undef TRY

JRT_END

bool gpu::Ptx::execute_kernel(address kernel, PTXKernelArguments &ptxka, JavaValue &ret) {
    return gpu::Ptx::execute_warp(1, 1, 1, kernel, ptxka, ret);
}

bool gpu::Ptx::execute_warp(int dimX, int dimY, int dimZ,
                            address kernel, PTXKernelArguments &ptxka, JavaValue &ret) {
  // grid dimensionality
  unsigned int gridX = 1;
  unsigned int gridY = 1;
  unsigned int gridZ = 1;

  // thread dimensionality
  unsigned int blockX = dimX;
  unsigned int blockY = dimY;
  unsigned int blockZ = dimZ;

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
    tty->print_cr("[CUDA] Success: Kernel Launch: X: %d Y: %d Z: %d", blockX, blockY, blockZ);
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
     case T_INT:
       {
         int return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._dev_return_value, T_INT_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jint(return_val);
       }
       break;
     case T_BOOLEAN:
       {
         int return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._dev_return_value, T_INT_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jint(return_val);
       }
       break;
     case T_FLOAT:
       {
         float return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._dev_return_value, T_FLOAT_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jfloat(return_val);
       }
       break;
     case T_DOUBLE:
       {
         double return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._dev_return_value, T_DOUBLE_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jdouble(return_val);
       }
       break;
     case T_LONG:
       {
         long return_val;
         status = gpu::Ptx::_cuda_cu_memcpy_dtoh(&return_val, ptxka._dev_return_value, T_LONG_BYTE_SIZE);
         if (status != GRAAL_CUDA_SUCCESS) {
           tty->print_cr("[CUDA] *** Error (%d) Failed to copy value to device argument", status);
           return false;
         }
         ret.set_jlong(return_val);
       }
       break;
     case T_VOID:
       break;
     default:
       tty->print_cr("[CUDA] TODO *** Unhandled return type: %d", return_type);
  }

  // Free device memory allocated for result
  status = gpu::Ptx::_cuda_cu_memfree(ptxka._dev_return_value);
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
      LOOKUP_CUDA_FUNCTION(cuInit, cuda_cu_init);
      LOOKUP_CUDA_FUNCTION(cuCtxSynchronize, cuda_cu_ctx_synchronize);
      LOOKUP_CUDA_FUNCTION(cuCtxGetCurrent, cuda_cu_ctx_get_current);
      LOOKUP_CUDA_FUNCTION(cuCtxSetCurrent, cuda_cu_ctx_set_current);
      LOOKUP_CUDA_FUNCTION(cuDeviceGetCount, cuda_cu_device_get_count);
      LOOKUP_CUDA_FUNCTION(cuDeviceGetName, cuda_cu_device_get_name);
      LOOKUP_CUDA_FUNCTION(cuDeviceGet, cuda_cu_device_get);
      LOOKUP_CUDA_FUNCTION(cuDeviceComputeCapability, cuda_cu_device_compute_capability);
      LOOKUP_CUDA_FUNCTION(cuDeviceGetAttribute, cuda_cu_device_get_attribute);
      LOOKUP_CUDA_FUNCTION(cuModuleGetFunction, cuda_cu_module_get_function);
      LOOKUP_CUDA_FUNCTION(cuModuleLoadDataEx, cuda_cu_module_load_data_ex);
      LOOKUP_CUDA_FUNCTION(cuLaunchKernel, cuda_cu_launch_kernel);
      LOOKUP_CUDA_FUNCTION(cuMemHostRegister, cuda_cu_mem_host_register);
      LOOKUP_CUDA_FUNCTION(cuMemHostUnregister, cuda_cu_mem_host_unregister);
#if defined(__x86_64) || defined(AMD64) || defined(_M_AMD64)
      LOOKUP_CUDA_V2_FUNCTION(cuCtxCreate, cuda_cu_ctx_create);
      LOOKUP_CUDA_V2_FUNCTION(cuCtxDestroy, cuda_cu_ctx_destroy);
      LOOKUP_CUDA_V2_FUNCTION(cuMemAlloc, cuda_cu_memalloc);
      LOOKUP_CUDA_V2_FUNCTION(cuMemFree, cuda_cu_memfree);
      LOOKUP_CUDA_V2_FUNCTION(cuMemcpyHtoD, cuda_cu_memcpy_htod);
      LOOKUP_CUDA_V2_FUNCTION(cuMemcpyDtoH, cuda_cu_memcpy_dtoh);
      LOOKUP_CUDA_V2_FUNCTION(cuMemHostGetDevicePointer, cuda_cu_mem_host_get_device_pointer);
#else
      LOOKUP_CUDA_FUNCTION(cuCtxCreate, cuda_cu_ctx_create);
      LOOKUP_CUDA_FUNCTION(cuCtxDestroy, cuda_cu_ctx_destroy);
      LOOKUP_CUDA_FUNCTION(cuMemAlloc, cuda_cu_memalloc);
      LOOKUP_CUDA_FUNCTION(cuMemFree, cuda_cu_memfree);
      LOOKUP_CUDA_FUNCTION(cuMemcpyHtoD, cuda_cu_memcpy_htod);
      LOOKUP_CUDA_FUNCTION(cuMemcpyDtoH, cuda_cu_memcpy_dtoh);
      LOOKUP_CUDA_FUNCTION(cuMemHostGetDevicePointer, cuda_cu_mem_host_get_device_pointer);
#endif

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
