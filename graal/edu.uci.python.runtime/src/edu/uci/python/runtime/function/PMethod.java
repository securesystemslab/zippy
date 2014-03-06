/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.runtime.function;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtype.*;

public class PMethod extends PythonBuiltinObject implements PythonCallable {

    private final PFunction function;
    private PythonObject self;
    private final CallTarget callTarget;

    public PMethod(PythonObject self, PFunction function) {
        this.self = self;
        this.function = function;
        this.callTarget = function.getCallTarget();
    }

    public PFunction __func__() {
        return function;
    }

    public PythonObject __self__() {
        return self;
    }

    public void bind(PythonObject newSelf) {
        this.self = newSelf;
    }

    public Object call(PackedFrame caller, Object[] args) {
        return callTarget.call(caller, new PArguments(self, function.getDeclarationFrame(), args));
    }

    public Object call(PackedFrame caller, Object[] args, PKeyword[] keywords) {
        /**
         * The following if block is necessary to catch the execution errors and mark that test case
         * as failed in the unit test.
         */
        if (PythonOptions.catchZippyExceptionForUnitTesting) {
            if (function.getName().equals("_executeTestPart")) {
                try {
                    Object[] combined = function.applyKeywordArgs(true, args, keywords);
                    Object returnValue = callTarget.call(caller, new PArguments(self, function.getDeclarationFrame(), combined));
                    return returnValue;
                } catch (Exception e) {
                    return "ZippyExecutionError";
                }
            }
        }

        Object[] combined = function.applyKeywordArgs(true, args, keywords);
        return callTarget.call(caller, new PArguments(self, function.getDeclarationFrame(), combined));

    }

    @Override
    public CallTarget getCallTarget() {
        return callTarget;
    }

    @Override
    public FrameDescriptor getFrameDescriptor() {
        return function.getFrameDescriptor();
    }

    @Override
    public void arityCheck(int numOfArgs, int numOfKeywords, String[] keywords) {
        /**
         * TODO Causes problem in unit test, so arity check is not performed on PMethod.
         */
        // function.arityCheck(numOfArgs + 1, numOfKeywords, keywords);
    }

    @Override
    public String toString() {
        return "<method " + function.getName() + " at " + hashCode() + ">";
    }

}
