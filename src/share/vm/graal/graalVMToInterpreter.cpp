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

#include "precompiled.hpp"
#include "graal/graalVMToInterpreter.hpp"
#include "graal/graalInterpreterToVM.hpp"
#include "classfile/systemDictionary.hpp"
#include "classfile/vmSymbols.hpp"
#include "graal/graalCompiler.hpp"

#ifdef HIGH_LEVEL_INTERPRETER

// those are *global* handles
jobject VMToInterpreter::_interpreterPermObject = NULL;
jobject VMToInterpreter::_interpreterPermKlass = NULL;

class JavaArgumentBoxer : public SignatureIterator {
 protected:
  JavaCallArguments* _args;
  Thread* _thread;
  objArrayHandle _obj_array;
  int _index;
  int _position;

 public:
  JavaArgumentBoxer(Symbol* signature, objArrayHandle obj_array, JavaCallArguments* args, bool is_static, TRAPS) : SignatureIterator(signature) {
    _obj_array = obj_array;
    _args = args;
    _index = _position = 0;
    _thread = THREAD;
    if (!is_static) {
      push(next_object(T_OBJECT));
    }
    iterate();
    assert(_index == _obj_array->length(), "arg count mismatch with signature");
  }

  inline void do_bool()   {
    if (!is_return_type()) {
      jvalue value;
      value.z = (jboolean)_args->get_int(_position);
      push(java_lang_boxing_object::create(T_BOOLEAN, &value, _thread));
    }
  }
  inline void do_char()   {
    if (!is_return_type()) {
      jvalue value;
      value.c = (jchar)_args->get_int(_position);
      push(java_lang_boxing_object::create(T_CHAR, &value, _thread));
    }
  }
  inline void do_short()  {
    if (!is_return_type()) {
      jvalue value;
      value.s = (jshort)_args->get_int(_position);
      push(java_lang_boxing_object::create(T_SHORT, &value, _thread));
    }
  }
  inline void do_byte()   {
    if (!is_return_type()) {
      jvalue value;
      value.b = (jbyte)_args->get_int(_position);
      push(java_lang_boxing_object::create(T_BYTE, &value, _thread));
    }
  }
  inline void do_int()    {
    if (!is_return_type()) {
      jvalue value;
      value.i = (jint)_args->get_int(_position);
      push(java_lang_boxing_object::create(T_INT, &value, _thread));
    }
  }

  inline void do_long()   {
    if (!is_return_type()) {
      jvalue value;
      value.j = (jlong)_args->get_long(_position);
      push(java_lang_boxing_object::create(T_LONG, &value, _thread));
    }
  }

  inline void do_float()  {
    if (!is_return_type()) {
      jvalue value;
      value.f = (jfloat)_args->get_float(_position);
      push(java_lang_boxing_object::create(T_FLOAT, &value, _thread));
    }
  }

  inline void do_double() {
    if (!is_return_type()) {
      jvalue value;
      value.d = (jdouble)_args->get_double(_position);
      push(java_lang_boxing_object::create(T_DOUBLE, &value, _thread));
    }
  }

  inline void do_object(int begin, int end) { if (!is_return_type()) push(next_object(T_OBJECT)); }
  inline void do_array(int begin, int end)  { if (!is_return_type()) push(next_object(T_ARRAY)); }
  inline void do_void()                     { }
  
  inline oop next_object(BasicType type) {
    assert(type == T_OBJECT || type == T_ARRAY, "must be");
    return *(_args->get_raw_oop(_position));
  }
  
  inline void push(oop obj) {
    _obj_array->obj_at_put(_index, obj);
    _index++;
  }
};

