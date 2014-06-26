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

#ifndef GPU_HSAIL_VM_GPU_HSAIL_HPP
#define GPU_HSAIL_VM_GPU_HSAIL_HPP

#include "runtime/gpu.hpp"
#include "utilities/exceptions.hpp"
#include "graal/graalEnv.hpp"
#include "gpu_hsail_OopMapHelper.hpp"
#include "gpu_hsail_Frame.hpp"
#include "gpu_hsail_Tlab.hpp"

struct HSAILKernelStats {
  int _dispatches;
  int _deopts;
  int _overflows;
  bool _changeSeen;
  
public:
  HSAILKernelStats() {
    _dispatches = _deopts = _overflows = 0;
    _changeSeen = false;
  }
  
  void incDeopts() {
    _deopts++;
    _changeSeen = true;
  }
  void incOverflows() {
    _overflows++;
    _changeSeen = true;
  }
  
  void finishDispatch() {
    _dispatches++;
    if (_changeSeen) {
      // print();
      _changeSeen = false;
    }
  }
  
  void print() {
    tty->print_cr("Disp=%d, Deopts=%d, Ovflows=%d", _dispatches, _deopts, _overflows);
  }
};

class Hsail : public Gpu {

  public:
  class HSAILKernelDeoptimization VALUE_OBJ_CLASS_SPEC {
    friend class VMStructs;
   private:
    // TODO: separate workitemid and actionAndReason out
    // since they are there only once even if there are multiple frames
    // for now, though we only ever have one hsail frame
    jint  _workitemid;
    jint  _actionAndReason;
    // the first (innermost) "hsail frame" starts after the above fields

   public:
    inline jint workitem() { return _workitemid; }
    inline jint reason() { return _actionAndReason; }
    inline jint pc_offset() { return first_frame()->pc_offset(); }
    inline HSAILFrame* first_frame() {
      // starts after the "header" fields
      return (HSAILFrame*) (((jbyte*) this) + sizeof(*this));
    }
  };

// 8 compute units * 40 waves per cu * wavesize 64
// TODO: query the device to get this number
#define MAX_DEOPT_SLOTS    (8 * 40 * 64)


  class HSAILDeoptimizationInfo : public CHeapObj<mtInternal> {
    friend class VMStructs;
   private:
    jint* _notice_safepoints;
    jint _deopt_occurred;
    jint _deopt_next_index;
    jint _num_slots;
    jint _deopt_span;
    jint _deopt_work_index;           // how far we are in processing the deopts
    HSAILTlabInfo** _cur_tlab_info;   // copy of what was in the HSAILAllocationInfo, to avoid an extra indirection
    HSAILAllocationInfo* _alloc_info;
    char _ignore;
    jobject _oop_map_array;
    // keep a pointer last so save area following it is word aligned
    jboolean* _never_ran_array; 

   public:
    // static HSAILKernelStats kernelStats;
    HSAILKernelDeoptimization _deopt_save_states[1];  // number and size of these can vary per kernel

    static inline size_t hdr_size() {
      return sizeof(HSAILDeoptimizationInfo);
    }

    inline jbyte* save_area_start() {
      return (jbyte*) (this) + hdr_size();
    }

    inline HSAILDeoptimizationInfo(int numSlots, int bytesPerSaveArea, int dimX, HSAILAllocationInfo* allocInfo, jobject oop_map_array) {
      _notice_safepoints = &Hsail::_notice_safepoints;
      _deopt_occurred = 0;
      _deopt_next_index = 0;
      _deopt_work_index = 0;
      _num_slots = numSlots;
      _never_ran_array = NEW_C_HEAP_ARRAY(jboolean, dimX, mtInternal);
      memset(_never_ran_array, 0, dimX * sizeof(jboolean));
      _alloc_info = allocInfo;
      _oop_map_array = oop_map_array;
      _deopt_span = sizeof(HSAILKernelDeoptimization) + sizeof(HSAILFrame) + bytesPerSaveArea;
      if (TraceGPUInteraction) {
        tty->print_cr("HSAILDeoptimizationInfo allocated, %d slots of size %d, total size = 0x%lx bytes", _num_slots, _deopt_span, (_num_slots * _deopt_span + sizeof(HSAILDeoptimizationInfo)));
      }
    }

    inline ~HSAILDeoptimizationInfo() {
      FREE_C_HEAP_ARRAY(jboolean, _never_ran_array, mtInternal);
    }

    inline jint deopt_occurred() {
      return _deopt_occurred;
    }
    inline jint num_deopts() { return _deopt_next_index; }
    inline jboolean* never_ran_array() { return _never_ran_array; }
    inline jint num_slots() {return _num_slots;}
    inline void set_deopt_work_index(int val) { _deopt_work_index = val; }
    inline jint deopt_work_index() { return _deopt_work_index; }

    inline HSAILKernelDeoptimization* get_deopt_save_state(int slot) {
      // use _deopt_span to index into _deopt_states
      return (HSAILKernelDeoptimization*) (save_area_start() + _deopt_span * slot);
    }

    void set_cur_tlabInfos(HSAILTlabInfo** ptlabInfos) {
      _cur_tlab_info = ptlabInfos;
    }

    void oops_do(OopClosure* f);

    void* operator new (size_t hdrSize, int numSlots, int bytesPerSaveArea) {
      assert(hdrSize <= hdr_size(), "");
      size_t totalSizeBytes = hdr_size()  + numSlots * (sizeof(HSAILKernelDeoptimization) + sizeof(HSAILFrame) + bytesPerSaveArea);
      return NEW_C_HEAP_ARRAY(char, totalSizeBytes, mtInternal);
    }

    void operator delete (void* ptr) {
      FREE_C_HEAP_ARRAY(char, ptr, mtInternal);
    }
  };

private:

  static JNINativeMethod HSAIL_methods[];

  // static native boolean initialize();
  JNIEXPORT static jboolean initialize(JNIEnv* env, jclass);

  // static native long generateKernel(byte[] targetCode, String name);
  JNIEXPORT static jlong generate_kernel(JNIEnv* env, jclass, jbyteArray code_handle, jstring name_handle);

  // static native boolean executeKernel0(HotSpotInstalledCode kernel, int jobSize, Object[] args);
  JNIEXPORT static jboolean execute_kernel_void_1d(JNIEnv* env, jclass, jobject hotspotInstalledCode, jint dimX, jobject args,
                                                   jobject donorThreads, int allocBytesPerWorkitem, jobject oop_map_array);

  static jboolean execute_kernel_void_1d_internal(address kernel, int dimX, jobject args, methodHandle& mh, nmethod* nm,
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
  typedef void* (*okra_create_kernel_func_t)(void*, unsigned char*, const char*);
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
#endif // GPU_HSAIL_VM_GPU_HSAIL_HPP
