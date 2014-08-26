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
#include "graal/graalRuntime.hpp"
#include "graal/graalJavaAccess.hpp"
#include "hsailKernelArguments.hpp"
#include "hsailJavaCallArguments.hpp"
#include "code/pcDesc.hpp"
#include "code/scopeDesc.hpp"
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
  {CC"executeKernel0",   CC"("HS_INSTALLED_CODE"I["OBJECT"II[I)Z",  FN_PTR(Hsail::execute_kernel_void_1d)},
};

void* Hsail::_device_context = NULL;
jint  Hsail::_notice_safepoints = false;

Hsail::okra_get_context_func_t     Hsail::_okra_get_context;
Hsail::okra_create_kernel_func_t   Hsail::_okra_create_kernel;
Hsail::okra_push_pointer_func_t    Hsail::_okra_push_pointer;
Hsail::okra_push_boolean_func_t    Hsail::_okra_push_boolean;
Hsail::okra_push_byte_func_t       Hsail::_okra_push_byte;
Hsail::okra_push_double_func_t     Hsail::_okra_push_double;
Hsail::okra_push_float_func_t      Hsail::_okra_push_float;
Hsail::okra_push_int_func_t        Hsail::_okra_push_int;
Hsail::okra_push_long_func_t       Hsail::_okra_push_long;
Hsail::okra_execute_kernel_func_t  Hsail::_okra_execute_kernel;
Hsail::okra_clear_args_func_t      Hsail::_okra_clear_args;
Hsail::okra_dispose_kernel_func_t  Hsail::_okra_dispose_kernel;
Hsail::okra_dispose_context_func_t Hsail::_okra_dispose_context;

//static jint in_kernel = 0;

void Hsail::notice_safepoints() {
  _notice_safepoints = true;
//  if (TraceGPUInteraction) {
//    tty->print_cr("[HSAIL] Notice safepoint in_kernel=%d", in_kernel);
//  }
}

void Hsail::ignore_safepoints() {
  _notice_safepoints = false;
}

GPU_VMENTRY(jboolean, Hsail::execute_kernel_void_1d, (JNIEnv* env, jclass, jobject kernel_handle, jint dimX, jobject args,
                                                      jint num_tlabs, jint allocBytesPerWorkitem, jobject oop_map_array))

  ResourceMark rm;
  jlong nmethodValue = InstalledCode::address(kernel_handle);
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

return execute_kernel_void_1d_internal((address) kernel, dimX, args, mh, nm, num_tlabs, allocBytesPerWorkitem, oop_map_array, CHECK_0);
GPU_END

