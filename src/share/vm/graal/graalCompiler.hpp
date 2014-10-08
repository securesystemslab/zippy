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

  /**
   * Number of methods compiled by Graal. This is not synchronized
   * so may not be 100% accurate.
   */
  volatile int  _methodsCompiled;

#endif

  static GraalCompiler* _instance;
 
  static elapsedTimer _codeInstallTimer;

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

  void bootstrap();
  
  // Compilation entry point for methods
  virtual void compile_method(ciEnv* env, ciMethod* target, int entry_bci);

  void compile_method(methodHandle target, int entry_bci, CompileTask* task);

  // Print compilation timers and statistics
  virtual void print_timers();

  // Print compilation statistics
  void reset_compilation_stats();
#endif // COMPILERGRAAL

  static elapsedTimer* codeInstallTimer() { return &_codeInstallTimer; }

#ifndef PRODUCT
  void compile_the_world();
#endif
};

#endif // SHARE_VM_GRAAL_GRAAL_COMPILER_HPP
