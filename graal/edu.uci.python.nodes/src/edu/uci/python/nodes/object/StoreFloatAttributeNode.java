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
package edu.uci.python.nodes.object;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.object.*;

public class StoreFloatAttributeNode extends StoreSpecializedAttributeNode {

    private final FloatStorageLocation storageLocation;

    public StoreFloatAttributeNode(String name, PNode primary, PNode rhs, ObjectLayout objectLayout, FloatStorageLocation storageLocation) {
        super(name, primary, rhs, objectLayout);
        this.storageLocation = storageLocation;
    }

    @Override
    public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        final PythonObject primaryObject = (PythonObject) primary.execute(frame);

        double value;

        try {
            value = rhs.executeDouble(frame);
        } catch (UnexpectedResultException e) {
            primaryObject.setAttribute(attributeId, e.getResult());
            replace(specialize(primaryObject));
            throw e;
        }

        if (!primaryObject.getObjectLayout().contains(objectLayout)) {
            respecialize(primaryObject, value);
            return value;
        }

        storageLocation.writeDouble(primaryObject, value);
        return value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return executeDouble(frame);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    @Override
    public Object executeWith(VirtualFrame frame, Object value) {
        final PythonObject primaryObject = (PythonObject) primary.execute(frame);

        if (primaryObject.getObjectLayout() != objectLayout) {
            respecialize(primaryObject, value);
            return value;
        }

        storageLocation.writeDouble(primaryObject, (double) value);
        return value;
    }

}
