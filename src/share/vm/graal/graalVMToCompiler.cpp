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
#include "classfile/systemDictionary.hpp"
#include "graal/graalVMToCompiler.hpp"
#include "runtime/gpu.hpp"

// these are *global* handles
jobject VMToCompiler::_HotSpotGraalRuntime_instance = NULL;
jobject VMToCompiler::_VMToCompiler_instance = NULL;
Klass* VMToCompiler::_VMToCompiler_klass = NULL;

static Klass* loadClass(Symbol* name) {
  Klass* klass = SystemDictionary::resolve_or_null(name, SystemDictionary::java_system_loader(), Handle(), Thread::current());
  if (klass == NULL) {
    tty->print_cr("Could not load class %s", name->as_C_string());
    vm_abort(false);
  }
  return klass;
}

KlassHandle VMToCompiler::VMToCompiler_klass() {
  if (_VMToCompiler_klass == NULL) {
    TempNewSymbol VMToCompiler = SymbolTable::new_symbol("com/oracle/graal/hotspot/bridge/VMToCompiler", Thread::current());
    Klass* result = loadClass(VMToCompiler);
    _VMToCompiler_klass = result;
  }
  return _VMToCompiler_klass;
}

Handle VMToCompiler::create_HotSpotTruffleRuntime() {
  Thread* THREAD = Thread::current();
  TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/truffle/hotspot/HotSpotTruffleRuntime", THREAD);
  KlassHandle klass = loadClass(name);

  TempNewSymbol makeInstance = SymbolTable::new_symbol("makeInstance", THREAD);
  TempNewSymbol sig = SymbolTable::new_symbol("()Lcom/oracle/graal/truffle/hotspot/HotSpotTruffleRuntime;", THREAD);
  JavaValue result(T_OBJECT);
  JavaCalls::call_static(&result, klass, makeInstance, sig, THREAD);
  check_pending_exception("Couldn't initialize HotSpotTruffleRuntime");
  return Handle((oop) result.get_jobject());
}

Handle VMToCompiler::get_HotSpotGraalRuntime() {
  if (JNIHandles::resolve(_HotSpotGraalRuntime_instance) == NULL) {
    Thread* THREAD = Thread::current();
    TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/hotspot/HotSpotGraalRuntime", THREAD);
    KlassHandle klass = loadClass(name);
    TempNewSymbol runtime = SymbolTable::new_symbol("runtime", THREAD);
    TempNewSymbol sig = SymbolTable::new_symbol("()Lcom/oracle/graal/hotspot/HotSpotGraalRuntime;", THREAD);
    JavaValue result(T_OBJECT);
    JavaCalls::call_static(&result, klass, runtime, sig, THREAD);
    check_pending_exception("Couldn't initialize HotSpotGraalRuntime");
    _HotSpotGraalRuntime_instance = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_HotSpotGraalRuntime_instance));
}

Handle VMToCompiler::VMToCompiler_instance() {
  if (JNIHandles::resolve(_VMToCompiler_instance) == NULL) {
    Thread* THREAD = Thread::current();
    TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/hotspot/HotSpotGraalRuntime", THREAD);
    KlassHandle klass = loadClass(name);
    TempNewSymbol getVMToCompiler = SymbolTable::new_symbol("getVMToCompiler", THREAD);
    TempNewSymbol sig = SymbolTable::new_symbol("()Lcom/oracle/graal/hotspot/bridge/VMToCompiler;", THREAD);
    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    args.set_receiver(get_HotSpotGraalRuntime());
    JavaCalls::call_virtual(&result, klass, getVMToCompiler, sig, &args, THREAD);
    check_pending_exception("Couldn't get VMToCompiler");
    _VMToCompiler_instance = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_VMToCompiler_instance));
}

void VMToCompiler::initOptions() {
  Thread* THREAD = Thread::current();
  TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/hotspot/HotSpotOptions", THREAD);
  KlassHandle optionsKlass = loadClass(name);
  optionsKlass->initialize(THREAD);
  check_pending_exception("Error while calling initOptions");
}

jboolean VMToCompiler::setOption(Handle option) {
  assert(!option.is_null(), "");
  Thread* THREAD = Thread::current();
  TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/hotspot/HotSpotOptions", THREAD);
  TempNewSymbol setOption = SymbolTable::new_symbol("setOption", THREAD);
  TempNewSymbol sig = SymbolTable::new_symbol("(Ljava/lang/String;)Z", THREAD);
  KlassHandle optionsKlass = loadClass(name);
  JavaValue result(T_BOOLEAN);
  JavaCalls::call_static(&result, optionsKlass, setOption, sig, option, THREAD);
  check_pending_exception("Error while calling setOption");
  return result.get_jboolean();
}

