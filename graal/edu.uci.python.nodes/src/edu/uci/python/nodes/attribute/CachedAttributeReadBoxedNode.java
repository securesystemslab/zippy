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

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.object.*;

public abstract class CachedAttributeReadBoxedNode extends AttributeReadBoxedNode {

    @Child protected PrimaryCheckBoxedNode primaryCheck;
    private final PythonBasicObject cachedStorage;

    public CachedAttributeReadBoxedNode(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storage) {
        super(attributeId);
        this.primaryCheck = checkNode;
        this.cachedStorage = storage;
    }

    public static AttributeReadBoxedNode createUninitialized(String attributeId) {
        return new AttributeReadBoxedNode.UninitializedCachedAttributeNode(attributeId);
    }

    public static CachedAttributeReadBoxedNode create(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storageCache, StorageLocation location) {
        if (location instanceof IntStorageLocation) {
            return new CachedAttributeReadBoxedNode.CachedIntAttributeNode(attributeId, checkNode, storageCache, (IntStorageLocation) location);
        } else if (location instanceof FloatStorageLocation) {
            return new CachedAttributeReadBoxedNode.CachedDoubleAttributeNode(attributeId, checkNode, storageCache, (FloatStorageLocation) location);
        } else {
            return new CachedAttributeReadBoxedNode.CachedObjectAttributeNode(attributeId, checkNode, storageCache, (ObjectStorageLocation) location);
        }
    }

    private PythonBasicObject getStorage(PythonBasicObject primaryObj) {
        return cachedStorage == null ? primaryObj : cachedStorage;
    }

    @Override
    public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(primaryObj)) {
                return getValueUnsafe(frame, getStorage(primaryObj));
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        throw new UnexpectedResultException(primaryObj);
    }

    @Override
    public int getIntValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(primaryObj)) {
                return getIntValueUnsafe(frame, getStorage(primaryObj));
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        throw new UnexpectedResultException(primaryObj);
    }

    @Override
    public double getDoubleValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(primaryObj)) {
                return getDoubleValueUnsafe(frame, getStorage(primaryObj));
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        throw new UnexpectedResultException(primaryObj);
    }

    @Override
    public boolean getBooleanValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(primaryObj)) {
                return getBooleanValueUnsafe(frame, getStorage(primaryObj));
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        throw new UnexpectedResultException(primaryObj);
    }

    public abstract Object getValueUnsafe(VirtualFrame frame, PythonBasicObject storage);

    public int getIntValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValueUnsafe(frame, storage));
    }

    public double getDoubleValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValueUnsafe(frame, storage));
    }

    public boolean getBooleanValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValueUnsafe(frame, storage));
    }

    public static final class CachedObjectAttributeNode extends CachedAttributeReadBoxedNode {

        private final ObjectStorageLocation objLocation;

        public CachedObjectAttributeNode(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storage, ObjectStorageLocation objLocation) {
            super(attributeId, checkNode, storage);
            this.objLocation = objLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject storage) {
            return objLocation.read(storage);
        }
    }

    public static class CachedIntAttributeNode extends CachedAttributeReadBoxedNode {

        private final IntStorageLocation intLocation;

        public CachedIntAttributeNode(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storage, IntStorageLocation intLocation) {
            super(attributeId, checkNode, storage);
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject storage) {
            return intLocation.read(storage);
        }

        @Override
        public int getIntValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
            return intLocation.readInt(storage);
        }
    }

    public static final class CachedDoubleAttributeNode extends CachedAttributeReadBoxedNode {

        private final FloatStorageLocation floatLocation;

        public CachedDoubleAttributeNode(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storage, FloatStorageLocation floatLocation) {
            super(attributeId, checkNode, storage);
            this.floatLocation = floatLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject storage) {
            return floatLocation.read(storage);
        }

        @Override
        public double getDoubleValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
            return floatLocation.readDouble(storage);
        }
    }

    public static final class CachedBooleanAttributeNode extends CachedAttributeReadBoxedNode {

        private final IntStorageLocation intLocation;

        public CachedBooleanAttributeNode(String attributeId, PrimaryCheckBoxedNode checkNode, PythonBasicObject storage, IntStorageLocation intLocation) {
            super(attributeId, checkNode, storage);
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject storage) {
            try {
                return intLocation.readBoolean(storage);
            } catch (UnexpectedResultException e) {
                return e.getResult();
            }
        }

        @Override
        public boolean getBooleanValueUnsafe(VirtualFrame frame, PythonBasicObject storage) throws UnexpectedResultException {
            return intLocation.readBoolean(storage);
        }
    }

}
