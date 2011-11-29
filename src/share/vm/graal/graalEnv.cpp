/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#include "precompiled.hpp"
#include "graal/graalEnv.hpp"
#include "classfile/systemDictionary.hpp"
#include "classfile/vmSymbols.hpp"
#include "code/scopeDesc.hpp"
#include "runtime/sweeper.hpp"
#include "compiler/compileBroker.hpp"
#include "compiler/compileLog.hpp"
#include "compiler/compilerOracle.hpp"
#include "interpreter/linkResolver.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/oopFactory.hpp"
#include "memory/universe.inline.hpp"
#include "oops/methodDataOop.hpp"
#include "oops/objArrayKlass.hpp"
#include "prims/jvmtiExport.hpp"
#include "prims/methodHandleWalk.hpp"
#include "runtime/init.hpp"
#include "runtime/reflection.hpp"
#include "runtime/sharedRuntime.hpp"
#include "utilities/dtrace.hpp"
#include "c1/c1_Runtime1.hpp"

// ------------------------------------------------------------------
// ciEnv::check_klass_accessiblity
//
// Note: the logic of this method should mirror the logic of
// constantPoolOopDesc::verify_constant_pool_resolve.
bool GraalEnv::check_klass_accessibility(KlassHandle accessing_klass, KlassHandle resolved_klass) {
  if (accessing_klass->oop_is_objArray()) {
    accessing_klass = objArrayKlass::cast(accessing_klass())->bottom_klass();
  }
  if (!accessing_klass->oop_is_instance()) {
    return true;
  }

  if (resolved_klass->oop_is_objArray()) {
    // Find the element klass, if this is an array.
    resolved_klass = objArrayKlass::cast(resolved_klass())->bottom_klass();
  }
  if (resolved_klass->oop_is_instance()) {
    return Reflection::verify_class_access(accessing_klass(), resolved_klass(), true);
  }
  return true;
}

// ------------------------------------------------------------------
// ciEnv::get_klass_by_name_impl
KlassHandle GraalEnv::get_klass_by_name_impl(KlassHandle& accessing_klass,
                                          constantPoolHandle& cpool,
                                          Symbol* sym,
                                          bool require_local) {
  EXCEPTION_CONTEXT;

  // Now we need to check the SystemDictionary
  if (sym->byte_at(0) == 'L' &&
    sym->byte_at(sym->utf8_length()-1) == ';') {
    // This is a name from a signature.  Strip off the trimmings.
    // Call recursive to keep scope of strippedsym.
    TempNewSymbol strippedsym = SymbolTable::new_symbol(sym->as_utf8()+1,
                    sym->utf8_length()-2,
                    CHECK_(KlassHandle()));
    return get_klass_by_name_impl(accessing_klass, cpool, strippedsym, require_local);
  }

  Handle loader(THREAD, (oop)NULL);
  Handle domain(THREAD, (oop)NULL);
  if (!accessing_klass.is_null()) {
    loader = Handle(THREAD, accessing_klass->class_loader());
    domain = Handle(THREAD, accessing_klass->protection_domain());
  }

  KlassHandle found_klass;
  {
    ttyUnlocker ttyul;  // release tty lock to avoid ordering problems
    MutexLocker ml(Compile_lock);
    klassOop kls;
    if (!require_local) {
      kls = SystemDictionary::find_constrained_instance_or_array_klass(sym, loader, CHECK_(KlassHandle()));
    } else {
      kls = SystemDictionary::find_instance_or_array_klass(sym, loader, domain, CHECK_(KlassHandle()));
    }
    found_klass = KlassHandle(THREAD, kls);
  }

  // If we fail to find an array klass, look again for its element type.
  // The element type may be available either locally or via constraints.
  // In either case, if we can find the element type in the system dictionary,
  // we must build an array type around it.  The CI requires array klasses
  // to be loaded if their element klasses are loaded, except when memory
  // is exhausted.
  if (sym->byte_at(0) == '[' &&
      (sym->byte_at(1) == '[' || sym->byte_at(1) == 'L')) {
    // We have an unloaded array.
    // Build it on the fly if the element class exists.
    TempNewSymbol elem_sym = SymbolTable::new_symbol(sym->as_utf8()+1,
                                                 sym->utf8_length()-1,
                                                 CHECK_(KlassHandle()));

    // Get element ciKlass recursively.
    KlassHandle elem_klass =
      get_klass_by_name_impl(accessing_klass,
                             cpool,
                             elem_sym,
                             require_local);
    if (!elem_klass.is_null()) {
      // Now make an array for it
      return elem_klass->array_klass(CHECK_(KlassHandle()));
    }
  }

  if (found_klass.is_null() && !cpool.is_null() && cpool->has_preresolution()) {
    // Look inside the constant pool for pre-resolved class entries.
    for (int i = cpool->length() - 1; i >= 1; i--) {
      if (cpool->tag_at(i).is_klass()) {
        klassOop kls = cpool->resolved_klass_at(i);
        if (Klass::cast(kls)->name() == sym) {
          return kls;
        }
      }
    }
  }

  return found_klass();
}

