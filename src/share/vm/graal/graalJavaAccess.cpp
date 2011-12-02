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
#include "graal/graalJavaAccess.hpp"
#include "runtime/jniHandles.hpp"
#include "classfile/symbolTable.hpp"
// This function is similar to javaClasses.cpp, it computes the field offset of a (static or instance) field.
// It looks up the name and signature symbols without creating new ones, all the symbols of these classes need to be already loaded.

void compute_offset(int &dest_offset, klassOop klass_oop, const char* name, const char* signature, bool static_field) {
  Symbol* name_symbol = SymbolTable::probe(name, (int)strlen(name));
  Symbol* signature_symbol = SymbolTable::probe(signature, (int)strlen(signature));
#ifdef DEBUG
  if (name_symbol == NULL) {
    tty->print_cr("symbol with name %s was not found in symbol table (klass=%s)", name, klass_oop->klass_part()->name()->as_C_string());
  }
#endif
  assert(name_symbol != NULL, "symbol not found - class layout changed?");
  assert(signature_symbol != NULL, "symbol not found - class layout changed?");

  instanceKlass* ik = instanceKlass::cast(klass_oop);
  fieldDescriptor fd;
  if (!ik->find_field(name_symbol, signature_symbol, &fd)) {
    ResourceMark rm;
    tty->print_cr("Invalid layout of %s at %s", name_symbol->as_C_string(), ik->external_name());
    fatal("Invalid layout of preloaded class");
  }
  assert(fd.is_static() == static_field, "static/instance mismatch");
  dest_offset = fd.offset();
}

// This piece of macro magic creates the contents of the graal_compute_offsets method that initializes the field indices of all the access classes.

#define START_CLASS(name) { klassOop k = SystemDictionary::name##_klass(); assert(k != NULL, "Could not find class " #name "");

#define END_CLASS }

#define FIELD(klass, name, signature, static_field) compute_offset(klass::_##name##_offset, k, #name, signature, static_field);
#define CHAR_FIELD(klass, name) FIELD(klass, name, "C", false)
#define INT_FIELD(klass, name) FIELD(klass, name, "I", false)
#define BOOLEAN_FIELD(klass, name) FIELD(klass, name, "Z", false)
#define LONG_FIELD(klass, name) FIELD(klass, name, "J", false)
#define FLOAT_FIELD(klass, name) FIELD(klass, name, "F", false)
#define OOP_FIELD(klass, name, signature) FIELD(klass, name, signature, false)
#define STATIC_OOP_FIELD(klass, name, signature) FIELD(klass, name, signature, true)


void graal_compute_offsets() {
  COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, BOOLEAN_FIELD, LONG_FIELD, FLOAT_FIELD, OOP_FIELD, STATIC_OOP_FIELD)
}

#define EMPTY0
#define EMPTY1(x)
#define EMPTY2(x,y)
#define FIELD2(klass, name) int klass::_##name##_offset = 0;
#define FIELD3(klass, name, sig) FIELD2(klass, name)

COMPILER_CLASSES_DO(EMPTY1, EMPTY0, FIELD2, FIELD2, FIELD2, FIELD2, FIELD2, FIELD3, FIELD3)





