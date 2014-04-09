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

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.object.*;

public class AttributeDispatchUnboxedNode extends AbstractAttributeUnboxedNode {

    @Child protected PrimaryCheckUnboxedNode primaryCheck;
    @Child protected AttributeReadNode read;
    private final PythonBasicObject cachedStorage;

    public AttributeDispatchUnboxedNode(PythonContext context, String attributeId, PrimaryCheckUnboxedNode checkNode, AttributeReadNode read, PythonBasicObject storage) {
        super(context, attributeId);
        this.primaryCheck = checkNode;
        this.read = read;
        this.cachedStorage = storage;
    }

    public static AttributeDispatchUnboxedNode create(PythonContext context, String attributeId, Object primaryObj, PythonBasicObject storage, StorageLocation location) {
        PrimaryCheckUnboxedNode check = PrimaryCheckUnboxedNode.create(primaryObj);
        AttributeReadNode read = AttributeReadNode.create(location);
        return new AttributeDispatchUnboxedNode(context, attributeId, check, read, storage);
    }

    public AttributeReadNode extractReadNode() {
        return read;
    }

    @Override
    public Object getValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return read.getValueUnsafe(cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    @Override
    public int getIntValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return read.getIntValueUnsafe(cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    @Override
    public double getDoubleValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return read.getDoubleValueUnsafe(cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

    @Override
    public boolean getBooleanValue(VirtualFrame frame, Object primaryObj) throws UnexpectedResultException {
        if (primaryCheck.accept(primaryObj)) {
            return read.getBooleanValueUnsafe(cachedStorage);
        } else {
            throw new UnexpectedResultException(primaryObj);
        }
    }

}
