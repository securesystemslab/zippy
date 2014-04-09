/*
 * Copyright (c) 2014, Regents of the University of California
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

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.object.*;

public abstract class AttributeReadNode extends Node {

    public static AttributeReadNode create(StorageLocation location) {
        if (location instanceof ObjectStorageLocation) {
            return new ReadObjectAttributeNode((ObjectStorageLocation) location);
        } else if (location instanceof IntStorageLocation) {
            return new ReadIntAttributeNode((IntStorageLocation) location);
        } else if (location instanceof FloatStorageLocation) {
            return new ReadDoubleAttributeNode((FloatStorageLocation) location);
        } else if (location instanceof IntStorageLocation) {
            // zwei TODO: Fix storage location for booealn!
            return new ReadBooleanAttributeNode((IntStorageLocation) location);
        }

        throw new IllegalStateException();
    }

    public abstract Object getValueUnsafe(PythonBasicObject storage);

    public int getIntValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValueUnsafe(storage));
    }

    public double getDoubleValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValueUnsafe(storage));
    }

    public boolean getBooleanValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValueUnsafe(storage));
    }

    public static final class ReadObjectAttributeNode extends AttributeReadNode {

        private final ObjectStorageLocation objLocation;

        public ReadObjectAttributeNode(ObjectStorageLocation objLocation) {
            this.objLocation = objLocation;
        }

        @Override
        public Object getValueUnsafe(PythonBasicObject storage) {
            return objLocation.read(storage);
        }
    }

    public static class ReadIntAttributeNode extends AttributeReadNode {

        private final IntStorageLocation intLocation;

        public ReadIntAttributeNode(IntStorageLocation intLocation) {
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(PythonBasicObject storage) {
            return intLocation.read(storage);
        }

        @Override
        public int getIntValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
            return intLocation.readInt(storage);
        }
    }

    public static final class ReadDoubleAttributeNode extends AttributeReadNode {

        private final FloatStorageLocation floatLocation;

        public ReadDoubleAttributeNode(FloatStorageLocation floatLocation) {
            this.floatLocation = floatLocation;
        }

        @Override
        public Object getValueUnsafe(PythonBasicObject storage) {
            return floatLocation.read(storage);
        }

        @Override
        public double getDoubleValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
            return floatLocation.readDouble(storage);
        }
    }

    public static final class ReadBooleanAttributeNode extends AttributeReadNode {

        private final IntStorageLocation intLocation;

        public ReadBooleanAttributeNode(IntStorageLocation intLocation) {
            this.intLocation = intLocation;
        }

        @Override
        public Object getValueUnsafe(PythonBasicObject storage) {
            try {
                return intLocation.readBoolean(storage);
            } catch (UnexpectedResultException e) {
                return e.getResult();
            }
        }

        @Override
        public boolean getBooleanValueUnsafe(PythonBasicObject storage) throws UnexpectedResultException {
            return intLocation.readBoolean(storage);
        }
    }

}
