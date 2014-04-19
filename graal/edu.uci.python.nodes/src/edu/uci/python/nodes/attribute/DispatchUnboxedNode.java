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

    private final PythonContext context;
    private final String attributeId;

    public DispatchUnboxedNode(PythonContext context, String attributeId) {
        this.context = context;
        this.attributeId = attributeId;
    }

    public abstract Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException;

    public int getIntValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValue(frame, primaryObj));
    }

    public double getDoubleValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValue(frame, primaryObj));
    }

    public boolean getBooleanValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValue(frame, primaryObj));
    }

    protected DispatchUnboxedNode rewrite(PythonBuiltinObject primaryObj) {
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

        AttributeDispatchUnboxedNode newNode = AttributeDispatchUnboxedNode.create(context, attributeId, primaryObj, current, getOwnValidLocation(current));
        checkAndReplace(newNode);
        return newNode;
    }

    private void checkAndReplace(Node newNode) {
        if (this.getParent() != null) {
            replace(newNode);
        }
    }

    private StorageLocation getOwnValidLocation(PythonBasicObject storage) {
        final StorageLocation location = storage.getObjectLayout().findStorageLocation(attributeId);
        assert location != null;
        return location;
    }

    public static class UninitializedDispatchUnboxedNode extends DispatchUnboxedNode {

        public UninitializedDispatchUnboxedNode(PythonContext context, String attributeId) {
            super(context, attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBuiltinObject primaryObj) throws UnexpectedResultException {
            CompilerDirectives.transferToInterpreter();
            return rewrite(primaryObj).getValue(frame, primaryObj);
        }
    }

}
