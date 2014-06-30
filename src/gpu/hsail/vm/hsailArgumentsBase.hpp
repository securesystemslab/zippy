/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef GPU_HSAIL_VM_HSAIL_ARGUMENTS_BASE_HPP
#define GPU_HSAIL_VM_HSAIL_ARGUMENTS_BASE_HPP

#include "runtime/signature.hpp"


// Base class which iterates thru a signature and pulls from a
// objArrayOop of boxed values.  Used as base for HSAILKernelArguments
// and HSAILJavaCallArguments The derived classes specify how to push
// args onto their data structure
class HSAILArgumentsBase : public SignatureIterator {

public:

private:
  // Array of java argument oops
  objArrayOop _args;
  // Length of args array
  int   _length;
  // Current index into _args
  int _index;
  // number of parameters in the signature
  int _parameter_count;

  Symbol* _signature;
  bool _is_static;

  // records first null parameter seen
  int _first_null_parameter_index;
  
  // Get next java argument
  oop next_arg(BasicType expectedType);

    virtual char* argsBuilderName() = 0;
    virtual void pushObject(void* obj) = 0;
    virtual void pushBool(jboolean z) = 0;
    virtual void pushByte(jbyte b) = 0;
    virtual void pushDouble(jdouble d) = 0;
    virtual void pushFloat(jfloat f) = 0;
    virtual void pushInt(jint i) = 0;
    virtual void pushLong(jlong j) = 0;
    virtual void handleFinalIntParameter() = 0;
    virtual void handleFinalObjParameter(void* obj) = 0;
    virtual void pushTrailingArgs() = 0;

 public:
  HSAILArgumentsBase(Symbol* signature, objArrayOop args, bool is_static) : SignatureIterator(signature) {
    this->_return_type = T_ILLEGAL;
    _index = 0;
    _args = args;
    _is_static = is_static;
    _signature = signature;

    _length = args->length();
    _parameter_count = ArgumentCount(signature).size();

    _first_null_parameter_index = -1;

  }

  void recordNullObjectParameter() {
    if (_first_null_parameter_index == -1) {
      _first_null_parameter_index = _parameter_index;
    }
  }

  bool is_static() {
    return _is_static;
  }

  int length() {
    return _length;
  }

  objArrayOop args() {
    return _args;
  }

  int getFirstNullParameterIndex() {
    return _first_null_parameter_index;
  }

  virtual void collectArgs();

  void do_bool();
  void do_byte();
  void do_double();
  void do_float();
  void do_int();
  void do_long();
  void do_array(int begin, int end);
  void do_object();
  void do_object(int begin, int end);

  void do_void();

  inline void do_char()   {
    /* TODO : To be implemented */
    guarantee(false, "do_char:NYI");
  }
  inline void do_short()  {
    /* TODO : To be implemented */
    guarantee(false, "do_short:NYI");
  }

  bool isLastParameter() {
      return  (_index == (_is_static ?  _parameter_count - 1 : _parameter_count));
  }

};

#endif  // GPU_HSAIL_VM_HSAIL_ARGUMENTS_BASE_HPP
