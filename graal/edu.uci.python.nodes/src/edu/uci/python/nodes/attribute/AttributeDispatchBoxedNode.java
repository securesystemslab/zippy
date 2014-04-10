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

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class AttributeDispatchBoxedNode extends AbstractDispatchBoxedNode {

    @Child protected PrimaryCheckBoxedNode primaryCheck;
    @Child protected AttributeReadNode read;
    @Child protected AbstractDispatchBoxedNode next;

    public AttributeDispatchBoxedNode(String attributeId, PrimaryCheckBoxedNode checkNode, AttributeReadNode read, AbstractDispatchBoxedNode next) {
        super(attributeId);
        this.primaryCheck = checkNode;
        this.read = read;
        this.next = next;
    }

    public static AttributeDispatchBoxedNode create(String attributeId, PythonBasicObject primaryObj, PythonBasicObject storage, StorageLocation location, int depth, AbstractDispatchBoxedNode next) {
        PrimaryCheckBoxedNode check = PrimaryCheckBoxedNode.create(primaryObj, depth);
        AttributeReadNode read = AttributeReadNode.create(location);

        if (primaryObj instanceof PythonObject && !(primaryObj instanceof PythonClass)) {
            if (depth == 0) {
                assert primaryObj == storage;
                return new InObjectAttributeDispatchNode(attributeId, check, read, primaryObj, next);
            } else {
                return new CachedObjectAttributeDispatchNode(attributeId, check, read, primaryObj, storage, next);
            }
        } else if (primaryObj instanceof PythonClass || primaryObj instanceof PythonModule) {
            return new CachedClassAttributeDispatchNode(attributeId, check, read, primaryObj, storage, next);
        }

        throw new IllegalStateException();
    }

    public AttributeReadNode extractReadNode() {
        return read;
    }

    abstract boolean dispatchGuard(PythonBasicObject primaryObj);

    abstract PythonBasicObject getStorage(PythonBasicObject primaryObj);

    @Override
    public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (dispatchGuard(primaryObj)) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getValueUnsafe(getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return rewrite(primaryObj, next).getValue(frame, primaryObj);
            }
        }

        return next.getValue(frame, primaryObj);
    }

    @Override
    public int getIntValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (dispatchGuard(primaryObj)) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getIntValueUnsafe(getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return rewrite(primaryObj, next).getIntValue(frame, primaryObj);
            }
        }

        return next.getIntValue(frame, primaryObj);
    }

    @Override
    public double getDoubleValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (dispatchGuard(primaryObj)) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getDoubleValueUnsafe(getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return rewrite(primaryObj, next).getDoubleValue(frame, primaryObj);
            }
        }

        return next.getDoubleValue(frame, primaryObj);
    }

    @Override
    public boolean getBooleanValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (dispatchGuard(primaryObj)) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getBooleanValueUnsafe(getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return rewrite(primaryObj, next).getBooleanValue(frame, primaryObj);
            }
        }

        return next.getBooleanValue(frame, primaryObj);
    }

    /**
     * Primary is an object, attribute is in the object.
     *
     */
    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    public static final class InObjectAttributeDispatchNode extends AttributeDispatchBoxedNode {

        private final PythonClass cachedClass;

        public InObjectAttributeDispatchNode(String attributeId, PrimaryCheckBoxedNode checkNode, AttributeReadNode read, PythonBasicObject primaryObj, AbstractDispatchBoxedNode next) {
            super(attributeId, checkNode, read, next);
            this.cachedClass = primaryObj.getPythonClass();
        }

        @Override
        boolean dispatchGuard(PythonBasicObject primaryObj) {
            return cachedClass == primaryObj.getPythonClass();
        }

        @Override
        PythonBasicObject getStorage(PythonBasicObject primaryObj) {
            return primaryObj;
        }
    }

    /**
     * Primary is an object, attribute is in its class or the super classes.
     *
     */
    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    public static final class CachedObjectAttributeDispatchNode extends AttributeDispatchBoxedNode {

        private final PythonClass cachedClass;
        private final PythonBasicObject cachedStorage;

        public CachedObjectAttributeDispatchNode(String attributeId, PrimaryCheckBoxedNode checkNode, AttributeReadNode read, PythonBasicObject primaryObj, PythonBasicObject storage,
                        AbstractDispatchBoxedNode next) {
            super(attributeId, checkNode, read, next);
            this.cachedClass = primaryObj.getPythonClass();
            this.cachedStorage = storage;
        }

        @Override
        boolean dispatchGuard(PythonBasicObject primaryObj) {
            return cachedClass == primaryObj.getPythonClass();
        }

        @Override
        PythonBasicObject getStorage(PythonBasicObject primaryObj) {
            return cachedStorage;
        }

    }

    /**
     * Primary is a class or a module, attribute is in the primary or one node in its lookup chain.
     */
    @NodeInfo(cost = NodeCost.MONOMORPHIC)
    public static final class CachedClassAttributeDispatchNode extends AttributeDispatchBoxedNode {

        private final PythonBasicObject cachedType;
        private final PythonBasicObject cachedStorage;

        public CachedClassAttributeDispatchNode(String attributeId, PrimaryCheckBoxedNode checkNode, AttributeReadNode read, PythonBasicObject primaryObj, PythonBasicObject storage,
                        AbstractDispatchBoxedNode next) {
            super(attributeId, checkNode, read, next);
            this.cachedType = primaryObj;
            this.cachedStorage = storage;
            assert primaryObj instanceof PythonClass || primaryObj instanceof PythonModule;
        }

        @Override
        boolean dispatchGuard(PythonBasicObject primaryObj) {
            return cachedType == primaryObj;
        }

        @Override
        PythonBasicObject getStorage(PythonBasicObject primaryObj) {
            return cachedStorage;
        }
    }

}
