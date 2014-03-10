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

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.call.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class CastToBooleanNode extends UnaryOpNode {

    public abstract boolean executeBoolean(Object value);

    @Override
    public abstract boolean executeBoolean(VirtualFrame frame);

    public abstract static class YesNode extends CastToBooleanNode {

        @Specialization
        boolean doInteger(int operand) {
            return operand != 0;
        }

        @Specialization
        boolean doBigInteger(BigInteger operand) {
            return operand.compareTo(BigInteger.ZERO) != 0;
        }

        @Specialization
        boolean doDouble(double operand) {
            return operand != 0;
        }

        @Specialization
        boolean doBoolean(boolean operand) {
            return operand;
        }

        @Specialization
        boolean doString(String operand) {
            return operand.length() != 0;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "isNone")
        boolean doNone(Object operand) {
            return false;
        }

        @Specialization
        boolean doPythonObject(PythonObject object) {
            Object boolAttribute = object.getAttribute("__bool__");
            if (boolAttribute != null && boolAttribute instanceof PFunction) {
                PFunction absFunction = (PFunction) boolAttribute;
                PMethod method = CallAttributeNode.createPMethodFor(object, absFunction);
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

        @Specialization
        boolean doPTuple(PTuple operand) {
            return operand.len() != 0;
        }

        @Specialization
        boolean doPList(PList operand) {
            return operand.len() != 0;
        }

        @Specialization
        boolean doPDictionary(PDict operand) {
            return operand.len() != 0;
        }

        @Generic
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand != null) {
                return true;
            }

            return false;
        }
    }

    public abstract static class NotNode extends CastToBooleanNode {

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
        boolean doBoolean(boolean operand) {
            return !operand;
        }

        @Specialization
        boolean doString(String operand) {
            return operand.length() == 0;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "isNone")
        boolean doNone(Object operand) {
            return true;
        }

        @Specialization
        boolean doPTuple(PTuple operand) {
            return operand.len() == 0;
        }

        @Specialization
        boolean doPList(PList operand) {
            return operand.len() == 0;
        }

        @Specialization
        boolean doPDictionary(PDict operand) {
            return operand.len() == 0;
        }

        @Generic
        boolean doGeneric(Object operand) {
            // anything except for 0 and None is true
            if (operand == null) {
                return true;
            }

            return false;
        }

    }

    static boolean isNone(Object value) {
        return value == PNone.NONE;
    }

}
