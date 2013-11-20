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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.objects.*;

public abstract class GetAttributeNode extends PNode {

    protected final String attributeId;

    @Child protected PNode primary;

    public GetAttributeNode(String attributeId, PNode primary) {
        this.attributeId = attributeId;
        this.primary = adoptChild(primary);
    }

    public static class BoxedGetAttributeNode extends GetAttributeNode {

        @Child protected AbstractBoxedAttributeNode cache;

        public BoxedGetAttributeNode(String attributeId, PNode primary, AbstractBoxedAttributeNode cache) {
            super(attributeId, primary);
            this.cache = adoptChild(cache);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                PythonBasicObject primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                // TODO: fall back
                return PNone.NONE;
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) {
            try {
                PythonBasicObject primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getIntValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                // TODO: fall back
                return 0;
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) {
            try {
                PythonBasicObject primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getDoubleValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                // TODO: fall back
                return 0;
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) {
            try {
                PythonBasicObject primaryObj = PythonTypesGen.PYTHONTYPES.expectPythonBasicObject(primary.execute(frame));
                return cache.getBooleanValue(frame, primaryObj);
            } catch (UnexpectedResultException e) {
                // TODO: fall back
                return false;
            }
        }
    }
}
