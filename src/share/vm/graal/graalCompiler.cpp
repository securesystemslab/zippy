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
#include "graal/graalVMToCompiler.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalVmIds.hpp"
#include "graal/graalEnv.hpp"
#include "c1/c1_Runtime1.hpp"
#include "compiler/compilerOracle.hpp"
#include "runtime/arguments.hpp"
#include "runtime/compilationPolicy.hpp"

GraalCompiler* GraalCompiler::_instance = NULL;

GraalCompiler::GraalCompiler() {
  _initialized = false;
  assert(_instance == NULL, "only one instance allowed");
  _instance = this;
}

// Initialization
void GraalCompiler::initialize() {
  
  ThreadToNativeFromVM trans(JavaThread::current());
  JavaThread* THREAD = JavaThread::current();
  TRACE_graal_1("GraalCompiler::initialize");

  _deopted_leaf_graph_count = 0;

  initialize_buffer_blob();
  Runtime1::initialize(THREAD->get_buffer_blob());

  JNIEnv *env = ((JavaThread *) Thread::current())->jni_environment();
  jclass klass = env->FindClass("com/oracle/graal/hotspot/bridge/CompilerToVMImpl");
  if (klass == NULL) {
    tty->print_cr("graal CompilerToVMImpl class not found");
    vm_abort(false);
  }
  env->RegisterNatives(klass, CompilerToVM_methods, CompilerToVM_methods_count());

  ResourceMark rm;
  HandleMark hm;
  {
    VM_ENTRY_MARK;
    check_pending_exception("Could not register natives");
  }

  graal_compute_offsets();

  {
    VM_ENTRY_MARK;
    HandleMark hm;
    VMToCompiler::setDefaultOptions();
    for (int i = 0; i < Arguments::num_graal_args(); ++i) {
      const char* arg = Arguments::graal_args_array()[i];
      Handle option = java_lang_String::create_from_str(arg, THREAD);
      jboolean result = VMToCompiler::setOption(option);
      if (!result) {
        tty->print_cr("Invalid option for graal: -G:%s", arg);
        vm_abort(false);
      }
    }
    if (UseCompiler) {
      VMToCompiler::startCompiler();
      _initialized = true;
      if (BootstrapGraal) {
        VMToCompiler::bootstrap();
      }
    }
  }
}

void GraalCompiler::deopt_leaf_graph(jlong leaf_graph_id) {
  assert(leaf_graph_id != -1, "unexpected leaf graph id");

  if (_deopted_leaf_graph_count < LEAF_GRAPH_ARRAY_SIZE) {
    MutexLockerEx y(GraalDeoptLeafGraphIds_lock, Mutex::_no_safepoint_check_flag);
    if (_deopted_leaf_graph_count < LEAF_GRAPH_ARRAY_SIZE) {
      _deopted_leaf_graphs[_deopted_leaf_graph_count++] = leaf_graph_id;
    }
  }
}

oop GraalCompiler::dump_deopted_leaf_graphs(TRAPS) {
  if (_deopted_leaf_graph_count == 0) {
    return NULL;
  }
  jlong* elements;
  int length;
  {
    MutexLockerEx y(GraalDeoptLeafGraphIds_lock, Mutex::_no_safepoint_check_flag);
    if (_deopted_leaf_graph_count == 0) {
      return NULL;
    }
    if (_deopted_leaf_graph_count == LEAF_GRAPH_ARRAY_SIZE) {
      length = 0;
    } else {
      length = _deopted_leaf_graph_count;
    }
    elements = new jlong[length];
    for (int i = 0; i < length; i++) {
      elements[i] = _deopted_leaf_graphs[i];
    }
    _deopted_leaf_graph_count = 0;
  }
  typeArrayOop array = oopFactory::new_longArray(length, CHECK_NULL);
  for (int i = 0; i < length; i++) {
    array->long_at_put(i, elements[i]);
  }
  delete elements;
  return array;
}

