/*
 * Copyright (c) 2015, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.nodes.call;

import java.lang.invoke.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class NewInstanceNode extends Node {

    protected final Assumption instanceLayoutStableAssumption;
    protected final MethodHandle instanceCtor;

    public static NewInstanceNode create(PythonClass clazz) {
        ObjectLayout layout = clazz.getInstanceObjectLayout();
        if (layout instanceof FlexibleObjectStorageLayout) {
            return new NewFlexibleInstanceNode(clazz);
        } else {
            return new NewFixedInstanceNode(clazz);
        }
    }

    public NewInstanceNode(PythonClass pythonClass) {
        this.instanceLayoutStableAssumption = pythonClass.getInstanceObjectLayout().getValidAssumption();
        this.instanceCtor = pythonClass.getInstanceConstructor();
    }

    public final PythonObject createNewInstance(PythonClass clazz) {
        try {
            instanceLayoutStableAssumption.check();
            return invokeCtor(clazz);
        } catch (InvalidAssumptionException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return rewriteAndExecute(clazz);
        }
    }

    public abstract PythonObject invokeCtor(PythonClass clazz);

    protected PythonObject rewriteAndExecute(PythonClass clazz) {
        return replace(create(clazz)).createNewInstance(clazz);
    }

    public static final class NewFixedInstanceNode extends NewInstanceNode {

        public NewFixedInstanceNode(PythonClass pythonClass) {
            super(pythonClass);
        }

        @Override
        public PythonObject invokeCtor(PythonClass clazz) {
            try {
                return (PythonObject) instanceCtor.invokeExact(clazz);
            } catch (Throwable e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new RuntimeException("instance constructor invocation failed in " + this);
            }
        }
    }

    public static final class NewFlexibleInstanceNode extends NewInstanceNode {

        public NewFlexibleInstanceNode(PythonClass pythonClass) {
            super(pythonClass);
        }

        @Override
        public FlexiblePythonObjectStorage invokeCtor(PythonClass clazz) {
            try {
                return (FlexiblePythonObjectStorage) instanceCtor.invokeExact(clazz);
            } catch (Throwable e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new RuntimeException("instance constructor invocation failed in " + this);
            }
        }
    }

}
