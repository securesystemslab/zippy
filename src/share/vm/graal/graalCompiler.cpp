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
#include "graal/graalCompiler.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalVMExits.hpp"
#include "graal/graalVMEntries.hpp"
#include "graal/graalVmIds.hpp"
#include "c1/c1_Runtime1.hpp"
#include "runtime/arguments.hpp"

GraalCompiler* GraalCompiler::_instance = NULL;

GraalCompiler::GraalCompiler() {
  _initialized = false;
  assert(_instance == NULL, "only one instance allowed");
  _instance = this;
}

// Initialization
void GraalCompiler::initialize() {
  if (_initialized) return;
  CompilerThread* THREAD = CompilerThread::current();
  _initialized = true;
  TRACE_graal_1("GraalCompiler::initialize");

  VmIds::initializeObjects();

  initialize_buffer_blob();
  Runtime1::initialize(THREAD->get_buffer_blob());

  JNIEnv *env = ((JavaThread *) Thread::current())->jni_environment();
  jclass klass = env->FindClass("com/oracle/max/graal/runtime/VMEntriesNative");
  if (klass == NULL) {
    tty->print_cr("graal VMEntries class not found");
    vm_abort(false);
  }
  env->RegisterNatives(klass, VMEntries_methods, VMEntries_methods_count());

  {
    VM_ENTRY_MARK;
    check_pending_exception("Could not register natives");
  }

  graal_compute_offsets();

  {
    VM_ENTRY_MARK;
    HandleMark hm;
    VMExits::setDefaultOptions();
    for (int i = 0; i < Arguments::num_graal_args(); ++i) {
      const char* arg = Arguments::graal_args_array()[i];
      Handle option = java_lang_String::create_from_str(arg, THREAD);
      jboolean result = VMExits::setOption(option);
      if (!result) {
        tty->print_cr("Invalid option for graal!");
        vm_abort(false);
      }
    }

    VMExits::initializeCompiler();
  }
}

void GraalCompiler::initialize_buffer_blob() {

  CompilerThread* THREAD = CompilerThread::current();
  if (THREAD->get_buffer_blob() == NULL) {
    // setup CodeBuffer.  Preallocate a BufferBlob of size
    // NMethodSizeLimit plus some extra space for constants.
    int code_buffer_size = Compilation::desired_max_code_buffer_size() +
      Compilation::desired_max_constant_size();
    BufferBlob* blob = BufferBlob::create("graal temporary CodeBuffer",
                                          code_buffer_size);
    guarantee(blob != NULL, "must create code buffer");
    THREAD->set_buffer_blob(blob);
  }
}

// Compilation entry point for methods
void GraalCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  initialize();
  VM_ENTRY_MARK;
  ResourceMark rm;
  HandleMark hm;

  initialize_buffer_blob();
  VmIds::initializeObjects();

  TRACE_graal_2("GraalCompiler::compile_method");

  CompilerThread::current()->set_compiling(true);
  methodOop method = (methodOop) target->get_oop();
  VMExits::compileMethod(VmIds::add<methodOop>(method), VmIds::toString<Handle>(method->name(), THREAD), entry_bci);
  CompilerThread::current()->set_compiling(false);

  VmIds::cleanupLocalObjects();
  TRACE_graal_2("GraalCompiler::compile_method exit");
}

void GraalCompiler::exit() {
  VMExits::shutdownCompiler();
}

// Print compilation timers and statistics
void GraalCompiler::print_timers() {
  TRACE_graal_1("GraalCompiler::print_timers");
}

oop GraalCompiler::get_RiType(ciType *type, KlassHandle accessor, TRAPS) {
  if (type->is_loaded()) {
    if (type->is_primitive_type()) {
      return VMExits::createRiTypePrimitive((int) type->basic_type(), THREAD);
    }
    KlassHandle klass = (klassOop) type->get_oop();
    Handle name = VmIds::toString<Handle>(klass->name(), THREAD);
    return createHotSpotTypeResolved(klass, name, CHECK_NULL);
  } else {
    Symbol* name = ((ciKlass *) type)->name()->get_symbol();
    return VMExits::createRiTypeUnresolved(VmIds::toString<Handle>(name, THREAD), THREAD);
  }
}

