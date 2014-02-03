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
import edu.uci.python.nodes.access.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

@NodeInfo(shortName = "add-class-attr")
public class AddClassAttributeNode extends PNode {

    private final String attributeId;
    @Child protected PNode rhs;

    public AddClassAttributeNode(String attributeId, PNode rhs) {
        this.attributeId = attributeId;
        this.rhs = adoptChild(rhs);
    }

    protected static PythonClass getClass(VirtualFrame frame) {
        PArguments args = frame.getArguments(PArguments.class);
        Object arg = args.getArgument(0);
        assert arg != null && arg instanceof PythonClass : "AddClassAttributeNode expects the first argument of the class definition method call to be the defining class";
        return (PythonClass) arg;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        PythonClass clazz = getClass(frame);
        clazz.setAttribute(attributeId, rhs.execute(frame));
        return PNone.NONE;
    }

    /**
     * This class exists only to make ReadNode -> WriteNode logic consistent in PythonTree
     * translation. Should be removed whenever {@link AddClassAttributeNode} is not longer needed.
     */
    public static class ReadClassAttributeNode extends PNode implements ReadNode {

        private final String attributeId;

        public ReadClassAttributeNode(String attributeId) {
            this.attributeId = attributeId;
        }

        public PNode makeWriteNode(PNode rhs) {
            return new AddClassAttributeNode(this.attributeId, rhs);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PythonClass clazz = AddClassAttributeNode.getClass(frame);
            return clazz.getAttribute(attributeId);
        }
    }
}
