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
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
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
                throw new IllegalStateException();
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
                throw new IllegalStateException();
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
                throw new IllegalStateException();
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
                throw new IllegalStateException();
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
            return cache.getValue(frame, primary.execute(frame));
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            return cache.getIntValue(frame, primary.execute(frame));
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            return cache.getDoubleValue(frame, primary.execute(frame));
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            return cache.getBooleanValue(frame, execute(frame));
        }
    }

    protected Object bootstrapBoxedOrUnboxed(VirtualFrame frame, Object primaryObj) {
        CompilerAsserts.neverPartOfCompilation();

        if (primaryObj instanceof PythonBasicObject) {
            return boxedSpecializeAndExecute(frame, (PythonBasicObject) primaryObj);
        } else {
            return unboxedSpecializeAndExecute(frame, primaryObj);
        }
    }

    protected Object boxedSpecializeAndExecute(VirtualFrame frame, PythonBasicObject primaryObj) {
        AbstractBoxedAttributeNode cacheNode = new AbstractBoxedAttributeNode.UninitializedCachedAttributeNode(attributeId);
        replace(new BoxedGetAttributeNode(context, attributeId, primary, cacheNode));
        return cacheNode.getValue(frame, primaryObj);
    }

    protected Object unboxedSpecializeAndExecute(VirtualFrame frame, Object primaryObj) {
        AbstractUnboxedAttributeNode cacheNode = new AbstractUnboxedAttributeNode.UninitializedCachedAttributeNode(context, attributeId);
        replace(new UnboxedGetAttributeNode(context, attributeId, primary, cacheNode));
        return cacheNode.getValue(frame, primaryObj);
    }

    public static class UninitializedGetAttributeNode extends GetAttributeNode {

        public UninitializedGetAttributeNode(PythonContext context, String attributeId, PNode primary) {
            super(context, attributeId, primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object primaryObj = primary.execute(frame);
            return bootstrapBoxedOrUnboxed(frame, primaryObj);
        }
    }
}
