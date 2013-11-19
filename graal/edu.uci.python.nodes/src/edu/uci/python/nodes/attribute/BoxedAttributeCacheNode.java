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

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.objects.*;

public abstract class BoxedAttributeCacheNode extends Node {

    private final String attributeId;
    @Child protected BoxedCheckNode primaryCheck;

    public BoxedAttributeCacheNode(String attributeId, BoxedCheckNode checkNode) {
        this.attributeId = attributeId;
        this.primaryCheck = adoptChild(checkNode);
    }

    public String getAttributeId() {
        return attributeId;
    }

    public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) {
        try {
            if (primaryCheck.accept(frame, primaryObj)) {
                return getValueUnsafe(frame, primaryObj);
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        // TODO: rewrite
        CompilerDirectives.transferToInterpreter();
        return null;
    }

    public int getIntValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(frame, primaryObj)) {
                return getIntValueUnsafe(frame, primaryObj);
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        // TODO: rewrite
        CompilerDirectives.transferToInterpreter();
        return 0;
    }

    public double getDoulbeValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(frame, primaryObj)) {
                return getDoubleValueUnsafe(frame, primaryObj);
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        // TODO: rewrite
        CompilerDirectives.transferToInterpreter();
        return 0;
    }

    public boolean getBooleanValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        try {
            if (primaryCheck.accept(frame, primaryObj)) {
                return getBooleanValueUnsafe(frame, primaryObj);
            }
        } catch (InvalidAssumptionException iae) {
            // fall through
        }

        // TODO: rewrite
        CompilerDirectives.transferToInterpreter();
        return false;
    }

    public abstract Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj);

    public int getIntValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValueUnsafe(frame, primaryObj));
    }

    public double getDoubleValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValueUnsafe(frame, primaryObj));
    }

    public boolean getBooleanValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValueUnsafe(frame, primaryObj));
    }

    public static class CachedObjectAttributeNode extends BoxedAttributeCacheNode {

        private final ObjectStorageLocation objLocation;

        public CachedObjectAttributeNode(String attributeId, BoxedCheckNode checkNode, ObjectStorageLocation objLocation) {
            super(attributeId, checkNode);
            this.objLocation = objLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) {
            return objLocation.read(primaryObj);
        }
    }

    public static class CachedIntAttributeNode extends BoxedAttributeCacheNode {

        private final IntStorageLocation intLocation;

        public CachedIntAttributeNode(String attributeId, BoxedCheckNode checkNode, IntStorageLocation intLocation) {
            super(attributeId, checkNode);
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) {
            return intLocation.read(primaryObj);
        }

        @Override
        public int getIntValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
            return intLocation.readInt(primaryObj);
        }
    }

    public static class CachedDoubleAttributeNode extends BoxedAttributeCacheNode {

        private final FloatStorageLocation floatLocation;

        public CachedDoubleAttributeNode(String attributeId, BoxedCheckNode checkNode, FloatStorageLocation floatLocation) {
            super(attributeId, checkNode);
            this.floatLocation = floatLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) {
            return floatLocation.read(primaryObj);
        }

        @Override
        public double getDoubleValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
            return floatLocation.readDouble(primaryObj);
        }
    }

    public static class CachedBooleanAttributeNode extends BoxedAttributeCacheNode {

        private final IntStorageLocation intLocation;

        public CachedBooleanAttributeNode(String attributeId, BoxedCheckNode checkNode, IntStorageLocation intLocation) {
            super(attributeId, checkNode);
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) {
            try {
                return intLocation.readBoolean(primaryObj);
            } catch (UnexpectedResultException e) {
                return e.getResult();
            }
        }

        @Override
        public boolean getBooleanValueUnsafe(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
            return intLocation.readBoolean(primaryObj);
        }
    }
}
