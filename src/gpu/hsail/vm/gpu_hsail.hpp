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

#ifndef GPU_HSAIL_HPP
#define GPU_HSAIL_HPP

class Hsail {

private:

  static JNINativeMethod HSAIL_methods[];

  // static native boolean initialize();
  JNIEXPORT static jboolean initialize(JNIEnv *env, jclass);

  // static native long generateKernel(byte[] targetCode, String name);
  JNIEXPORT static jlong generate_kernel(JNIEnv *env, jclass, jbyteArray code_handle, jstring name_handle);

  // static native boolean executeKernel0(HotSpotInstalledCode kernel, int jobSize, Object[] args);
  JNIEXPORT static jboolean execute_kernel_void_1d(JNIEnv *env, jclass, jobject hotspotInstalledCode, jint dimX, jobject args);

  static void register_heap();

public:

  // Registers the implementations for the native methods in HSAILHotSpotBackend
  static bool register_natives(JNIEnv* env);

#if defined(__x86_64) || defined(AMD64) || defined(_M_AMD64)
  typedef unsigned long long CUdeviceptr;
#else
  typedef unsigned int CUdeviceptr;
#endif

private:
  typedef void* (*okra_create_context_func_t)();
  typedef void* (*okra_create_kernel_func_t)(void*, unsigned char *, const char *);
  typedef bool (*okra_push_object_func_t)(void*, void*);
  typedef bool (*okra_push_boolean_func_t)(void*, jboolean);
  typedef bool (*okra_push_byte_func_t)(void*, jbyte);
  typedef bool (*okra_push_double_func_t)(void*, jdouble);
  typedef bool (*okra_push_float_func_t)(void*, jfloat);
  typedef bool (*okra_push_int_func_t)(void*, jint);
  typedef bool (*okra_push_long_func_t)(void*, jlong);
  typedef bool (*okra_execute_with_range_func_t)(void*, jint);
  typedef bool (*okra_clearargs_func_t)(void*);
  typedef bool (*okra_register_heap_func_t)(void*, size_t);

public:
  static okra_create_context_func_t             _okra_create_context;
  static okra_create_kernel_func_t              _okra_create_kernel;
  static okra_push_object_func_t                _okra_push_object;
  static okra_push_boolean_func_t               _okra_push_boolean;
  static okra_push_byte_func_t                  _okra_push_byte;
  static okra_push_double_func_t                _okra_push_double;
  static okra_push_float_func_t                 _okra_push_float;
  static okra_push_int_func_t                   _okra_push_int;
  static okra_push_long_func_t                  _okra_push_long;
  static okra_execute_with_range_func_t         _okra_execute_with_range;
  static okra_clearargs_func_t                  _okra_clearargs;
  static okra_register_heap_func_t              _okra_register_heap;
  
protected:
  static void* _device_context;
};
#endif // GPU_HSAIL_HPP
