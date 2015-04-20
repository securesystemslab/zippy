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
package edu.uci.python.runtime.object.location;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;

public final class BooleanStorageLocation extends FieldStorageLocation {

    public BooleanStorageLocation(ObjectLayout objectLayout, int index, long offset) {
        super(objectLayout, index, offset);
    }

    @Override
    public Object read(PythonObject object) {
        try {
            return readBoolean(object);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public boolean readBoolean(PythonObject object) throws UnexpectedResultException {
        if (isSet(object)) {
            return CompilerDirectives.unsafeGetBoolean(object, offset, true, this);
        } else {
            throw new UnexpectedResultException(PNone.NONE);
        }
    }

    @Override
    public void write(PythonObject object, Object value) throws StorageLocationGeneralizeException {
        if (value instanceof Boolean) {
            writeBoolean(object, (boolean) value);
        } else if (value instanceof PNone) {
            markAsUnset(object);
        } else {
            throw new StorageLocationGeneralizeException();
        }
    }

    public void writeBoolean(PythonObject object, boolean value) {
        CompilerDirectives.unsafePutBoolean(object, offset, value, this);
        markAsSet(object);
    }

    @Override
    public Class<?> getStoredClass() {
        return Boolean.class;
    }

    @Override
    public String toString() {
        return "boolean" + index;
    }

}