// ------------------------------------------------------------------
// ciEnv::get_klass_by_name
KlassHandle GraalEnv::get_klass_by_name(KlassHandle& accessing_klass,
                                  Symbol* klass_name,
                                  bool require_local) {
  ResourceMark rm;
  constantPoolHandle cpool;
  return get_klass_by_name_impl(accessing_klass,
                                                 cpool,
                                                 klass_name,
                                                 require_local);
}

// ------------------------------------------------------------------
// ciEnv::get_klass_by_index_impl
//
// Implementation of get_klass_by_index.
KlassHandle GraalEnv::get_klass_by_index_impl(constantPoolHandle& cpool,
                                        int index,
                                        bool& is_accessible,
                                        KlassHandle& accessor) {
  EXCEPTION_CONTEXT;
  KlassHandle klass (THREAD, constantPoolOopDesc::klass_at_if_loaded(cpool, index));
  Symbol* klass_name = NULL;
  if (klass.is_null()) {
    // The klass has not been inserted into the constant pool.
    // Try to look it up by name.
    {
      // We have to lock the cpool to keep the oop from being resolved
      // while we are accessing it.
      ObjectLocker ol(cpool, THREAD);

      constantTag tag = cpool->tag_at(index);
      if (tag.is_klass()) {
        // The klass has been inserted into the constant pool
        // very recently.
        klass = KlassHandle(THREAD, cpool->resolved_klass_at(index));
      } else if (tag.is_symbol()) {
        klass_name = cpool->symbol_at(index);
      } else {
        assert(cpool->tag_at(index).is_unresolved_klass(), "wrong tag");
        klass_name = cpool->unresolved_klass_at(index);
      }
    }
  }

  if (klass.is_null()) {
    // Not found in constant pool.  Use the name to do the lookup.
    KlassHandle k = get_klass_by_name_impl(accessor,
                                        cpool,
                                        klass_name,
                                        false);
    // Calculate accessibility the hard way.
    if (k.is_null()) {
      is_accessible = false;
    } else if (k->class_loader() != accessor->class_loader() &&
               get_klass_by_name_impl(accessor, cpool, k->name(), true).is_null()) {
      // Loaded only remotely.  Not linked yet.
      is_accessible = false;
    } else {
      // Linked locally, and we must also check public/private, etc.
      is_accessible = check_klass_accessibility(accessor, k);
    }
    if (!is_accessible) {
      return KlassHandle();
    }
    return k;
  }

  // It is known to be accessible, since it was found in the constant pool.
  is_accessible = true;
  return klass;
}

// ------------------------------------------------------------------
// ciEnv::get_klass_by_index
//
// Get a klass from the constant pool.
KlassHandle GraalEnv::get_klass_by_index(constantPoolHandle& cpool,
                                   int index,
                                   bool& is_accessible,
                                   KlassHandle& accessor) {
  ResourceMark rm;
  KlassHandle result = get_klass_by_index_impl(cpool, index, is_accessible, accessor);
  return result;
}

