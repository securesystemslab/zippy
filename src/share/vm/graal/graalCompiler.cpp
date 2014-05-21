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
#include "graal/graalVMToCompiler.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalRuntime.hpp"
#include "runtime/compilationPolicy.hpp"
#include "runtime/globals_extension.hpp"

GraalCompiler* GraalCompiler::_instance = NULL;

GraalCompiler::GraalCompiler() : AbstractCompiler(graal) {
#ifdef COMPILERGRAAL
  _started = false;
#endif
  assert(_instance == NULL, "only one instance allowed");
  _instance = this;
}

// Initialization
void GraalCompiler::initialize() {
#ifdef COMPILERGRAAL
  if (!UseCompiler || !should_perform_init()) {
    return;
  }

  BufferBlob* buffer_blob = GraalRuntime::initialize_buffer_blob();
  if (!UseGraalCompilationQueue) {
    // This path is used for initialization both by the native queue and the graal queue
    // but set_state acquires a lock which might not be safe during JVM_CreateJavaVM, so
    // only update the state flag for the native queue.
    if (buffer_blob == NULL) {
      set_state(failed);
    } else {
      set_state(initialized);
    }
  }

  {
    HandleMark hm;

    bool bootstrap = UseGraalCompilationQueue && (FLAG_IS_DEFAULT(BootstrapGraal) ? !TieredCompilation : BootstrapGraal);
    VMToCompiler::startCompiler(bootstrap);
    _started = true;

    // Graal is considered as application code so we need to
    // stop the VM deferring compilation now.
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
      VMToCompiler::compileTheWorld();
    }
#endif
  }
#endif // COMPILERGRAAL
}

#ifdef COMPILERGRAAL
void GraalCompiler::compile_method(methodHandle method, int entry_bci, CompileTask* task, jboolean blocking) {
  GRAAL_EXCEPTION_CONTEXT
  if (!_started) {
    CompilationPolicy::policy()->delay_compilation(method());
    return;
  }

  assert(_started, "must already be started");
  ResourceMark rm;
  thread->set_is_graal_compiling(true);
  VMToCompiler::compileMethod(method(), entry_bci, (jlong) (address) task, blocking);
  thread->set_is_graal_compiling(false);
}


// Compilation entry point for methods
void GraalCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  ShouldNotReachHere();
}

void GraalCompiler::shutdown() {
  if (_started) {
    VMToCompiler::shutdownCompiler();
    _started = false;
  }
}

// Print compilation timers and statistics
void GraalCompiler::print_timers() {
  TRACE_graal_1("GraalCompiler::print_timers");
}

#endif // COMPILERGRAAL
