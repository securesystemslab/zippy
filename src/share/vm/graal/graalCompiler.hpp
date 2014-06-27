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

class GraalCompiler : public AbstractCompiler {

private:

#ifdef COMPILERGRAAL
  bool _bootstrapping;

  void start_compilation_queue();
  void shutdown_compilation_queue();
  void bootstrap();
#endif

  static GraalCompiler* _instance;
 
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

#ifdef COMPILERGRAAL
  // Compilation entry point for methods
  virtual void compile_method(ciEnv* env, ciMethod* target, int entry_bci);

  void compile_method(methodHandle target, int entry_bci, CompileTask* task, jboolean blocking);

  // Print compilation timers and statistics
  virtual void print_timers();

  // Print compilation statistics
  void reset_compilation_stats();

  void shutdown();
#endif // COMPILERGRAAL

#ifndef PRODUCT
  void compile_the_world();
#endif
};

#ifdef COMPILERGRAAL
/**
 * Creates a scope in which scheduling a Graal compilation is disabled.
 * Scheduling a compilation can happen anywhere a counter can overflow and
 * calling back into the Java code for scheduling a compilation (i.e.,
 * CompilationTask.compileMetaspaceMethod()) from such arbitrary locations
 * can cause objects to be in an unexpected state.
 *
 * In addition, it can be useful to disable compilation scheduling in
 * other circumstances such as when initializing the Graal compilation
 * queue or when running the Graal bootstrap process.
 */
class NoGraalCompilationScheduling: public StackObj {
 private:
  JavaThread* _thread;
 public:

  NoGraalCompilationScheduling(JavaThread *thread) {
    assert(thread == JavaThread::current(), "not the current thread");
    assert(thread->can_schedule_graal_compilation(), "recursive Graal compilation scheduling");
    _thread = thread;
    thread->set_can_schedule_graal_compilation(false);
  }

  ~NoGraalCompilationScheduling() {
    assert(!_thread->can_schedule_graal_compilation(), "unexpected Graal compilation scheduling state");
    _thread->set_can_schedule_graal_compilation(true);
  }
};
#endif // COMPILERGRAAL

#endif // SHARE_VM_GRAAL_GRAAL_COMPILER_HPP
