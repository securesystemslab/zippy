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
	TRACE_C1X_1("initialize");

  JNIEnv *env = ((JavaThread *)Thread::current())->jni_environment();
  jclass klass = env->FindClass("com/sun/hotspot/c1x/VMEntries");
  env->RegisterNatives(klass, VMEntries_methods, VMEntries_methods_count() );
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
  
  initialize();

	VM_ENTRY_MARK;

	
	ResourceMark rm;
	HandleMark hm;

  CompilerThread::current()->set_compiling(true);
  oop rimethod = get_rimethod(target);
  VMExits::compileMethod(rimethod, entry_bci);
	CompilerThread::current()->set_compiling(false);
}

// Print compilation timers and statistics
void C1XCompiler::print_timers() {
	TRACE_C1X_1("print_timers");
}

oop C1XCompiler::get_rimethod(ciMethod *method) {
  methodOop m = (methodOop)method->get_oop();
  // TODO: implement caching
  return VMExits::createRiMethod(m);
}