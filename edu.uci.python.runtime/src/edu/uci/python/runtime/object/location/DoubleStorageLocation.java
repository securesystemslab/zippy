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
package edu.uci.python.runtime.object.location;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;

/**
 * A storage location for floats.
 */
public final class DoubleStorageLocation extends FieldStorageLocation {

    public DoubleStorageLocation(ObjectLayout objectLayout, int index, long offset) {
        super(objectLayout, index, offset);
    }

    @Override
    public Object read(PythonObject object) {
        try {
            return readDouble(object);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public double readDouble(PythonObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return CompilerDirectives.unsafeGetDouble(object, offset, true, this);
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    @Override
    public void write(PythonObject object, Object value) throws StorageLocationGeneralizeException {
        if (value instanceof Double) {
            writeDouble(object, (double) value);
        } else if (value instanceof PNone) {
            markAsUnset(object);
        } else {
            throw new StorageLocationGeneralizeException();
        }
    }

    public void writeDouble(PythonObject object, Double value) {
        CompilerDirectives.unsafePutDouble(object, offset, value, this);
        markAsSet(object);
    }

    @Override
    public Class<?> getStoredClass() {
        return Double.class;
    }

}
