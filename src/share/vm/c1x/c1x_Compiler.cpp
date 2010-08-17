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

  compute_offsets();
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  initialize();

  VM_ENTRY_MARK;
  
  ResourceMark rm;
  HandleMark hm;

  C1XObjects::initializeObjects();

  CompilerThread::current()->set_compiling(true);
  methodOop method = (methodOop)target->get_oop();
  VMExits::compileMethod(C1XObjects::add<methodOop>(method), C1XObjects::toString<Handle>(method->name(), THREAD), entry_bci);
  CompilerThread::current()->set_compiling(false);

  C1XObjects::cleanupLocalObjects();
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
    return VMExits::createRiType(C1XObjects::add<klassOop>(klass), C1XObjects::toString<Handle>(klass->klass_part()->name(), THREAD), THREAD);
  } else {
    symbolOop name = ((ciKlass *)type)->name()->get_symbolOop();
    return VMExits::createRiTypeUnresolved(C1XObjects::toString<Handle>(name, THREAD), C1XObjects::add<klassOop>(accessor), THREAD);
  }
}

oop C1XCompiler::get_RiField(ciField *field, TRAPS) {
  oop field_holder = get_RiType(field->holder(), NULL, CHECK_0);
  oop field_type = get_RiType(field->type(), NULL, CHECK_0);
  Handle field_name = C1XObjects::toString<Handle>(field->name()->get_symbolOop(), CHECK_0);
  int offset = field->offset();

  // TODO: implement caching
  return VMExits::createRiField(field_holder, field_name, field_type, offset, THREAD);
}

// C1XObjects implementation

GrowableArray<address>* C1XObjects::_stubs = NULL;
GrowableArray<jobject>* C1XObjects::_localHandles = NULL;

void C1XObjects::initializeObjects() {
  if (_stubs == NULL) {
    assert(_localHandles == NULL, "inconsistent state");
    _stubs = new(ResourceObj::C_HEAP) GrowableArray<address>(64, true);
    _localHandles = new(ResourceObj::C_HEAP) GrowableArray<jobject>(64, true);
  }
  assert(_localHandles->length() == 0, "invalid state");
}

void C1XObjects::cleanupLocalObjects() {
  for (int i=0; i<_localHandles->length(); i++) {
    JNIHandles::destroy_global(_localHandles->at(i));
  }
  _localHandles->clear();
}

jlong C1XObjects::addStub(address stub) {
  assert(!_stubs->contains(stub), "duplicate stub");
  return _stubs->append(stub) | STUB;
}

jlong C1XObjects::add(Handle obj, CompilerObjectType type) {
  assert(!obj.is_null(), "cannot add NULL handle");
  int idx = -1;
  for (int i=0; i<_localHandles->length(); i++)
    if (JNIHandles::resolve_non_null(_localHandles->at(i)) == obj()) {
      idx = i;
      break;
    }
  if (idx = -1) {
    if (JavaThread::current()->thread_state() == _thread_in_vm) {
      idx = _localHandles->append(JNIHandles::make_global(obj));
    } else {
      VM_ENTRY_MARK;
      idx = _localHandles->append(JNIHandles::make_global(obj));
    }
  }
  return idx | type;
}

address C1XObjects::getStub(jlong id) {
  assert((id & TYPE_MASK) == STUB, "wrong id type, STUB expected");
  assert((id & ~TYPE_MASK) >= 0 && (id & ~TYPE_MASK) < _stubs->length(), "STUB index out of bounds");
  return _stubs->at(id & ~TYPE_MASK);
}

oop C1XObjects::getObject(jlong id) {
  assert((id & TYPE_MASK) != STUB, "wrong id type");
  assert((id & ~TYPE_MASK) >= 0 && (id & ~TYPE_MASK) < _localHandles->length(), "index out of bounds");
  return JNIHandles::resolve_non_null(_localHandles->at(id & ~TYPE_MASK));
}


static void compute_offset(int &dest_offset, klassOop klass_oop, const char* name, const char* signature) {
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
  dest_offset = fd.offset();
}

// create the compute_class
#define START_CLASS(name) { klassOop k = SystemDictionary::name##_klass();

#define END_CLASS }

#define FIELD(klass, name, signature) compute_offset(klass::_##name##_offset, k, #name, signature);
#define CHAR_FIELD(klass, name) FIELD(klass, name, "C")
#define INT_FIELD(klass, name) FIELD(klass, name, "I")
#define LONG_FIELD(klass, name) FIELD(klass, name, "J")
#define OOP_FIELD(klass, name, signature) FIELD(klass, name, signature)


void C1XCompiler::compute_offsets() {
  COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, LONG_FIELD, OOP_FIELD)
}

#define EMPTY0
#define EMPTY1(x)
#define EMPTY2(x,y)
#define FIELD2(klass, name) int klass::_##name##_offset = 0;
#define FIELD3(klass, name, sig) FIELD2(klass, name)

COMPILER_CLASSES_DO(EMPTY1, EMPTY0, FIELD2, FIELD2, FIELD2, FIELD3)





