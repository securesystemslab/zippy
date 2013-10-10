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
package edu.uci.python.nodes.calls;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.standardtypes.*;

public class UninitializedCallAttributeNode extends CallAttributeNode {

    @Child protected PNode primary;

    public UninitializedCallAttributeNode(String attributeId, PNode primary, PNode[] arguments) {
        super(arguments, attributeId);
        this.primary = adoptChild(primary);
    }

    @Override
    public PNode getPrimary() {
        return primary;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerAsserts.neverPartOfCompilation();
        Object primaryObj = primary.execute(frame);

        if (primaryObj instanceof PythonObject) {
            CallMethodNode callNode = new CallMethodNode(attributeId, primary, arguments);
            replace(callNode);
            return callNode.callMethod(frame, (PythonObject) primaryObj);
        } else {
            replace(CallAttributeNodeFactory.create(arguments, attributeId, primary));
            return executeGenericSlowPath(frame, primaryObj);
        }
    }

    protected Object executeGenericSlowPath(VirtualFrame frame, Object primaryObj) {
        if (primaryObj instanceof String) {
            return doString(frame, (String) primaryObj);
        } else if (primaryObj instanceof PObject) {
            return doPObject(frame, (PObject) primaryObj);
        } else {
            return doGeneric(frame, primaryObj);
        }
    }
}
