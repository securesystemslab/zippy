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

#ifndef SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP
#define SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP

#include "interpreter/interpreter.hpp"
#include "memory/allocation.hpp"
#include "runtime/deoptimization.hpp"

class GraalRuntime: public AllStatic {
 public:
  static void new_instance(JavaThread* thread, Klass* klass);
  static void new_array(JavaThread* thread, Klass* klass, jint length);
  static void new_multi_array(JavaThread* thread, Klass* klass, int rank, jint* dims);
  static void dynamic_new_array(JavaThread* thread, oopDesc* element_mirror, jint length);
  static void dynamic_new_instance(JavaThread* thread, oopDesc* type_mirror);
  static jboolean thread_is_interrupted(JavaThread* thread, oopDesc* obj, jboolean clear_interrupted);
  static void vm_message(jboolean vmError, jlong format, jlong v1, jlong v2, jlong v3);
  static jint identity_hash_code(JavaThread* thread, oopDesc* obj);
  static address exception_handler_for_pc(JavaThread* thread);
  static void monitorenter(JavaThread* thread, oopDesc* obj, BasicLock* lock);
  static void monitorexit (JavaThread* thread, oopDesc* obj, BasicLock* lock);
  static void create_null_exception(JavaThread* thread);
  static void create_out_of_bounds_exception(JavaThread* thread, jint index);
  static void vm_error(JavaThread* thread, oopDesc* where, oopDesc* format, jlong value);
  static oopDesc* load_and_clear_exception(JavaThread* thread);
  static void log_printf(JavaThread* thread, oopDesc* format, jlong v1, jlong v2, jlong v3);
  static void log_primitive(JavaThread* thread, jchar typeChar, jlong value, jboolean newline);
  // Note: Must be kept in sync with constants in com.oracle.graal.replacements.Log
  enum {
    LOG_OBJECT_NEWLINE = 0x01,
    LOG_OBJECT_STRING  = 0x02,
    LOG_OBJECT_ADDRESS = 0x04
  };
  static void log_object(JavaThread* thread, oopDesc* msg, jint flags);
  static void write_barrier_pre(JavaThread* thread, oopDesc* obj);
  static void write_barrier_post(JavaThread* thread, void* card);
  static jboolean validate_object(JavaThread* thread, oopDesc* parent, oopDesc* child);
  static void new_store_pre_barrier(JavaThread* thread);
};

#endif // SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP
