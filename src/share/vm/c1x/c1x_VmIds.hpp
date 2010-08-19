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

class VmIds : public AllStatic {

private:
  static GrowableArray<address>* _stubs;
  static GrowableArray<jobject>* _localHandles;

  static oop getObject(jlong id);

public:
  // this enum needs to have the same values as the one in HotSpotProxy.java
  enum CompilerObjectType {
    STUB           = 0x100000000000000l,
    METHOD         = 0x200000000000000l,
    CLASS          = 0x300000000000000l,
    SYMBOL         = 0x400000000000000l,
    CONSTANT_POOL  = 0x500000000000000l,
    CONSTANT       = 0x600000000000000l,
    TYPE_MASK      = 0xf00000000000000l,
    DUMMY_CONSTANT = 0x6ffffffffffffffl
  };

  static void initializeObjects();
  static void cleanupLocalObjects();

  static jlong addStub(address stub);
  static jlong add(Handle obj, CompilerObjectType type);
  template <typename T> static jlong add(T obj);

  static address getStub(jlong id);
  template <typename T> static T get(jlong id);

  template <typename T> static T toString(symbolOop symbol, TRAPS);
  static symbolOop toSymbol(jstring string);
};

template <> inline jlong VmIds::add<methodOop>(methodOop obj){
  assert(obj != NULL && obj->is_method(), "trying to add NULL or mistyped object");
  return add(Handle(obj), METHOD);
}
template <> inline jlong VmIds::add<klassOop>(klassOop obj) {
  assert(obj != NULL && obj->is_klass(), "trying to add NULL or mistyped object");
  return add(Handle(obj), CLASS);
}
template <> inline jlong VmIds::add<symbolOop>(symbolOop obj) {
  assert(obj != NULL && obj->is_symbol(), "trying to add NULL or mistyped object");
  return add(Handle(obj), SYMBOL);
}
template <> inline jlong VmIds::add<constantPoolOop>(constantPoolOop obj) {
  assert(obj != NULL && obj->is_constantPool(), "trying to add NULL or mistyped object");
  return add(Handle(obj), CONSTANT_POOL);
}
template <> inline jlong VmIds::add<oop>(oop obj) {
  assert(obj != NULL && obj->is_oop(), "trying to add NULL or mistyped object");
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
template <> inline symbolOop VmIds::get<symbolOop>(jlong id) {
  assert((id & TYPE_MASK) == SYMBOL, "SYMBOL expected");
  assert(getObject(id)->is_symbol(), "symbolOop expected");
  return (symbolOop)getObject(id);
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

template <> inline Handle VmIds::toString<Handle>(symbolOop symbol, TRAPS) {
  return java_lang_String::create_from_symbol(symbol, THREAD);
}
template <> inline oop VmIds::toString<oop>(symbolOop symbol, TRAPS) {
  return toString<Handle>(symbol, THREAD)();
}
template <> inline jstring VmIds::toString<jstring>(symbolOop symbol, TRAPS) {
  return (jstring)JNIHandles::make_local(toString<oop>(symbol, THREAD));
}
template <> inline jobject VmIds::toString<jobject>(symbolOop symbol, TRAPS) {
  return JNIHandles::make_local(toString<oop>(symbol, THREAD));
}

inline symbolOop VmIds::toSymbol(jstring string) {
  return java_lang_String::as_symbol_or_null(JNIHandles::resolve(string));
}