void VMToCompiler::finalizeOptions(jboolean ciTime) {
  Thread* THREAD = Thread::current();
  TempNewSymbol name = SymbolTable::new_symbol("com/oracle/graal/hotspot/HotSpotOptions", THREAD);
  TempNewSymbol finalizeOptions = SymbolTable::new_symbol("finalizeOptions", THREAD);
  TempNewSymbol sig = SymbolTable::new_symbol("(Ljava/lang/String;)Z", THREAD);
  KlassHandle optionsKlass = loadClass(name);
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_int(ciTime);
  JavaCalls::call_static(&result, optionsKlass, finalizeOptions, vmSymbols::bool_void_signature(), &args, THREAD);
  check_pending_exception("Error while calling finalizeOptions");
}

void VMToCompiler::startRuntime() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  TempNewSymbol startRuntime = SymbolTable::new_symbol("startRuntime", THREAD);
  args.push_oop(VMToCompiler_instance());
  JavaCalls::call_interface(&result, VMToCompiler_klass(), startRuntime, vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling startRuntime");
}

#ifdef COMPILERGRAAL
void VMToCompiler::bootstrap() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  TempNewSymbol bootstrap = SymbolTable::new_symbol("bootstrap", THREAD);
  args.push_oop(VMToCompiler_instance());
  JavaCalls::call_interface(&result, VMToCompiler_klass(), bootstrap, vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling bootstrap");
}

void VMToCompiler::startCompiler(jboolean bootstrap_enabled) {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  TempNewSymbol startCompiler = SymbolTable::new_symbol("startCompiler", THREAD);
  args.push_oop(VMToCompiler_instance());
  args.push_int(bootstrap_enabled);
  JavaCalls::call_interface(&result, VMToCompiler_klass(), startCompiler, vmSymbols::bool_void_signature(), &args, THREAD);
  check_pending_exception("Error while calling startCompiler");
}

void VMToCompiler::compileMethod(Method* method, int entry_bci, jlong ctask, jboolean blocking) {
  assert(method != NULL, "just checking");
  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(VMToCompiler_instance());
  args.push_long((jlong) (address) method);
  args.push_int(entry_bci);
  args.push_long(ctask);
  args.push_int(blocking);
  JavaCalls::call_interface(&result, VMToCompiler_klass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileMethod");
}

void VMToCompiler::shutdownCompiler() {
  JavaThread* THREAD = JavaThread::current();
  HandleMark hm(THREAD);
  TempNewSymbol shutdownCompiler = SymbolTable::new_symbol("shutdownCompiler", THREAD);
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(VMToCompiler_instance());
  JavaCalls::call_interface(&result, VMToCompiler_klass(), shutdownCompiler, vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling shutdownCompiler");
}
#endif // COMPILERGRAAL

void VMToCompiler::shutdownRuntime() {
  if (_HotSpotGraalRuntime_instance != NULL) {
    JavaThread* THREAD = JavaThread::current();
    HandleMark hm(THREAD);
    TempNewSymbol shutdownRuntime = SymbolTable::new_symbol("shutdownRuntime", THREAD);
    JavaValue result(T_VOID);
    JavaCallArguments args;
    args.push_oop(VMToCompiler_instance());
    JavaCalls::call_interface(&result, VMToCompiler_klass(), shutdownRuntime, vmSymbols::void_method_signature(), &args, THREAD);
    check_pending_exception("Error while calling shutdownRuntime");

    JNIHandles::destroy_global(_HotSpotGraalRuntime_instance);
    JNIHandles::destroy_global(_VMToCompiler_instance);

    _HotSpotGraalRuntime_instance = NULL;
    _VMToCompiler_instance = NULL;
    _VMToCompiler_klass = NULL;
  }
}

#ifndef PRODUCT
void VMToCompiler::compileTheWorld() {
  // We turn off CompileTheWorld so that Graal can
  // be compiled by C1/C2 when Graal does a CTW.
  CompileTheWorld = false;

  JavaThread* THREAD = JavaThread::current();
  TempNewSymbol compileTheWorld = SymbolTable::new_symbol("compileTheWorld", THREAD);
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(VMToCompiler_instance());
  JavaCalls::call_interface(&result, VMToCompiler_klass(), compileTheWorld, vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileTheWorld");
}
#endif
