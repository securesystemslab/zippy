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
#include "hsail/vm/gpu_hsail.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/ostream.hpp"
#include "memory/allocation.hpp"
#include "memory/allocation.inline.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalJavaAccess.hpp"
#include "hsailKernelArguments.hpp"

// Entry to GPU native method implementation that transitions current thread to '_thread_in_vm'.
#define GPU_VMENTRY(result_type, name, signature) \
  JNIEXPORT result_type JNICALL name signature { \
  GRAAL_VM_ENTRY_MARK; \

// Entry to GPU native method implementation that calls a JNI function
// and hence cannot transition current thread to '_thread_in_vm'.
#define GPU_ENTRY(result_type, name, signature) \
  JNIEXPORT result_type JNICALL name signature { \

#define GPU_END }

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &(f))

#define OBJECT                "Ljava/lang/Object;"
#define STRING                "Ljava/lang/String;"
#define HS_INSTALLED_CODE     "Lcom/oracle/graal/hotspot/meta/HotSpotInstalledCode;"

//  public native void executeKernel(HotSpotNmethod kernel, int jobSize, int i, int j, Object[] args) throws InvalidInstalledCodeException;

JNINativeMethod Hsail::HSAIL_methods[] = {
  {CC"initialize",       CC"()Z",                               FN_PTR(Hsail::initialize)},
  {CC"generateKernel",   CC"([B" STRING ")J",                   FN_PTR(Hsail::generate_kernel)},
  {CC"executeKernel0",   CC"("HS_INSTALLED_CODE"I["OBJECT")Z",  FN_PTR(Hsail::execute_kernel_void_1d)},
};

void * Hsail::_device_context = NULL;

Hsail::okra_create_context_func_t  Hsail::_okra_create_context;
Hsail::okra_create_kernel_func_t   Hsail::_okra_create_kernel;
Hsail::okra_push_object_func_t     Hsail::_okra_push_object;
Hsail::okra_push_boolean_func_t    Hsail::_okra_push_boolean;
Hsail::okra_push_byte_func_t       Hsail::_okra_push_byte;
Hsail::okra_push_double_func_t     Hsail::_okra_push_double;
Hsail::okra_push_float_func_t      Hsail::_okra_push_float;
Hsail::okra_push_int_func_t        Hsail::_okra_push_int;
Hsail::okra_push_long_func_t       Hsail::_okra_push_long;
Hsail::okra_execute_with_range_func_t    Hsail::_okra_execute_with_range;
Hsail::okra_clearargs_func_t       Hsail::_okra_clearargs;
Hsail::okra_register_heap_func_t   Hsail::_okra_register_heap;


void Hsail::register_heap() {
  // After the okra functions are set up and the heap is initialized, register the java heap with HSA
  guarantee(Universe::heap() != NULL, "heap should be there by now.");
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] heap=" PTR_FORMAT, Universe::heap());
    tty->print_cr("[HSAIL] base=0x%08x, capacity=%ld", Universe::heap()->base(), Universe::heap()->capacity());
  }
  _okra_register_heap(Universe::heap()->base(), Universe::heap()->capacity());
}

GPU_VMENTRY(jboolean, Hsail::execute_kernel_void_1d, (JNIEnv* env, jclass, jobject kernel_handle, jint dimX, jobject args_handle))

  ResourceMark rm;
  jlong nmethodValue = HotSpotInstalledCode::codeBlob(kernel_handle);
  if (nmethodValue == 0) {
    SharedRuntime::throw_and_post_jvmti_exception(JavaThread::current(), vmSymbols::com_oracle_graal_api_code_InvalidInstalledCodeException(), NULL);
  }
  nmethod* nm = (nmethod*) (address) nmethodValue;
  methodHandle mh = nm->method();
  Symbol* signature = mh->signature();

  void* kernel = (void*) HotSpotInstalledCode::codeStart(kernel_handle);
  if (kernel == NULL) {
    SharedRuntime::throw_and_post_jvmti_exception(JavaThread::current(), vmSymbols::com_oracle_graal_api_code_InvalidInstalledCodeException(), NULL);
  }

  objArrayOop args = (objArrayOop) JNIHandles::resolve(args_handle);

  // Reset the kernel arguments
  _okra_clearargs(kernel);

  // This object sets up the kernel arguments
  HSAILKernelArguments hka((address) kernel, mh->signature(), args, mh->is_static());

  // Run the kernel
  return _okra_execute_with_range(kernel, dimX);
