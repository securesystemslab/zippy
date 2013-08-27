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

import org.python.core.*;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.datatypes.*;

import static edu.uci.python.nodes.truffle.PythonTypesUtil.*;

@NodeChild(value = "primary", type = PNode.class)
public abstract class AttributeLoadNode extends PNode {

    private final String attributeId;

    public AttributeLoadNode(String name) {
        this.attributeId = name;
    }

    protected AttributeLoadNode(AttributeLoadNode node) {
        this.attributeId = node.attributeId;
    }

    public abstract PNode getPrimary();

    public String getName() {
        return attributeId;
    }

    @Specialization
    public Object doPObject(PObject operand) {
        return operand.findAttribute(attributeId);
    }

    @Specialization
    public Object doString(String operand) {
        PString primString = new PString(operand);
        return primString.findAttribute(attributeId);
    }

    @Generic
    public Object doGeneric(Object operand) {
        PyObject primary = (PyObject) operand;
        return unboxPyObject(primary.__findattr__(attributeId));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " ( " + getPrimary() + ", " + attributeId + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            ASTInterpreter.trace("    ");
        }
        ASTInterpreter.trace(this);

        level++;
        getPrimary().visualize(level);
    }

}
