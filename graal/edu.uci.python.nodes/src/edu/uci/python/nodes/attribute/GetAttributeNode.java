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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.calls.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.objects.*;

public abstract class GetAttributeNode extends PNode {

    protected final PythonContext context;
    protected final String attributeId;
    @Child protected PNode primary;

    public GetAttributeNode(PythonContext context, String attributeId, PNode primary) {
        this.context = context;
        this.attributeId = attributeId;
        this.primary = adoptChild(primary);
    }

    public static class BoxedGetAttributeNode extends GetAttributeNode {

        @Child protected AbstractBoxedAttributeNode cache;

        public BoxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, AbstractBoxedAttributeNode cache) {
            super(context, attributeId, primary);
            this.cache = adoptChild(cache);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }

            return cache.getValue(frame, primaryObj);
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }

            return cache.getIntValue(frame, primaryObj);
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }

            return cache.getDoubleValue(frame, primaryObj);
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            PythonBasicObject primaryObj;

            try {
                primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }

            return cache.getBooleanValue(frame, primaryObj);
        }
    }

    public static class UnboxedGetAttributeNode extends GetAttributeNode {

        @Child protected AbstractUnboxedAttributeNode cache;

        public UnboxedGetAttributeNode(PythonContext context, String attributeId, PNode primary, AbstractUnboxedAttributeNode cache) {
            super(context, attributeId, primary);
            this.cache = adoptChild(cache);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                return cache.getValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getIntValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectInteger(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getDoubleValue(frame, primary.execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectDouble(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                return cache.getBooleanValue(frame, execute(frame));
            } catch (UnexpectedResultException e) {
                CompilerDirectives.transferToInterpreter();
                return PythonTypesGen.PYTHONTYPES.expectBoolean(bootstrapBoxedOrUnboxed(frame, e.getResult(), this));
            }
        }
    }

    public static class UnboxedGetMethodNode extends UnboxedGetAttributeNode {

        private final PBuiltinMethod cachedMethod;

        public UnboxedGetMethodNode(PythonContext context, String attributeId, PNode primary, AbstractUnboxedAttributeNode cache, PBuiltinMethod cachedMethod) {
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
                CompilerDirectives.transferToInterpreter();
                return bootstrapBoxedOrUnboxed(frame, e.getResult(), this);
            }
            return cachedMethod;
        }
    }

    protected static Object bootstrapBoxedOrUnboxed(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        CompilerAsserts.neverPartOfCompilation();

        if (primaryObj instanceof PythonBasicObject) {
            return boxedSpecializeAndExecute(frame, (PythonBasicObject) primaryObj, current);
        } else {
            return unboxedSpecializeAndExecute(frame, primaryObj, current);
        }
    }

    protected static Object boxedSpecializeAndExecute(VirtualFrame frame, PythonBasicObject primaryObj, GetAttributeNode current) {
        AbstractBoxedAttributeNode cacheNode = new AbstractBoxedAttributeNode.UninitializedCachedAttributeNode(current.attributeId);
        current.replace(new BoxedGetAttributeNode(current.context, current.attributeId, current.primary, cacheNode));
        return cacheNode.getValue(frame, primaryObj);
    }

    protected static Object unboxedSpecializeAndExecute(VirtualFrame frame, Object primaryObj, GetAttributeNode current) {
        AbstractUnboxedAttributeNode cacheNode = new AbstractUnboxedAttributeNode.UninitializedCachedAttributeNode(current.context, current.attributeId);
        Object value = null;
        try {
            value = cacheNode.getValue(frame, primaryObj);
        } catch (UnexpectedResultException e) {
            throw new IllegalStateException("Attribute access failed in slow path!");
        }

        if (value instanceof PBuiltinFunction) {
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