// ------------------------------------------------------------------
// ciEnv::get_field_by_index_impl
//
// Implementation of get_field_by_index.
//
// Implementation note: the results of field lookups are cached
// in the accessor klass.
void GraalEnv::get_field_by_index_impl(instanceKlassHandle& klass, fieldDescriptor& field_desc,
                                        int index) {
  EXCEPTION_CONTEXT;

  assert(klass->is_linked(), "must be linked before using its constan-pool");

  constantPoolHandle cpool(thread, klass->constants());

  // Get the field's name, signature, and type.
  Symbol* name  = cpool->name_ref_at(index);

  int nt_index = cpool->name_and_type_ref_index_at(index);
  int sig_index = cpool->signature_ref_index_at(nt_index);
  Symbol* signature = cpool->symbol_at(sig_index);

  // Get the field's declared holder.
  //
  // Note: we actually create a ciInstanceKlass for this klass,
  // even though we may not need to.
  int holder_index = cpool->klass_ref_index_at(index);
  bool holder_is_accessible;
  KlassHandle declared_holder = get_klass_by_index(cpool, holder_index,
                                               holder_is_accessible,
                                               klass);

  // The declared holder of this field may not have been loaded.
  // Bail out with partial field information.
  if (!holder_is_accessible) {
    return;
  }


  // Perform the field lookup.
  klassOop canonical_holder =
    instanceKlass::cast(declared_holder())->find_field(name, signature, &field_desc);
  if (canonical_holder == NULL) {
    return;
  }

  assert(canonical_holder == field_desc.field_holder(), "just checking");
}

// ------------------------------------------------------------------
// ciEnv::get_field_by_index
//
// Get a field by index from a klass's constant pool.
void GraalEnv::get_field_by_index(instanceKlassHandle& accessor, fieldDescriptor& fd,
                                   int index) {
  ResourceMark rm;
  return get_field_by_index_impl(accessor, fd, index);
}

// ------------------------------------------------------------------
// ciEnv::lookup_method
//
// Perform an appropriate method lookup based on accessor, holder,
// name, signature, and bytecode.
methodHandle GraalEnv::lookup_method(instanceKlassHandle& h_accessor,
                               instanceKlassHandle& h_holder,
                               Symbol*       name,
                               Symbol*       sig,
                               Bytecodes::Code bc) {
  EXCEPTION_CONTEXT;
  LinkResolver::check_klass_accessability(h_accessor, h_holder, KILL_COMPILE_ON_FATAL_(NULL));
  methodHandle dest_method;
  switch (bc) {
  case Bytecodes::_invokestatic:
    dest_method =
      LinkResolver::resolve_static_call_or_null(h_holder, name, sig, h_accessor);
    break;
  case Bytecodes::_invokespecial:
    dest_method =
      LinkResolver::resolve_special_call_or_null(h_holder, name, sig, h_accessor);
    break;
  case Bytecodes::_invokeinterface:
    dest_method =
      LinkResolver::linktime_resolve_interface_method_or_null(h_holder, name, sig,
                                                              h_accessor, true);
    break;
  case Bytecodes::_invokevirtual:
    dest_method =
      LinkResolver::linktime_resolve_virtual_method_or_null(h_holder, name, sig,
                                                            h_accessor, true);
    break;
  default: ShouldNotReachHere();
  }

  return dest_method;
}


