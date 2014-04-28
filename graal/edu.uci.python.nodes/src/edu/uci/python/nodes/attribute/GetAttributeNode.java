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
package edu.uci.python.nodes.attribute;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

/*-
 * @author zwei
 *
 * The structure of a GetAttributeNode
 * -----------------------------------
 * GetAttributeNode
 *  |
 *  |--- primary : PNode
 *  |
 *  |--- attribute : LinkedDispatchNode
 *         |
 *         |--- check : ShapeCheckNode
 *         |
 *         |--- Read : AttributeReadNode
 *         |
 *         |--- next : LinkedDispatchNode
 *               |
 *               |--- check : ShapeCheckNode
 *               |
 *               |--- Read : AttributeReadNode
 *               |
 *               |--- next : UninitializedDispatchNode
 *
 */
public abstract class GetAttributeNode extends PNode implements ReadNode, HasPrimaryNode {

    protected final PythonContext context;
    protected final String attributeId;
    @Child protected PNode primaryNode;

    public GetAttributeNode(PythonContext context, String attributeId, PNode primary) {
        this.context = context;
        this.attributeId = attributeId;
        this.primaryNode = primary;
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return new SetAttributeNode(attributeId, primaryNode, rhs, context);
    }

    @Override
    public PNode extractPrimary() {
        return primaryNode;
    }

    public abstract Object executeWithPrimary(VirtualFrame frame, Object primaryObj);

    public static class BoxedGetAttributeNode extends GetAttributeNode {

        @Child protected DispatchBoxedNode attribute;

        public BoxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, DispatchBoxedNode cache) {
            super(context, attributeId, primary);
            this.attribute = cache;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getIntValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getDoubleValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getBooleanValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primaryObj) {
            return attribute.getValue(frame, (PythonObject) primaryObj);
        }
    }

    /**
     * Do not cache {@link PMethod} in {@link GetAttributeNode}.
     *
     */
    public static final class BoxedGetMethodNode extends BoxedGetAttributeNode {

        public BoxedGetMethodNode(PythonContext context, String attributeId, PNode primary, DispatchBoxedNode cache) {
            super(context, attributeId, primary, cache);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primaryObj;
            Object value;
            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                value = attribute.getValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }

            if (value instanceof PFunction) {
                return new PMethod(primaryObj, (PFunction) value);
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, primaryObj, this);
            }
        }
    }

    public static class UnboxedGetAttributeNode extends GetAttributeNode {

        @Child protected DispatchUnboxedNode attribute;

        public UnboxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, DispatchUnboxedNode cache) {
            super(context, attributeId, primary);
            this.attribute = cache;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                return attribute.getValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getIntValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getDoubleValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getBooleanValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primaryObj) {
            return attribute.getValue(frame, (PythonBuiltinObject) primaryObj);
        }
    }

    public static final class UnboxedGetMethodNode extends UnboxedGetAttributeNode {

        private final PBuiltinMethod cachedMethod;

        public UnboxedGetMethodNode(PythonContext context, String attributeId, PNode primary, DispatchUnboxedNode cache, PBuiltinMethod cachedMethod) {
            super(context, attributeId, primary, cache);
            this.cachedMethod = cachedMethod;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonBuiltinObject primaryObj;
            try {
                primaryObj = PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame));
                attribute.getValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
            return cachedMethod;
        }
    }

    protected Object bootstrapBoxedOrUnboxed(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        CompilerAsserts.neverPartOfCompilation();

        if (primaryObj instanceof PythonObject) {
            return boxedSpecializeAndExecute(frame, (PythonObject) primaryObj, current);
        } else if (!(primaryObj instanceof PyObject)) {
            return unboxedSpecializeAndExecute(frame, primaryObj, current);
        } else {
            /**
             * Always go with the slow route to perform generic lookup (dependency to PyObject).
             * Should be remove once all built-in modules are implemented.
             */
            current.replace(new LoadGenericAttributeNode.LoadPyObjectAttributeNode(current.attributeId, current.primaryNode));
            return LoadGenericAttributeNode.executeGeneric(primaryObj, current.attributeId);
        }
    }

    protected Object boxedSpecializeAndExecute(VirtualFrame frame, PythonObject primaryObj, GetAttributeNode current) {
        DispatchBoxedNode dispatch = new DispatchBoxedNode.UninitializedDispatchBoxedNode(current.attributeId);
        GetAttributeNode specialized = new BoxedGetAttributeNode(current.context, current.attributeId, current.primaryNode, dispatch);
        Object value = specialized.executeWithPrimary(frame, primaryObj);

        if (value instanceof PFunction && !(primaryObj instanceof PythonClass) && !(primaryObj instanceof PythonModule)) {
            value = new PMethod(primaryObj, (PFunction) value);
            specialized = new BoxedGetMethodNode(current.context, current.attributeId, current.primaryNode, dispatch);
        }

        current.replace(specialized);
        return value;
    }

    protected Object unboxedSpecializeAndExecute(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        PythonBuiltinObject builtinPrimaryObj;
        try {
            builtinPrimaryObj = PythonContext.boxAsPythonBuiltinObject(primaryObj);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException();
        }

        DispatchUnboxedNode dispatch = new DispatchUnboxedNode.UninitializedDispatchUnboxedNode(current.attributeId);
        dispatch = dispatch.rewrite(builtinPrimaryObj, dispatch);
        Object value = null;

        value = dispatch.getValue(frame, builtinPrimaryObj);

        if (value instanceof PBuiltinFunction && !(primaryObj instanceof PythonBuiltinClass)) {
            try {
                value = new PBuiltinMethod(PythonContext.boxAsPythonBuiltinObject(builtinPrimaryObj), (PBuiltinFunction) value);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Attribute access failed in slow path!");
            }
            current.replace(new UnboxedGetMethodNode(current.context, current.attributeId, current.primaryNode, dispatch, (PBuiltinMethod) value));
        } else {
            current.replace(new UnboxedGetAttributeNode(current.context, current.attributeId, current.primaryNode, dispatch));
        }

        return value;
    }

    public static final class UninitializedGetAttributeNode extends GetAttributeNode {

        public UninitializedGetAttributeNode(PythonContext context, String attributeId, PNode primary) {
            super(context, attributeId, primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primaryObj = primaryNode.execute(frame);
            return bootstrapBoxedOrUnboxed(frame, primaryObj, this);
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primaryObj) {
            return bootstrapBoxedOrUnboxed(frame, primaryObj, this);
        }
    }

}
