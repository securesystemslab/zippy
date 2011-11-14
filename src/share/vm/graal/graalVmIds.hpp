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

#include "memory/allocation.hpp"
#include "utilities/growableArray.hpp"
#include "oops/oop.hpp"
#include "runtime/handles.hpp"
#include "runtime/thread.hpp"
#include "classfile/javaClasses.hpp"
#include "runtime/jniHandles.hpp"

class VmIds : public AllStatic {

private:
  static GrowableArray<address>* _stubs;

  static oop getObject(jlong id);

public:
  // these constants needs to have the same values as the one in HotSpotProxy.java
  static const jlong STUB           = 0x100000000000000LL;        // address
  static const jlong METHOD         = 0x200000000000000LL;        // methodOop
  static const jlong CLASS          = 0x300000000000000LL;        // klassOop
  static const jlong CONSTANT_POOL  = 0x500000000000000LL;        // constantPoolOop
  static const jlong CONSTANT       = 0x600000000000000LL;        // oop
  static const jlong TYPE_MASK      = 0xf00000000000000LL;
  static const jlong DUMMY_CONSTANT = 0x6ffffffffffffffLL;

  // Initializes the VmIds for a compilation, by creating the arrays
  static void initializeObjects();

  // Adds a stub address, and returns the corresponding vmId (which is of type STUB)
  static jlong addStub(address stub);

  // Returns the stub address with the given vmId
  static address getStub(jlong id);

  // Returns the stub address with the given vmId taken from a java.lang.Long
  static address getStub(oop id);

  // Helper function to convert a symbol to a java.lang.String object
  template <typename T> static T toString(Symbol* symbol, TRAPS);

  // Helper function to convert a java.lang.String object to a symbol (this will return NULL if the symbol doesn't exist in the system)
  static Symbol* toSymbol(jstring string);

  // Helper function to get the contents of a java.lang.Long
  static jlong getBoxedLong(oop obj);
};

inline address VmIds::getStub(oop obj) {
  return getStub(getBoxedLong(obj));
}

template <> inline Handle VmIds::toString<Handle>(Symbol* symbol, TRAPS) {
  return java_lang_String::create_from_symbol(symbol, THREAD);
}

template <> inline oop VmIds::toString<oop>(Symbol* symbol, TRAPS) {
  return toString<Handle>(symbol, THREAD)();
}

template <> inline jstring VmIds::toString<jstring>(Symbol* symbol, TRAPS) {
  return (jstring)JNIHandles::make_local(toString<oop>(symbol, THREAD));
}

template <> inline jobject VmIds::toString<jobject>(Symbol* symbol, TRAPS) {
  return JNIHandles::make_local(toString<oop>(symbol, THREAD));
}

inline Symbol* VmIds::toSymbol(jstring string) {
  return java_lang_String::as_symbol_or_null(JNIHandles::resolve(string));
}

inline jlong VmIds::getBoxedLong(oop obj) {
  assert(obj->is_oop(true), "cannot unbox null or non-oop");
  return obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
}
