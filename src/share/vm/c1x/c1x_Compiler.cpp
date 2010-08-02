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
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  initialize();

  VM_ENTRY_MARK;
  
  ResourceMark rm;
  HandleMark hm;

  CompilerThread::current()->set_compiling(true);
  VMExits::compileMethod((methodOop)target->get_oop(), entry_bci);
  CompilerThread::current()->set_compiling(false);
}

// Print compilation timers and statistics
void C1XCompiler::print_timers() {
	TRACE_C1X_1("print_timers");
}

oop C1XCompiler::get_RiType(oop name, klassOop accessingType, TRAPS) {
  symbolOop klass = java_lang_String::as_symbol_or_null(name);

  if (klass == vmSymbols::byte_signature()) {
    return VMExits::createRiTypePrimitive((int)T_BYTE, THREAD);
  } else if (klass == vmSymbols::char_signature()) {
    return VMExits::createRiTypePrimitive((int)T_CHAR, THREAD);
  } else if (klass == vmSymbols::double_signature()) {
    return VMExits::createRiTypePrimitive((int)T_DOUBLE, THREAD);
  } else if (klass == vmSymbols::float_signature()) {
    return VMExits::createRiTypePrimitive((int)T_FLOAT, THREAD);
  } else if (klass == vmSymbols::int_signature()) {
    return VMExits::createRiTypePrimitive((int)T_INT, THREAD);
  } else if (klass == vmSymbols::long_signature()) {
    return VMExits::createRiTypePrimitive((int)T_LONG, THREAD);
  } else if (klass == vmSymbols::bool_signature()) {
    return VMExits::createRiTypePrimitive((int)T_BOOLEAN, THREAD);
  }
  Handle classloader;
  if (accessingType != NULL) {
    classloader = accessingType->klass_part()->class_loader();
  }
  klassOop resolved_type = SystemDictionary::resolve_or_null(klass, classloader, accessingType->klass_part()->protection_domain(), Thread::current());
  if (resolved_type != NULL) {
    return VMExits::createRiType(resolved_type, THREAD);
  } else {
    return VMExits::createRiTypeUnresolved(klass, accessingType, THREAD);
  }
}

oop C1XCompiler::get_RiType(ciType *type, klassOop accessor, TRAPS) {
  if (type->is_loaded()) {
    if (type->is_primitive_type()) {
      return VMExits::createRiTypePrimitive((int)type->basic_type(), THREAD);
    }
    return VMExits::createRiType((klassOop)type->get_oop(), THREAD);
  } else {
    return VMExits::createRiTypeUnresolved(((ciKlass *)type)->name()->get_symbolOop(), accessor, THREAD);
  }
}

oop C1XCompiler::get_RiField(ciField *field, TRAPS) {
  oop field_holder = get_RiType(field->holder(), NULL, CHECK_0);
  oop field_type = get_RiType(field->type(), NULL, CHECK_0);
  symbolOop field_name = field->name()->get_symbolOop();
  int offset = field->offset();

  // TODO: implement caching
  return VMExits::createRiField(field_holder, field_name, field_type, offset, THREAD);
}

/*
oop C1XCompiler::get_RiMethod(ciMethod *method, TRAPS) {
  methodOop m = (methodOop)method->get_oop();
  return get_RiMethod(m, THREAD);
}

oop C1XCompiler::get_RiMethod(methodOop m, TRAPS) {
  // TODO: implement caching
  return VMExits::createRiMethod(m, THREAD);
}


oop C1XCompiler::get_RiType(klassOop klass, TRAPS) {
  // TODO: implement caching
  return VMExits::createRiType(klass, THREAD);
}

oop C1XCompiler::get_RiConstantPool(constantPoolOop cp, TRAPS) {
  // TODO: implement caching
  return VMExits::createRiConstantPool(cp, THREAD);
}

oop C1XCompiler::get_unresolved_RiType(symbolOop klass, klassOop accessingType, TRAPS)  {
  // TODO: implement caching
  return VMExits::createRiTypeUnresolved(klass, accessingType, THREAD);
}
*/

// conversion internal objects -> reflected objects

oop C1XObjects::getReflectedMethod(methodOop method, TRAPS) {
  if (method->is_initializer()) {
    return Reflection::new_constructor(method, CHECK_0);
  } else {
    return Reflection::new_method(method, UseNewReflection, false, CHECK_0);
  }
}

oop C1XObjects::getReflectedClass(klassOop klass) {
  return klass->klass_part()->java_mirror();
}

oop C1XObjects::getReflectedSymbol(symbolOop symbol, TRAPS) {
  return java_lang_String::create_from_symbol(symbol, THREAD)();
}

// conversion reflected objects -> internal objects

methodOop C1XObjects::getInternalMethod(oop method) {
  // copied from JNIEnv::FromReflectedMethod
  oop mirror     = NULL;
  int slot       = 0;

  if (method->klass() == SystemDictionary::reflect_Constructor_klass()) {
    mirror = java_lang_reflect_Constructor::clazz(method);
    slot   = java_lang_reflect_Constructor::slot(method);
  } else {
    assert(method->klass() == SystemDictionary::reflect_Method_klass(), "wrong type");
    mirror = java_lang_reflect_Method::clazz(method);
    slot   = java_lang_reflect_Method::slot(method);
  }
  klassOop k     = java_lang_Class::as_klassOop(mirror);
  return instanceKlass::cast(k)->method_with_idnum(slot);
}

klassOop C1XObjects::getInternalClass(oop klass) {
  return java_lang_Class::as_klassOop(klass);
}

symbolOop C1XObjects::getInternalSymbol(oop string) {
  return java_lang_String::as_symbol_or_null(string);
}



