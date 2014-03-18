/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
 */

#include "precompiled.hpp"
#include "asm/codeBuffer.hpp"
#include "graal/graalRuntime.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "memory/oopFactory.hpp"
#include "prims/jvm.h"
#include "runtime/biasedLocking.hpp"
#include "runtime/interfaceSupport.hpp"
#include "runtime/reflection.hpp"
#include "utilities/debug.hpp"

// Simple helper to see if the caller of a runtime stub which
// entered the VM has been deoptimized

static bool caller_is_deopted() {
  JavaThread* thread = JavaThread::current();
  RegisterMap reg_map(thread, false);
  frame runtime_frame = thread->last_frame();
  frame caller_frame = runtime_frame.sender(&reg_map);
  assert(caller_frame.is_compiled_frame(), "must be compiled");
  return caller_frame.is_deoptimized_frame();
}

// Stress deoptimization
static void deopt_caller() {
  if ( !caller_is_deopted()) {
    JavaThread* thread = JavaThread::current();
    RegisterMap reg_map(thread, false);
    frame runtime_frame = thread->last_frame();
    frame caller_frame = runtime_frame.sender(&reg_map);
    Deoptimization::deoptimize_frame(thread, caller_frame.id(), Deoptimization::Reason_constraint);
    assert(caller_is_deopted(), "Must be deoptimized");
  }
}

JRT_BLOCK_ENTRY(void, GraalRuntime::new_instance(JavaThread* thread, Klass* klass))
  JRT_BLOCK;
  assert(klass->is_klass(), "not a class");
  instanceKlassHandle h(thread, klass);
  h->check_valid_for_instantiation(true, CHECK);
  // make sure klass is initialized
  h->initialize(CHECK);
  // allocate instance and return via TLS
  oop obj = h->allocate_instance(CHECK);
  thread->set_vm_result(obj);
  JRT_BLOCK_END;

  if (GraalDeferredInitBarriers) {
    new_store_pre_barrier(thread);
  }
JRT_END

JRT_BLOCK_ENTRY(void, GraalRuntime::new_array(JavaThread* thread, Klass* array_klass, jint length))
  JRT_BLOCK;
  // Note: no handle for klass needed since they are not used
  //       anymore after new_objArray() and no GC can happen before.
  //       (This may have to change if this code changes!)
  assert(array_klass->is_klass(), "not a class");
  oop obj;
  if (array_klass->oop_is_typeArray()) {
    BasicType elt_type = TypeArrayKlass::cast(array_klass)->element_type();
    obj = oopFactory::new_typeArray(elt_type, length, CHECK);
  } else {
    Klass* elem_klass = ObjArrayKlass::cast(array_klass)->element_klass();
    obj = oopFactory::new_objArray(elem_klass, length, CHECK);
  }
  thread->set_vm_result(obj);
  // This is pretty rare but this runtime patch is stressful to deoptimization
  // if we deoptimize here so force a deopt to stress the path.
  if (DeoptimizeALot) {
    static int deopts = 0;
    // Alternate between deoptimizing and raising an error (which will also cause a deopt)
    if (deopts++ % 2 == 0) {
      ResourceMark rm(THREAD);
      THROW(vmSymbols::java_lang_OutOfMemoryError());
    } else {
      deopt_caller();
    }
  }
  JRT_BLOCK_END;

  if (GraalDeferredInitBarriers) {
    new_store_pre_barrier(thread);
  }
JRT_END

void GraalRuntime::new_store_pre_barrier(JavaThread* thread) {
  // After any safepoint, just before going back to compiled code,
  // we inform the GC that we will be doing initializing writes to
  // this object in the future without emitting card-marks, so
  // GC may take any compensating steps.
  // NOTE: Keep this code consistent with GraphKit::store_barrier.

  oop new_obj = thread->vm_result();
  if (new_obj == NULL)  return;

  assert(Universe::heap()->can_elide_tlab_store_barriers(),
         "compiler must check this first");
  // GC may decide to give back a safer copy of new_obj.
  new_obj = Universe::heap()->new_store_pre_barrier(thread, new_obj);
  thread->set_vm_result(new_obj);
}

