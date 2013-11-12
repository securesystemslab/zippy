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
package edu.uci.python.nodes.expressions;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

@SuppressWarnings("unused")
@NodeChildren({@NodeChild(value = "condition", type = PNode.class), @NodeChild(value = "then", type = PNode.class), @NodeChild(value = "orelse", type = PNode.class)})
public abstract class IfExpressionNode extends StatementNode {

    public abstract PNode getCondition();

    public abstract PNode getThen();

    public abstract PNode getOrelse();

    @Child protected StatementNode body;

    @Child protected StatementNode orelse;

    @Specialization
    Object doInteger(int test, Object body, Object orelse) {
        boolean condition = test != 0;
        return runIfExp(condition, body, orelse);
    }

    @Specialization
    Object doBigInteger(BigInteger test, Object body, Object orelse) {
        boolean condition = test.compareTo(BigInteger.ZERO) != 0;
        return runIfExp(condition, body, orelse);
    }

    @Specialization
    Object doDouble(VirtualFrame frame, double test, Object body, Object orelse) {
        boolean condition = test != 0;
        return runIfExp(condition, body, orelse);
    }

    @Specialization
    Object doString(VirtualFrame frame, String test, Object body, Object orelse) {
        boolean condition = test.length() != 0;
        return runIfExp(condition, body, orelse);

    }

    @Specialization
    Object doPTuple(VirtualFrame frame, PTuple test, Object body, Object orelse) {
        boolean condition = test.len() != 0;
        return runIfExp(condition, body, orelse);
    }

    @Specialization
    Object doPList(VirtualFrame frame, PList test, Object body, Object orelse) {
        boolean condition = test.len() != 0;
        return runIfExp(condition, body, orelse);
    }

    @Specialization
    Object doPDictionary(PDictionary test, Object body, Object orelse) {
        boolean condition = test.len() != 0;
        return runIfExp(condition, body, orelse);
    }

    @Generic
    Object doGeneric(Object test, Object body, Object orelse) {
        boolean condition = test != null;
        return runIfExp(condition, body, orelse);
    }

    private static Object runIfExp(boolean test, Object body, Object orelse) {
        if (test) {
            return body;
        } else {
            return orelse;
        }
    }
}
