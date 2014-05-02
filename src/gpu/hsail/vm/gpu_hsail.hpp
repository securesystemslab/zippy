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

#include "utilities/exceptions.hpp"
#include "graal/graalEnv.hpp"
#include "gpu_hsail_Frame.hpp"

class Hsail : public Gpu {

  public:
  class HSAILKernelDeoptimization {
    friend class VMStructs;
   private:
    // TODO: separate workitemid and actionAndReason out
    // since they are there only once even if there are multiple frames
    // for now, though we only ever have one hsail fram
    jint  _workitemid;
    jint  _actionAndReason;
    // the first (innermost) "hsail frame" starts here
    HSAILFrame _first_frame;

   public:
    inline jint workitem() { return _workitemid; }
    inline jint reason() { return _actionAndReason; }
    inline jint pc_offset() { return _first_frame.pc_offset(); }
    inline HSAILFrame *first_frame() { return &_first_frame; }
  };

// 8 compute units * 40 waves per cu * wavesize 64
// TODO: query the device to get this number
#define MAX_DEOPT_SLOTS    (8 * 40 * 64)

  class HSAILDeoptimizationInfo : public ResourceObj {
    friend class VMStructs;
   private:
    jint* _notice_safepoints;
    jint _deopt_occurred;
    jint _deopt_next_index;
    JavaThread** _donor_threads;
    jboolean * _never_ran_array;
    jint _num_slots;
    jint _bytesPerSaveArea;
    jint _deopt_span;

   public:
    HSAILKernelDeoptimization _deopt_save_states[1];  // number and size of these can vary per kernel

    inline HSAILDeoptimizationInfo(int numSlots, int bytesPerSaveArea) {
      _notice_safepoints = &Hsail::_notice_safepoints;
      _deopt_occurred = 0;
      _deopt_next_index = 0;
      _num_slots = numSlots;
      _bytesPerSaveArea = bytesPerSaveArea;
      _deopt_span = sizeof(HSAILKernelDeoptimization) + bytesPerSaveArea;
      if (TraceGPUInteraction) {
        tty->print_cr("HSAILDeoptimizationInfo allocated, %d slots of size %d, total size = 0x%lx bytes", _num_slots, _deopt_span, (_num_slots * _deopt_span + sizeof(HSAILDeoptimizationInfo)));
      }
    }

    inline jint deopt_occurred() {
      return _deopt_occurred;
    }
    inline jint num_deopts() { return _deopt_next_index; }
    inline jboolean *never_ran_array() { return _never_ran_array; }
    inline void  set_never_ran_array(jboolean *p) { _never_ran_array = p; }
    inline void  set_donor_threads(JavaThread **threads) { _donor_threads = threads; }
    inline jint num_slots() {return _num_slots;}

    inline HSAILKernelDeoptimization * get_deopt_save_state(int slot) {
      // use _deopt_span to index into _deopt_states
      char *p = (char *) _deopt_save_states;
      p += _deopt_span * slot;
      return (HSAILKernelDeoptimization *) p;
    }

    void * operator new (size_t hdrSize, int numSlots, int bytesPerSaveArea) {
      size_t totalSizeBytes = hdrSize + numSlots * (sizeof(HSAILKernelDeoptimization) + bytesPerSaveArea);
      return NEW_C_HEAP_ARRAY(char, totalSizeBytes, mtInternal);
    }

    void operator delete (void *ptr) {
      FREE_C_HEAP_ARRAY(char, ptr, mtInternal);
    }
  };


private:

  static JNINativeMethod HSAIL_methods[];

  // static native boolean initialize();
  JNIEXPORT static jboolean initialize(JNIEnv *env, jclass);

  // static native long generateKernel(byte[] targetCode, String name);
  JNIEXPORT static jlong generate_kernel(JNIEnv *env, jclass, jbyteArray code_handle, jstring name_handle);

  // static native boolean executeKernel0(HotSpotInstalledCode kernel, int jobSize, Object[] args);
  JNIEXPORT static jboolean execute_kernel_void_1d(JNIEnv *env, jclass, jobject hotspotInstalledCode, jint dimX, jobject args, jobject oopsSave,
                                                   jobject donorThreads, int allocBytesPerWorkitem, jobject oop_map_array);

  // static native void getThreadPointers(Object[] donorThreads, long[] threadPointersOut);
  JNIEXPORT static void get_thread_pointers(JNIEnv *env, jclass, jobject donor_threads_handle, jobject thread_ptrs_handle);

  static void getNewTlabForDonorThread(ThreadLocalAllocBuffer* tlab, size_t tlabMinHsail);

  static jboolean execute_kernel_void_1d_internal(address kernel, int dimX, jobject args, methodHandle& mh, nmethod *nm, jobject oopsSave,
                                                  jobject donorThreads, int allocBytesPerWorkitem, jobject oop_map_array, TRAPS);

  static void register_heap();

  static GraalEnv::CodeInstallResult install_code(Handle& compiled_code, CodeBlob*& cb, Handle installed_code, Handle triggered_deoptimizations);

public:

  // Registers the implementations for the native methods in HSAILHotSpotBackend
  static bool register_natives(JNIEnv* env);

  virtual const char* name() { return "HSAIL"; }

  virtual void notice_safepoints();
  virtual void ignore_safepoints();

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

  // true if safepoints are activated
  static jint _notice_safepoints;
};
#endif // GPU_HSAIL_HPP