JRT_ENTRY(void, GraalRuntime::new_multi_array(JavaThread* thread, Klass* klass, int rank, jint* dims))
  assert(klass->is_klass(), "not a class");
  assert(rank >= 1, "rank must be nonzero");
  oop obj = ArrayKlass::cast(klass)->multi_allocate(rank, dims, CHECK);
  thread->set_vm_result(obj);
JRT_END

JRT_ENTRY(void, GraalRuntime::dynamic_new_array(JavaThread* thread, oopDesc* element_mirror, jint length))
  oop obj = Reflection::reflect_new_array(element_mirror, length, CHECK);
  thread->set_vm_result(obj);
JRT_END

JRT_ENTRY(void, GraalRuntime::dynamic_new_instance(JavaThread* thread, oopDesc* type_mirror))
  instanceKlassHandle klass(THREAD, java_lang_Class::as_Klass(type_mirror));

  if (klass == NULL) {
    ResourceMark rm(THREAD);
    THROW(vmSymbols::java_lang_InstantiationException());
  }

  // Create new instance (the receiver)
  klass->check_valid_for_instantiation(false, CHECK);

  // Make sure klass gets initialized
  klass->initialize(CHECK);

  oop obj = klass->allocate_instance(CHECK);
  thread->set_vm_result(obj);
JRT_END

extern void vm_exit(int code);

// Enter this method from compiled code handler below. This is where we transition
// to VM mode. This is done as a helper routine so that the method called directly
// from compiled code does not have to transition to VM. This allows the entry
// method to see if the nmethod that we have just looked up a handler for has
// been deoptimized while we were in the vm. This simplifies the assembly code
// cpu directories.
//
// We are entering here from exception stub (via the entry method below)
// If there is a compiled exception handler in this method, we will continue there;
// otherwise we will unwind the stack and continue at the caller of top frame method
// Note: we enter in Java using a special JRT wrapper. This wrapper allows us to
// control the area where we can allow a safepoint. After we exit the safepoint area we can
// check to see if the handler we are going to return is now in a nmethod that has
// been deoptimized. If that is the case we return the deopt blob
// unpack_with_exception entry instead. This makes life for the exception blob easier
// because making that same check and diverting is painful from assembly language.
JRT_ENTRY_NO_ASYNC(static address, exception_handler_for_pc_helper(JavaThread* thread, oopDesc* ex, address pc, nmethod*& nm))
  // Reset method handle flag.
  thread->set_is_method_handle_return(false);

  Handle exception(thread, ex);
  nm = CodeCache::find_nmethod(pc);
  assert(nm != NULL, "this is not an nmethod");
  // Adjust the pc as needed/
  if (nm->is_deopt_pc(pc)) {
    RegisterMap map(thread, false);
    frame exception_frame = thread->last_frame().sender(&map);
    // if the frame isn't deopted then pc must not correspond to the caller of last_frame
    assert(exception_frame.is_deoptimized_frame(), "must be deopted");
    pc = exception_frame.pc();
  }
#ifdef ASSERT
  assert(exception.not_null(), "NULL exceptions should be handled by throw_exception");
  assert(exception->is_oop(), "just checking");
  // Check that exception is a subclass of Throwable, otherwise we have a VerifyError
  if (!(exception->is_a(SystemDictionary::Throwable_klass()))) {
    if (ExitVMOnVerifyError) vm_exit(-1);
    ShouldNotReachHere();
  }
