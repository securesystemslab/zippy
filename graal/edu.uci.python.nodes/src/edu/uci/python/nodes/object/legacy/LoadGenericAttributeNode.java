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

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

import org.python.core.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public class LoadGenericAttributeNode extends LoadAttributeNode {

    public LoadGenericAttributeNode(LoadAttributeNode node) {
        super(node.attributeId, node.primary);
    }

    public static Object executeGeneric(Object primary, String attributeId) {
        if (primary instanceof PythonObject) {
            return ((PythonObject) primary).getAttribute(attributeId);
        } else if (primary instanceof PythonBuiltinObject) {
            return ((PythonBuiltinObject) primary).__getattribute__(attributeId);
        } else if (primary instanceof PyObject) {
            PyObject pyObj = (PyObject) primary;
            return unboxPyObject(pyObj.__findattr__(attributeId));
        } else {
            throw new RuntimeException("Unexpected primary type " + primary);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return ((PythonObject) primary.execute(frame)).getAttribute(attributeId);
    }

    public static class LoadPObjectAttributeNode extends LoadAttributeNode {

        public LoadPObjectAttributeNode(LoadAttributeNode node) {
            super(node.attributeId, node.primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return ((PythonBuiltinObject) primary.execute(frame)).__getattribute__(attributeId);
        }
    }

    public static class LoadPyObjectAttributeNode extends LoadAttributeNode {

        public LoadPyObjectAttributeNode(LoadAttributeNode node) {
            super(node.attributeId, node.primary);
        }

        public LoadPyObjectAttributeNode(String attributeId, PNode primary) {
            super(attributeId, primary);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PyObject pyObj = (PyObject) primary.execute(frame);
            return unboxPyObject(pyObj.__findattr__(attributeId));
        }
    }
}
