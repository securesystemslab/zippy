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
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class GetAttributeNode extends PNode implements ReadNode {

    protected final PythonContext context;
    protected final String attributeId;
    @Child protected PNode primary;

    public GetAttributeNode(PythonContext context, String attributeId, PNode primary) {
        this.context = context;
        this.attributeId = attributeId;
        this.primary = primary;
    }

    public PNode makeWriteNode(PNode rhs) {
        return new UninitializedStoreAttributeNode(this.attributeId, this.primary, rhs);
    }

    public static class BoxedGetAttributeNode extends GetAttributeNode {

        @Child protected AttributeReadBoxedNode cache;

        public BoxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, AttributeReadBoxedNode cache) {
            super(context, attributeId, primary);
            this.cache = cache;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getIntValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getDoubleValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getBooleanValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }
    }

    public static class BoxedGetMethodNode extends BoxedGetAttributeNode {

        private final PMethod cachedMethod;

        public BoxedGetMethodNode(PythonContext context, String attributeId, PNode primary, AttributeReadBoxedNode cache, PMethod cachedMethod) {
            super(context, attributeId, primary, cache);
            this.cachedMethod = cachedMethod;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                PythonObject primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonObject(primary.execute(frame));
                cache.getValue(frame, primaryObj);
                cachedMethod.bind(primaryObj);
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }

            return cachedMethod;
        }
    }

    public static class UnboxedGetAttributeNode extends GetAttributeNode {

        @Child protected AttributeReadUnboxedNode cache;

        public UnboxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, AttributeReadUnboxedNode cache) {
            super(context, attributeId, primary);
            this.cache = cache;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                return cache.getValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getIntValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getDoubleValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getBooleanValue(frame, execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }
    }

    public static class UnboxedGetMethodNode extends UnboxedGetAttributeNode {

        private final PBuiltinMethod cachedMethod;

        public UnboxedGetMethodNode(PythonContext context, String attributeId, PNode primary, AttributeReadUnboxedNode cache, PBuiltinMethod cachedMethod) {
            super(context, attributeId, primary, cache);
            this.cachedMethod = cachedMethod;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primaryObj = primary.execute(frame);
            try {
                cache.getValue(frame, primaryObj);
                cachedMethod.bind(context.boxAsPythonBuiltinObject(primaryObj));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
            return cachedMethod;
        }
    }

    protected Object bootstrapBoxedOrUnboxed(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        CompilerAsserts.neverPartOfCompilation();

        if (primaryObj instanceof PythonBasicObject) {
            return boxedSpecializeAndExecute(frame, (PythonBasicObject) primaryObj, current);
        } else if (!(primaryObj instanceof PyObject)) {
            return unboxedSpecializeAndExecute(frame, primaryObj, current);
        } else {
            /**
             * Always go with the slow route to perform generic lookup (dependency to PyObject).
             * Should be remove once all built-in modules are implemented.
             */
            current.replace(new LoadGenericAttributeNode.LoadPyObjectAttributeNode(current.attributeId, current.primary));
            return LoadGenericAttributeNode.executeGeneric(primaryObj, current.attributeId);
        }
    }

    protected Object boxedSpecializeAndExecute(VirtualFrame frame, PythonBasicObject primaryObj, GetAttributeNode current) {
        AttributeReadBoxedNode cacheNode = new AttributeReadBoxedNode.UninitializedCachedAttributeNode(current.attributeId).rewrite(primaryObj);
        Object value;

        try {
            value = cacheNode.getValue(frame, primaryObj);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException();
        }

        if (value instanceof PFunction && !(primaryObj instanceof PythonClass) && !(primaryObj instanceof PythonModule)) {
            value = CallAttributeNode.createPMethodFor((PythonObject) primaryObj, (PFunction) value);
            current.replace(new BoxedGetMethodNode(current.context, current.attributeId, current.primary, cacheNode, (PMethod) value));
        } else {
            current.replace(new BoxedGetAttributeNode(current.context, current.attributeId, current.primary, cacheNode));
        }

        return value;
    }

    protected Object unboxedSpecializeAndExecute(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        AttributeReadUnboxedNode cacheNode = new AttributeReadUnboxedNode.UninitializedCachedAttributeNode(current.context, current.attributeId).rewrite(primaryObj);
        Object value = null;

        try {
            value = cacheNode.getValue(frame, primaryObj);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException("Attribute access failed in slow path!");
        }

        if (value instanceof PBuiltinFunction && !(primaryObj instanceof PythonBuiltinClass)) {
            try {
                value = CallAttributeNode.createPBuiltinMethodFor(current.context.boxAsPythonBuiltinObject(primaryObj), (PBuiltinFunction) value);
            } catch (UnexpectedResultException e) {
                throw new IllegalStateException("Attribute access failed in slow path!");
            }
            current.replace(new UnboxedGetMethodNode(current.context, current.attributeId, current.primary, cacheNode, (PBuiltinMethod) value));
        } else {
            current.replace(new UnboxedGetAttributeNode(current.context, current.attributeId, current.primary, cacheNode));
        }

        return value;
    }

    public static class UninitializedGetAttributeNode extends GetAttributeNode {

        public UninitializedGetAttributeNode(PythonContext context, String attributeId, PNode primary) {
            super(context, attributeId, primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primaryObj = primary.execute(frame);
            return bootstrapBoxedOrUnboxed(frame, primaryObj, this);
        }
    }
}
