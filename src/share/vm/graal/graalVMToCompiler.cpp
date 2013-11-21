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

// this is a *global* handle
jobject VMToCompiler::_graalRuntimePermObject = NULL;
jobject VMToCompiler::_vmToCompilerPermObject = NULL;
Klass* VMToCompiler::_vmToCompilerPermKlass = NULL;

static Klass* loadClass(Symbol* name) {
  Klass* klass = SystemDictionary::resolve_or_null(name, SystemDictionary::java_system_loader(), Handle(), Thread::current());
  if (klass == NULL) {
    tty->print_cr("Could not load class %s", name->as_C_string());
    vm_abort(false);
  }
  return klass;
}

KlassHandle VMToCompiler::vmToCompilerKlass() {
  if (_vmToCompilerPermKlass == NULL) {
    Klass* result = loadClass(vmSymbols::com_oracle_graal_hotspot_bridge_VMToCompiler());
    _vmToCompilerPermKlass = result;
  }
  return _vmToCompilerPermKlass;
}

Handle VMToCompiler::truffleRuntime() {
  Symbol* name = vmSymbols::com_oracle_graal_truffle_GraalTruffleRuntime();
  KlassHandle klass = loadClass(name);

  JavaValue result(T_OBJECT);
  JavaCalls::call_static(&result, klass, vmSymbols::makeInstance_name(), vmSymbols::makeInstance_signature(), Thread::current());
  check_pending_exception("Couldn't initialize GraalTruffleRuntime");
  return Handle((oop) result.get_jobject());
}

Handle VMToCompiler::graalRuntime() {
  if (JNIHandles::resolve(_graalRuntimePermObject) == NULL) {
    KlassHandle klass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotGraalRuntime());
    JavaValue result(T_OBJECT);
    JavaCalls::call_static(&result, klass, vmSymbols::runtime_name(), vmSymbols::runtime_signature(), Thread::current());
    check_pending_exception("Couldn't initialize HotSpotGraalRuntime");
    _graalRuntimePermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_graalRuntimePermObject));
}

Handle VMToCompiler::instance() {
  if (JNIHandles::resolve(_vmToCompilerPermObject) == NULL) {
    KlassHandle compilerKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotGraalRuntime());

    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    args.set_receiver(graalRuntime());
    JavaCalls::call_virtual(&result, compilerKlass, vmSymbols::getVMToCompiler_name(), vmSymbols::getVMToCompiler_signature(), &args, Thread::current());
    check_pending_exception("Couldn't get VMToCompiler");
    _vmToCompilerPermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_vmToCompilerPermObject));
}

void VMToCompiler::initOptions() {
  KlassHandle optionsKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotOptions());
  Thread* THREAD = Thread::current();
  optionsKlass->initialize(THREAD);
  check_pending_exception("Error while calling initOptions");
}

jboolean VMToCompiler::setOption(Handle option) {
  assert(!option.is_null(), "");
  KlassHandle optionsKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotOptions());

  Thread* THREAD = Thread::current();
  JavaValue result(T_BOOLEAN);
  JavaCalls::call_static(&result, optionsKlass, vmSymbols::setOption_name(), vmSymbols::setOption_signature(), option, THREAD);
  check_pending_exception("Error while calling setOption");
  return result.get_jboolean();
}

void VMToCompiler::finalizeOptions(jboolean ciTime) {
  KlassHandle optionsKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotOptions());
  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_int(ciTime);
  JavaCalls::call_static(&result, optionsKlass, vmSymbols::finalizeOptions_name(), vmSymbols::bool_void_signature(), &args, THREAD);
  check_pending_exception("Error while calling finalizeOptions");
}

void VMToCompiler::compileMethod(Method* method, Handle holder, int entry_bci, jboolean blocking) {
  assert(method != NULL, "just checking");
  assert(!holder.is_null(), "just checking");
  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long((jlong) (address) method);
  args.push_oop(holder());
  args.push_int(entry_bci);
  args.push_int(blocking);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileMethod");
}

void VMToCompiler::shutdownCompiler() {
  if (_graalRuntimePermObject != NULL) {
    HandleMark hm;
    JavaThread* THREAD = JavaThread::current();
    JavaValue result(T_VOID);
    JavaCallArguments args;
    args.push_oop(instance());
    JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::shutdownCompiler_name(), vmSymbols::void_method_signature(), &args, THREAD);
    check_pending_exception("Error while calling shutdownCompiler");

    JNIHandles::destroy_global(_graalRuntimePermObject);
    JNIHandles::destroy_global(_vmToCompilerPermObject);
    //JNIHandles::destroy_global(_vmToCompilerPermKlass);

    _graalRuntimePermObject = NULL;
    _vmToCompilerPermObject = NULL;
    _vmToCompilerPermKlass = NULL;
  }
}

