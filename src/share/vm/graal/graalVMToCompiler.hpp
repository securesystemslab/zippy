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

#ifndef SHARE_VM_GRAAL_GRAAL_VM_TO_COMPILER_HPP
#define SHARE_VM_GRAAL_GRAAL_VM_TO_COMPILER_HPP

#include "memory/allocation.hpp"
#include "oops/oop.hpp"
#include "runtime/handles.hpp"
#include "runtime/thread.hpp"
#include "classfile/javaClasses.hpp"
#include "runtime/jniHandles.hpp"
#include "runtime/javaCalls.hpp"

class VMToCompiler : public AllStatic {

private:
  static jobject _graalRuntimePermObject;
  static jobject _vmToCompilerPermObject;
  static Klass* _vmToCompilerPermKlass;

  static KlassHandle vmToCompilerKlass();
  static Handle instance();

public:
  static Handle graalRuntime();
  static Handle truffleRuntime();

  static jobject graalRuntimePermObject() {
    graalRuntime();
    return _graalRuntimePermObject;
  }

  // public static boolean HotSpotOptions.<clinit>();
  static void initOptions();

  // public static boolean HotSpotOptions.setOption(String option);
  static jboolean setOption(Handle option);

  // public static void HotSpotOptions.finalizeOptions(boolean ciTime);
  static void finalizeOptions(jboolean ciTime);

  // public abstract boolean compileMethod(long vmId, int entry_bci, boolean blocking);
  static void compileMethod(Method* method, int entry_bci, jboolean blocking);

  // public abstract void shutdownCompiler();
  static void shutdownCompiler();
  
  // public abstract void startCompiler(boolean bootstrapEnabled);
  static void startCompiler(jboolean bootstrap_enabled);
  
  // public abstract void bootstrap();
  static void bootstrap();

  // public abstract void compileTheWorld();
  static void compileTheWorld();

  // public abstract JavaField createJavaField(JavaType holder, String name, JavaType type, int flags, int offset);
  static oop createJavaField(Handle holder, Handle name, Handle type, int index, int flags, jboolean internal, TRAPS);

  // public abstract JavaMethod createUnresolvedJavaMethod(String name, String signature, JavaType holder);
  static oop createUnresolvedJavaMethod(Handle name, Handle signature, Handle holder, TRAPS);

  // public abstract JavaMethod createResolvedJavaMethod(JavaType holder, long metaspaceMethod);
  static oop createResolvedJavaMethod(Handle holder, Method* method, TRAPS);

  // public abstract JavaType createUnresolvedJavaType(String name);
  static oop createUnresolvedJavaType(Handle name, TRAPS);

  // public abstract ResolvedJavaType createResolvedJavaType(Class javaMirror);
  static oop createResolvedJavaType(Handle java_mirror, TRAPS);

  // public abstract JavaType createPrimitiveJavaType(int basicType);
  static oop createPrimitiveJavaType(int basicType, TRAPS);
};

inline void check_pending_exception(const char* message, bool dump_core = false) {
  Thread* THREAD = Thread::current();
  if (THREAD->has_pending_exception()) {
    Handle exception = PENDING_EXCEPTION;
    CLEAR_PENDING_EXCEPTION;

    assert(exception->is_a(SystemDictionary::Throwable_klass()), "Throwable instance expected");
    JavaValue result(T_VOID);
    JavaCalls::call_virtual(&result,
                            exception,
                            KlassHandle(THREAD,
                            SystemDictionary::Throwable_klass()),
                            vmSymbols::printStackTrace_name(),
                            vmSymbols::void_method_signature(),
                            THREAD);

    vm_abort(dump_core);
  }
}

#endif // SHARE_VM_GRAAL_GRAAL_VM_TO_COMPILER_HPP
