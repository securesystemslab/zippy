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

#ifndef GPU_HSAIL_VM_HSAIL_KERNEL_ARGUMENTS_HPP
#define GPU_HSAIL_VM_HSAIL_KERNEL_ARGUMENTS_HPP

#include "gpu_hsail.hpp"
#include "runtime/signature.hpp"
#include "hsailArgumentsBase.hpp"

class HSAILKernelArguments : public HSAILArgumentsBase {
  friend class Hsail;

public:

private:
  // Kernel to push into
  address _kernel;
  void* _exceptionHolder;

 public:
    HSAILKernelArguments(address kernel, Symbol* signature, objArrayOop args, bool is_static, void* exceptionHolder) : HSAILArgumentsBase(signature, args, is_static) {
        _kernel = kernel;
        _exceptionHolder = exceptionHolder;
        collectArgs();
    }
    virtual char* argsBuilderName() {return (char*)"HSAILKernelArguments";}
    virtual void pushObject(void* obj) {
        jint status = Hsail::_okra_push_pointer(_kernel, obj);
        assert(status == 0, "arg push failed");
    }
    virtual void pushBool(jboolean z) {
        jint status = Hsail::_okra_push_boolean(_kernel, z);
        assert(status == 0, "arg push failed");
    }
    virtual void pushByte(jbyte b) {
        jint status = Hsail::_okra_push_byte(_kernel, b);
        assert(status == 0, "arg push failed");
    }

    virtual void pushDouble(jdouble d) {
        jint status = Hsail::_okra_push_double(_kernel, d);
        assert(status == 0, "arg push failed");
    }

    virtual void pushFloat(jfloat f) {
        jint status = Hsail::_okra_push_float(_kernel, f);
        assert(status == 0, "arg push failed");
    }

    virtual void pushInt(jint i) {
        jint status = Hsail::_okra_push_int(_kernel, i);
        assert(status == 0, "arg push failed");
    }

    virtual void pushLong(jlong j) {
        jint status = Hsail::_okra_push_long(_kernel, j);
        assert(status == 0, "arg push failed");
    }
    virtual void pushTrailingArgs() {
        if (UseHSAILDeoptimization) {
            // Last argument is the exception info block
            if (TraceGPUInteraction) {
                tty->print_cr("[HSAIL] exception block=" PTR_FORMAT, _exceptionHolder);
            }
            pushObject(_exceptionHolder);
        }
    }

    // For kernel arguments we don't pass the final int parameter
    // since we use the HSAIL workitemid instruction in place of that int value
    virtual void handleFinalIntParameter() {
      ShouldNotReachHere();
    }

    // For kernel arguments, final obj parameter should be an object
    // stream source array (already checked in the base class) so here we just pass it
    virtual void handleFinalObjParameter(void* arg) {
      ShouldNotReachHere();
    }

    virtual void collectArgs();

};

#endif  // GPU_HSAIL_VM_HSAIL_KERNEL_ARGUMENTS_HPP
