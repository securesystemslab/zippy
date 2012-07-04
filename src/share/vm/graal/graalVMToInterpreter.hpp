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

#include "memory/allocation.hpp"
#include "oops/oop.hpp"
#include "runtime/handles.hpp"
#include "runtime/thread.hpp"
#include "classfile/javaClasses.hpp"
#include "runtime/jniHandles.hpp"
#include "runtime/javaCalls.hpp"

#ifdef HIGH_LEVEL_INTERPRETER
#ifndef SHARE_VM_GRAAL_GRAAL_VM_TO_INTERPRETER_HPP
#define SHARE_VM_GRAAL_GRAAL_VM_TO_INTERPRETER_HPP

class VMToInterpreter : public AllStatic {

private:
  static jobject _interpreterPermObject;
  static jobject _interpreterPermKlass;

  static Handle interpreter_instance();
  static KlassHandle interpreter_klass();
  static void unbox_primitive(JavaValue* boxed, JavaValue* result);

public:
  static bool allocate_interpreter(const char* interpreter_class_name, const char* interpreter_arguments, TRAPS);
 
  // invokes the interpreter method execute(ResolvedJavaMethod method, Object... arguments)
  static void execute(JavaValue* result, methodHandle* m, JavaCallArguments* args, BasicType expected_result_type, TRAPS);
};

#endif // SHARE_VM_GRAAL_GRAAL_VM_TO_INTERPRETER_HPP
#endif // HIGH_LEVEL_INTERPRETER