#endif

  // Check the stack guard pages and reenable them if necessary and there is
  // enough space on the stack to do so.  Use fast exceptions only if the guard
  // pages are enabled.
  bool guard_pages_enabled = thread->stack_yellow_zone_enabled();
  if (!guard_pages_enabled) guard_pages_enabled = thread->reguard_stack();

  if (JvmtiExport::can_post_on_exceptions()) {
    // To ensure correct notification of exception catches and throws
    // we have to deoptimize here.  If we attempted to notify the
    // catches and throws during this exception lookup it's possible
    // we could deoptimize on the way out of the VM and end back in
    // the interpreter at the throw site.  This would result in double
    // notifications since the interpreter would also notify about
    // these same catches and throws as it unwound the frame.

    RegisterMap reg_map(thread);
    frame stub_frame = thread->last_frame();
    frame caller_frame = stub_frame.sender(&reg_map);

    // We don't really want to deoptimize the nmethod itself since we
    // can actually continue in the exception handler ourselves but I
    // don't see an easy way to have the desired effect.
    Deoptimization::deoptimize_frame(thread, caller_frame.id(), Deoptimization::Reason_constraint);
    assert(caller_is_deopted(), "Must be deoptimized");

    return SharedRuntime::deopt_blob()->unpack_with_exception_in_tls();
  }

  // ExceptionCache is used only for exceptions at call sites and not for implicit exceptions
  if (guard_pages_enabled) {
    address fast_continuation = nm->handler_for_exception_and_pc(exception, pc);
    if (fast_continuation != NULL) {
      // Set flag if return address is a method handle call site.
      thread->set_is_method_handle_return(nm->is_method_handle_return(pc));
      return fast_continuation;
    }
  }

  // If the stack guard pages are enabled, check whether there is a handler in
  // the current method.  Otherwise (guard pages disabled), force an unwind and
  // skip the exception cache update (i.e., just leave continuation==NULL).
  address continuation = NULL;
  if (guard_pages_enabled) {

    // New exception handling mechanism can support inlined methods
    // with exception handlers since the mappings are from PC to PC

    // debugging support
    // tracing
    if (TraceExceptions) {
      ttyLocker ttyl;
      ResourceMark rm;
      int offset = pc - nm->code_begin();
      tty->print_cr("Exception <%s> (0x%x) thrown in compiled method <%s> at PC " PTR_FORMAT " [" PTR_FORMAT "+%d] for thread 0x%x",
                    exception->print_value_string(), (address)exception(), nm->method()->print_value_string(), pc, nm->code_begin(), offset, thread);
    }
    // for AbortVMOnException flag
    NOT_PRODUCT(Exceptions::debug_check_abort(exception));

    // Clear out the exception oop and pc since looking up an
    // exception handler can cause class loading, which might throw an
    // exception and those fields are expected to be clear during
    // normal bytecode execution.
    thread->set_exception_oop(NULL);
    thread->set_exception_pc(NULL);

    continuation = SharedRuntime::compute_compiled_exc_handler(nm, pc, exception, false, false);
    // If an exception was thrown during exception dispatch, the exception oop may have changed
    thread->set_exception_oop(exception());
    thread->set_exception_pc(pc);

    // the exception cache is used only by non-implicit exceptions
    if (continuation != NULL && !SharedRuntime::deopt_blob()->contains(continuation)) {
      nm->add_handler_for_exception_and_pc(exception, pc, continuation);
    }
  }

  // Set flag if return address is a method handle call site.
  thread->set_is_method_handle_return(nm->is_method_handle_return(pc));

  if (TraceExceptions) {
    ttyLocker ttyl;
    ResourceMark rm;
    tty->print_cr("Thread " PTR_FORMAT " continuing at PC " PTR_FORMAT " for exception thrown at PC " PTR_FORMAT,
                  thread, continuation, pc);
  }

  return continuation;
JRT_END

