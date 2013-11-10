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
#include "hsailKernelArguments.hpp"

void * gpu::Hsail::_device_context;

gpu::Hsail::okra_ctx_create_func_t      gpu::Hsail::_okra_ctx_create;
gpu::Hsail::okra_kernel_create_func_t   gpu::Hsail::_okra_kernel_create;
gpu::Hsail::okra_push_object_func_t     gpu::Hsail::_okra_push_object;
gpu::Hsail::okra_push_boolean_func_t    gpu::Hsail::_okra_push_boolean;
gpu::Hsail::okra_push_byte_func_t       gpu::Hsail::_okra_push_byte;
gpu::Hsail::okra_push_double_func_t     gpu::Hsail::_okra_push_double;
gpu::Hsail::okra_push_float_func_t      gpu::Hsail::_okra_push_float;
gpu::Hsail::okra_push_int_func_t        gpu::Hsail::_okra_push_int;
gpu::Hsail::okra_push_long_func_t       gpu::Hsail::_okra_push_long;
gpu::Hsail::okra_execute_with_range_func_t    gpu::Hsail::_okra_execute_with_range;
gpu::Hsail::okra_clearargs_func_t       gpu::Hsail::_okra_clearargs;
gpu::Hsail::okra_register_heap_func_t   gpu::Hsail::_okra_register_heap;


bool gpu::Hsail::initialize_gpu() {
  // All the initialization is done in the okra library so
  // nothing to do here.
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] Simulator: initialize_gpu");
  }
  return true;
}

unsigned int gpu::Hsail::total_cores() {
  // This is not important with simulator
  return 1;
}

void gpu::Hsail::register_heap() {
  // After the okra functions are set up and the heap is initialized, register the java heap with HSA
  guarantee(Universe::heap() != NULL, "heap should be there by now.");
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] heap=" PTR_FORMAT, Universe::heap());
    tty->print_cr("[HSAIL] base=0x%08x, capacity=%ld", Universe::heap()->base(), Universe::heap()->capacity());
  }
  _okra_register_heap(Universe::heap()->base(), Universe::heap()->capacity());
}

bool  gpu::Hsail::execute_kernel_void_1d(address kernel, int dimX, jobject args, methodHandle& mh) {
  objArrayOop argsArray = (objArrayOop) JNIHandles::resolve(args);

  // Reset the kernel arguments
  _okra_clearargs(kernel);

  // This object sets up the kernel arguments
  HSAILKernelArguments hka(kernel, mh->signature(), argsArray, mh->is_static());

  // Run the kernel
  bool success = _okra_execute_with_range(kernel, dimX);
  return success;
}

void *gpu::Hsail::generate_kernel(unsigned char *code, int code_len, const char *name) {

  gpu::Hsail::register_heap();

  // The kernel entrypoint is always run for the time being  
  const char* entryPointName = "&run";

  _device_context = _okra_ctx_create();

  // code is not null terminated, must be a better way to do this
  unsigned char* nullTerminatedCodeBuffer = (unsigned char*) malloc(code_len + 1);
  memcpy(nullTerminatedCodeBuffer, code, code_len);
  nullTerminatedCodeBuffer[code_len] = 0;
  void* kernel = _okra_kernel_create(_device_context, nullTerminatedCodeBuffer, entryPointName);
  free(nullTerminatedCodeBuffer);
  return kernel;
}

#if defined(LINUX)
static const char okra_library_name[] = "libokra_x86_64.so";
#elif defined (_WINDOWS)
static char const okra_library_name[] = "okra_x86_64.dll";
#else
static char const okra_library_name[] = "";
#endif

#define STD_BUFFER_SIZE 1024

bool gpu::Hsail::probe_linkage() {
  if (okra_library_name != NULL) {
    char *buffer = (char*)malloc(STD_BUFFER_SIZE);
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library is %s", okra_library_name);
    }
    void *handle = os::dll_load(okra_library_name, buffer, STD_BUFFER_SIZE);
    free(buffer);
    if (handle != NULL) {

      _okra_ctx_create =
        CAST_TO_FN_PTR(okra_ctx_create_func_t, os::dll_lookup(handle, "okra_create_context"));
      _okra_kernel_create =
        CAST_TO_FN_PTR(okra_kernel_create_func_t, os::dll_lookup(handle, "okra_create_kernel"));
      _okra_push_object =
        CAST_TO_FN_PTR(okra_push_object_func_t, os::dll_lookup(handle, "okra_push_object"));
      _okra_push_boolean =
        CAST_TO_FN_PTR(okra_push_boolean_func_t, os::dll_lookup(handle, "okra_push_boolean"));
      _okra_push_byte =
        CAST_TO_FN_PTR(okra_push_byte_func_t, os::dll_lookup(handle, "okra_push_byte"));
      _okra_push_double =
        CAST_TO_FN_PTR(okra_push_double_func_t, os::dll_lookup(handle, "okra_push_double"));
      _okra_push_float =
        CAST_TO_FN_PTR(okra_push_float_func_t, os::dll_lookup(handle, "okra_push_float"));
      _okra_push_int =
        CAST_TO_FN_PTR(okra_push_int_func_t, os::dll_lookup(handle, "okra_push_int"));
      _okra_push_long =
        CAST_TO_FN_PTR(okra_push_long_func_t, os::dll_lookup(handle, "okra_push_long"));
      _okra_execute_with_range =
        CAST_TO_FN_PTR(okra_execute_with_range_func_t, os::dll_lookup(handle, "okra_execute_with_range"));
      _okra_clearargs =
        CAST_TO_FN_PTR(okra_clearargs_func_t, os::dll_lookup(handle, "okra_clearargs"));
      _okra_register_heap =
        CAST_TO_FN_PTR(okra_register_heap_func_t, os::dll_lookup(handle, "okra_register_heap"));

      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] Success: library linkage _okra_clearargs=0x%08x", _okra_clearargs);
      }
      return true;
    } else {
      // Unable to dlopen okra
      tty->print_cr("[HSAIL] library load failed.");
      return false;
    }
  } else {
    tty->print_cr("Unsupported HSAIL platform");
    return false;
  }
  tty->print_cr("Failed to find HSAIL linkage");
  return false;
}
