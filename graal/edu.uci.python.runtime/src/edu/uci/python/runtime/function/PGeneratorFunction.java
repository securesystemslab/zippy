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
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;

public final class PGeneratorFunction extends PFunction {

    private final int numOfGeneratorBlockNode;
    private final int numOfGeneratorForNode;
    private final CallTarget parallelCallTarget;

    @CompilationFinal private boolean isWorthParallelizing = true;

    public PGeneratorFunction(String name, PythonContext context, Arity arity, RootCallTarget callTarget, FrameDescriptor frameDescriptor, MaterializedFrame declarationFrame,
                    CallTarget parallelCallTarget, int numOfGeneratorBlockNode, int numOfGeneratorForNode) {
        super(name, context, arity, callTarget, frameDescriptor, declarationFrame);
        this.numOfGeneratorBlockNode = numOfGeneratorBlockNode;
        this.numOfGeneratorForNode = numOfGeneratorForNode;
        this.parallelCallTarget = parallelCallTarget;
    }

    public int getNumOfGeneratorBlockNode() {
        return numOfGeneratorBlockNode;
    }

    public int getNumOfGeneratorForNode() {
        return numOfGeneratorForNode;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args) {
        if (PythonOptions.ParallelizeGeneratorCalls) {
            assert parallelCallTarget != null;
            return makeParallelGeneratorHelper(args);
        } else {
            return PGenerator.create(context, getName(), getCallTarget(), getFrameDescriptor(), getDeclarationFrame(), args, numOfGeneratorBlockNode, numOfGeneratorForNode);
        }
    }

    private PGenerator makeParallelGeneratorHelper(Object[] args) {
        if (isWorthParallelizing) {
            PParallelGenerator generator = PParallelGenerator.create(getName(), context, parallelCallTarget, getFrameDescriptor(), getDeclarationFrame(), args);

            if (PythonOptions.ProfileGeneratorCalls) {
                context.getStandardOut().println("[ZipPy] create parallel generator " + generator);
            }

            return generator;
        } else {
            return PGenerator.create(context, getName(), getCallTarget(), getFrameDescriptor(), getDeclarationFrame(), args, numOfGeneratorBlockNode, numOfGeneratorForNode);
        }
    }

    @Override
    public Object call(PackedFrame caller, Object[] arguments, PKeyword[] keywords) {
        throw new UnsupportedOperationException();
    }

}