// Enter this method from compiled code only if there is a Java exception handler
// in the method handling the exception.
// We are entering here from exception stub. We don't do a normal VM transition here.
// We do it in a helper. This is so we can check to see if the nmethod we have just
// searched for an exception handler has been deoptimized in the meantime.
address GraalRuntime::exception_handler_for_pc(JavaThread* thread) {
  oop exception = thread->exception_oop();
  address pc = thread->exception_pc();
  // Still in Java mode
  DEBUG_ONLY(ResetNoHandleMark rnhm);
  nmethod* nm = NULL;
  address continuation = NULL;
  {
    // Enter VM mode by calling the helper
    ResetNoHandleMark rnhm;
    continuation = exception_handler_for_pc_helper(thread, exception, pc, nm);
  }
  // Back in JAVA, use no oops DON'T safepoint

  // Now check to see if the nmethod we were called from is now deoptimized.
  // If so we must return to the deopt blob and deoptimize the nmethod
  if (nm != NULL && caller_is_deopted()) {
    continuation = SharedRuntime::deopt_blob()->unpack_with_exception_in_tls();
  }

  assert(continuation != NULL, "no handler found");
  return continuation;
}

JRT_ENTRY(void, GraalRuntime::create_null_exception(JavaThread* thread))
  SharedRuntime::throw_and_post_jvmti_exception(thread, vmSymbols::java_lang_NullPointerException());
  thread->set_vm_result(PENDING_EXCEPTION);
  CLEAR_PENDING_EXCEPTION;
JRT_END

JRT_ENTRY(void, GraalRuntime::create_out_of_bounds_exception(JavaThread* thread, jint index))
  char message[jintAsStringSize];
  sprintf(message, "%d", index);
  SharedRuntime::throw_and_post_jvmti_exception(thread, vmSymbols::java_lang_ArrayIndexOutOfBoundsException(), message);
  thread->set_vm_result(PENDING_EXCEPTION);
  CLEAR_PENDING_EXCEPTION;
JRT_END

JRT_ENTRY_NO_ASYNC(void, GraalRuntime::monitorenter(JavaThread* thread, oopDesc* obj, BasicLock* lock))
  if (TraceGraal >= 3) {
    char type[O_BUFLEN];
    obj->klass()->name()->as_C_string(type, O_BUFLEN);
    markOop mark = obj->mark();
    tty->print_cr("%s: entered locking slow case with obj=" INTPTR_FORMAT ", type=%s, mark=" INTPTR_FORMAT ", lock=" INTPTR_FORMAT, thread->name(), obj, type, mark, lock);
    tty->flush();
  }
#ifdef ASSERT
  if (PrintBiasedLockingStatistics) {
    Atomic::inc(BiasedLocking::slow_path_entry_count_addr());
  }
#endif
  Handle h_obj(thread, obj);
  assert(h_obj()->is_oop(), "must be NULL or an object");
  if (UseBiasedLocking) {
    // Retry fast entry if bias is revoked to avoid unnecessary inflation
    ObjectSynchronizer::fast_enter(h_obj, lock, true, CHECK);
  } else {
    if (GraalUseFastLocking) {
      // When using fast locking, the compiled code has already tried the fast case
      ObjectSynchronizer::slow_enter(h_obj, lock, THREAD);
    } else {
      ObjectSynchronizer::fast_enter(h_obj, lock, false, THREAD);
    }
  }
  if (TraceGraal >= 3) {
    tty->print_cr("%s: exiting locking slow with obj=" INTPTR_FORMAT, thread->name(), obj);
  }
JRT_END

JRT_LEAF(void, GraalRuntime::monitorexit(JavaThread* thread, oopDesc* obj, BasicLock* lock))
  assert(thread == JavaThread::current(), "threads must correspond");
  assert(thread->last_Java_sp(), "last_Java_sp must be set");
  // monitorexit is non-blocking (leaf routine) => no exceptions can be thrown
  EXCEPTION_MARK;

#ifdef DEBUG
  if (!obj->is_oop()) {
    ResetNoHandleMark rhm;
    nmethod* method = thread->last_frame().cb()->as_nmethod_or_null();
    if (method != NULL) {
      tty->print_cr("ERROR in monitorexit in method %s wrong obj " INTPTR_FORMAT, method->name(), obj);
    }
    thread->print_stack_on(tty);
    assert(false, "invalid lock object pointer dected");
  }
