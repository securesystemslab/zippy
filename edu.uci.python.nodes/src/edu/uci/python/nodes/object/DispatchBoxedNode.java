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

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class DispatchBoxedNode extends Node {

    protected final String attributeId;

    public DispatchBoxedNode(String attributeId) {
        this.attributeId = attributeId;
    }

    public abstract Object getValue(VirtualFrame frame, PythonObject primary);

    public int getIntValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValue(frame, primary));
    }

    public double getDoubleValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValue(frame, primary));
    }

    public boolean getBooleanValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValue(frame, primary));
    }

    protected DispatchBoxedNode rewrite(PythonObject primary, DispatchBoxedNode next) {
        CompilerDirectives.transferToInterpreterAndInvalidate();

        /**
         * zwei: If a PythonObject's layout is invalid, force it to update its layout from its
         * Class's instanceLayout. This avoids infinite recursion when a PythonObject is created
         * with an old instanceLayout and invalidates the previous dispatchNode.
         */
        if (!primary.getStableAssumption().isValid()) {
            primary.syncObjectLayoutWithClass();
        }

        PythonObject storage = primary.getValidStorageFullLookup(attributeId);

        if (storage == null) {
            throw Py.AttributeError(primary + " object has no attribute " + attributeId);
        }

        return replace(LinkedDispatchBoxedNode.create(attributeId, primary, storage, primary.isOwnAttribute(attributeId), next));
    }

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static class UninitializedDispatchBoxedNode extends DispatchBoxedNode {

        public UninitializedDispatchBoxedNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonObject primary) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            if (!primary.getStableAssumption().isValid()) {
                primary.syncObjectLayoutWithClass();
            }

            DispatchBoxedNode current = this;
            int depth = 0;

            while (current.getParent() instanceof DispatchBoxedNode) {
                current = (DispatchBoxedNode) current.getParent();
                depth++;

                if (!(current instanceof LinkedDispatchBoxedNode)) {
                    continue;
                }

                LinkedDispatchBoxedNode linked = (LinkedDispatchBoxedNode) current;
                try {
                    if (linked.primaryCheck.accept(primary)) {
                        return linked.getValue(frame, primary);
                    }
                } catch (InvalidAssumptionException e) {
                    throw new RuntimeException();
                }
            }

            DispatchBoxedNode specialized;

            if (depth < PythonOptions.AttributeAccessInlineCacheMaxDepth) {
                specialized = rewrite(primary, this);
            } else {
                specialized = current.replace(new GenericDispatchBoxedNode(attributeId));
            }

            return specialized.getValue(frame, primary);
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericDispatchBoxedNode extends DispatchBoxedNode {

        public GenericDispatchBoxedNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonObject primary) {
            if (!primary.getStableAssumption().isValid()) {
                primary.syncObjectLayoutWithClass();
            }

            return primary.getAttribute(attributeId);
        }
    }

    public static final class LinkedDispatchBoxedNode extends DispatchBoxedNode {

        @Child protected LayoutCheckNode primaryCheck;
        @Child protected AttributeReadNode read;
        @Child protected DispatchBoxedNode next;

        private final PythonObject cachedStorage;

        public LinkedDispatchBoxedNode(String attributeId, LayoutCheckNode checkNode, AttributeReadNode read, PythonObject storage, DispatchBoxedNode next) {
            super(attributeId);
            this.primaryCheck = checkNode;
            this.read = read;
            this.next = next;
            this.cachedStorage = storage;
        }

        public static LinkedDispatchBoxedNode create(String attributeId, PythonObject primary, PythonObject storage, boolean isAttributeInPlace, DispatchBoxedNode next) {
            LayoutCheckNode check = LayoutCheckNode.create(primary, attributeId, isAttributeInPlace);
            AttributeReadNode read = AttributeReadNode.create(storage.getOwnValidLocation(attributeId));

            if (!(primary instanceof PythonClass)) {
                if (isAttributeInPlace) {
                    assert primary == storage;
                    return new LinkedDispatchBoxedNode(attributeId, check, read, null, next);
                } else {
                    return new LinkedDispatchBoxedNode(attributeId, check, read, storage, next);
                }
            } else if (primary instanceof PythonClass || primary instanceof PythonModule) {
                return new LinkedDispatchBoxedNode(attributeId, check, read, storage, next);
            }

            throw new IllegalStateException();
        }

        public AttributeReadNode extractReadNode() {
            return read;
        }

        private PythonObject getStorage(PythonObject primary) {
            return cachedStorage == null ? primary : cachedStorage;
        }

        @Override
        public NodeCost getCost() {
            if (next != null && next.getCost() == NodeCost.MONOMORPHIC) {
                return NodeCost.POLYMORPHIC;
            }
            return super.getCost();
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonObject primaryObj) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getValueUnsafe(getStorage(primaryObj));
                } else {
                    return next.getValue(frame, primaryObj);
                }
            } catch (InvalidAssumptionException e) {
                return rewrite(primaryObj, next).getValue(frame, primaryObj);
            }
        }

        @Override
        public int getIntValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
            try {
                if (primaryCheck.accept(primary)) {
                    return read.getIntValueUnsafe(getStorage(primary));
                } else {
                    return next.getIntValue(frame, primary);
                }
            } catch (InvalidAssumptionException e) {
                return rewrite(primary, next).getIntValue(frame, primary);
            }
        }

        @Override
        public double getDoubleValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
            try {
                if (primaryCheck.accept(primary)) {
                    return read.getDoubleValueUnsafe(getStorage(primary));
                } else {
                    return next.getDoubleValue(frame, primary);
                }
            } catch (InvalidAssumptionException e) {
                return rewrite(primary, next).getDoubleValue(frame, primary);
            }
        }

        @Override
        public boolean getBooleanValue(VirtualFrame frame, PythonObject primary) throws UnexpectedResultException {
            try {
                if (primaryCheck.accept(primary)) {
                    return read.getBooleanValueUnsafe(getStorage(primary));
                } else {
                    return next.getBooleanValue(frame, primary);
                }
            } catch (InvalidAssumptionException e) {
                return rewrite(primary, next).getBooleanValue(frame, primary);
            }
        }
    }

}
