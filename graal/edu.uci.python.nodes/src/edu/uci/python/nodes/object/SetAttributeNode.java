/*
 * Copyright (c) 2014, Regents of the University of California
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
package edu.uci.python.nodes.object;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.object.*;

/*-
 * @author zwei
 *
 * The structure of a SetAttributeNode
 * -----------------------------------
 * SetAttributeNode
 *  |
 *  |--- primary : PNode
 *  |
 *  |--- rhs : PNode
 *  |
 *  |--- dispatch : LinkedSetDispatchNode
 *         |
 *         |--- check : ShapeCheckNode
 *         |
 *         |--- write : AttributeWriteNode
 *         |
 *         |--- next : LinkedSetDispatchNode
 *               |
 *               |--- check : ShapeCheckNode
 *               |
 *               |--- write : AttributeWriteNode
 *               |
 *               |--- next : UninitializedSetDispatchNode
 *
 */
public abstract class SetAttributeNode extends PNode implements WriteNode {

    @Child protected PNode primaryNode;
    @Child protected PNode rhs;
    @Child protected SetDispatchNode dispatch;

    private final String attributeId;

    public SetAttributeNode(String attributeId, PNode primary, PNode rhs) {
        this.primaryNode = primary;
        this.rhs = rhs;
        this.attributeId = attributeId;
        this.dispatch = new SetDispatchNode.UninitializedSetDispatchNode(attributeId);
    }

    @Override
    public PNode makeReadNode() {
        return new GetAttributeNode.UninitializedGetAttributeNode(attributeId, primaryNode);
    }

    @Override
    public PNode getRhs() {
        return rhs;
    }

    protected final PythonObject executePrimary(VirtualFrame frame) {
        try {
            return primaryNode.executePythonObject(frame);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        PythonObject primary = executePrimary(frame);
        Object value = rhs.execute(frame);
        return specialize(value).executeWithValue(frame, primary, value);
    }

    @Override
    public Object executeWrite(VirtualFrame frame, Object value) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        PythonObject primary = executePrimary(frame);
        return specialize(value).executeWithValue(frame, primary, value);
    }

    protected final Object executeWithValue(VirtualFrame frame, PythonObject primary, Object value) {
        dispatch.setValue(frame, primary, value);
        return value;
    }

    protected SetAttributeNode specialize(Object value) {
        CompilerAsserts.neverPartOfCompilation();
        SetAttributeNode specialized;

        if (value instanceof Integer) {
            specialized = new SetIntAttributeNode(attributeId, primaryNode, rhs);
        } else if (value instanceof Double) {
            specialized = new SetDoubleAttributeNode(attributeId, primaryNode, rhs);
        } else if (value instanceof Boolean) {
            specialized = new SetBooleanAttributeNode(attributeId, primaryNode, rhs);
        } else {
            specialized = new SetObjectAttributeNode(attributeId, primaryNode, rhs);
        }

        return replace(specialized);
    }

    public static final class UninitializedSetAttributeNode extends SetAttributeNode {

        public UninitializedSetAttributeNode(String attributeId, PNode primary, PNode rhs) {
            super(attributeId, primary, rhs);
        }

    }

    public static final class SetObjectAttributeNode extends SetAttributeNode {

        public SetObjectAttributeNode(String attributeId, PNode primary, PNode rhs) {
            super(attributeId, primary, rhs);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary = executePrimary(frame);
            Object value = rhs.execute(frame);
            dispatch.setValue(frame, primary, value);
            return value;
        }

        @Override
        public Object executeWrite(VirtualFrame frame, Object value) {
            PythonObject primary = executePrimary(frame);
            return executeWithValue(frame, primary, value);
        }
    }

    public static final class SetIntAttributeNode extends SetAttributeNode {

        public SetIntAttributeNode(String attributeId, PNode primary, PNode rhs) {
            super(attributeId, primary, rhs);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary = executePrimary(frame);
            try {
                int value = rhs.executeInt(frame);
                dispatch.setIntValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                return specialize(value).executeWithValue(frame, primary, value);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary = executePrimary(frame);
            try {
                int value = rhs.executeInt(frame);
                dispatch.setIntValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                throw new UnexpectedResultException(specialize(value).executeWithValue(frame, primary, value));
            }
        }

        @Override
        public Object executeWrite(VirtualFrame frame, Object value) {
            PythonObject primary = executePrimary(frame);
            try {
                int ivalue = PythonTypesGen.PYTHONTYPES.expectInteger(value);
                dispatch.setIntValue(frame, primary, ivalue);
                return ivalue;
            } catch (UnexpectedResultException e) {
                Object result = e.getResult();
                return specialize(value).executeWithValue(frame, primary, result);
            }
        }
    }

    public static final class SetDoubleAttributeNode extends SetAttributeNode {

        public SetDoubleAttributeNode(String attributeId, PNode primary, PNode rhs) {
            super(attributeId, primary, rhs);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary = executePrimary(frame);
            try {
                double value = rhs.executeDouble(frame);
                dispatch.setDoubleValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                return specialize(value).executeWithValue(frame, primary, value);
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary = executePrimary(frame);
            try {
                double value = rhs.executeDouble(frame);
                dispatch.setDoubleValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                throw new UnexpectedResultException(specialize(value).executeWithValue(frame, primary, value));
            }
        }

        @Override
        public Object executeWrite(VirtualFrame frame, Object value) {
            PythonObject primary = executePrimary(frame);
            try {
                double dvalue = PythonTypesGen.PYTHONTYPES.expectDouble(value);
                dispatch.setDoubleValue(frame, primary, dvalue);
                return dvalue;
            } catch (UnexpectedResultException e) {
                Object result = e.getResult();
                return specialize(value).executeWithValue(frame, primary, result);
            }
        }
    }

    public static final class SetBooleanAttributeNode extends SetAttributeNode {

        public SetBooleanAttributeNode(String attributeId, PNode primary, PNode rhs) {
            super(attributeId, primary, rhs);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary = executePrimary(frame);
            try {
                boolean value = rhs.executeBoolean(frame);
                dispatch.setBooleanValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                return specialize(value).executeWithValue(frame, primary, value);
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary = executePrimary(frame);
            try {
                boolean value = rhs.executeBoolean(frame);
                dispatch.setBooleanValue(frame, primary, value);
                return value;
            } catch (UnexpectedResultException e) {
                Object value = e.getResult();
                throw new UnexpectedResultException(specialize(value).executeWithValue(frame, primary, value));
            }
        }

        @Override
        public Object executeWrite(VirtualFrame frame, Object value) {
            PythonObject primary = executePrimary(frame);
            try {
                boolean bvalue = PythonTypesGen.PYTHONTYPES.expectBoolean(value);
                dispatch.setBooleanValue(frame, primary, bvalue);
                return bvalue;
            } catch (UnexpectedResultException e) {
                Object result = e.getResult();
                return specialize(value).executeWithValue(frame, primary, result);
            }
        }
    }

}
