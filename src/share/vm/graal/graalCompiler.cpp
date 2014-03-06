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
#include "memory/oopFactory.hpp"
#include "runtime/javaCalls.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalRuntime.hpp"
#include "runtime/arguments.hpp"
#include "runtime/compilationPolicy.hpp"

GraalCompiler* GraalCompiler::_instance = NULL;

GraalCompiler::GraalCompiler() : AbstractCompiler(graal) {
  _initialized = false;
  assert(_instance == NULL, "only one instance allowed");
  _instance = this;
}

// Initialization
void GraalCompiler::initialize() {
  
  ThreadToNativeFromVM trans(JavaThread::current());
  JavaThread* THREAD = JavaThread::current();
  TRACE_graal_1("GraalCompiler::initialize");

  uintptr_t heap_end = (uintptr_t) Universe::heap()->reserved_region().end();
  uintptr_t allocation_end = heap_end + ((uintptr_t)16) * 1024 * 1024 * 1024;
  AMD64_ONLY(guarantee(heap_end < allocation_end, "heap end too close to end of address space (might lead to erroneous TLAB allocations)"));
  NOT_LP64(error("check TLAB allocation code for address space conflicts"));

  _deopted_leaf_graph_count = 0;

  BufferBlob* buffer_blob = initialize_buffer_blob();
  if (buffer_blob == NULL) {
    // If we are called from JNI_CreateJavaVM we cannot use set_state yet because it takes a lock.
    // set_state(failed);
  } else {
    // set_state(initialized);
  }

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
    GRAAL_VM_ENTRY_MARK;
    check_pending_exception("Could not register natives");
  }

  graal_compute_offsets();

  // Ensure _non_oop_bits is initialized
  Universe::non_oop_word();

  {
    GRAAL_VM_ENTRY_MARK;
    HandleMark hm;
    VMToCompiler::initOptions();
    for (int i = 0; i < Arguments::num_graal_args(); ++i) {
      const char* arg = Arguments::graal_args_array()[i];
      Handle option = java_lang_String::create_from_str(arg, THREAD);
      jboolean result = VMToCompiler::setOption(option);
      if (!result) {
        tty->print_cr("Invalid option for graal: -G:%s", arg);
        vm_abort(false);
      }
    }
    VMToCompiler::finalizeOptions(CITime || CITimeEach);

    if (UseCompiler) {
      bool bootstrap = GRAALVM_ONLY(BootstrapGraal) NOT_GRAALVM(false);
      VMToCompiler::startCompiler(bootstrap);
      _initialized = true;
      CompilationPolicy::completed_vm_startup();
      if (bootstrap) {
        // Avoid -Xcomp and -Xbatch problems by turning on interpreter and background compilation for bootstrapping.
        FlagSetting a(UseInterpreter, true);
        FlagSetting b(BackgroundCompilation, true);
#ifndef PRODUCT
        // Turn off CompileTheWorld during bootstrap so that a counter overflow event
        // triggers further compilation (see NonTieredCompPolicy::event()) hence
        // allowing a complete bootstrap
        FlagSetting c(CompileTheWorld, false);
#endif
        VMToCompiler::bootstrap();
      }

#ifndef PRODUCT
      if (CompileTheWorld) {
        // We turn off CompileTheWorld so that Graal can
        // be compiled by C1/C2 when Graal does a CTW.
        CompileTheWorld = false;
        VMToCompiler::compileTheWorld();
      }
#endif
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
    elements = NEW_C_HEAP_ARRAY(jlong, length, mtCompiler);
    for (int i = 0; i < length; i++) {
      elements[i] = _deopted_leaf_graphs[i];
    }
    _deopted_leaf_graph_count = 0;
  }
  typeArrayOop array = oopFactory::new_longArray(length, CHECK_NULL);
  for (int i = 0; i < length; i++) {
    array->long_at_put(i, elements[i]);
  }
  FREE_C_HEAP_ARRAY(jlong, elements, mtCompiler);
  return array;
}

BufferBlob* GraalCompiler::initialize_buffer_blob() {
  JavaThread* THREAD = JavaThread::current();
  BufferBlob* buffer_blob = THREAD->get_buffer_blob();
  if (buffer_blob == NULL) {
    buffer_blob = BufferBlob::create("Graal thread-local CodeBuffer", GraalNMethodSizeLimit);
    if (buffer_blob != NULL) {
      THREAD->set_buffer_blob(buffer_blob);
    }
  }
  return buffer_blob;
}

void GraalCompiler::compile_method(methodHandle method, int entry_bci, jboolean blocking) {
  GRAAL_EXCEPTION_CONTEXT
  if (!_initialized) {
    CompilationPolicy::policy()->delay_compilation(method());
    return;
  }

  assert(_initialized, "must already be initialized");
  ResourceMark rm;
  thread->set_is_graal_compiling(true);
  VMToCompiler::compileMethod(method(), entry_bci, blocking);
  thread->set_is_graal_compiling(false);
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

KlassHandle GraalCompiler::get_KlassFromSignature(Symbol* signature, KlassHandle loading_klass) {
  BasicType field_type = FieldType::basic_type(signature);
  // If the field is a pointer type, get the klass of the field.
  if (field_type == T_OBJECT || field_type == T_ARRAY) {
    return GraalEnv::get_klass_by_name(loading_klass, signature, false);
  }
  return NULL;
}

KlassHandle GraalCompiler::get_Klass(constantPoolHandle cp, int index, KlassHandle loading_klass, Symbol*& klass_name) {
  bool is_accessible = false;

  KlassHandle klass = GraalEnv::get_klass_by_index(cp, index, is_accessible, loading_klass);
  if (klass.is_null()) {
    klass_name = NULL;
    {
      // We have to lock the cpool to keep the oop from being resolved
      // while we are accessing it. But we must release the lock before
      // calling up into Java.
      MonitorLockerEx ml(cp->lock());
      constantTag tag = cp->tag_at(index);
      if (tag.is_klass()) {
        // The klass has been inserted into the constant pool
        // very recently.
        klass = cp->resolved_klass_at(index);
        klass_name = klass->name();
        return klass;
      } else if (tag.is_symbol()) {
        klass_name = cp->symbol_at(index);
      } else {
        assert(cp->tag_at(index).is_unresolved_klass(), "wrong tag");
        klass_name = cp->unresolved_klass_at(index);
      }
    }
  } else {
    klass_name = klass->name();
  }
  return klass;
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
      fatal(err_msg("unexpected Kind: %c", ch));
      break;
  }
  return T_ILLEGAL;
}
