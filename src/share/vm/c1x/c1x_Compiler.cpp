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
# include "incls/_c1x_Compiler.cpp.incl"


// Initialization
void C1XCompiler::initialize() {
  if (_initialized) return;
  _initialized = true;
    TRACE_C1X_1("initialize");

  JNIEnv *env = ((JavaThread *)Thread::current())->jni_environment();
  jclass klass = env->FindClass("com/sun/hotspot/c1x/VMEntriesNative");
  assert(klass != NULL, "c1x VMEntries class not found");
  env->RegisterNatives(klass, VMEntries_methods, VMEntries_methods_count() );
  
  check_pending_exception("Could not register natives");

  c1x_compute_offsets();
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  initialize();

  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;

  VmIds::initializeObjects();

  CompilerThread::current()->set_compiling(true);
  methodOop method = (methodOop)target->get_oop();
  VMExits::compileMethod(VmIds::add<methodOop>(method), VmIds::toString<Handle>(method->name(), THREAD), entry_bci);
  CompilerThread::current()->set_compiling(false);

  VmIds::cleanupLocalObjects();
}

// Print compilation timers and statistics
void C1XCompiler::print_timers() {
	TRACE_C1X_1("print_timers");
}

oop C1XCompiler::get_RiType(ciType *type, klassOop accessor, TRAPS) {
  if (type->is_loaded()) {
    if (type->is_primitive_type()) {
      return VMExits::createRiTypePrimitive((int)type->basic_type(), THREAD);
    }
    klassOop klass = (klassOop)type->get_oop();
    return VMExits::createRiType(VmIds::add<klassOop>(klass), VmIds::toString<Handle>(klass->klass_part()->name(), THREAD), THREAD);
  } else {
    symbolOop name = ((ciKlass *)type)->name()->get_symbolOop();
    return VMExits::createRiTypeUnresolved(VmIds::toString<Handle>(name, THREAD), VmIds::add<klassOop>(accessor), THREAD);
  }
}

oop C1XCompiler::get_RiField(ciField *field, TRAPS) {
  oop field_holder = get_RiType(field->holder(), NULL, CHECK_0);
  oop field_type = get_RiType(field->type(), NULL, CHECK_0);
  Handle field_name = VmIds::toString<Handle>(field->name()->get_symbolOop(), CHECK_0);
  int offset = field->offset();

  // TODO: implement caching
  return VMExits::createRiField(field_holder, field_name, field_type, offset, THREAD);
}

