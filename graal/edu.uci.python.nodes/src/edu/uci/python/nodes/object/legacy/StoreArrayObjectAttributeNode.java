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
package edu.uci.python.nodes.object.legacy;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.object.*;

public class StoreArrayObjectAttributeNode extends StoreSpecializedAttributeNode {

    private final ArrayObjectStorageLocation storageLocation;

    public StoreArrayObjectAttributeNode(String name, PNode primary, PNode rhs, ObjectLayout objLayout, ArrayObjectStorageLocation storageLocation) {
        super(name, primary, rhs, objLayout);
        this.storageLocation = storageLocation;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final PythonObject primaryObject = (PythonObject) primary.execute(frame);
        final Object value = rhs.execute(frame);
        return doObject(primaryObject, value);
    }

    @Override
    public Object executeWith(VirtualFrame frame, Object value) {
        final PythonObject primaryObject = (PythonObject) primary.execute(frame);
        return doObject(primaryObject, value);
    }

    private Object doObject(PythonObject primaryObject, Object value) {
        if (primaryObject.getObjectLayout() != objectLayout) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            respecialize(primaryObject, value);
            return value;
        }

        storageLocation.write(primaryObject, value);
        return value;
    }

}