// ------------------------------------------------------------------
// ciEnv::get_method_by_index_impl
methodHandle GraalEnv::get_method_by_index_impl(constantPoolHandle& cpool,
                                          int index, Bytecodes::Code bc,
                                          instanceKlassHandle& accessor) {
  int holder_index = cpool->klass_ref_index_at(index);
  bool holder_is_accessible;
  KlassHandle holder = get_klass_by_index_impl(cpool, holder_index, holder_is_accessible, accessor);

  // Get the method's name and signature.
  Symbol* name_sym = cpool->name_ref_at(index);
  Symbol* sig_sym  = cpool->signature_ref_at(index);

  if (holder_is_accessible) { // Our declared holder is loaded.
    instanceKlassHandle lookup = get_instance_klass_for_declared_method_holder(holder);
    methodHandle m = lookup_method(accessor, lookup, name_sym, sig_sym, bc);
    if (!m.is_null() &&
        (bc == Bytecodes::_invokestatic
         ?  instanceKlass::cast(m->method_holder())->is_not_initialized()
         : !instanceKlass::cast(m->method_holder())->is_loaded())) {
      m = NULL;
    }
    if (!m.is_null()) {
      // We found the method.
      return m;
    }
  }

  // Either the declared holder was not loaded, or the method could
  // not be found.  Create a dummy ciMethod to represent the failed
  // lookup.

  return NULL;
}

// ------------------------------------------------------------------
// ciEnv::get_instance_klass_for_declared_method_holder
instanceKlassHandle GraalEnv::get_instance_klass_for_declared_method_holder(KlassHandle& method_holder) {
  // For the case of <array>.clone(), the method holder can be a ciArrayKlass
  // instead of a ciInstanceKlass.  For that case simply pretend that the
  // declared holder is Object.clone since that's where the call will bottom out.
  // A more correct fix would trickle out through many interfaces in CI,
  // requiring ciInstanceKlass* to become ciKlass* and many more places would
  // require checks to make sure the expected type was found.  Given that this
  // only occurs for clone() the more extensive fix seems like overkill so
  // instead we simply smear the array type into Object.
  if (method_holder->oop_is_instance()) {
    return instanceKlassHandle(method_holder());
  } else if (method_holder->oop_is_array()) {
    return instanceKlassHandle(SystemDictionary::Object_klass());
  } else {
    ShouldNotReachHere();
  }
  return NULL;
}


// ------------------------------------------------------------------
// ciEnv::get_method_by_index
methodHandle GraalEnv::get_method_by_index(constantPoolHandle& cpool,
                                     int index, Bytecodes::Code bc,
                                     instanceKlassHandle& accessor) {
  ResourceMark rm;
  assert(bc != Bytecodes::_invokedynamic, "invokedynamic not yet supported");
  return get_method_by_index_impl(cpool, index, bc, accessor);
}

// ------------------------------------------------------------------
// ciEnv::check_for_system_dictionary_modification
// Check for changes to the system dictionary during compilation
// class loads, evolution, breakpoints
bool GraalEnv::check_for_system_dictionary_modification(Dependencies* dependencies) {
  // Dependencies must be checked when the system dictionary changes.
  // If logging is enabled all violated dependences will be recorded in
  // the log.  In debug mode check dependencies even if the system
  // dictionary hasn't changed to verify that no invalid dependencies
  // were inserted.  Any violated dependences in this case are dumped to
  // the tty.

  // TODO (tw): Always check dependency for now.
  //bool counter_changed = system_dictionary_modification_counter_changed();
  //bool test_deps = counter_changed;
  //DEBUG_ONLY(test_deps = true);
  //if (!test_deps)  return;

  for (Dependencies::DepStream deps(dependencies); deps.next(); ) {
    klassOop witness = deps.check_dependency();
    if (witness != NULL) {
      return false;
    }
  }

  return true;
}

