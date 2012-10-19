/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
#include "prims/jni.h"
#include "runtime/javaCalls.hpp"
#include "memory/oopFactory.hpp"
#include "graal/graalInterpreterToVM.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalCompilerToVM.hpp"

#ifdef HIGH_LEVEL_INTERPRETER

// public Object invoke(HotSpotResolvedJavaMethod method, boolean highLevel, Object... args);
JNIEXPORT jobject JNICALL Java_com_oracle_graal_hotspot_HotSpotRuntimeInterpreterInterface_invoke(JNIEnv *env, jobject, jobject method, jobject args) {
  TRACE_graal_3("InterpreterToVM::invoke");

  VM_ENTRY_MARK;
  HandleMark hm;
  
  assert(method != NULL, "just checking");
  assert(thread->is_Java_thread(), "must be");
  methodHandle mh = getMethodFromHotSpotMethod(method);
    
  JavaCallArguments jca;
  JavaArgumentUnboxer jap(mh->signature(), &jca, (arrayOop) JNIHandles::resolve(args), mh->is_static());
  
#ifndef PRODUCT
  if (PrintHighLevelInterpreterVMTransitions) {
    ResourceMark rm;
    tty->print_cr("High level interpreter -> VM (%s)", mh->name_and_sig_as_C_string());
  }
#endif

  JavaValue result(jap.get_ret_type());
  thread->set_high_level_interpreter_in_vm(true);
  JavaCalls::call(&result, mh, &jca, THREAD);
  thread->set_high_level_interpreter_in_vm(false);

#ifndef PRODUCT
  if (PrintHighLevelInterpreterVMTransitions) {
    ResourceMark rm;
    tty->print_cr("VM (%s) -> high level interpreter", mh->name_and_sig_as_C_string());
  }
#endif

  if (thread->has_pending_exception()) {
    return NULL;
  }

  if (jap.get_ret_type() == T_VOID) {
    return NULL;
  } else if (jap.get_ret_type() == T_OBJECT || jap.get_ret_type() == T_ARRAY) {
    return JNIHandles::make_local((oop) result.get_jobject());
  } else {
    oop o = java_lang_boxing_object::create(jap.get_ret_type(), (jvalue *) result.get_value_addr(), CHECK_NULL);
    return JNIHandles::make_local(o);
  }
}

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &(Java_com_oracle_graal_hotspot_HotSpotRuntimeInterpreterInterface_##f))

#define RESOLVED_METHOD "Lcom/oracle/graal/api/meta/ResolvedJavaMethod;"
#define OBJECT          "Ljava/lang/Object;"

JNINativeMethod InterpreterToVM_methods[] = {
  {CC"invoke",                     CC"("RESOLVED_METHOD "["OBJECT")"OBJECT,     FN_PTR(invoke)}
};

int InterpreterToVM_methods_count() {
  return sizeof(InterpreterToVM_methods) / sizeof(JNINativeMethod);
}

#endif // HIGH_LEVEL_INTERPRETER