GPU_END

GPU_ENTRY(jlong, Hsail::generate_kernel, (JNIEnv *env, jclass, jbyteArray code_handle, jstring name_handle))
  guarantee(_okra_create_kernel != NULL, "[HSAIL] Okra not linked");
  ResourceMark rm;
  jsize name_len = env->GetStringLength(name_handle);
  jsize code_len = env->GetArrayLength(code_handle);

  char* name = NEW_RESOURCE_ARRAY(char, name_len + 1);
  unsigned char *code = NEW_RESOURCE_ARRAY(unsigned char, code_len + 1);

  code[code_len] = 0;
  name[name_len] = 0;

  env->GetByteArrayRegion(code_handle, 0, code_len, (jbyte*) code);
  env->GetStringUTFRegion(name_handle, 0, name_len, name);

  register_heap();

  // The kernel entrypoint is always run for the time being  
  const char* entryPointName = "&run";

  _device_context = _okra_create_context();

  return (jlong) _okra_create_kernel(_device_context, code, entryPointName);
GPU_END

#if defined(LINUX)
static const char* okra_library_name = "libokra_x86_64.so";
#elif defined(_WINDOWS)
static char const* okra_library_name = "okra_x86_64.dll";
#else
static char const* okra_library_name = NULL;
#endif

#define STRINGIFY(x)     #x

#define LOOKUP_OKRA_FUNCTION(name, alias)  \
  _##alias =                               \
    CAST_TO_FN_PTR(alias##_func_t, os::dll_lookup(handle, STRINGIFY(name))); \
  if (_##alias == NULL) {      \
  tty->print_cr("[HSAIL] ***** Error: Failed to lookup %s in %s, wrong version of OKRA?", STRINGIFY(name), okra_library_name); \
        return false; \
  } \

GPU_ENTRY(jboolean, Hsail::initialize, (JNIEnv *env, jclass))
  if (okra_library_name == NULL) {
    if (TraceGPUInteraction) {
      tty->print_cr("Unsupported HSAIL platform");
    }
    return false;
  }

  // here we know we have a valid okra_library_name to try to load
  char ebuf[O_BUFLEN];
  if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library is %s", okra_library_name);
  }
  void *handle = os::dll_load(okra_library_name, ebuf, O_BUFLEN);
  // try alternate location if env variable set
  char *okra_lib_name_from_env_var = getenv("_OKRA_SIM_LIB_PATH_");
  if ((handle == NULL) && (okra_lib_name_from_env_var != NULL)) {
    handle = os::dll_load(okra_lib_name_from_env_var, ebuf, O_BUFLEN);
    if ((handle != NULL) && TraceGPUInteraction) {
      tty->print_cr("[HSAIL] using _OKRA_SIM_LIB_PATH_=%s", getenv("_OKRA_SIM_LIB_PATH_"));
    }
  }

  if (handle == NULL) {
    // Unable to dlopen okra
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library load failed.");
    }
    return false;
  }
  
  guarantee(_okra_create_context == NULL, "cannot repeat GPU initialization");

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

  gpu::initialized_gpu("Okra");

  return true;
GPU_END

bool Hsail::register_natives(JNIEnv* env) {
  jclass klass = env->FindClass("com/oracle/graal/hotspot/hsail/HSAILHotSpotBackend");
  if (klass == NULL) {
    if (TraceGPUInteraction) {
      tty->print_cr("HSAILHotSpotBackend class not found");
    }
    return false;
  }
  jint status = env->RegisterNatives(klass, HSAIL_methods, sizeof(HSAIL_methods) / sizeof(JNINativeMethod));
  if (status != JNI_OK) {
    if (TraceGPUInteraction) {
      tty->print_cr("Error registering natives for HSAILHotSpotBackend: %d", status);
    }
    return false;
  }
  return true;
}
