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
package edu.uci.python.nodes.expression;

import java.math.BigInteger;

import org.python.core.*;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.ast.VisitorIF;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

@GenerateNodeFactory
public abstract class CastToBooleanNode extends UnaryOpNode {

    /**
     * zwei: Added the dummy parameter {@link VirtualFrame} to get around the Truffle DSL code gen
     * bug.
     */
    public abstract boolean executeBoolean(VirtualFrame frame, Object value);

    @Override
    public abstract boolean executeBoolean(VirtualFrame frame);

    @GenerateNodeFactory
    public abstract static class YesNode extends CastToBooleanNode {

        @Specialization()
        boolean doBoolean(boolean operand) {
            return operand;
        }

        @Specialization()
        boolean doInteger(int operand) {
            return operand != 0;
        }

        @Specialization()
        boolean doBigInteger(BigInteger operand) {
            return operand.compareTo(BigInteger.ZERO) != 0;
        }

        @Specialization()
        boolean doDouble(double operand) {
            return operand != 0;
        }

        @Specialization()
        boolean doString(String operand) {
            return operand.length() != 0;
        }

        @Specialization(guards = "isNone(operand)")
        boolean doNone(@SuppressWarnings("unused") Object operand) {
            return false;
        }

        @TruffleBoundary
        @Specialization()
        boolean doPythonObject(PythonObject object) {
            Object boolAttribute = object.getAttribute("__bool__");
            if (boolAttribute != null && boolAttribute instanceof PFunction) {
                PMethod method = new PMethod(object, (PFunction) boolAttribute);
                Object value = method.call(null, null);
                if (value instanceof Boolean) {
                    return (Boolean) value;
                } else {
                    throw Py.TypeError("__bool__ should return bool, returned " + object);
                }
            } else {
                return true;
            }
        }

        @Specialization()
        boolean doPTuple(PTuple operand) {
            return operand.len() != 0;
        }

        @Specialization(guards = "isEmptyStorage(list)")
        public boolean doPListEmpty(@SuppressWarnings("unused") PList list) {
            return false;
        }

        @Specialization(guards = "isIntStorage(primary)")
        public boolean doPListInt(PList primary) {
            IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
            return store.length() != 0;
        }

        @Specialization(guards = "isDoubleStorage(primary)")
        public boolean doPListDouble(PList primary) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
            return store.length() != 0;
        }

        @Specialization(guards = "isObjectStorage(primary)")
        public boolean doPListObject(PList primary) {
            ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
            return store.length() != 0;
        }

        @Specialization()
        boolean doPList(PList operand) {
            return operand.len() != 0;
        }

        @Specialization()
        boolean doPDictionary(PDict operand) {
            return operand.len() != 0;
        }

        @Fallback
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand != null) {
                return true;
            }

            return false;
        }
    }

    @GenerateNodeFactory
    public abstract static class NotNode extends CastToBooleanNode {

        @Specialization
        boolean doBool(boolean operand) {
            return !operand;
        }

        @Specialization
        boolean doInteger(int operand) {
            return operand == 0;
        }

        @Specialization
        boolean doBigInteger(BigInteger operand) {
            return operand.compareTo(BigInteger.ZERO) == 0;
        }

        @Specialization
        boolean doDouble(double operand) {
            return operand == 0;
        }

        @Specialization
        boolean doString(String operand) {
            return operand.length() == 0;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "isNone(operand)")
        boolean doNone(Object operand) {
            return true;
        }

        @Specialization
        boolean doPTuple(PTuple operand) {
            return operand.len() == 0;
        }

        @Specialization(guards = "isEmptyStorage(list)")
        public boolean doPListEmpty(@SuppressWarnings("unused") PList list) {
            return false;
        }

        @Specialization(guards = "isIntStorage(primary)")
        public boolean doPListInt(PList primary) {
            IntSequenceStorage store = (IntSequenceStorage) primary.getStorage();
            return store.length() == 0;
        }

        @Specialization(guards = "isDoubleStorage(primary)")
        public boolean doPListDouble(PList primary) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) primary.getStorage();
            return store.length() == 0;
        }

        @Specialization(guards = "isObjectStorage(primary)")
        public boolean doPListObject(PList primary) {
            ObjectSequenceStorage store = (ObjectSequenceStorage) primary.getStorage();
            return store.length() == 0;
        }

        @Specialization
        boolean doPList(PList operand) {
            return operand.len() == 0;
        }

        @Specialization
        boolean doPDictionary(PDict operand) {
            return operand.len() == 0;
        }

        @Fallback
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand == null) {
                return true;
            }

            return false;
        }
    }

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitCastToBooleanNode(this);
    }

}
