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
package org.python.ast.nodes.literals;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.python.ast.nodes.PNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.frame.*;

public class DictLiteralNode extends LiteralNode {

    @Children protected PNode[] keys;

    @Children protected PNode[] values;

    public DictLiteralNode(PNode[] keys, PNode[] values) {
        this.keys = adoptChildren(keys);
        this.values = adoptChildren(values);
    }

    protected DictLiteralNode(DictLiteralNode node) {
        this(node.keys, node.values);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        List<Object> resolvedKeys = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            PNode e = keys[i];
            resolvedKeys.add(e.execute(frame));
        }

        List<Object> resolvedValues = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            PNode e = values[i];
            resolvedValues.add(e.execute(frame));
        }

        Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
        for (int i = 0; i < resolvedKeys.size(); i++) {
            map.put(resolvedKeys.get(i), resolvedValues.get(i));
        }

        return PythonTypesUtil.createDictionary(map);
    }

    @Override
    public String toString() {
        return "dict";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            ASTInterpreter.trace("    ");
        }
        ASTInterpreter.trace(this);

        level++;

        for (PNode k : keys) {
            k.visualize(level);
        }

        for (PNode v : values) {
            v.visualize(level);
        }
    }

}
