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
#include "graal/graalVMToCompiler.hpp"

// this is a *global* handle
jobject VMToCompiler::_compilerPermObject = NULL;
jobject VMToCompiler::_vmToCompilerPermObject = NULL;
jobject VMToCompiler::_vmToCompilerPermKlass = NULL;

static klassOop loadClass(Symbol* name) {
  klassOop klass = SystemDictionary::resolve_or_null(name, SystemDictionary::java_system_loader(), NULL, Thread::current());
  if (klass == NULL) {
    tty->print_cr("Could not load class %s", name->as_C_string());
    vm_abort(false);
  }
  return klass;
}

KlassHandle VMToCompiler::vmToCompilerKlass() {
  if (JNIHandles::resolve(_vmToCompilerPermKlass) == NULL) {
    klassOop result = loadClass(vmSymbols::com_oracle_graal_hotspot_bridge_VMToCompiler());
    _vmToCompilerPermKlass = JNIHandles::make_global(result);
  }
  return KlassHandle((klassOop)JNIHandles::resolve_non_null(_vmToCompilerPermKlass));
}

Handle VMToCompiler::compilerInstance() {
  if (JNIHandles::resolve(_compilerPermObject) == NULL) {
#ifdef AMD64
    Symbol* name = vmSymbols::com_oracle_graal_hotspot_amd64_AMD64HotSpotGraalRuntime();
#endif
    KlassHandle klass = loadClass(name);

    JavaValue result(T_OBJECT);
    JavaCalls::call_static(&result, klass, vmSymbols::initialize_name(), vmSymbols::getInstance_signature(), Thread::current());
    check_pending_exception("Couldn't initialize HotSpotGraalRuntime");
    _compilerPermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_compilerPermObject));
}

Handle VMToCompiler::instance() {
  if (JNIHandles::resolve(_vmToCompilerPermObject) == NULL) {
    KlassHandle compilerKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotGraalRuntime());

    JavaValue result(T_OBJECT);
    JavaCallArguments args;
    args.set_receiver(compilerInstance());
    JavaCalls::call_virtual(&result, compilerKlass, vmSymbols::getVMToCompiler_name(), vmSymbols::getVMToCompiler_signature(), &args, Thread::current());
    check_pending_exception("Couldn't get VMToCompiler");
    _vmToCompilerPermObject = JNIHandles::make_global((oop) result.get_jobject());
  }
  return Handle(JNIHandles::resolve_non_null(_vmToCompilerPermObject));
}

jboolean VMToCompiler::setOption(Handle option) {
  assert(!option.is_null(), "");
  KlassHandle compilerKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotOptions());

  Thread* THREAD = Thread::current();
  JavaValue result(T_BOOLEAN);
  JavaCalls::call_static(&result, compilerKlass, vmSymbols::setOption_name(), vmSymbols::setOption_signature(), option, THREAD);
  check_pending_exception("Error while calling setOption");
  return result.get_jboolean();
}

void VMToCompiler::setDefaultOptions() {
  KlassHandle compilerKlass = loadClass(vmSymbols::com_oracle_graal_hotspot_HotSpotOptions());

  Thread* THREAD = Thread::current();
  JavaValue result(T_VOID);
  JavaCalls::call_static(&result, compilerKlass, vmSymbols::setDefaultOptions_name(), vmSymbols::void_method_signature(), THREAD);
  check_pending_exception("Error while calling setDefaultOptions");
}

jboolean VMToCompiler::compileMethod(Handle hotspot_method, int entry_bci, jboolean blocking, int priority) {
  assert(!hotspot_method.is_null(), "just checking");
  Thread* THREAD = Thread::current();
  JavaValue result(T_BOOLEAN);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(hotspot_method);
  args.push_int(entry_bci);
  args.push_int(blocking);
  args.push_int(priority);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::compileMethod_name(), vmSymbols::compileMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling compileMethod");
  return result.get_jboolean();
}

void VMToCompiler::shutdownCompiler() {
  if (_compilerPermObject != NULL) {
    HandleMark hm;
    JavaThread* THREAD = JavaThread::current();
    JavaValue result(T_VOID);
    JavaCallArguments args;
    args.push_oop(instance());
    JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::shutdownCompiler_name(), vmSymbols::void_method_signature(), &args, THREAD);
    check_pending_exception("Error while calling shutdownCompiler");

    JNIHandles::destroy_global(_compilerPermObject);
    JNIHandles::destroy_global(_vmToCompilerPermObject);
    JNIHandles::destroy_global(_vmToCompilerPermKlass);

    _compilerPermObject = NULL;
    _vmToCompilerPermObject = NULL;
    _vmToCompilerPermKlass = NULL;
  }
}

void VMToCompiler::startCompiler() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::startCompiler_name(), vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling startCompiler");
}

void VMToCompiler::bootstrap() {
  JavaThread* THREAD = JavaThread::current();
  JavaValue result(T_VOID);
  JavaCallArguments args;
  args.push_oop(instance());
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::bootstrap_name(), vmSymbols::void_method_signature(), &args, THREAD);
  check_pending_exception("Error while calling boostrap");
}

oop VMToCompiler::createResolvedJavaMethod(jlong vmId, Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_long(vmId);
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createResolvedJavaMethod_name(), vmSymbols::createResolvedJavaMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling createResolvedJavaMethod");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createJavaMethod(Handle name, Handle signature, Handle holder, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  args.push_oop(signature);
  args.push_oop(holder);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createJavaMethod_name(), vmSymbols::createJavaMethod_signature(), &args, THREAD);
  check_pending_exception("Error while calling createJavaMethod");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createJavaField(Handle holder, Handle name, Handle type, int index, int flags, TRAPS) {
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
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createJavaField_name(), vmSymbols::createJavaField_signature(), &args, THREAD);
  check_pending_exception("Error while calling createJavaField");
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

oop VMToCompiler::createJavaType(Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createJavaType_name(), vmSymbols::createJavaType_signature(), &args, THREAD);
  check_pending_exception("Error while calling createJavaType");
  return (oop) result.get_jobject();
}

oop VMToCompiler::createSignature(Handle name, TRAPS) {
  assert(!name.is_null(), "just checking");
  JavaValue result(T_OBJECT);
  JavaCallArguments args;
  args.push_oop(instance());
  args.push_oop(name);
  JavaCalls::call_interface(&result, vmToCompilerKlass(), vmSymbols::createSignature_name(), vmSymbols::createSignature_signature(), &args, THREAD);
  check_pending_exception("Error while calling createSignature");
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

