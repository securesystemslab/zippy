/*
 * Copyright (c) 2014, Regents of the University of California
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
package edu.uci.python.nodes.call;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

public abstract class BinarySpecialMethodCallNode extends BinaryOpNode {

    @Child protected CallDispatchBoxedNode dispatch;

    private final String specialMethodId;

    public static BinarySpecialMethodCallNode create(String specialMethodId, PythonObject primary, PNode left, PNode right) {
        RuntimeValueNode wrapper = new RuntimeValueNode(primary);
        GetAttributeNode calleeNode = new GetAttributeNode.UninitializedGetAttributeNode(specialMethodId, wrapper);
        CallDispatchBoxedNode uninitialized = new CallDispatchBoxedNode.UninitializedDispatchBoxedNode(specialMethodId, calleeNode, false);
        return BinarySpecialMethodCallNodeFactory.create(specialMethodId, uninitialized, left, right);
    }

    public BinarySpecialMethodCallNode(String specialMethodId, CallDispatchBoxedNode dispatch) {
        this.dispatch = dispatch;
        this.specialMethodId = specialMethodId;
    }

    protected BinarySpecialMethodCallNode(BinarySpecialMethodCallNode prev) {
        this(prev.specialMethodId, prev.dispatch);
    }

    public String getSpecialMethodId() {
        return specialMethodId;
    }

    @Specialization
    public Object executeCall(VirtualFrame frame, PythonObject left, Object right) {
        return dispatch.executeCall(frame, left, new Object[]{left, right}, PKeyword.EMPTY_KEYWORDS);
    }

}
