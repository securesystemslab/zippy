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

#ifndef SHARE_VM_GRAAL_GRAAL_VM_IDS_HPP
#define SHARE_VM_GRAAL_GRAAL_VM_IDS_HPP

#include "memory/allocation.hpp"
#include "utilities/growableArray.hpp"
#include "oops/oop.hpp"
#include "runtime/handles.hpp"
#include "runtime/thread.hpp"
#include "classfile/javaClasses.hpp"
#include "runtime/jniHandles.hpp"

class VmIds : public AllStatic {

public:

  // Returns the stub address with the given vmId taken from a java.lang.Long
  static address getStub(oop id);

  // Helper function to get the contents of a java.lang.Long
  static jlong getBoxedLong(oop obj);
};

inline address VmIds::getStub(oop obj) {
  return (address)(getBoxedLong(obj));
}

inline jlong VmIds::getBoxedLong(oop obj) {
  assert(obj->is_oop(true), "cannot unbox null or non-oop");
  return obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
}

#endif // SHARE_VM_GRAAL_GRAAL_VM_IDS_HPP