// ------------------------------------------------------------------
// ciEnv::register_method
nmethod* GraalEnv::register_method(methodHandle& method,
                                int entry_bci,
                                CodeOffsets* offsets,
                                int orig_pc_offset,
                                CodeBuffer* code_buffer,
                                int frame_words,
                                OopMapSet* oop_map_set,
                                ExceptionHandlerTable* handler_table,
                                ImplicitExceptionTable* inc_table,
                                AbstractCompiler* compiler,
                                DebugInformationRecorder* debug_info,
                                Dependencies* dependencies,
                                CompileTask* task,
                                int compile_id,
                                bool has_debug_info,
                                bool has_unsafe_access,
                                bool install_code) {
  EXCEPTION_CONTEXT;
  NMethodSweeper::possibly_sweep();
  nmethod* nm = NULL;
  int comp_level = CompLevel_simple;
  {
    // To prevent compile queue updates.
    MutexLocker locker(MethodCompileQueue_lock, THREAD);

    // Prevent SystemDictionary::add_to_hierarchy from running
    // and invalidating our dependencies until we install this method.
    MutexLocker ml(Compile_lock);

    // Encode the dependencies now, so we can check them right away.
    dependencies->encode_content_bytes();

    // Check for {class loads, evolution, breakpoints} during compilation
    if (!check_for_system_dictionary_modification(dependencies)) {
      // While not a true deoptimization, it is a preemptive decompile.
      methodDataOop mdo = method()->method_data();
      if (mdo != NULL) {
        mdo->inc_decompile_count();
      }

      // All buffers in the CodeBuffer are allocated in the CodeCache.
      // If the code buffer is created on each compile attempt
      // as in C2, then it must be freed.
      //code_buffer->free_blob();
      return NULL;
    }

    assert(offsets->value(CodeOffsets::Deopt) != -1, "must have deopt entry");
    assert(offsets->value(CodeOffsets::Exceptions) != -1, "must have exception entry");

    nm =  nmethod::new_nmethod(method,
                               compile_id,
                               entry_bci,
                               offsets,
                               orig_pc_offset,
                               debug_info, dependencies, code_buffer,
                               frame_words, oop_map_set,
                               handler_table, inc_table,
                               compiler, comp_level);

    // Free codeBlobs
    //code_buffer->free_blob();

    // stress test 6243940 by immediately making the method
    // non-entrant behind the system's back. This has serious
    // side effects on the code cache and is not meant for
    // general stress testing
    if (nm != NULL && StressNonEntrant) {
      MutexLockerEx pl(Patching_lock, Mutex::_no_safepoint_check_flag);
      NativeJump::patch_verified_entry(nm->entry_point(), nm->verified_entry_point(),
                  SharedRuntime::get_handle_wrong_method_stub());
    }

    if (nm == NULL) {
      // The CodeCache is full.  Print out warning and disable compilation.
      {
        MutexUnlocker ml(Compile_lock);
        MutexUnlocker locker(MethodCompileQueue_lock);
        CompileBroker::handle_full_code_cache();
      }
    } else {
      NOT_PRODUCT(nm->set_has_debug_info(has_debug_info); )
      nm->set_has_unsafe_access(has_unsafe_access);

      // Record successful registration.
      // (Put nm into the task handle *before* publishing to the Java heap.)
      if (task != NULL)  task->set_code(nm);

      if (install_code) {
        if (entry_bci == InvocationEntryBci) {
          if (TieredCompilation) {
            // If there is an old version we're done with it
            nmethod* old = method->code();
            if (TraceMethodReplacement && old != NULL) {
              ResourceMark rm;
              char *method_name = method->name_and_sig_as_C_string();
              tty->print_cr("Replacing method %s", method_name);
            }
            if (old != NULL ) {
              old->make_not_entrant();
            }
          }
          if (TraceNMethodInstalls ) {
            ResourceMark rm;
            char *method_name = method->name_and_sig_as_C_string();
            ttyLocker ttyl;
            tty->print_cr("Installing method (%d) %s ",
                          comp_level,
                          method_name);
          }
          // Allow the code to be executed
          method->set_code(method, nm);
        } else {
          if (TraceNMethodInstalls ) {
            ResourceMark rm;
            char *method_name = method->name_and_sig_as_C_string();
            ttyLocker ttyl;
            tty->print_cr("Installing osr method (%d) %s @ %d",
                          comp_level,
                          method_name,
                          entry_bci);
          }
          instanceKlass::cast(method->method_holder())->add_osr_nmethod(nm);

        }
      }
    }
  }
  // JVMTI -- compiled method notification (must be done outside lock)
  if (nm != NULL) {
    nm->post_compiled_method_load_event();
  }

  return nm;
}

