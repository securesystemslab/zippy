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

import org.python.core.PyObject;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statements.*;

@NodeChildren({@NodeChild(value = "primary", type = PNode.class), @NodeChild(value = "rightNode", type = PNode.class)})
public abstract class AttributeStoreNode extends StatementNode implements Amendable {

    private final String attributeId;

    public AttributeStoreNode(String name) {
        this.attributeId = name;
    }

    protected AttributeStoreNode(AttributeStoreNode node) {
        this.attributeId = node.attributeId;
    }

    public abstract PNode getPrimary();

    @Override
    public StatementNode updateRhs(PNode newRhs) {
        return AttributeStoreNodeFactory.create(attributeId, getPrimary(), newRhs);
    }

    @Specialization
    public Object doGeneric(Object primary, Object value) {
        PyObject prim = (PyObject) primary;
        prim.__setattr__(attributeId, (PyObject) value);
        return null;
    }

}