bool VMToInterpreter::allocate_interpreter(const char* interpreter_class_name, const char* interpreter_arguments, TRAPS) {
  assert(_interpreterPermObject == NULL && _interpreterPermKlass == NULL, "no need to allocate twice");

  HandleMark hm;
  // load the interpreter class using its fully qualified class name
  Symbol* class_name = SymbolTable::lookup(interpreter_class_name, (int)strlen(interpreter_class_name), CHECK_false);
  instanceKlassHandle interpreter_klass = SystemDictionary::resolve_or_null(class_name, SystemDictionary::java_system_loader(), NULL, CHECK_false);
  if (interpreter_klass.is_null()) {
    tty->print_cr("Could not load HighLevelInterpreterClass '%s'", interpreter_class_name);
    return false;
  }

  // allocate an interpreter instance
  interpreter_klass->initialize(CHECK_false);
  instanceHandle interpreter_instance = interpreter_klass->allocate_instance_handle(CHECK_false);
  
  // initialize the interpreter instance
  Handle args;
  if (interpreter_arguments != NULL) {
    args = java_lang_String::create_from_platform_dependent_str(interpreter_arguments, CHECK_false);
  }

  JavaValue result(T_BOOLEAN);
  JavaCalls::call_virtual(&result, interpreter_instance, interpreter_klass, vmSymbols::initialize_name(), vmSymbols::setOption_signature(), args, CHECK_false);
  if (result.get_jboolean() != JNI_TRUE) {
    tty->print_cr("Could not invoke '%s::initialize(String)'", interpreter_class_name);
    return false;
  }

  // store the instance globally and keep it alive
  _interpreterPermObject = JNIHandles::make_global(interpreter_instance);
  _interpreterPermKlass = JNIHandles::make_global(interpreter_klass);

  // register the native functions that are needed by the interpreter
  {
    assert(THREAD->is_Java_thread(), "must be");
    JavaThread* thread = (JavaThread*) THREAD;
    ThreadToNativeFromVM trans(thread);
    JNIEnv *env = thread->jni_environment();
    jclass klass = env->FindClass("com/oracle/graal/hotspot/HotSpotRuntimeInterpreterInterface");
    if (klass == NULL) {
      tty->print_cr("Could not find class HotSpotRuntimeInterpreterInterface");
      return false;
    }
    env->RegisterNatives(klass, InterpreterToVM_methods, InterpreterToVM_methods_count());
    if (thread->has_pending_exception()) {
      tty->print_cr("Could not register HotSpotRuntimeInterpreterInterface native methods");
      return false;
    }
  }

  return true;
}

Handle VMToInterpreter::interpreter_instance() {
  return Handle(JNIHandles::resolve_non_null(_interpreterPermObject));
}

KlassHandle VMToInterpreter::interpreter_klass() {
  return KlassHandle(JNIHandles::resolve_non_null(_interpreterPermKlass));
}

void VMToInterpreter::execute(JavaValue* result, methodHandle* m, JavaCallArguments* args, BasicType expected_result_type, TRAPS) {
  assert(interpreter_instance().not_null(), "must be allocated before the first call");
  assert(THREAD->is_Java_thread(), "must be");
  assert(m != NULL, "must be");
  assert(args != NULL, "must be");

  JavaThread* thread = (JavaThread*)THREAD;
  methodHandle method = *m;
  int parameter_count = ArgumentCount(method->signature()).size() + (method->is_static() ? 0 : 1);
  objArrayHandle args_array = oopFactory::new_objArray(SystemDictionary::Object_klass(), parameter_count, CHECK);
  JavaArgumentBoxer jab(method->signature(), args_array, args, method->is_static(), thread);
  Handle hotspot_method = GraalCompiler::createHotSpotResolvedJavaMethod(method, CHECK);

  JavaValue boxed_result(T_OBJECT);
  JavaCallArguments boxed_args;
  boxed_args.set_receiver(interpreter_instance());
  boxed_args.push_oop(hotspot_method);
  boxed_args.push_oop(args_array);
  
#ifndef PRODUCT
  if (PrintHighLevelInterpreterVMTransitions) {
    ResourceMark m;
    tty->print_cr("VM -> high level interpreter (%s)", method->name_and_sig_as_C_string());
  }
#endif
  
  thread->set_high_level_interpreter_in_vm(false);
  JavaCalls::call_virtual(&boxed_result, interpreter_klass(), vmSymbols::interpreter_execute_name(), vmSymbols::interpreter_execute_signature(), &boxed_args, thread);
  thread->set_high_level_interpreter_in_vm(true);
  
#ifndef PRODUCT
  if (PrintHighLevelInterpreterVMTransitions) {
    ResourceMark m;
    tty->print_cr("High level interpreter (%s) -> VM", method->name_and_sig_as_C_string());
  }
#endif
  
  if (HAS_PENDING_EXCEPTION) {
    return;
  }

  // unbox the result if necessary
  if (is_java_primitive(expected_result_type)) {
    unbox_primitive(&boxed_result, result);
  } else if (expected_result_type == T_OBJECT || expected_result_type == T_ARRAY) {
    result->set_jobject(boxed_result.get_jobject());
  }
}

void VMToInterpreter::unbox_primitive(JavaValue* boxed, JavaValue* result) {
  oop box = JNIHandles::resolve(boxed->get_jobject());

  jvalue value;
  BasicType type = java_lang_boxing_object::get_value(box, &value);
    switch (type) {
    case T_BOOLEAN:
      result->set_jint(value.z);
      break;
    case T_CHAR:
      result->set_jint(value.c);
      break;
    case T_FLOAT:
      result->set_jfloat(value.f);
      break;
    case T_DOUBLE:
      result->set_jdouble(value.d);
      break;
    case T_BYTE:
      result->set_jint(value.b);
      break;
    case T_SHORT:
      result->set_jint(value.s);
      break;
    case T_INT:
      result->set_jint(value.i);
      break;
    case T_LONG:
      result->set_jlong(value.j);
      break;
    default:
      ShouldNotReachHere();
      break;
  }
}

#endif // HIGH_LEVEL_INTERPRETER
