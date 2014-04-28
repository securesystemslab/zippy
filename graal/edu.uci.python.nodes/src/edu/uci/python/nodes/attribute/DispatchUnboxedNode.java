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

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class DispatchUnboxedNode extends Node {

    protected final String attributeId;

    public DispatchUnboxedNode(String attributeId) {
        this.attributeId = attributeId;
    }

    public abstract Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj);

    public int getIntValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValue(frame, primaryObj));
    }

    public double getDoubleValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValue(frame, primaryObj));
    }

    public boolean getBooleanValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValue(frame, primaryObj));
    }

    protected DispatchUnboxedNode rewrite(PythonBuiltinObject primaryObj, DispatchUnboxedNode next) {
        PythonClass current = primaryObj.__class__();
        assert current != null;

        do {
            if (current.isOwnAttribute(attributeId)) {
                break;
            }

            current = current.getSuperClass();
        } while (current != null);

        if (current == null) {
            throw Py.AttributeError(primaryObj + " object has no attribute " + attributeId);
        }

        LinkedDispatchUnboxedNode newNode = new LinkedDispatchUnboxedNode(attributeId, primaryObj, current, next);
        checkAndReplace(newNode);
        return newNode;
    }

    private void checkAndReplace(Node newNode) {
        if (this.getParent() != null) {
            replace(newNode);
        }
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static final class UninitializedDispatchUnboxedNode extends DispatchUnboxedNode {

        public UninitializedDispatchUnboxedNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj) {
            CompilerDirectives.transferToInterpreter();

            Node current = this;
            int depth = 0;
            DispatchUnboxedNode specialized;

            if (current.getParent() == null) {
                specialized = rewrite(primaryObj, this);
                return specialized.getValue(frame, primaryObj);
            }

            while (current.getParent() instanceof DispatchUnboxedNode) {
                current = current.getParent();
                depth++;
            }

            if (depth < PythonOptions.AttributeAccessInlineCacheMaxDepth) {
                specialized = rewrite(primaryObj, this);
            } else {
                specialized = current.replace(new GenericDispatchUnboxedNode(attributeId));
            }

            return specialized.getValue(frame, primaryObj);
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchUnboxedNode extends DispatchUnboxedNode {

        public GenericDispatchUnboxedNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj) {
            return primaryObj.__class__().getAttribute(attributeId);
        }
    }

    public static final class LinkedDispatchUnboxedNode extends DispatchUnboxedNode {

        @Child protected AttributeReadNode read;
        @Child protected DispatchUnboxedNode next;

        private final Class cachedClass;
        private final PythonObject cachedStorage;

        public LinkedDispatchUnboxedNode(String attributeId, Object primary, PythonObject storage, DispatchUnboxedNode next) {
            super(attributeId);
            this.read = AttributeReadNode.create(storage.getOwnValidLocation(attributeId));
            this.next = next;
            this.cachedClass = primary.getClass();
            this.cachedStorage = storage;
        }

        public AttributeReadNode extractReadNode() {
            return read;
        }

        protected boolean dispatchGuard(Object primary) {
            return primary.getClass() == cachedClass;
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj) {
            if (dispatchGuard(primaryObj)) {
                return read.getValueUnsafe(cachedStorage);
            }

            return next.getValue(frame, primaryObj);
        }

        @Override
        public int getIntValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
            if (dispatchGuard(primaryObj)) {
                return read.getIntValueUnsafe(cachedStorage);
            }

            return next.getIntValue(frame, primaryObj);
        }

        @Override
        public double getDoubleValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
            if (dispatchGuard(primaryObj)) {
                return read.getDoubleValueUnsafe(cachedStorage);
            }

            return next.getDoubleValue(frame, primaryObj);
        }

        @Override
        public boolean getBooleanValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
            if (dispatchGuard(primaryObj)) {
                return read.getBooleanValueUnsafe(cachedStorage);
            }

            return next.getBooleanValue(frame, primaryObj);
        }
    }

}
