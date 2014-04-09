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
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;

public abstract class AttributeCacheUnboxedNode extends AbstractAttributeCacheUnboxedNode {

    @Child protected PrimaryCheckUnboxedNode primaryCheck;
    private final PythonBasicObject cachedStorage;

    public AttributeCacheUnboxedNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage) {
        super(context, attributeId);
        this.primaryCheck = checkNode;
        this.cachedStorage = storage;
    }

    public static AbstractAttributeCacheUnboxedNode createUninitialized(PythonContext context, String attributeId) {
        return new AbstractAttributeCacheUnboxedNode.UninitializedCachedAttributeNode(context, attributeId);
    }

    public static AttributeCacheUnboxedNode create(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage, StorageLocation location) {
        if (location instanceof IntStorageLocation) {
            return new AttributeCacheUnboxedNode.CachedIntAttributeNode(context, attributeId, checkNode, storage, (IntStorageLocation) location);
        } else if (location instanceof FloatStorageLocation) {
            return new AttributeCacheUnboxedNode.CachedDoubleAttributeNode(context, attributeId, checkNode, storage, (FloatStorageLocation) location);
        } else {
            return new AttributeCacheUnboxedNode.CachedObjectAttributeNode(context, attributeId, checkNode, storage, (ObjectStorageLocation) location);
        }
    }

    @Override
    public Object getValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return getValueUnsafe(frame, cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    @Override
    public int getIntValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return getIntValueUnsafe(frame, cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    public double getDoulbeValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return getDoubleValueUnsafe(frame, cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    @Override
    public boolean getBooleanValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return getBooleanValueUnsafe(frame, cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
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

    public static class CachedObjectAttributeNode extends AttributeCacheUnboxedNode {

        private final ObjectStorageLocation objLocation;

        public CachedObjectAttributeNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage, ObjectStorageLocation objLocation) {
            super(context, attributeId, checkNode, storage);
            this.objLocation = objLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) {
            return objLocation.read(primaryObj);
        }
    }

    public static class CachedIntAttributeNode extends AttributeCacheUnboxedNode {

        private final IntStorageLocation intLocation;

        public CachedIntAttributeNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage, IntStorageLocation intLocation) {
            super(context, attributeId, checkNode, storage);
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

    public static class CachedDoubleAttributeNode extends AttributeCacheUnboxedNode {

        private final FloatStorageLocation floatLocation;

        public CachedDoubleAttributeNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage, FloatStorageLocation floatLocation) {
            super(context, attributeId, checkNode, storage);
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

    public static class CachedBooleanAttributeNode extends AttributeCacheUnboxedNode {

        private final IntStorageLocation intLocation;

        public CachedBooleanAttributeNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, PythonBasicObject storage, IntStorageLocation intLocation) {
            super(context, attributeId, checkNode, storage);
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