void GraalCompiler::initialize_buffer_blob() {

  JavaThread* THREAD = JavaThread::current();
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

void GraalCompiler::compile_method(methodHandle method, int entry_bci, jboolean blocking) {
  EXCEPTION_CONTEXT
  if (!_initialized) {
    method->clear_queued_for_compilation();
    method->invocation_counter()->reset();
    method->backedge_counter()->reset();
    return;
  }
  assert(_initialized, "must already be initialized");
  ResourceMark rm;
  ciEnv* current_env = JavaThread::current()->env();
  JavaThread::current()->set_env(NULL);
  JavaThread::current()->set_compiling(true);
  Handle hotspot_method = GraalCompiler::createHotSpotMethodResolved(method, CHECK);
  jboolean success = VMToCompiler::compileMethod(hotspot_method, entry_bci, blocking, method->graal_priority());
  JavaThread::current()->set_compiling(false);
  JavaThread::current()->set_env(current_env);
  if (success != JNI_TRUE) {
    method->clear_queued_for_compilation();
    CompilationPolicy::policy()->delay_compilation(method());
  }
}

// Compilation entry point for methods
void GraalCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  ShouldNotReachHere();
}

void GraalCompiler::exit() {
  if (_initialized) {
    VMToCompiler::shutdownCompiler();
  }
}

// Print compilation timers and statistics
void GraalCompiler::print_timers() {
  TRACE_graal_1("GraalCompiler::print_timers");
}

Handle GraalCompiler::get_RiType(Symbol* klass_name, TRAPS) {
   return VMToCompiler::createRiTypeUnresolved(VmIds::toString<Handle>(klass_name, THREAD), THREAD);
}

Handle GraalCompiler::get_RiTypeFromSignature(constantPoolHandle cp, int index, KlassHandle loading_klass, TRAPS) {
  
  Symbol* signature = cp->symbol_at(index);
  BasicType field_type = FieldType::basic_type(signature);
  // If the field is a pointer type, get the klass of the
  // field.
  if (field_type == T_OBJECT || field_type == T_ARRAY) {
    KlassHandle handle = GraalEnv::get_klass_by_name(loading_klass, signature, false);
    if (handle.is_null()) {
      return get_RiType(signature, CHECK_NULL);
    } else {
      return get_RiType(handle, CHECK_NULL);
    }
  } else {
    return VMToCompiler::createRiTypePrimitive(field_type, CHECK_NULL);
  }
}

Handle GraalCompiler::get_RiType(constantPoolHandle cp, int index, KlassHandle loading_klass, TRAPS) {
  bool is_accessible = false;

  KlassHandle klass = GraalEnv::get_klass_by_index(cp, index, is_accessible, loading_klass);
  oop catch_class = NULL;
  if (klass.is_null()) {
    // We have to lock the cpool to keep the oop from being resolved
    // while we are accessing it.
    ObjectLocker ol(cp, THREAD);

    Symbol* klass_name = NULL;
    constantTag tag = cp->tag_at(index);
    if (tag.is_klass()) {
      // The klass has been inserted into the constant pool
      // very recently.
      return GraalCompiler::get_RiType(cp->resolved_klass_at(index), CHECK_NULL);
    } else if (tag.is_symbol()) {
      klass_name = cp->symbol_at(index);
    } else {
      assert(cp->tag_at(index).is_unresolved_klass(), "wrong tag");
      klass_name = cp->unresolved_klass_at(index);
    }
    return GraalCompiler::get_RiType(klass_name, CHECK_NULL);
  } else {
    return GraalCompiler::get_RiType(klass, CHECK_NULL);
  }
}

Handle GraalCompiler::get_RiType(KlassHandle klass, TRAPS) {
  Handle name = VmIds::toString<Handle>(klass->name(), THREAD);
  return createHotSpotTypeResolved(klass, name, CHECK_NULL);
}

Handle GraalCompiler::get_RiField(int offset, int flags, Symbol* field_name, Handle field_holder, Handle field_type, Bytecodes::Code byteCode, TRAPS) {
  Handle name = VmIds::toString<Handle>(field_name, CHECK_NULL);
  return VMToCompiler::createRiField(field_holder, name, field_type, offset, flags, CHECK_NULL);
}

