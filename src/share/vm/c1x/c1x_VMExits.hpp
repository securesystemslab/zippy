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
/*
class OopCache : public AllStatic {

private:
  static Handle* handles;
  static Handle* mirrors;
  static int capacity;
  static int used;

public:
  static void initialize();
  static Handle mirror(oop internal_object);
  static Handle resolve(oop mirror);

};
*/
class VMExits : public AllStatic {

private:

  static KlassHandle _vmExitsKlass;
  static Handle _vmExitsObject;

public:

  static KlassHandle& vmExitsKlass();
  static Handle& instance();


  static void compileMethod(oop method, int entry_bci);

  static oop createRiMethod(methodOop m);
  static oop createRiField(oop field_holder, symbolOop field_name, oop field_type, int index);
  static oop createRiType(klassOop k);
  static oop createRiConstantPool(constantPoolOop cp);
  static oop createRiTypeUnresolved(symbolOop name, klassOop accessor);
  static oop createRiSignature(symbolOop name);
  static oop createCiConstantInt(jint value);
  static oop createCiConstantLong(jlong value);
  static oop createCiConstantFloat(jfloat value);
  static oop createCiConstantDouble(jdouble value);
  static oop createCiConstantObject(oop value);
  static oop createRiTypePrimitive(int basic_type);
};

inline void check_pending_exception(const char* message) {
  if (Thread::current()->has_pending_exception()) {
    Thread::current()->pending_exception()->print();
    fatal(message);
  }
}
