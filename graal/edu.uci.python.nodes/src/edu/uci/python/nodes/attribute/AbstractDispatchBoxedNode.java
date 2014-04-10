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

public abstract class AbstractDispatchBoxedNode extends Node {

    protected final String attributeId;

    public AbstractDispatchBoxedNode(String attributeId) {
        this.attributeId = attributeId;
    }

    public abstract Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException;

    public int getIntValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectInteger(getValue(frame, primaryObj));
    }

    public double getDoubleValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectDouble(getValue(frame, primaryObj));
    }

    public boolean getBooleanValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        return PythonTypesGen.PYTHONTYPES.expectBoolean(getValue(frame, primaryObj));
    }

    protected AbstractDispatchBoxedNode rewrite(PythonBasicObject primaryObj, AbstractDispatchBoxedNode next) {
        CompilerAsserts.neverPartOfCompilation();

        // PythonModule
        if (primaryObj instanceof PythonModule) {
            if (!primaryObj.isOwnAttribute(attributeId)) {
                throw new IllegalStateException("module: " + primaryObj + " does not contain attribute " + attributeId);
            }

            AbstractDispatchBoxedNode newNode = AttributeDispatchBoxedNode.create(attributeId, primaryObj, primaryObj, getOwnValidLocation(primaryObj), 0, next);
            checkAndReplace(newNode);
            return newNode;
        }

        int depth = 0;
        PythonClass current = null;
        // Plain PythonObject
        if (!(primaryObj instanceof PythonClass)) {
            assert primaryObj instanceof PythonObject;

            // In place attribute
            if (primaryObj.isOwnAttribute(attributeId)) {
                AbstractDispatchBoxedNode newNode = AttributeDispatchBoxedNode.create(attributeId, primaryObj, primaryObj, getOwnValidLocation(primaryObj), 0, next);
                checkAndReplace(newNode);
                return newNode;
            }

            depth++;
            current = primaryObj.getPythonClass();
        }

        // if primary itself is a PythonClass
        if (current == null) {
            current = (PythonClass) primaryObj;
        }

        // class chain lookup
        do {
            if (current.isOwnAttribute(attributeId)) {
                break;
            }

            current = current.getSuperClass();
            depth++;
        } while (current != null);

        if (current == null) {
            throw Py.AttributeError(primaryObj + " object has no attribute " + attributeId);
        }

        AbstractDispatchBoxedNode newNode = AttributeDispatchBoxedNode.create(attributeId, primaryObj, current, getOwnValidLocation(current), depth, next);
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

    @NodeInfo(cost = NodeCost.UNINITIALIZED)
    public static class UninitializedCachedAttributeNode extends AbstractDispatchBoxedNode {

        public UninitializedCachedAttributeNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            Node current = this;
            int depth = 0;
            AbstractDispatchBoxedNode specialized;

            if (current.getParent() == null) {
                specialized = rewrite(primaryObj, this);
                return specialized.getValue(frame, primaryObj);
            }

            while (current.getParent() instanceof AbstractDispatchBoxedNode) {
                current = current.getParent();
                depth++;
            }

            if (depth < PythonOptions.AttributeAccessInlineCacheMaxDepth) {
                specialized = rewrite(primaryObj, this);
            } else {
                specialized = new GenericAttributeReadBoxedNode(attributeId);
            }

            return specialized.getValue(frame, primaryObj);
        }
    }

    @NodeInfo(cost = NodeCost.MEGAMORPHIC)
    public static final class GenericAttributeReadBoxedNode extends AbstractDispatchBoxedNode {

        public GenericAttributeReadBoxedNode(String attributeId) {
            super(attributeId);
        }

        @Override
        public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
            return primaryObj.getAttribute(attributeId);
        }
    }

}
