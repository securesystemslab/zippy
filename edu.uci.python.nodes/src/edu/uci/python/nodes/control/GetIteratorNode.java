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
package edu.uci.python.nodes.control;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class GetIteratorNode extends UnaryOpNode {

    public abstract Object executeWith(VirtualFrame frame, Object value);

    @Specialization(order = 1, guards = "isIntStorage")
    public Object doPListInt(PList value) {
        return new PIntegerSequenceIterator((IntSequenceStorage) value.getStorage());
    }

    @Specialization(order = 2, guards = "isDoubleStorage")
    public Object doPListDouble(PList value) {
        return new PDoubleSequenceIterator((DoubleSequenceStorage) value.getStorage());
    }

    @Specialization(order = 4)
    public Object doPList(PList value) {
        return value.__iter__();
    }

    @Specialization(order = 5)
    public Object doPRange(PRange value) {
        return value.__iter__();
    }

    @Specialization(order = 6)
    public Object doPIntArray(PIntArray value) {
        return value.__iter__();
    }

    @Specialization(order = 7)
    public Object doPDoubleArray(PDoubleArray value) {
        return value.__iter__();
    }

    @Specialization(order = 8)
    public Object doCharArray(PCharArray value) {
        return value.__iter__();
    }

    @Specialization(order = 9)
    public Object doPSequence(PSequence value) {
        return value.__iter__();
    }

    @Specialization(order = 10)
    public Object doPBaseSet(PBaseSet value) {
        return value.__iter__();
    }

    @Specialization(order = 11)
    public Object doString(String value) {
        return new PStringIterator(value);
    }

    @Specialization(order = 12)
    public Object doPDictionary(PDict value) {
        return value.__iter__();
    }

    @Specialization(order = 13)
    public Object doPEnumerate(PEnumerate value) {
        return value.__iter__();
    }

    @Specialization(order = 14)
    public Object doPZip(PZip value) {
        return value.__iter__();
    }

    @Specialization(order = 15)
    public PIntegerIterator doPIntegerIterator(PIntegerIterator value) {
        return value;
    }

    @Specialization(order = 16)
    public PIterator doPIterable(PIterable value) {
        return value.__iter__();
    }

    @Specialization(order = 17)
    public PGenerator doPGenerator(PGenerator value) {
        replace(new GetGeneratorIteratorNode(getOperand()));
        return value;
    }

    @Specialization(order = 18)
    public PIterator doPIterator(PIterator value) {
        return value;
    }

    @Specialization(order = 20)
    public Object doPythonObject(VirtualFrame frame, PythonObject value) {
        return doSpecialMethodCall(frame, "__iter__", value);
    }

    @Fallback
    public PIterator doGeneric(Object value) {
        throw new RuntimeException("tuple does not support iterable object " + value);
    }

    public static class GetGeneratorIteratorNode extends GetIteratorNode {

        @Child PNode operandNode;
        private PGenerator cachedGenerator;

        public GetGeneratorIteratorNode(PNode operandNode) {
            this.operandNode = operandNode;
        }

        protected GetGeneratorIteratorNode(GetGeneratorIteratorNode prev) {
            this.operandNode = prev.getOperand();
        }

        public final PGenerator getGenerator() {
            return cachedGenerator;
        }

        @Override
        public PNode getOperand() {
            return operandNode;
        }

        @Override
        public Object executeWith(VirtualFrame frame, Object value) {
            PGenerator generator;

            try {
                generator = PythonTypesGen.PYTHONTYPES.expectPGenerator(value);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException();
            }

            if (CompilerDirectives.inInterpreter()) {
                if (cachedGenerator == null) {
                    cachedGenerator = generator;
                }
            }

            return generator;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return executeWith(frame, operandNode.execute(frame));
        }
    }

}
