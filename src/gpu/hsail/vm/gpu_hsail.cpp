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

gpu::Hsail::okra_create_context_func_t  gpu::Hsail::_okra_create_context;
gpu::Hsail::okra_create_kernel_func_t   gpu::Hsail::_okra_create_kernel;
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

  if (_okra_create_kernel == NULL) {
    // probe linkage and we really need it to work this time
    bool success = probe_linkage_internal(true);
    guarantee(success, "[HSAIL] loading okra library");
  }

  gpu::Hsail::register_heap();

  // The kernel entrypoint is always run for the time being  
  const char* entryPointName = "&run";

  _device_context = _okra_create_context();

  // code is not null terminated, must be a better way to do this
  unsigned char* nullTerminatedCodeBuffer = (unsigned char*) malloc(code_len + 1);
  memcpy(nullTerminatedCodeBuffer, code, code_len);
  nullTerminatedCodeBuffer[code_len] = 0;
  void* kernel = _okra_create_kernel(_device_context, nullTerminatedCodeBuffer, entryPointName);
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

#define STRINGIFY(x)     #x

#define LOOKUP_OKRA_FUNCTION(name, alias)  \
  _##alias =                               \
    CAST_TO_FN_PTR(alias##_func_t, os::dll_lookup(handle, STRINGIFY(name))); \
  if (_##alias == NULL) {      \
  tty->print_cr("[HSAIL] ***** Error: Failed to lookup %s in %s, wrong version of OKRA?", STRINGIFY(name), okra_library_name); \
        return 0; \
  } \

bool gpu::Hsail::probe_linkage() {
  return probe_linkage_internal(false);
}


bool gpu::Hsail::probe_linkage_internal(bool isRequired) {
  if (okra_library_name == NULL) {
    if (TraceGPUInteraction) {
      tty->print_cr("Unsupported HSAIL platform");
    }
    return false;
  }

  // here we know we have a valid okra_library_name to try to load
  // the isRequired boolean specifies whether it is an error if the
  // probe does not find the okra library
  char *buffer = (char*)malloc(STD_BUFFER_SIZE);
  if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library is %s", okra_library_name);
  }
  void *handle = os::dll_load(okra_library_name, buffer, STD_BUFFER_SIZE);
  // try alternate location if env variable set
  char *okra_lib_name_from_env_var = getenv("_OKRA_SIM_LIB_PATH_");
  if ((handle == NULL) && (okra_lib_name_from_env_var != NULL)) {
    handle = os::dll_load(okra_lib_name_from_env_var, buffer, STD_BUFFER_SIZE);
    if ((handle != NULL) && TraceGPUInteraction) {
      tty->print_cr("[HSAIL] using _OKRA_SIM_LIB_PATH_=%s", getenv("_OKRA_SIM_LIB_PATH_"));
    }
  }
  free(buffer);

  if ((handle == NULL) && !isRequired) {
    // return true for now but we will probe again later
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library load not in PATH, waiting for Java to put in tmpdir.");
    }
    return true;
  }

  if ((handle == NULL) && isRequired) {
    // Unable to dlopen okra
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library load failed.");
    }
    return false;
  }
  
  // at this point we know handle is valid and we can lookup the functions
  LOOKUP_OKRA_FUNCTION(okra_create_context, okra_create_context);
  LOOKUP_OKRA_FUNCTION(okra_create_kernel, okra_create_kernel);
  LOOKUP_OKRA_FUNCTION(okra_push_object, okra_push_object);
  LOOKUP_OKRA_FUNCTION(okra_push_boolean, okra_push_boolean);
  LOOKUP_OKRA_FUNCTION(okra_push_byte, okra_push_byte);
  LOOKUP_OKRA_FUNCTION(okra_push_double, okra_push_double);
  LOOKUP_OKRA_FUNCTION(okra_push_float, okra_push_float);
  LOOKUP_OKRA_FUNCTION(okra_push_int, okra_push_int);
  LOOKUP_OKRA_FUNCTION(okra_push_long, okra_push_long);
  LOOKUP_OKRA_FUNCTION(okra_execute_with_range, okra_execute_with_range);
  LOOKUP_OKRA_FUNCTION(okra_clearargs, okra_clearargs);
  LOOKUP_OKRA_FUNCTION(okra_register_heap, okra_register_heap);
  
  // if we made it this far, real success
  return true;
}