#endif

  if (GraalUseFastLocking) {
    // When using fast locking, the compiled code has already tried the fast case
    ObjectSynchronizer::slow_exit(obj, lock, THREAD);
  } else {
    ObjectSynchronizer::fast_exit(obj, lock, THREAD);
  }
  if (TraceGraal >= 3) {
    char type[O_BUFLEN];
    obj->klass()->name()->as_C_string(type, O_BUFLEN);
    tty->print_cr("%s: exited locking slow case with obj=" INTPTR_FORMAT ", type=%s, mark=" INTPTR_FORMAT ", lock=" INTPTR_FORMAT, thread->name(), obj, type, obj->mark(), lock);
    tty->flush();
  }
JRT_END

JRT_ENTRY(void, GraalRuntime::log_object(JavaThread* thread, oopDesc* obj, jint flags))
  bool string =  mask_bits_are_true(flags, LOG_OBJECT_STRING);
  bool addr = mask_bits_are_true(flags, LOG_OBJECT_ADDRESS);
  bool newline = mask_bits_are_true(flags, LOG_OBJECT_NEWLINE);
  if (!string) {
    if (!addr && obj->is_oop_or_null(true)) {
      char buf[O_BUFLEN];
      tty->print("%s@%p", obj->klass()->name()->as_C_string(buf, O_BUFLEN), (address)obj);
    } else {
      tty->print("%p", (address)obj);
    }
  } else {
    ResourceMark rm;
    assert(obj != NULL && java_lang_String::is_instance(obj), "must be");
    char *buf = java_lang_String::as_utf8_string(obj);
    tty->print(buf);
  }
  if (newline) {
    tty->cr();
  }
JRT_END

JRT_LEAF(void, GraalRuntime::write_barrier_pre(JavaThread* thread, oopDesc* obj))
  thread->satb_mark_queue().enqueue(obj);
JRT_END

JRT_LEAF(void, GraalRuntime::write_barrier_post(JavaThread* thread, void* card_addr))
  thread->dirty_card_queue().enqueue(card_addr);
JRT_END

JRT_LEAF(jboolean, GraalRuntime::validate_object(JavaThread* thread, oopDesc* parent, oopDesc* child))
  bool ret = true;
  if(!Universe::heap()->is_in_closed_subset(parent)) {
    tty->print_cr("Parent Object "INTPTR_FORMAT" not in heap", parent);
    parent->print();
    ret=false;
  }
  if(!Universe::heap()->is_in_closed_subset(child)) {
    tty->print_cr("Child Object "INTPTR_FORMAT" not in heap", child);
    child->print();
    ret=false;
  }
  return (jint)ret;
JRT_END

JRT_ENTRY(void, GraalRuntime::vm_error(JavaThread* thread, oopDesc* where, oopDesc* format, jlong value))
  ResourceMark rm;
  assert(where == NULL || java_lang_String::is_instance(where), "must be");
  const char *error_msg = where == NULL ? "<internal Graal error>" : java_lang_String::as_utf8_string(where);
  char *detail_msg = NULL;
  if (format != NULL) {
    const char* buf = java_lang_String::as_utf8_string(format);
    size_t detail_msg_length = strlen(buf) * 2;
    detail_msg = (char *) NEW_RESOURCE_ARRAY(u_char, detail_msg_length);
    jio_snprintf(detail_msg, detail_msg_length, buf, value);
  }
  report_vm_error(__FILE__, __LINE__, error_msg, detail_msg);
JRT_END

JRT_LEAF(oopDesc*, GraalRuntime::load_and_clear_exception(JavaThread* thread))
  oop exception = thread->exception_oop();
  assert(exception != NULL, "npe");
  thread->set_exception_oop(NULL);
  thread->set_exception_pc(0);
  return exception;
JRT_END

