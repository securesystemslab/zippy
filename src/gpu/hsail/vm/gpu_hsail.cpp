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
#include "runtime/deoptimization.hpp"
#include "gpu_hsail.hpp"
#include "utilities/debug.hpp"
#include "utilities/exceptions.hpp"
#include "hsail/vm/gpu_hsail.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/ostream.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalJavaAccess.hpp"
#include "hsailKernelArguments.hpp"
#include "hsailJavaCallArguments.hpp"
#include "code/pcDesc.hpp"
#include "code/scopeDesc.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "gpu_hsail_Frame.hpp"

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
#define JLTHREAD              "Ljava/lang/Thread;"
#define HS_INSTALLED_CODE     "Lcom/oracle/graal/hotspot/meta/HotSpotInstalledCode;"
#define HS_COMPILED_NMETHOD   "Lcom/oracle/graal/hotspot/HotSpotCompiledNmethod;"
#define HS_NMETHOD            "Lcom/oracle/graal/hotspot/meta/HotSpotNmethod;"

JNINativeMethod Hsail::HSAIL_methods[] = {
  {CC"initialize",       CC"()Z",                               FN_PTR(Hsail::initialize)},
  {CC"generateKernel",   CC"([B" STRING ")J",                   FN_PTR(Hsail::generate_kernel)},
  {CC"executeKernel0",   CC"("HS_INSTALLED_CODE"I["OBJECT"["OBJECT"["JLTHREAD"I)Z",  FN_PTR(Hsail::execute_kernel_void_1d)},
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

struct Stats {
  int _dispatches;
  int _deopts;
  int _overflows;
  bool _changeSeen;

public:
  Stats() {
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

static Stats kernelStats;


void Hsail::register_heap() {
  // After the okra functions are set up and the heap is initialized, register the java heap with HSA
  guarantee(Universe::heap() != NULL, "heap should be there by now.");
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] heap=" PTR_FORMAT, Universe::heap());
    tty->print_cr("[HSAIL] base=0x%08x, capacity=%ld", Universe::heap()->base(), Universe::heap()->capacity());
  }
  _okra_register_heap(Universe::heap()->base(), Universe::heap()->capacity());
}

GPU_VMENTRY(jboolean, Hsail::execute_kernel_void_1d, (JNIEnv* env, jclass, jobject kernel_handle, jint dimX, jobject args, jobject oops_save,
                                                      jobject donor_threads, jint allocBytesPerWorkitem))

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

  return execute_kernel_void_1d_internal((address) kernel, dimX, args, mh, nm, oops_save, donor_threads, allocBytesPerWorkitem, CHECK_0);
GPU_END

static void showRanges(jboolean *a, int len) {
  // show ranges
  bool lookFor = true;
  for (int i = 0; i < len; i++) {
    if ((lookFor == true) && (a[i] != 0)) {
      tty->print("%d", i);
      lookFor = false;
    } else if ((lookFor == false) && (a[i] == 0)) {
      tty->print_cr("-%d", i-1);
      lookFor = true;
    }
  }
  if (lookFor == false) {
    tty->print_cr("-%d", len-1);
  }
}

// fill and retire old tlab and get a new one
// if we can't get one, no problem someone will eventually do a gc
void Hsail::getNewTlabForDonorThread(ThreadLocalAllocBuffer* tlab, size_t tlabMinHsail) {
  tlab->clear_before_allocation();    // fill and retire old tlab (will also check for null)

  // get a size for a new tlab that is at least tlabMinHsail.
  size_t new_tlab_size = tlab->compute_size(tlabMinHsail);
  if (new_tlab_size == 0) return;

  HeapWord* tlab_start = Universe::heap()->allocate_new_tlab(new_tlab_size);
  if (tlab_start == NULL) return;

  // ..and clear it if required
  if (ZeroTLAB) {
    Copy::zero_to_words(tlab_start, new_tlab_size);
  }
  // and init the tlab pointers
  tlab->fill(tlab_start, tlab_start, new_tlab_size);
}

static void printTlabInfo (ThreadLocalAllocBuffer* tlab) {
  HeapWord *start = tlab->start();
  HeapWord *top = tlab->top();
  HeapWord *end = tlab->end();
  // sizes are in bytes
  size_t tlabFree = tlab->free() * HeapWordSize;
  size_t tlabUsed = tlab->used() * HeapWordSize;
  size_t tlabSize = tlabFree + tlabUsed;
  double freePct = 100.0 * (double) tlabFree/(double) tlabSize;
  tty->print_cr("(%p, %p, %p), siz=%ld, free=%ld (%f%%)", start, top, end, tlabSize, tlabFree, freePct);
}



jboolean Hsail::execute_kernel_void_1d_internal(address kernel, int dimX, jobject args, methodHandle& mh, nmethod *nm, jobject oops_save,
                                                jobject donor_threads, int allocBytesPerWorkitem, TRAPS) {
  ResourceMark rm(THREAD);
  objArrayOop argsArray = (objArrayOop) JNIHandles::resolve(args);

  // TODO: avoid donor thread logic if kernel does not allocate
  objArrayOop donorThreadObjects = (objArrayOop) JNIHandles::resolve(donor_threads);
  int numDonorThreads = donorThreadObjects->length();
  guarantee(numDonorThreads > 0, "need at least one donor thread");
  JavaThread** donorThreads = NEW_RESOURCE_ARRAY(JavaThread*, numDonorThreads);
  for (int i = 0; i < numDonorThreads; i++) {
    donorThreads[i] = java_lang_Thread::thread(donorThreadObjects->obj_at(i));
  }


  // compute tlabMinHsail based on number of workitems, number of donor
  // threads, allocBytesPerWorkitem rounded up
  size_t tlabMinHsail = (allocBytesPerWorkitem * dimX + (numDonorThreads - 1)) / numDonorThreads;
  if (TraceGPUInteraction) {
    tty->print_cr("computed tlabMinHsail = %d", tlabMinHsail);
  }

  for (int i = 0; i < numDonorThreads; i++) {
    JavaThread* donorThread = donorThreads[i];
    ThreadLocalAllocBuffer* tlab = &donorThread->tlab();
    if (TraceGPUInteraction) {
      tty->print("donorThread %d, is %p, tlab at %p -> ", i, donorThread, tlab);
      printTlabInfo(tlab);
    }

    // note: this used vs. free limit checking should be based on some
    // heuristic where we see how much this kernel tends to allocate
    if ((tlab->end() == NULL) || (tlab->free() * HeapWordSize < tlabMinHsail)) {
      getNewTlabForDonorThread(tlab, tlabMinHsail);
      if (TraceGPUInteraction) {
        tty->print("donorThread %d, refilled tlab, -> ", i);
        printTlabInfo(tlab);
      }
    }
  }

  // Reset the kernel arguments
  _okra_clearargs(kernel);

  HSAILDeoptimizationInfo* e;
  if (UseHSAILDeoptimization) {
    e = new (ResourceObj::C_HEAP, mtInternal) HSAILDeoptimizationInfo();
    e->set_never_ran_array(NEW_C_HEAP_ARRAY(jboolean, dimX, mtInternal));
    memset(e->never_ran_array(), 0, dimX * sizeof(jboolean));
    e->set_donor_threads(donorThreads);
  }

  // This object sets up the kernel arguments
  HSAILKernelArguments hka((address) kernel, mh->signature(), argsArray, mh->is_static(), e);

  // if any object passed was null, throw an exception here
  // doing this means the kernel code can avoid null checks on the object parameters.
  if (hka.getFirstNullParameterIndex() >= 0) {
    char buf[64];
    sprintf(buf, "Null Kernel Parameter seen, Parameter Index: %d", hka.getFirstNullParameterIndex());
    JavaThread* thread = (JavaThread*)THREAD;
    thread->set_gpu_exception_bci(0);
    thread->set_gpu_exception_method(mh());
    THROW_MSG_0(vmSymbols::java_lang_NullPointerException(), buf);
  }

  // Run the kernel
  bool success = false;
  {
    TraceTime t1("execute kernel", TraceGPUInteraction);
    success = _okra_execute_with_range(kernel, dimX);
  }

  // fix up any tlab tops that overflowed
  bool anyOverflows = false;
  for (int i = 0; i < numDonorThreads; i++) {
    JavaThread * donorThread = donorThreads[i];
    ThreadLocalAllocBuffer* tlab = &donorThread->tlab();
    if (tlab->top() > tlab->end()) {
      anyOverflows = true;
      long overflowAmount = (long) tlab->top() - (long) tlab->pf_top(); 
      // tlab->set_top is private this ugly hack gets around that
      *(long *)((char *)tlab + in_bytes(tlab->top_offset())) = (long) tlab->pf_top();
      if (TraceGPUInteraction) {
        tty->print_cr("donorThread %d at %p overflowed by %ld bytes, setting last good top to %p", i, donorThread, overflowAmount, tlab->top());
      }
    }
  }
  if (anyOverflows) {
    kernelStats.incOverflows();
  }

  if (UseHSAILDeoptimization) {
    // check if any workitem requested a deopt
    // currently we only support at most one such workitem
    int deoptcode = e->deopt_occurred();
    if (deoptcode != 0) {
      if (deoptcode != 1) {
        // error condition detected in deopt code
        char msg[200];
        sprintf(msg, "deopt error detected, slot for workitem %d was not empty", -1 * (deoptcode + 1));
        guarantee(deoptcode == 1, msg);
      }
      kernelStats.incDeopts();
      {
        TraceTime t3("handle deoptimizing workitems", TraceGPUInteraction);
        if (TraceGPUInteraction) {
          tty->print_cr("deopt happened.");
          HSAILKernelDeoptimization * pdeopt = &e->_deopt_save_states[0];
          tty->print_cr("first deopter was workitem %d", pdeopt->workitem());
        }

        // Before handling any deopting workitems, save the pointers from
        // the hsail frames in oops_save so they get adjusted by any
        // GC. Need to do this before leaving thread_in_vm mode.
        // resolve handle only needed once here (not exiting vm mode)
        objArrayOop oopsSaveArray = (objArrayOop) JNIHandles::resolve(oops_save);

        // since slots are allocated from the beginning, we know how far to look
        assert(e->num_deopts() < MAX_DEOPT_SAVE_STATES_SIZE, "deopt save state overflow");
        for (int k = 0; k < e->num_deopts(); k++) {
          HSAILKernelDeoptimization * pdeopt = &e->_deopt_save_states[k];
          jint workitem = pdeopt->workitem();
          if (workitem != -1) {
            // this is a workitem that deopted
            HSAILFrame *hsailFrame = pdeopt->first_frame();
            int dregOopMap = hsailFrame->dreg_oops_map();
            for (int bit = 0; bit < 16; bit++) {
              if ((dregOopMap & (1 << bit)) != 0) {
                // the dregister at this bit is an oop, save it in the array
                int index = k * 16 + bit;
                void* saved_oop = (void*) hsailFrame->get_d_reg(bit);
                oopsSaveArray->obj_at_put(index, (oop) saved_oop);
              }
            }
          }
        }

        // Handle any deopting workitems.
        int count_deoptimized = 0;
        for (int k = 0; k < e->num_deopts(); k++) {
          HSAILKernelDeoptimization * pdeopt = &e->_deopt_save_states[k];

          jint workitem = pdeopt->workitem();
          if (workitem != -1) {
            int deoptId = pdeopt->pc_offset();
            HSAILFrame *hsailFrame = pdeopt->first_frame();

            // update the hsailFrame from the oopsSaveArray
            // re-resolve the handle
            oopsSaveArray = (objArrayOop) JNIHandles::resolve(oops_save);

            int dregOopMap = hsailFrame->dreg_oops_map();
            for (int bit = 0; bit < 16; bit++) {
              if ((dregOopMap & (1 << bit)) != 0) {
                // the dregister at this bit is an oop, retrieve it from array and put back in frame
                int index = k * 16 + bit;
                void * dregValue = (void *) oopsSaveArray->obj_at(index);
                void * oldDregValue = (void *) hsailFrame->get_d_reg(bit);
                assert((oldDregValue != 0 ? dregValue != 0 : dregValue == 0), "bad dregValue retrieved");
                if (TraceGPUInteraction) {
                  if (dregValue != oldDregValue) {
                    tty->print_cr("oop moved for $d%d, workitem %d, slot %d, old=%p, new=%p", bit, workitem, k, oldDregValue, dregValue);
                  }
                }
                hsailFrame->put_d_reg(bit, (jlong) dregValue);
              }
            }

            JavaValue result(T_VOID);
            JavaCallArguments javaArgs;
            javaArgs.set_alternative_target(nm);
            javaArgs.push_int(deoptId);
            javaArgs.push_long((jlong) hsailFrame);

            // override the deoptimization action with Action_none until we decide
            // how to handle the other actions.
            int myActionReason = Deoptimization::make_trap_request(Deoptimization::trap_request_reason(pdeopt->reason()), Deoptimization::Action_none);
            javaArgs.push_int(myActionReason);
            javaArgs.push_oop((oop) NULL);
            if (TraceGPUInteraction) {
              int dregOopMap = hsailFrame->dreg_oops_map();
              tty->print_cr("[HSAIL] Deoptimizing to host for workitem=%d (slot=%d) with deoptId=%d, frame=" INTPTR_FORMAT ", actionAndReason=%d, dregOopMap=%04x", workitem, k, deoptId, hsailFrame, myActionReason, dregOopMap);
              // show the registers containing references
              for (int bit = 0; bit < 16; bit++) {
                if ((dregOopMap & (1 << bit)) != 0) {
                  tty->print_cr("  oop $d%d = %p", bit, hsailFrame->get_d_reg(bit));
                }
              }
            }
            JavaCalls::call(&result, mh, &javaArgs, THREAD);
            count_deoptimized++;
          }
        }
        if (TraceGPUInteraction) {
          tty->print_cr("[HSAIL] Deoptimizing to host completed for %d workitems", count_deoptimized);
        }
      }

      {
        TraceTime t3("handle never-rans", TraceGPUInteraction);

        // Handle any never_ran workitems if there were any
        int count_never_ran = 0;
        bool handleNeverRansHere = true;
        // turn off verbose trace stuff for javacall arg setup
        bool savedTraceGPUInteraction = TraceGPUInteraction;
        TraceGPUInteraction = false;
        jboolean *never_ran_array = e->never_ran_array();
        if (handleNeverRansHere) {
          for (int k = 0; k < dimX; k++) {
            if (never_ran_array[k]) {
              // run it as a javaCall
              KlassHandle methKlass = mh->method_holder();
              Thread* THREAD = Thread::current();
              JavaValue result(T_VOID);
              JavaCallArguments javaArgs;
              // re-resolve the args_handle here
              objArrayOop resolvedArgsArray = (objArrayOop) JNIHandles::resolve(args);
              // This object sets up the javaCall arguments
              // the way argsArray is set up, this should work for instance methods as well
              // (the receiver will be the first oop pushed)
              HSAILJavaCallArguments hjca(&javaArgs, k, mh->signature(), resolvedArgsArray, mh->is_static());
              if (mh->is_static()) {
                JavaCalls::call_static(&result, methKlass, mh->name(), mh->signature(), &javaArgs, THREAD);
              } else {
                JavaCalls::call_virtual(&result, methKlass, mh->name(), mh->signature(), &javaArgs, THREAD);
              }
              count_never_ran++;
            }
          }
          TraceGPUInteraction = savedTraceGPUInteraction;
          if (TraceGPUInteraction) {
            tty->print_cr("%d workitems never ran, have been run via JavaCall", count_never_ran);
            showRanges(never_ran_array, dimX);
          }
        } // end of never-ran handling
      }
    }
    
    FREE_C_HEAP_ARRAY(jboolean, e->never_ran_array(), mtInternal);
    delete e;
  }
  kernelStats.finishDispatch();
  return success;
}

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
