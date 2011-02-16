/*
 * Copyright 2000-2010 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

class Thread;

class VmIds : public AllStatic {

private:
  static GrowableArray<address>* _stubs;
  static GrowableArray<jobject>* _localHandles;

  static oop getObject(jlong id);

public:
  // this enum needs to have the same values as the one in HotSpotProxy.java
  enum CompilerObjectType {
    STUB           = 0x100000000000000l,        // address
    METHOD         = 0x200000000000000l,        // methodOop
    CLASS          = 0x300000000000000l,        // klassOop
    CONSTANT_POOL  = 0x500000000000000l,        // constantPoolOop
    CONSTANT       = 0x600000000000000l,        // oop
    TYPE_MASK      = 0xf00000000000000l,
    DUMMY_CONSTANT = 0x6ffffffffffffffl
  };

  // Initializes the VmIds for a compilation, by creating the arrays
  static void initializeObjects();
  // Cleans up after a compilation, by deallocating the arrays
  static void cleanupLocalObjects();

  // Adds a stub address, and returns the corresponding vmId (which is of type STUB)
  static jlong addStub(address stub);

  // Adds an object, and returns the corresponding vmId (with the given type)
  static jlong add(Handle obj, CompilerObjectType type);

  // Adds an object, and returns the corresponding vmId (the type of which is determined by the template parameter)
  template <typename T> static jlong add(T obj);


  // Returns the stub address with the given vmId
  static address getStub(jlong id);
  // Returns the stub address with the given vmId taken from a java.lang.Long
  static address getStub(oop id);

  // Returns the object with the given id, the return type is defined by the template parameter (which must correspond to the actual type of the vmId)
  template <typename T> static T get(jlong id);


  // Helper function to convert a symbol to a java.lang.String object
  template <typename T> static T toString(Symbol* symbol, TRAPS);

  // Helper function to convert a java.lang.String object to a symbol (this will return NULL if the symbol doesn't exist in the system)
  static Symbol* toSymbol(jstring string);

  // Helper function to get the contents of a java.lang.Long
  static jlong getBoxedLong(oop obj);
};


template <> inline jlong VmIds::add<methodOop>(methodOop obj){
  assert(obj != NULL, "trying to add NULL<methodOop>");
  assert(obj->is_method(), "trying to add mistyped object");
  return add(Handle(obj), METHOD);
}
template <> inline jlong VmIds::add<klassOop>(klassOop obj) {
  assert(obj != NULL, "trying to add NULL<klassOop>");
  assert(obj->is_klass(), "trying to add mistyped object");
  return add(Handle(obj), CLASS);
}
template <> inline jlong VmIds::add<constantPoolOop>(constantPoolOop obj) {
  assert(obj != NULL, "trying to add NULL<constantPoolOop>");
  assert(obj->is_constantPool(), "trying to add mistyped object");
  return add(Handle(obj), CONSTANT_POOL);
}
template <> inline jlong VmIds::add<oop>(oop obj) {
  assert(obj != NULL, "trying to add NULL<oop>");
  assert(obj->is_oop(), "trying to add mistyped object");
  return add(Handle(obj), CONSTANT);
}


template <> inline methodOop VmIds::get<methodOop>(jlong id){
  assert((id & TYPE_MASK) == METHOD, "METHOD expected");
  assert(getObject(id)->is_method(), "methodOop expected");
  return (methodOop)getObject(id);
}
template <> inline klassOop VmIds::get<klassOop>(jlong id) {
  assert((id & TYPE_MASK) == CLASS, "CLASS expected");
  assert(getObject(id)->is_klass(), "klassOop expected");
  return (klassOop)getObject(id);
}
template <> inline constantPoolOop VmIds::get<constantPoolOop>(jlong id) {
  assert((id & TYPE_MASK) == CONSTANT_POOL, "CONSTANT_POOL expected");
  assert(getObject(id)->is_constantPool(), "constantPoolOop expected");
  return (constantPoolOop)getObject(id);
}
template <> inline oop VmIds::get<oop>(jlong id) {
  assert((id & TYPE_MASK) == CONSTANT, "CONSTANT expected");
  assert(getObject(id)->is_oop(true), "oop expected");
  return (oop)getObject(id);
}

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
