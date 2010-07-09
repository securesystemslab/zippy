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
  jclass klass = env->FindClass("com/sun/hotspot/c1x/VMEntries");
  assert(klass != NULL, "c1x VMEntries class not found");
  env->RegisterNatives(klass, VMEntries_methods, VMEntries_methods_count() );
  
  if (Thread::current()->has_pending_exception()) {

    Thread::current()->pending_exception()->print();
    fatal("Could not register natives");
  }
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  
  initialize();

	VM_ENTRY_MARK;

	
	ResourceMark rm;
	//HandleMark hm;

  CompilerThread::current()->set_compiling(true);
  oop rimethod = get_RiMethod(target);
  VMExits::compileMethod(rimethod, entry_bci);
	CompilerThread::current()->set_compiling(false);
}

// Print compilation timers and statistics
void C1XCompiler::print_timers() {
	TRACE_C1X_1("print_timers");
}

oop C1XCompiler::get_RiMethod(ciMethod *method) {
  methodOop m = (methodOop)method->get_oop();
  return get_RiMethod(m);
}

oop C1XCompiler::get_RiField(ciField *field) {
  oop field_holder = get_RiType(field->holder());
  oop field_type = get_RiType(field->type());
  symbolOop field_name = field->name()->get_symbolOop();
  int offset = field->offset();

  // TODO: implement caching
  return VMExits::createRiField(field_holder, field_name, field_type, offset);
}

oop C1XCompiler::get_RiMethod(methodOop m) {
  // TODO: implement caching
  return VMExits::createRiMethod(m);
}

oop C1XCompiler::get_RiType(ciType *type) {
  if (type->is_loaded()) {
    if (type->is_primitive_type()) {
      return VMExits::createRiTypePrimitive((int)type->basic_type());
    }
    return get_RiType((klassOop)type->get_oop());
  } else {
    return get_unresolved_RiType(((ciKlass *)type)->name()->get_symbolOop(), NULL);
  }
}

oop C1XCompiler::get_RiType(klassOop klass) {
  // TODO: implement caching
  return VMExits::createRiType(klass);
}

oop C1XCompiler::get_RiConstantPool(constantPoolOop cp) {
  // TODO: implement caching
  return VMExits::createRiConstantPool(cp);
}

oop C1XCompiler::get_RiType(symbolOop klass, klassOop accessingType) {
  if (klass == vmSymbols::byte_signature()) {
    return VMExits::createRiTypePrimitive((int)T_BYTE);
  } else if (klass == vmSymbols::char_signature()) {
    return VMExits::createRiTypePrimitive((int)T_CHAR);
  } else if (klass == vmSymbols::double_signature()) {
    return VMExits::createRiTypePrimitive((int)T_DOUBLE);
  } else if (klass == vmSymbols::float_signature()) {
    return VMExits::createRiTypePrimitive((int)T_FLOAT);
  } else if (klass == vmSymbols::int_signature()) {
    return VMExits::createRiTypePrimitive((int)T_INT);
  } else if (klass == vmSymbols::long_signature()) {
    return VMExits::createRiTypePrimitive((int)T_LONG);
  } else if (klass == vmSymbols::bool_signature()) {
    return VMExits::createRiTypePrimitive((int)T_BOOLEAN);
  }
  klassOop resolved_type = SystemDictionary::resolve_or_null(klass, accessingType->klass_part()->class_loader(), accessingType->klass_part()->protection_domain(), Thread::current());
  if (resolved_type != NULL) {
    return get_RiType(resolved_type);
  } else {
    return get_unresolved_RiType(klass, accessingType);
  }
}

oop C1XCompiler::get_unresolved_RiType(symbolOop klass, klassOop accessingType)  {
  // TODO: implement caching
  return VMExits::createRiTypeUnresolved(klass, accessingType);
}
