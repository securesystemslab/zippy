/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_GRAAL_COMPILER_TO_VM_HPP
#define SHARE_VM_GRAAL_GRAAL_COMPILER_TO_VM_HPP

#include "prims/jni.h"
#include "runtime/javaCalls.hpp"

extern JNINativeMethod CompilerToVM_methods[];
int CompilerToVM_methods_count();

inline Method* asMethod(jlong metaspaceMethod) {
  return (Method*) (address) metaspaceMethod;
}

inline MethodData* asMethodData(jlong metaspaceMethodData) {
  return (MethodData*) (address) metaspaceMethodData;
}

inline Klass* asKlass(jlong metaspaceKlass) {
  return (Klass*) (address) metaspaceKlass;
}

/**
 * Gets the Method metaspace object from a HotSpotResolvedJavaMethod Java object.
 */
Method* getMethodFromHotSpotMethod(oop hotspot_method);

class JavaArgumentUnboxer : public SignatureIterator {
 protected:
  JavaCallArguments*  _jca;
  arrayOop _args;
  int _index;

  oop next_arg(BasicType expectedType) {
    assert(_index < _args->length(), "out of bounds");
    oop arg=((objArrayOop) (_args))->obj_at(_index++);
    assert(expectedType == T_OBJECT || java_lang_boxing_object::is_instance(arg, expectedType), "arg type mismatch");
    return arg;
  }

 public:
  JavaArgumentUnboxer(Symbol* signature, JavaCallArguments*  jca, arrayOop args, bool is_static) : SignatureIterator(signature) {
    this->_return_type = T_ILLEGAL;
    _jca = jca;
    _index = 0;
    _args = args;
    if (!is_static) {
      _jca->push_oop(next_arg(T_OBJECT));
    }
    iterate();
    assert(_index == args->length(), "arg count mismatch with signature");
  }

  inline void do_bool()   { if (!is_return_type()) _jca->push_int(next_arg(T_BOOLEAN)->bool_field(java_lang_boxing_object::value_offset_in_bytes(T_BOOLEAN))); }
  inline void do_char()   { if (!is_return_type()) _jca->push_int(next_arg(T_CHAR)->char_field(java_lang_boxing_object::value_offset_in_bytes(T_CHAR))); }
  inline void do_short()  { if (!is_return_type()) _jca->push_int(next_arg(T_SHORT)->short_field(java_lang_boxing_object::value_offset_in_bytes(T_SHORT))); }
  inline void do_byte()   { if (!is_return_type()) _jca->push_int(next_arg(T_BYTE)->byte_field(java_lang_boxing_object::value_offset_in_bytes(T_BYTE))); }
  inline void do_int()    { if (!is_return_type()) _jca->push_int(next_arg(T_INT)->int_field(java_lang_boxing_object::value_offset_in_bytes(T_INT))); }

  inline void do_long()   { if (!is_return_type()) _jca->push_long(next_arg(T_LONG)->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG))); }
  inline void do_float()  { if (!is_return_type()) _jca->push_float(next_arg(T_FLOAT)->float_field(java_lang_boxing_object::value_offset_in_bytes(T_FLOAT))); }
  inline void do_double() { if (!is_return_type()) _jca->push_double(next_arg(T_DOUBLE)->double_field(java_lang_boxing_object::value_offset_in_bytes(T_DOUBLE))); }

  inline void do_object() { _jca->push_oop(next_arg(T_OBJECT)); }
  inline void do_object(int begin, int end) { if (!is_return_type()) _jca->push_oop(next_arg(T_OBJECT)); }
  inline void do_array(int begin, int end)  { if (!is_return_type()) _jca->push_oop(next_arg(T_OBJECT)); }
  inline void do_void()                     { }
};

// nothing here - no need to define the jni method implementations in a header file

#endif // SHARE_VM_GRAAL_GRAAL_COMPILER_TO_VM_HPP
