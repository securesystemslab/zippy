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

# include "incls/_precompiled.incl"
# include "incls/_c1x_TargetMethod.cpp.incl"

static void compute_offset(int &dest_offset, klassOop klass_oop, const char* name, const char* signature, bool static_field) {
  symbolOop name_symbol = SymbolTable::probe(name, strlen(name));
  symbolOop signature_symbol = SymbolTable::probe(signature, strlen(signature));
  assert(name_symbol != NULL, "symbol not found - class layout changed?");
  assert(signature_symbol != NULL, "symbol not found - class layout changed?");

  instanceKlass* ik = instanceKlass::cast(klass_oop);
  fieldDescriptor fd;
  if (!ik->find_field(name_symbol, signature_symbol, &fd)) {
    ResourceMark rm;
    tty->print_cr("Invalid layout of %s at %s", ik->external_name(), name_symbol->as_C_string());
    fatal("Invalid layout of preloaded class");
  }
  assert(fd.is_static() == static_field, "static/instance mismatch");
  dest_offset = fd.offset();
}

// create the compute_class
#define START_CLASS(name) { klassOop k = SystemDictionary::name##_klass();

#define END_CLASS }

#define FIELD(klass, name, signature, static_field) compute_offset(klass::_##name##_offset, k, #name, signature, static_field);
#define CHAR_FIELD(klass, name) FIELD(klass, name, "C", false)
#define INT_FIELD(klass, name) FIELD(klass, name, "I", false)
#define BOOLEAN_FIELD(klass, name) FIELD(klass, name, "Z", false)
#define LONG_FIELD(klass, name) FIELD(klass, name, "J", false)
#define OOP_FIELD(klass, name, signature) FIELD(klass, name, signature, false)
#define STATIC_OOP_FIELD(klass, name, signature) FIELD(klass, name, signature, true)


void c1x_compute_offsets() {
  COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, BOOLEAN_FIELD, LONG_FIELD, OOP_FIELD, STATIC_OOP_FIELD)
}

#define EMPTY0
#define EMPTY1(x)
#define EMPTY2(x,y)
#define FIELD2(klass, name) int klass::_##name##_offset = 0;
#define FIELD3(klass, name, sig) FIELD2(klass, name)

COMPILER_CLASSES_DO(EMPTY1, EMPTY0, FIELD2, FIELD2, FIELD2, FIELD2, FIELD3, FIELD3)