void VMToCompiler::startCompiler(jboolean bootstrap_enabled, jlong compilerStatisticsAddress) {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_int(bootstrap_enabled);
  args.push_long(compilerStatisticsAddress);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::startCompiler_name(), vmSymbols::boolean_long_void_signature(), &args, THREAD);
  check_pending_exception("Error while calling startCompiler");
}

void VMToCompiler::bootstrap() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::bootstrap_name(), vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling bootstrap");
}

void VMToCompiler::compileTheWorld() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::compileTheWorld_name(), vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileTheWorld");
}

oop VMToCompiler::createJavaField(Handle holder, Handle name, Handle type, int index, int flags, jboolean internal, TRAPS) {
  assert(!holder.is_null(), "just checking");
  assert(!name.is_null(), "just checking");
  assert(!type.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(holder);
  args.push_oop(name);
  args.push_oop(type);
  args.push_int(index);
  args.push_int(flags);
  args.push_int(internal);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createJavaField_name(), vmSymbols::createJavaField_signature(), &args, THREAD);
  check_pending_exception("Error while calling createJavaField");
  assert(result.get_type() == T_OBJECT, "just checking");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createUnresolvedJavaMethod(Handle name, Handle signature, Handle holder, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_oop(signature);
  args.push_oop(holder);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createUnresolvedJavaMethod_name(), vmSymbols::createUnresolvedJavaMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling createUnresolvedJavaMethod");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createResolvedJavaMethod(Handle holder, Method* method, TRAPS) {
  assert(!holder.is_null(), "just checking");
  assert(method != NULL, "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(holder);
  args.push_long((jlong) (address) method);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createResolvedJavaMethod_name(), vmSymbols::createResolvedJavaMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling createResolvedJavaMethod");
  assert(result.get_type() == T_OBJECT, "just checking");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createPrimitiveJavaType(int basic_type, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_int(basic_type);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createPrimitiveJavaType_name(), vmSymbols::createPrimitiveJavaType_signature(), &args, THREAD);
  check_pending_exception("Error while calling createPrimitiveJavaType");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createUnresolvedJavaType(Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createUnresolvedJavaType_name(), vmSymbols::createUnresolvedJavaType_signature(), &args, THREAD);
  check_pending_exception("Error while calling createUnresolvedJavaType");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createResolvedJavaType(Klass* klass, Handle name, Handle simpleName, Handle java_mirror, jint sizeOrSpecies, TRAPS) {
  assert(!name.is_null(), "just checking");
  assert(!simpleName.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long((jlong) (address) klass);
  args.push_oop(name);
  args.push_oop(simpleName);
  args.push_oop(java_mirror);
  args.push_int(sizeOrSpecies);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createResolvedJavaType_name(), vmSymbols::createResolvedJavaType_signature(), &args, THREAD);
  check_pending_exception("Error while calling createResolvedJavaType");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createConstant(Handle kind, jlong value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(kind());
  args.push_long(value);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createConstant_name(), vmSymbols::createConstant_signature(), &args, THREAD);
  check_pending_exception("Error while calling createConstantFloat");
  return (oop) result.get_jobject();

}

oop VMToCompiler::createConstantFloat(jfloat value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_float(value);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createConstantFloat_name(), vmSymbols::createConstantFloat_signature(), &args, THREAD);
  check_pending_exception("Error while calling createConstantFloat");
  return (oop) result.get_jobject();

}

oop VMToCompiler::createConstantDouble(jdouble value, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_double(value);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createConstantDouble_name(), vmSymbols::createConstantDouble_signature(), &args, THREAD);
  check_pending_exception("Error while calling createConstantDouble");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createConstantObject(Handle object, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  KlassHandle klass = loadClass(vmSymbols::com_oracle_graal_api_meta_Constant());
  JavaCalls::call_static(&result, klass(), vmSymbols::forObject_name(), vmSymbols::createConstantObject_signature(), object, THREAD);
  check_pending_exception("Error while calling Constant.forObject");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createLocal(Handle name, Handle typeInfo, int bci_start, int bci_end, int slot, Handle holder, TRAPS) {
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_oop(typeInfo);
  args.push_oop(holder);
  args.push_int(bci_start);
  args.push_int(bci_end);
  args.push_int(slot);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createLocalImpl_name(), vmSymbols::createLocalImpl_signature(), &args, THREAD);
  check_pending_exception("Error while calling createConstantFloat");
  return (oop) result.get_jobject();

}