static void showRanges(jboolean* a, int len) {
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

jboolean Hsail::execute_kernel_void_1d_internal(address kernel, int dimX, jobject args, methodHandle& mh, nmethod* nm,
                                                jint num_tlabs, int allocBytesPerWorkitem, jobject oop_map_array, TRAPS) {
  ResourceMark rm(THREAD);
  objArrayOop argsArray = (objArrayOop) JNIHandles::resolve(args);
  // Note this length does not include the iteration variable since it is replaced by the HSA workitemid
  int argsArrayLength = argsArray->length();
  assert(THREAD->is_Java_thread(), "must be a JavaThread");

  // We avoid HSAILAllocationInfo logic if kernel does not allocate
  // in which case the num_tlabs passed in will be 0
  HSAILAllocationInfo* allocInfo = (num_tlabs == 0 ?  NULL : new HSAILAllocationInfo(num_tlabs, dimX, allocBytesPerWorkitem));
  
  // Reset the kernel arguments
  _okra_clear_args(kernel);

  JavaThread* thread = (JavaThread*)THREAD;
  HSAILDeoptimizationInfo* e;
  if (UseHSAILDeoptimization) {
    // get how many bytes per deopt save area are required
    int saveAreaCounts = HSAILOopMapHelper::get_save_area_counts(oop_map_array);
    int numSRegs = saveAreaCounts & 0xff;
    int numDRegs = (saveAreaCounts >> 8) & 0xff;
    int numStackSlots = (saveAreaCounts >> 16);
    int bytesPerSaveArea = numSRegs * 4 + (numDRegs + numStackSlots) * 8;

    e = new (MAX_DEOPT_SLOTS, bytesPerSaveArea) HSAILDeoptimizationInfo(MAX_DEOPT_SLOTS, bytesPerSaveArea, dimX, allocInfo, oop_map_array);
    // copy cur_tlab_infos
    if (allocInfo != NULL) {
      e->set_cur_tlabInfos(allocInfo->getCurTlabInfos());
    }
    // set deopt info in thread so gc oops_do processing can find it
    thread->set_gpu_hsail_deopt_info(e);
  }

  // This object sets up the kernel arguments
  HSAILKernelArguments hka((address) kernel, mh->signature(), argsArray, mh->is_static(), e);
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] range=%d", dimX);
  }

  // If any object passed was null, throw an exception here. Doing this
  // means the kernel code can avoid null checks on the object parameters.
  if (hka.getFirstNullParameterIndex() >= 0) {
    char buf[64];
    sprintf(buf, "Null Kernel Parameter seen, Parameter Index: %d", hka.getFirstNullParameterIndex());
    thread->set_gpu_exception_bci(0);
    thread->set_gpu_exception_method(mh());
    THROW_MSG_0(vmSymbols::java_lang_NullPointerException(), buf);
  }

  // Run the kernel
  jboolean success = false;
  {
    TraceTime t("execute kernel", TraceGPUInteraction);
    graal_okra_range_t kernel_range = {0};

    //in_kernel = 1;
    // Run the kernel
    kernel_range.dimension = 1;
    kernel_range.global_size[0] = dimX;
    success = _okra_execute_kernel(_device_context, kernel, &kernel_range);
    //in_kernel = 0;
  }

  // avoid HSAILAllocationInfo logic if kernel does not allocate
  if (allocInfo != NULL) {
    allocInfo->postKernelCleanup();
  }

  if (UseHSAILDeoptimization) {
    // check if any workitem requested a deopt
    int deoptcode = e->deopt_occurred();
    if (deoptcode != 1) {
      if (deoptcode == 0) {
        if (TraceGPUInteraction && _notice_safepoints != 0) {
          tty->print_cr("[HSAIL] observed safepoint during kernel");
        }
      } else {
        // error condition detected in deopt code
        char msg[200];
        sprintf(msg, "deopt error detected, slot for workitem %d was not empty", -1 * (deoptcode + 1));
        guarantee(deoptcode == 1, msg);
      }
    } else {
      {
        TraceTime t3("handle deoptimizing workitems", TraceGPUInteraction);
        if (TraceGPUInteraction) {
          tty->print_cr("deopt happened.");
          HSAILKernelDeoptimization* pdeopt = e->get_deopt_save_state(0);
          tty->print_cr("first deopter was workitem %d", pdeopt->workitem());
        }

        // Handle any deopting workitems.
        int count_deoptimized = 0;
        for (int k = 0; k < e->num_deopts(); k++) {
          HSAILKernelDeoptimization* pdeopt = e->get_deopt_save_state(k);

          jint workitem = pdeopt->workitem();
          if (workitem != -1) {
            int deoptId = pdeopt->pc_offset();
            HSAILFrame* hsailFrame = pdeopt->first_frame();

            JavaValue result(T_VOID);
            JavaCallArguments javaArgs;
            javaArgs.set_alternative_target(nm);
            javaArgs.push_int(deoptId);
            javaArgs.push_long((jlong) hsailFrame);

            // Override the deoptimization action with Action_none until we decide
            // how to handle the other actions.
            int myActionReason = Deoptimization::make_trap_request(Deoptimization::trap_request_reason(pdeopt->reason()), Deoptimization::Action_none);
            javaArgs.push_int(myActionReason);
            javaArgs.push_oop((oop) NULL);
            if (TraceGPUInteraction) {
              tty->print_cr("[HSAIL] Deoptimizing to host for workitem=%d (slot=%d) with deoptId=%d, frame=" INTPTR_FORMAT ", actionAndReason=%d", workitem, k, deoptId, hsailFrame, myActionReason);
              // show the $d registers or stack slots containing references
              int maxOopBits = hsailFrame->num_d_regs() + hsailFrame->num_stack_slots();
              HSAILOopMapHelper oopMapHelper(oop_map_array);
              int pc_offset = hsailFrame->pc_offset();
              for (int bit = 0; bit < maxOopBits; bit++) {
                if (oopMapHelper.is_oop(pc_offset, bit)) {
                  if (bit < hsailFrame->num_d_regs()) {
                    // show $d reg oop
                    tty->print_cr("  oop $d%d = %p", bit, hsailFrame->get_oop_for_bit(bit));
                  } else {
                    // show stack slot oop
                    int stackOffset = (bit - hsailFrame->num_d_regs()) * 8;  // 8 bytes per stack slot
                    tty->print_cr("  oop stk:%d = %p", stackOffset, hsailFrame->get_oop_for_bit(bit));
                  }
                }
              }
            }
            JavaCalls::call(&result, mh, &javaArgs, THREAD);
            count_deoptimized++;
            e->set_deopt_work_index(k + 1);
          }
        }
        if (TraceGPUInteraction) {
          tty->print_cr("[HSAIL] Deoptimizing to host completed for %d workitems", count_deoptimized);
        }
      }
    }
    // when we are done with the deopts, we don't need to oops_do anything
    // in the saved state anymore
    thread->set_gpu_hsail_deopt_info(NULL);  

    // Handle any never_ran workitems if there were any
    {
      TraceTime t("handle never-rans ", TraceGPUInteraction);
      int count_never_ran = 0;
      bool handleNeverRansHere = true;
      // turn off verbose trace stuff for javacall arg setup
      bool savedTraceGPUInteraction = TraceGPUInteraction;
      TraceGPUInteraction = false;
      jboolean* never_ran_array = e->never_ran_array();
      if (handleNeverRansHere) {
        for (int k = 0; k < dimX; k++) {
          if (never_ran_array[k]) {
            // run it as a javaCall
            KlassHandle methKlass = mh->method_holder();
            Thread* THREAD = Thread::current();
            JavaValue result(T_VOID);
            // Add the iteration variable to the HSA args length
            JavaCallArguments javaArgs(argsArrayLength + 1);
            // re-resolve the args_handle here
            objArrayOop resolvedArgsArray = (objArrayOop) JNIHandles::resolve(args);

            // This object sets up the javaCall arguments. The way
            // argsArray is set up, this should work for instance
            // methods as well (the receiver will be the first oop pushed)
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
        if (TraceGPUInteraction && (count_never_ran > 0)) {
          tty->print_cr("%d workitems never ran, have been run via JavaCall", count_never_ran);
          showRanges(never_ran_array, dimX);
        }
      } // end of never-ran handling
    }

    delete e;
    delete allocInfo;
  }
  return success;
}

GPU_ENTRY(jlong, Hsail::generate_kernel, (JNIEnv* env, jclass, jbyteArray code_handle, jstring name_handle))
  guarantee(_okra_create_kernel != NULL, "[HSAIL] Okra not linked");
  ResourceMark rm;
  jsize name_len = env->GetStringLength(name_handle);
  jsize code_len = env->GetArrayLength(code_handle);

  char* name = NEW_RESOURCE_ARRAY(char, name_len + 1);
  unsigned char* code = NEW_RESOURCE_ARRAY(unsigned char, code_len + 1);

  code[code_len] = 0;
  name[name_len] = 0;

  env->GetByteArrayRegion(code_handle, 0, code_len, (jbyte*) code);
  env->GetStringUTFRegion(name_handle, 0, name_len, name);

  // The kernel entrypoint is always run for the time being  
  const char* entryPointName = "&run";
  jlong okra_kernel;
  {
    TraceTime t("generate kernel ", TraceGPUInteraction);
    jint okra_status = _okra_create_kernel(_device_context, code, entryPointName, (void**)&okra_kernel);
    guarantee(okra_status==0, "_okra_create_kernel failed");
  }
  return (jlong) okra_kernel;
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
    CAST_TO_FN_PTR(alias##_func_t, os::dll_lookup(okra_lib_handle, STRINGIFY(name))); \
  if (_##alias == NULL) {      \
  tty->print_cr("[HSAIL] ***** Error: Failed to lookup %s in %s, wrong version of OKRA?", STRINGIFY(name), okra_library_name); \
        return false; \
  } \

GPU_ENTRY(jboolean, Hsail::initialize, (JNIEnv* env, jclass))
  if (okra_library_name == NULL) {
    if (TraceGPUInteraction) {
      tty->print_cr("Unsupported HSAIL platform");
    }
    return false;
  }

  // here we know we have a valid okra_library_name to try to load
  char ebuf[O_BUFLEN];
  char* okra_lib_name_from_env_var = getenv("_OKRA_SIM_LIB_PATH_");
  if (okra_lib_name_from_env_var != NULL) {
    okra_library_name = okra_lib_name_from_env_var;
  }
  if (TraceGPUInteraction) {
    tty->print_cr("[HSAIL] library is %s", okra_library_name);
  }
  void* okra_lib_handle = NULL;
#if defined(LINUX)
  // Check first if the Okra library is already loaded.
  // TODO: Figure out how to do this on other OSes.
  okra_lib_handle = ::dlopen(okra_library_name, RTLD_LAZY | RTLD_NOLOAD);
#endif
  // If Okra library is not already loaded, load it here
  if (okra_lib_handle == NULL) {
    okra_lib_handle = os::dll_load(okra_library_name, ebuf, O_BUFLEN);
  }  
  if (okra_lib_handle == NULL) {
    // Unable to open Okra library
    if (TraceGPUInteraction) {
      tty->print_cr("[HSAIL] library load failed.");
    }
    return false;
  }
  
  guarantee(_okra_get_context == NULL, "cannot repeat GPU initialization");

  // At this point we know  okra_lib_handle is valid whether we loaded
  // here or earlier.  In either case, we can lookup the functions.
  LOOKUP_OKRA_FUNCTION(okra_get_context, okra_get_context);
  LOOKUP_OKRA_FUNCTION(okra_create_kernel, okra_create_kernel);
  LOOKUP_OKRA_FUNCTION(okra_push_pointer, okra_push_pointer);
  LOOKUP_OKRA_FUNCTION(okra_push_boolean, okra_push_boolean);
  LOOKUP_OKRA_FUNCTION(okra_push_byte, okra_push_byte);
  LOOKUP_OKRA_FUNCTION(okra_push_double, okra_push_double);
  LOOKUP_OKRA_FUNCTION(okra_push_float, okra_push_float);
  LOOKUP_OKRA_FUNCTION(okra_push_int, okra_push_int);
  LOOKUP_OKRA_FUNCTION(okra_push_long, okra_push_long);
  LOOKUP_OKRA_FUNCTION(okra_execute_kernel, okra_execute_kernel);
  LOOKUP_OKRA_FUNCTION(okra_clear_args, okra_clear_args);
  LOOKUP_OKRA_FUNCTION(okra_dispose_kernel, okra_dispose_kernel);
  LOOKUP_OKRA_FUNCTION(okra_dispose_context, okra_dispose_context);
  // if we made it this far, real success

  Gpu::initialized_gpu(new Hsail());

    // There is 1 context per process
  jint result = _okra_get_context(&_device_context);
  guarantee(result==0, "get context failed");

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


void Hsail::HSAILDeoptimizationInfo::oops_do(OopClosure* f) {
  int unprocessed_deopts = num_deopts() - deopt_work_index();
  if (TraceGPUInteraction) {
    tty->print_cr("HSAILDeoptimizationInfo::oops_do deopt_occurred=%d, total_deopts=%d, unprocessed_deopts=%d, oop_map_array=%p", _deopt_occurred, num_deopts(), unprocessed_deopts, _oop_map_array);
  }
  if (num_deopts() == 0 || unprocessed_deopts <= 0) {
    return; // nothing to do
  }
  HSAILOopMapHelper oopMapHelper(_oop_map_array);
  oopMapHelper.resolve_arrays();  // resolve once before processing

  // go thru the unprocessed deopt frames, finding each oop and applying the closre
  for (int k = deopt_work_index(); k < num_deopts(); k++) {
    HSAILKernelDeoptimization* pdeopt = get_deopt_save_state(k);
    assert (pdeopt->workitem() >= 0, "bad workitem in deopt");
    if (TraceGPUInteraction) {
      tty->print_cr("  deopt %d, workitem %d, pc %d", k, pdeopt->workitem(), pdeopt->pc_offset());
    }
    HSAILFrame* hsailFrame = pdeopt->first_frame();
    hsailFrame->oops_do(f, &oopMapHelper);
  }
}
