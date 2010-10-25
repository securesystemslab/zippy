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

class VMExits : public AllStatic {

private:
  static jobject _vmExitsPermObject;
  static jobject _vmExitsPermKlass;

  static KlassHandle vmExitsKlass();
  static Handle instance();

public:

  // public abstract boolean setOption(String option);
  static jboolean setOption(Handle option);

  // public abstract void compileMethod(long vmId, String name, int entry_bci);
  static void compileMethod(jlong vmId, Handle name, int entry_bci);

  // public abstract RiMethod createRiMethodResolved(long vmId, String name);
  static oop createRiMethodResolved(jlong vmId, Handle name, TRAPS);

  // public abstract RiMethod createRiMethodUnresolved(String name, String signature, RiType holder);
  static oop createRiMethodUnresolved(Handle name, Handle signature, Handle holder, TRAPS);

  // public abstract RiField createRiField(RiType holder, String name, RiType type, int offset);
  static oop createRiField(Handle holder, Handle name, Handle type, int index, TRAPS);

  // public abstract RiType createRiType(long vmId, String name);
  static oop createRiType(jlong vmId, Handle name, TRAPS);

  // public abstract RiType createRiTypeUnresolved(String name, long accessingClassVmId);
  static oop createRiTypeUnresolved(Handle name, jlong accessingClassVmId, TRAPS);

  // public abstract RiConstantPool createRiConstantPool(long vmId);
  static oop createRiConstantPool(jlong vmId, TRAPS);

  // public abstract RiType createRiTypePrimitive(int basicType);
  static oop createRiTypePrimitive(int basicType, TRAPS);

  // public abstract RiSignature createRiSignature(String signature);
  static oop createRiSignature(Handle name, TRAPS);

  // public abstract CiConstant createCiConstantInt(int value);
  static oop createCiConstantInt(jint value, TRAPS);

  // public abstract CiConstant createCiConstantLong(long value);
  static oop createCiConstantLong(jlong value, TRAPS);

  // public abstract CiConstant createCiConstantFloat(float value);
  static oop createCiConstantFloat(jfloat value, TRAPS);

  // public abstract CiConstant createCiConstantDouble(double value);
  static oop createCiConstantDouble(jdouble value, TRAPS);

  // public abstract CiConstant createCiConstantObject(long vmId);
  static oop createCiConstantObject(jlong vmId, TRAPS);
};

inline void check_pending_exception(const char* message) {
  if (Thread::current()->has_pending_exception()) {
    Thread::current()->pending_exception()->print();
    fatal(message);
  }
}