Handle GraalCompiler::createHotSpotTypeResolved(KlassHandle klass, Handle name, TRAPS) {
  ObjectLocker ol(klass, THREAD);

  if (klass->graal_mirror() != NULL) {
    return klass->graal_mirror();
  }

  instanceKlass::cast(HotSpotTypeResolved::klass())->initialize(CHECK_NULL);
  Handle obj = instanceKlass::cast(HotSpotTypeResolved::klass())->allocate_instance(CHECK_NULL);
  assert(obj() != NULL, "must succeed in allocating instance");

  if (klass->oop_is_instance()) {
    ResourceMark rm;
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
  HotSpotTypeResolved::set_superCheckOffset(obj, klass->super_check_offset());
  HotSpotTypeResolved::set_isInstanceClass(obj, klass->oop_is_instance());

  if (klass->oop_is_javaArray()) {
    HotSpotTypeResolved::set_isArrayClass(obj, true);
  } else {
    HotSpotTypeResolved::set_isArrayClass(obj, false);
    HotSpotTypeResolved::set_instanceSize(obj, instanceKlass::cast(klass())->size_helper() * HeapWordSize);
    HotSpotTypeResolved::set_hasFinalizer(obj, klass->has_finalizer());
  }

  // TODO replace these with correct values
  HotSpotTypeResolved::set_hasFinalizableSubclass(obj, false);

  klass->set_graal_mirror(obj());

  return obj;
}

Handle GraalCompiler::createHotSpotMethodResolved(methodHandle method, TRAPS) {
  if (method->graal_mirror() != NULL) {
    assert(method->graal_mirror()->is_a(HotSpotMethodResolved::klass()), "unexpected class...");
    return method->graal_mirror();
  }
  Handle name = VmIds::toString<Handle>(method->name(), CHECK_NULL);

  instanceKlass::cast(HotSpotMethodResolved::klass())->initialize(CHECK_NULL);
  Handle obj = instanceKlass::cast(HotSpotMethodResolved::klass())->allocate_instance(CHECK_NULL);
  assert(obj() != NULL, "must succeed in allocating instance");

  // (thomaswue) Cannot use reflection here, because the compiler thread could dead lock with the running application.
  // oop reflected = getReflectedMethod(method(), CHECK_NULL);
  HotSpotMethodResolved::set_javaMirror(obj, method());
  HotSpotMethodResolved::set_name(obj, name());
  
  KlassHandle klass = method->method_holder();
  Handle holder_name = VmIds::toString<Handle>(klass->name(), CHECK_NULL);
  Handle holder = GraalCompiler::createHotSpotTypeResolved(klass, holder_name, CHECK_NULL);
  HotSpotMethodResolved::set_holder(obj, holder());
  
  HotSpotMethodResolved::set_codeSize(obj, method->code_size());
  HotSpotMethodResolved::set_accessFlags(obj, method->access_flags().as_int());
  HotSpotMethodResolved::set_maxLocals(obj, method->max_locals());
  HotSpotMethodResolved::set_maxStackSize(obj, method->max_stack());
  HotSpotMethodResolved::set_canBeInlined(obj, !method->is_not_compilable() && !CompilerOracle::should_not_inline(method));
  
  method->set_graal_mirror(obj());
  return obj;
}

Handle GraalCompiler::createHotSpotMethodData(methodDataHandle method_data, TRAPS) {
  if(method_data->graal_mirror() != NULL) {
    assert(method_data->graal_mirror()->is_a(HotSpotMethodData::klass()), "unexpected class");
    return method_data->graal_mirror();
  }

  instanceKlass::cast(HotSpotMethodData::klass())->initialize(CHECK_NULL);
  Handle obj = instanceKlass::cast(HotSpotMethodData::klass())->allocate_instance(CHECK_NULL);
  assert(obj.not_null(), "must succeed in allocating instance");
  
  HotSpotMethodData::set_hotspotMirror(obj, method_data());
  HotSpotMethodData::set_normalDataSize(obj, method_data()->data_size());
  HotSpotMethodData::set_extraDataSize(obj, method_data()->extra_data_size());

  method_data->set_graal_mirror(obj());
  return obj;
}

BasicType GraalCompiler::kindToBasicType(jchar ch) {
  switch(ch) {
    case 'z': return T_BOOLEAN;
    case 'b': return T_BYTE;
    case 's': return T_SHORT;
    case 'c': return T_CHAR;
    case 'i': return T_INT;
    case 'f': return T_FLOAT;
    case 'j': return T_LONG;
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