JRT_LEAF(void, GraalRuntime::log_printf(JavaThread* thread, oopDesc* format, jlong v1, jlong v2, jlong v3))
  ResourceMark rm;
  assert(format != NULL && java_lang_String::is_instance(format), "must be");
  char *buf = java_lang_String::as_utf8_string(format);
  tty->print(buf, v1, v2, v3);
JRT_END

static void decipher(jlong v, bool ignoreZero) {
  if (v != 0 || !ignoreZero) {
    void* p = (void *)(address) v;
    CodeBlob* cb = CodeCache::find_blob(p);
    if (cb) {
      if (cb->is_nmethod()) {
        char buf[O_BUFLEN];
        tty->print("%s [%p+%d]", cb->as_nmethod_or_null()->method()->name_and_sig_as_C_string(buf, O_BUFLEN), cb->code_begin(), (address)v - cb->code_begin());
        return;
      }
      cb->print_value_on(tty);
      return;
    }
    if (Universe::heap()->is_in(p)) {
      oop obj = oop(p);
      obj->print_value_on(tty);
      return;
    }
    tty->print("%p [long: %d, double %f, char %c]", v, v, v, v);
  }
}

JRT_LEAF(void, GraalRuntime::vm_message(jboolean vmError, jlong format, jlong v1, jlong v2, jlong v3))
  ResourceMark rm;
  char *buf = (char*) (address) format;
  if (vmError) {
    if (buf != NULL) {
      fatal(err_msg(buf, v1, v2, v3));
    } else {
      fatal("<anonymous error>");
    }
  } else if (buf != NULL) {
    tty->print(buf, v1, v2, v3);
  } else {
    assert(v2 == 0, "v2 != 0");
    assert(v3 == 0, "v3 != 0");
    decipher(v1, false);
  }
JRT_END

JRT_ENTRY(void, GraalRuntime::log_primitive(JavaThread* thread, jchar typeChar, jlong value, jboolean newline))
  union {
      jlong l;
      jdouble d;
      jfloat f;
  } uu;
  uu.l = value;
  switch (typeChar) {
    case 'z': tty->print(value == 0 ? "false" : "true"); break;
    case 'b': tty->print("%d", (jbyte) value); break;
    case 'c': tty->print("%c", (jchar) value); break;
    case 's': tty->print("%d", (jshort) value); break;
    case 'i': tty->print("%d", (jint) value); break;
    case 'f': tty->print("%f", uu.f); break;
    case 'j': tty->print(INT64_FORMAT, value); break;
    case 'd': tty->print("%lf", uu.d); break;
    default: assert(false, "unknown typeChar"); break;
  }
  if (newline) {
    tty->cr();
  }
JRT_END

JRT_ENTRY(jint, GraalRuntime::identity_hash_code(JavaThread* thread, oopDesc* obj))
  return (jint) obj->identity_hash();
JRT_END

JRT_ENTRY(jboolean, GraalRuntime::thread_is_interrupted(JavaThread* thread, oopDesc* receiver, jboolean clear_interrupted))
  // Ensure that the C++ Thread and OSThread structures aren't freed before we operate
  Handle receiverHandle(thread, receiver);
  MutexLockerEx ml(thread->threadObj() == (void*)receiver ? NULL : Threads_lock);
  JavaThread* receiverThread = java_lang_Thread::thread(receiverHandle());
  if (receiverThread == NULL) {
    // The other thread may exit during this process, which is ok so return false.
    return JNI_FALSE;
  } else {
    return (jint) Thread::is_interrupted(receiverThread, clear_interrupted != 0);
  }
JRT_END

// JVM_InitializeGraalRuntime
JVM_ENTRY(jobject, JVM_InitializeGraalRuntime(JNIEnv *env, jclass graalclass))
  return VMToCompiler::graalRuntimePermObject();
JVM_END

// JVM_InitializeTruffleRuntime
JVM_ENTRY(jobject, JVM_InitializeTruffleRuntime(JNIEnv *env, jclass graalclass))
  return JNIHandles::make_local(VMToCompiler::truffleRuntime()());
JVM_END
