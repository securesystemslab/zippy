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

public class AttributeDispatchBoxedNode extends AbstractAttributeBoxedNode {

    @Child protected PrimaryCheckBoxedNode primaryCheck;
    @Child protected AttributeReadNode read;
    @Child protected AbstractAttributeBoxedNode next;

    private final PythonClass primaryType;
    private final PythonBasicObject cachedStorage;

    public AttributeDispatchBoxedNode(String attributeId, PrimaryCheckBoxedNode checkNode, AttributeReadNode read, PythonClass primaryType, PythonBasicObject storage, AbstractAttributeBoxedNode next) {
        super(attributeId);
        this.primaryCheck = checkNode;
        this.read = read;
        this.next = next;
        this.primaryType = primaryType;
        this.cachedStorage = storage;
    }

    public static AttributeDispatchBoxedNode create(String attributeId, PythonBasicObject primaryObj, PythonClass primaryType, PythonBasicObject storageCache, StorageLocation location, int depth,
                    AbstractAttributeBoxedNode next) {
        PrimaryCheckBoxedNode check = PrimaryCheckBoxedNode.create(primaryObj, depth);
        AttributeReadNode read = AttributeReadNode.create(location);
        return new AttributeDispatchBoxedNode(attributeId, check, read, primaryType, storageCache, next);
    }

    public AttributeReadNode extractReadNode() {
        return read;
    }

    private PythonBasicObject getStorage(PythonBasicObject primaryObj) {
        return cachedStorage == null ? primaryObj : cachedStorage;
    }

    @Override
    public Object getValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (primaryType == primaryObj.getPythonClass()) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getValueUnsafe(frame, getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rewrite(primaryObj, next).getValue(frame, primaryObj);
            }
        }
        return next.getValue(frame, primaryObj);
    }

    @Override
    public int getIntValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (primaryType == primaryObj.getPythonClass()) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getIntValueUnsafe(frame, getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rewrite(primaryObj, next).getIntValue(frame, primaryObj);
            }
        }
        return next.getIntValue(frame, primaryObj);
    }

    @Override
    public double getDoubleValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (primaryType == primaryObj.getPythonClass()) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getDoubleValueUnsafe(frame, getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rewrite(primaryObj, next).getDoubleValue(frame, primaryObj);
            }
        }
        return next.getDoubleValue(frame, primaryObj);
    }

    @Override
    public boolean getBooleanValue(VirtualFrame frame, PythonBasicObject primaryObj) throws UnexpectedResultException {
        if (primaryType == primaryObj.getPythonClass()) {
            try {
                if (primaryCheck.accept(primaryObj)) {
                    return read.getBooleanValueUnsafe(frame, getStorage(primaryObj));
                }
            } catch (InvalidAssumptionException iae) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rewrite(primaryObj, next).getBooleanValue(frame, primaryObj);
            }
        }
        return next.getBooleanValue(frame, primaryObj);
    }

}
