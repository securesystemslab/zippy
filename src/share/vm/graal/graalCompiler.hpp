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

#ifndef SHARE_VM_GRAAL_GRAAL_COMPILER_HPP
#define SHARE_VM_GRAAL_GRAAL_COMPILER_HPP

#include "compiler/abstractCompiler.hpp"

#define LEAF_GRAPH_ARRAY_SIZE (8192)

class GraalCompiler : public AbstractCompiler {

private:

  bool                  _initialized;

  static GraalCompiler* _instance;

  jlong                 _deopted_leaf_graphs[LEAF_GRAPH_ARRAY_SIZE];
  int                   _deopted_leaf_graph_count;

public:

  GraalCompiler();

  static GraalCompiler* instance() { return _instance; }


  virtual const char* name() { return "Graal"; }

  virtual bool supports_native()                 { return true; }
  virtual bool supports_osr   ()                 { return true; }

  bool is_graal()                                { return true; }
  bool is_c1   ()                                { return false; }
  bool is_c2   ()                                { return false; }

  bool needs_stubs            () { return false; }

  // Initialization
  virtual void initialize();

  // Compilation entry point for methods
  virtual void compile_method(ciEnv* env, ciMethod* target, int entry_bci);

  void compile_method(methodHandle target, int entry_bci, jboolean blocking);

  void deopt_leaf_graph(jlong leaf_graph_id);
  oop dump_deopted_leaf_graphs(TRAPS);

  // Print compilation timers and statistics
  virtual void print_timers();

  static Handle get_JavaTypeFromSignature(Symbol* signature, KlassHandle accessor, TRAPS);
  static Handle get_JavaType(constantPoolHandle cp, int index, KlassHandle accessor, TRAPS);

  void exit();

  static BasicType kindToBasicType(jchar ch);

  static int to_cp_index_u2(int index) {
    // Tag.
    return index + ConstantPool::CPCACHE_INDEX_TAG;
  }

  static int to_cp_index(int raw_index, Bytecodes::Code bc) {
    int cp_index;
    if (bc == Bytecodes::_invokedynamic) {
      cp_index = raw_index;
      assert(ConstantPool::is_invokedynamic_index(cp_index), "not an invokedynamic constant pool index");
    } else {
      assert(bc == Bytecodes::_getfield        || bc == Bytecodes::_putfield  ||
             bc == Bytecodes::_getstatic       || bc == Bytecodes::_putstatic ||
             bc == Bytecodes::_invokeinterface || bc == Bytecodes::_invokevirtual ||
             bc == Bytecodes::_invokespecial   || bc == Bytecodes::_invokestatic, err_msg("unexpected invoke opcode: %d %s", bc, Bytecodes::name(bc)));
      cp_index = to_cp_index_u2(raw_index);
    }
    return cp_index;
  }

  static BufferBlob* initialize_buffer_blob();
};

// Tracing macros

#define IF_TRACE_graal_1 if (!(TraceGraal >= 1)) ; else
#define IF_TRACE_graal_2 if (!(TraceGraal >= 2)) ; else
#define IF_TRACE_graal_3 if (!(TraceGraal >= 3)) ; else
#define IF_TRACE_graal_4 if (!(TraceGraal >= 4)) ; else
#define IF_TRACE_graal_5 if (!(TraceGraal >= 5)) ; else

// using commas and else to keep one-instruction semantics

#define TRACE_graal_1 if (!(TraceGraal >= 1 && (tty->print("TraceGraal-1: "), true))) ; else tty->print_cr
#define TRACE_graal_2 if (!(TraceGraal >= 2 && (tty->print("   TraceGraal-2: "), true))) ; else tty->print_cr
#define TRACE_graal_3 if (!(TraceGraal >= 3 && (tty->print("      TraceGraal-3: "), true))) ; else tty->print_cr
#define TRACE_graal_4 if (!(TraceGraal >= 4 && (tty->print("         TraceGraal-4: "), true))) ; else tty->print_cr
#define TRACE_graal_5 if (!(TraceGraal >= 5 && (tty->print("            TraceGraal-5: "), true))) ; else tty->print_cr

#endif // SHARE_VM_GRAAL_GRAAL_COMPILER_HPP
