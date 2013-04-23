/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP
#define SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP

#include "code/stubs.hpp"
#include "interpreter/interpreter.hpp"
#include "memory/allocation.hpp"
#include "runtime/deoptimization.hpp"

// A GraalStubAssembler is a MacroAssembler w/ extra functionality for runtime
// stubs. Currently it 'knows' some stub info. Eventually, the information
// may be set automatically or can be asserted when using specialised
// GraalStubAssembler functions.

class GraalStubAssembler: public MacroAssembler {
 private:
  const char* _name;
  bool        _must_gc_arguments;
  int         _frame_size;
  int         _num_rt_args;
  int         _stub_id;

 public:

  enum {
    no_frame_size            = -1
  };

  // creation
  GraalStubAssembler(CodeBuffer* code, const char * name, int stub_id);
  void set_info(const char* name, bool must_gc_arguments);

  void set_frame_size(int size);
  void set_num_rt_args(int args);

  // accessors
  const char* name() const                       { return _name; }
  bool  must_gc_arguments() const                { return _must_gc_arguments; }
  int frame_size() const                         { return _frame_size; }
  int num_rt_args() const                        { return _num_rt_args; }
  int stub_id() const                            { return _stub_id; }

  void verify_stack_oop(int offset) PRODUCT_RETURN;
  void verify_not_null_oop(Register r)  PRODUCT_RETURN;

  // runtime calls (return offset of call to be used by GC map)
  int call_RT(Register oop_result1, Register metadata_result, address entry, int args_size = 0);
  int call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1);
  int call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2);
  int call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2, Register arg3);
  int call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2, Register arg3, Register arg4);
};

// set frame size and return address offset to these values in blobs
// (if the compiled frame uses ebp as link pointer on IA; otherwise,
// the frame size must be fixed)

// Holds all assembly stubs and VM
// runtime routines needed by code code generated
// by Graal.
#define GRAAL_STUBS(stub, last_entry) \
  stub(register_finalizer)      \
  stub(new_instance)            \
  stub(new_array)               \
  stub(new_multi_array)         \
  stub(handle_exception_nofpu) /* optimized version that does not preserve fpu registers */ \
  stub(unwind_exception_call)   \
  stub(OSR_migration_end)       \
  stub(arithmetic_frem)         \
  stub(arithmetic_drem)         \
  stub(monitorenter)            \
  stub(monitorexit)             \
  stub(verify_oop)              \
  stub(vm_error)                \
  stub(create_null_pointer_exception) \
  stub(create_out_of_bounds_exception) \
  stub(log_object)              \
  stub(log_printf)              \
  stub(stub_printf)             \
  stub(log_primitive)           \
  stub(identity_hash_code)      \
  stub(thread_is_interrupted)   \
  stub(wb_pre_call)             \
  stub(wb_post_call)             \
 last_entry(number_of_ids)

#define DECLARE_STUB_ID(x)       x ## _id ,
#define DECLARE_LAST_STUB_ID(x)  x
#define STUB_NAME(x)             #x " GraalRuntime stub",
#define LAST_STUB_NAME(x)        #x " GraalRuntime stub"

class GraalRuntime: public AllStatic {
  friend class VMStructs;

 public:
  enum StubID {
    GRAAL_STUBS(DECLARE_STUB_ID, DECLARE_LAST_STUB_ID)
  };

 private:
  static CodeBlob* _blobs[number_of_ids];
  static const char* _blob_names[];

  // stub generation
  static void       generate_blob_for(BufferBlob* blob, StubID id);
  static OopMapSet* generate_code_for(StubID id, GraalStubAssembler* sasm);
  static OopMapSet* generate_handle_exception(StubID id, GraalStubAssembler* sasm);
  static void       generate_unwind_exception(GraalStubAssembler *sasm);

  static OopMapSet* generate_stub_call(GraalStubAssembler* sasm, Register result, address entry,
                                       Register arg1 = noreg, Register arg2 = noreg, Register arg3 = noreg);

  // runtime entry points
  static void new_instance(JavaThread* thread, Klass* klass);
  static void new_array(JavaThread* thread, Klass* klass, jint length);
  static void new_multi_array(JavaThread* thread, Klass* klass, int rank, jint* dims);

  static void unimplemented_entry(JavaThread* thread, StubID id);

  static address exception_handler_for_pc(JavaThread* thread);

  static void create_null_exception(JavaThread* thread);
  static void create_out_of_bounds_exception(JavaThread* thread, jint index);
  static void monitorenter(JavaThread* thread, oopDesc* obj, BasicLock* lock);
  static void monitorexit (JavaThread* thread, oopDesc* obj, BasicLock* lock);
  static void vm_error(JavaThread* thread, oop where, oop format, jlong value);
  static void log_printf(JavaThread* thread, oop format, jlong v1, jlong v2, jlong v3);
  static void stub_printf(JavaThread* thread, jlong format, jlong v1, jlong v2, jlong v3);
  static void log_primitive(JavaThread* thread, jchar typeChar, jlong value, jboolean newline);
  static void wb_pre_call(JavaThread* thread, oopDesc* obj);
  static void wb_post_call(JavaThread* thread, oopDesc* obj, void* card);

  static jint identity_hash_code(JavaThread* thread, oopDesc* objd);
  static jboolean thread_is_interrupted(JavaThread* thread, oopDesc* obj, jboolean clear_interrupte);

  // Note: Must be kept in sync with constants in com.oracle.graal.replacements.Log
  enum {
    LOG_OBJECT_NEWLINE = 0x01,
    LOG_OBJECT_STRING  = 0x02,
    LOG_OBJECT_ADDRESS = 0x04
  };
  static void log_object(JavaThread* thread, oop msg, jint flags);

 public:
  // initialization
  static void initialize(BufferBlob* blob);

  // stubs
  static CodeBlob* blob_for (StubID id);
  static address   entry_for(StubID id)          { return blob_for(id)->code_begin(); }
  static const char* name_for (StubID id);
  static const char* name_for_address(address entry);
};

#endif // SHARE_VM_GRAAL_GRAAL_RUNTIME_HPP
