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

#ifndef JAVACALL_ARGUMENTS_HSAIL_HPP
#define JAVACALL_ARGUMENTS_HSAIL_HPP

#include "hsailArgumentsBase.hpp"
#include "runtime/javaCalls.hpp"

class HSAILJavaCallArguments : public HSAILArgumentsBase {

public:

private:
  // JavaCall Args to push into
  JavaCallArguments *_javaArgs;
  int _workitemid;
 public:
    HSAILJavaCallArguments(JavaCallArguments *javaArgs, int workitemid, Symbol* signature, objArrayOop args, bool is_static) : HSAILArgumentsBase(signature, args, is_static) {
        _javaArgs = javaArgs;
        _workitemid = workitemid;
        collectArgs();
    }
    virtual char *argsBuilderName() {return (char *)"HSAILJavaCallArguments";}
    virtual void pushObject(void *obj) { _javaArgs->push_oop((oop) obj);  }
    virtual void pushBool(jboolean z) { pushInt(z); }
    virtual void pushByte(jbyte b) { pushInt(b); }
    virtual void pushDouble(jdouble d) { _javaArgs->push_double(d); }
    virtual void pushFloat(jfloat f) { _javaArgs->push_float(f); }
    virtual void pushInt(jint i) { _javaArgs->push_int(i); }
    virtual void pushLong(jlong j) { _javaArgs->push_long(j); }
    virtual void pushTrailingArgs() {  }

    // For javaCall the final int parameter gets replaced with the workitemid
    // which was passed back in the deopt info
    virtual void handleFinalIntParameter() {
      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] HSAILJavaCallArguments, passing workitemid %d as trailing int parameter", _workitemid);
      }
      pushInt(_workitemid);
    }
    // for kernel arguments, final obj parameter should be an object
    // stream source array (already checked in the base class) so for
    // a javacall we need to extract the correct obj from it based on
    // the workitemid
    virtual void handleFinalObjParameter(void *arg) {
      objArrayOop objArrayArg = (objArrayOop) arg;
      oop extractedObj = objArrayArg->obj_at(_workitemid);
      if (TraceGPUInteraction) {
        tty->print_cr("[HSAIL] HSAILJavaCallArguments, extracted obj #%d from array, 0x%08x is a %s",
                      _workitemid, (address) extractedObj,
                      (extractedObj == NULL ? "null" : extractedObj->klass()->external_name()));
      }
      pushObject(extractedObj);
    }

};

#endif  // JAVACALL_ARGUMENTS_HSAIL_HPP

