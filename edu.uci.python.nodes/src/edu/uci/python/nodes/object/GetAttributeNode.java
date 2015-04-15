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
package edu.uci.python.nodes.object;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;
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
 *         |--- read : AttributeReadNode
 *         |
 *         |--- next : LinkedDispatchNode
 *               |
 *               |--- check : ShapeCheckNode
 *               |
 *               |--- read : AttributeReadNode
 *               |
 *               |--- next : UninitializedDispatchNode
 *
 */
@NodeInfo(shortName = "get_attribute")
public abstract class GetAttributeNode extends PNode implements ReadNode, HasPrimaryNode {

    protected final String attributeId;
    @Child protected PNode primaryNode;

    public GetAttributeNode(String attributeId, PNode primary) {
        this.attributeId = attributeId;
        this.primaryNode = primary;
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return new SetAttributeNode.UninitializedSetAttributeNode(attributeId, primaryNode, rhs);
    }

    @Override
    public PNode extractPrimary() {
        return primaryNode;
    }

    @Override
    public String getAttributeId() {
        return attributeId;
    }

    public static class BoxedGetAttributeNode extends GetAttributeNode {

        @Child protected DispatchBoxedNode attribute;

        public BoxedGetAttributeNode(String attributeId, PNode primary, DispatchBoxedNode cache) {
            super(attributeId, primary);
            this.attribute = cache;
            this.adoptChildren();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;

            try {
                primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, e.getResult());
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary;

            try {
                primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getIntValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary;

            try {
                primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getDoubleValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            PythonObject primary;

            try {
                primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                return attribute.getBooleanValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return attribute.getValue(frame, (PythonObject) primary);
        }
    }

    /**
     * Do not cache {@link PMethod} in {@link GetAttributeNode}.
     *
     */
    public static final class BoxedGetMethodNode extends BoxedGetAttributeNode {

        public BoxedGetMethodNode(String attributeId, PNode primary, DispatchBoxedNode cache) {
            super(attributeId, primary, cache);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonObject primary;
            Object value;
            try {
                primary = PythonTypesGen.PYTHONTYPES.expectPythonObject(primaryNode.execute(frame));
                value = attribute.getValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, e.getResult());
            }

            if (value instanceof PFunction) {
                return new PMethod(primary, (PFunction) value);
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, primary);
            }
        }
    }

    public static class UnboxedGetAttributeNode extends GetAttributeNode {

        @Child protected DispatchUnboxedNode attribute;

        public UnboxedGetAttributeNode(String attributeId, PNode primary, DispatchUnboxedNode cache) {
            super(attributeId, primary);
            this.attribute = cache;
            this.adoptChildren();
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                return attribute.getValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, e.getResult());
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getIntValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getDoubleValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return attribute.getBooleanValue(frame, PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame)));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(specializeAndExecute(frame, e.getResult()));
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return attribute.getValue(frame, (PythonBuiltinObject) primary);
        }
    }

    public static final class UnboxedGetMethodNode extends UnboxedGetAttributeNode {

        private final PBuiltinMethod cachedMethod;

        public UnboxedGetMethodNode(String attributeId, PNode primary, DispatchUnboxedNode cache, PBuiltinMethod cachedMethod) {
            super(attributeId, primary, cache);
            this.cachedMethod = cachedMethod;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonBuiltinObject primary;
            try {
                primary = PythonContext.boxAsPythonBuiltinObject(primaryNode.execute(frame));
                attribute.getValue(frame, primary);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, e.getResult());
            }
            return cachedMethod;
        }
    }

    public static final class GetPyObjectAttributeNode extends GetAttributeNode {

        public GetPyObjectAttributeNode(String attributeId, PNode primary) {
            super(attributeId, primary);
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            PyObject pyobj = (PyObject) primary;
            return findAttr(pyobj);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PyObject pyobj;
            try {
                pyobj = primaryNode.executePyObject(frame);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeAndExecute(frame, e.getResult());
            }
            return findAttr(pyobj);
        }

        @SlowPath
        private Object findAttr(PyObject pyobj) {
            return unboxPyObject(pyobj.__findattr__(attributeId));
        }
    }

    protected Object specializeAndExecute(VirtualFrame frame, Object primary) {
        CompilerDirectives.transferToInterpreterAndInvalidate();

        if (primary instanceof PythonObject) {
            return boxedSpecializeAndExecute(frame, (PythonObject) primary);
        } else if (!(primary instanceof PyObject)) {
            return unboxedSpecializeAndExecute(frame, primary);
        } else {
            /**
             * Always go with the slow route to perform generic lookup (dependency to PyObject).
             * Should be remove once all built-in modules are implemented.
             */
            return replace(new GetPyObjectAttributeNode(attributeId, primaryNode)).executeWithPrimary(frame, primary);
        }
    }

    private Object boxedSpecializeAndExecute(VirtualFrame frame, PythonObject primary) {
        DispatchBoxedNode dispatch = new DispatchBoxedNode.UninitializedDispatchBoxedNode(attributeId);
        BoxedGetAttributeNode specialized = new BoxedGetAttributeNode(attributeId, primaryNode, dispatch);
        Object value = specialized.executeWithPrimary(frame, primary);

        if (value instanceof PFunction) {
            boolean isClassMethod = ((PFunction) value).isClassMethod();
            boolean isStaticMethod = ((PFunction) value).isStaticMethod();
            boolean isPrimaryBindable = !(primary instanceof PythonClass) && !(primary instanceof PythonModule);

            if (!isStaticMethod && (isClassMethod || isPrimaryBindable)) {
                value = new PMethod(primary, (PFunction) value);
                specialized = new BoxedGetMethodNode(attributeId, primaryNode, specialized.attribute);
            }
        }

        replace(specialized);
        return value;
    }

    private Object unboxedSpecializeAndExecute(VirtualFrame frame, Object primary) {
        PythonBuiltinObject builtinPrimary;
        try {
            builtinPrimary = PythonContext.boxAsPythonBuiltinObject(primary);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException();
        }

        DispatchUnboxedNode dispatch = new DispatchUnboxedNode.UninitializedDispatchUnboxedNode(attributeId);
        UnboxedGetAttributeNode specialized = new UnboxedGetAttributeNode(attributeId, primaryNode, dispatch);
        Object value = specialized.executeWithPrimary(frame, builtinPrimary);

        if (value instanceof PBuiltinFunction && !(primary instanceof PythonBuiltinClass)) {
            value = new PBuiltinMethod(builtinPrimary, (PBuiltinFunction) value);
            specialized = new UnboxedGetMethodNode(attributeId, primaryNode, specialized.attribute, (PBuiltinMethod) value);
        }

        replace(specialized);
        return value;
    }

    public static final class UninitializedGetAttributeNode extends GetAttributeNode {

        public UninitializedGetAttributeNode(String attributeId, PNode primary) {
            super(attributeId, primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primary = primaryNode.execute(frame);
            return specializeAndExecute(frame, primary);
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return specializeAndExecute(frame, primary);
        }
    }

}