oop GraalCompiler::get_RiField(ciField *field, ciInstanceKlass* accessor_klass, KlassHandle accessor, Bytecodes::Code byteCode, TRAPS) {
  bool will_link = field->will_link_from_vm(accessor_klass, byteCode);
  int offset = (field->holder()->is_loaded() && will_link) ? field->offset() : -1;
  Handle field_name = VmIds::toString<Handle>(field->name()->get_symbol(), CHECK_0);
  Handle field_holder = get_RiType(field->holder(), accessor, CHECK_0);
  Handle field_type = get_RiType(field->type(), accessor, CHECK_0);
  int flags = field->flags().as_int();
  return VMExits::createRiField(field_holder, field_name, field_type, offset, flags, THREAD);
}

oop GraalCompiler::createHotSpotTypeResolved(KlassHandle klass, Handle name, TRAPS) {
  if (klass->graal_mirror() != NULL) {
    return klass->graal_mirror();
  }

  instanceKlass::cast(HotSpotTypeResolved::klass())->initialize(CHECK_NULL);
  Handle obj = instanceKlass::cast(HotSpotTypeResolved::klass())->allocate_instance(CHECK_NULL);
  assert(obj() != NULL, "must succeed in allocating instance");

  HotSpotTypeResolved::set_compiler(obj, VMExits::compilerInstance()());

  if (klass->oop_is_instance()) {
    instanceKlass* ik = (instanceKlass*)klass()->klass_part();
    Handle full_name = java_lang_String::create_from_str(ik->signature_name(), CHECK_NULL);
    HotSpotType::set_name(obj, full_name());
  } else {
    HotSpotType::set_name(obj, name());
  }

  HotSpotTypeResolved::set_javaMirror(obj, klass->java_mirror());
  HotSpotTypeResolved::set_simpleName(obj, name());
  HotSpotTypeResolved::set_accessFlags(obj, klass->access_flags().as_int());
  HotSpotTypeResolved::set_isInterface(obj, klass->is_interface());
  HotSpotTypeResolved::set_isInstanceClass(obj, klass->oop_is_instance());

  if (klass->oop_is_javaArray()) {
    HotSpotTypeResolved::set_isArrayClass(obj, true);
    HotSpotTypeResolved::set_componentType(obj, NULL);
  } else {
    HotSpotTypeResolved::set_isArrayClass(obj, false);
    HotSpotTypeResolved::set_componentType(obj, NULL);
    HotSpotTypeResolved::set_isInitialized(obj, instanceKlass::cast(klass())->is_initialized());
    HotSpotTypeResolved::set_instanceSize(obj, instanceKlass::cast(klass())->size_helper() * HeapWordSize);
    HotSpotTypeResolved::set_hasFinalizer(obj, klass->has_finalizer());
  }

  // TODO replace these with correct values
  HotSpotTypeResolved::set_hasSubclass(obj, false);
  HotSpotTypeResolved::set_hasFinalizableSubclass(obj, false);

  klass->set_graal_mirror(obj());

  return obj();
}

BasicType GraalCompiler::kindToBasicType(jchar ch) {
  switch(ch) {
    case 'z': return T_BOOLEAN;
    case 'b': return T_BYTE;
    case 's': return T_SHORT;
    case 'c': return T_CHAR;
    case 'i': return T_INT;
    case 'f': return T_FLOAT;
    case 'l': return T_LONG;
    case 'd': return T_DOUBLE;
    case 'a': return T_OBJECT;
    case 'r': return T_ADDRESS;
    case '-': return T_ILLEGAL;
    default:
      fatal(err_msg("unexpected CiKind: %c", ch));
      break;
  }
  return T_ILLEGAL;
}

