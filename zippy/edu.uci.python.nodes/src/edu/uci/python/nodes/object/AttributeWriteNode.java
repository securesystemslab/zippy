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
package edu.uci.python.nodes.object;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.object.location.*;

public abstract class AttributeWriteNode extends Node {

    public static AttributeWriteNode create(StorageLocation location) {
        if (location instanceof ArrayObjectStorageLocation) {
            return new WriteArrayObjectAttributeNode((ArrayObjectStorageLocation) location);
        } else if (location instanceof BooleanStorageLocation) {
            return new WriteBooleanAttributeNode((BooleanStorageLocation) location);
        } else if (location instanceof IntStorageLocation) {
            return new WriteIntAttributeNode((IntStorageLocation) location);
        } else if (location instanceof DoubleStorageLocation) {
            return new WriteDoubleAttributeNode((DoubleStorageLocation) location);
        } else if (location instanceof FieldObjectStorageLocation) {
            return new WriteFieldObjectAttributeNode((FieldObjectStorageLocation) location);
        }

        throw new IllegalStateException();
    }

    public abstract void setValueUnsafe(PythonObject storage, Object value) throws StorageLocationGeneralizeException;

    public void setIntValueUnsafe(PythonObject storage, int value) throws StorageLocationGeneralizeException {
        setValueUnsafe(storage, value);
    }

    public void setDoubleValueUnsafe(PythonObject storage, double value) throws StorageLocationGeneralizeException {
        setValueUnsafe(storage, value);
    }

    public void setBooleanValueUnsafe(PythonObject storage, boolean value) throws StorageLocationGeneralizeException {
        setValueUnsafe(storage, value);
    }

    public static final class WriteArrayObjectAttributeNode extends AttributeWriteNode {

        private final ArrayObjectStorageLocation objLocation;

        public WriteArrayObjectAttributeNode(ArrayObjectStorageLocation objLocation) {
            this.objLocation = objLocation;
        }

        @Override
        public void setValueUnsafe(PythonObject storage, Object value) {
            objLocation.write(storage, value);
        }
    }

    public static final class WriteFieldObjectAttributeNode extends AttributeWriteNode {

        private final FieldObjectStorageLocation objLocation;

        public WriteFieldObjectAttributeNode(FieldObjectStorageLocation objLocation) {
            this.objLocation = objLocation;
        }

        @Override
        public void setValueUnsafe(PythonObject storage, Object value) {
            objLocation.write(storage, value);
        }
    }

    public static final class WriteIntAttributeNode extends AttributeWriteNode {

        private final IntStorageLocation intLocation;

        public WriteIntAttributeNode(IntStorageLocation intLocation) {
            this.intLocation = intLocation;
        }

        @Override
        public void setValueUnsafe(PythonObject storage, Object value) throws StorageLocationGeneralizeException {
            intLocation.write(storage, value);
        }

        @Override
        public void setIntValueUnsafe(PythonObject storage, int value) throws StorageLocationGeneralizeException {
            intLocation.writeInt(storage, value);
        }
    }

    public static final class WriteDoubleAttributeNode extends AttributeWriteNode {

        private final DoubleStorageLocation floatLocation;

        public WriteDoubleAttributeNode(DoubleStorageLocation floatLocation) {
            this.floatLocation = floatLocation;
        }

        @Override
        public void setValueUnsafe(PythonObject storage, Object value) throws StorageLocationGeneralizeException {
            floatLocation.write(storage, value);
        }

        @Override
        public void setDoubleValueUnsafe(PythonObject storage, double value) throws StorageLocationGeneralizeException {
            floatLocation.writeDouble(storage, value);
        }
    }

    public static final class WriteBooleanAttributeNode extends AttributeWriteNode {

        private final BooleanStorageLocation booleanLocation;

        public WriteBooleanAttributeNode(BooleanStorageLocation booleanLocation) {
            this.booleanLocation = booleanLocation;
        }

        @Override
        public void setValueUnsafe(PythonObject storage, Object value) throws StorageLocationGeneralizeException {
            booleanLocation.write(storage, value);
        }

        @Override
        public void setBooleanValueUnsafe(PythonObject storage, boolean value) throws StorageLocationGeneralizeException {
            booleanLocation.writeBoolean(storage, value);
        }
    }

}
