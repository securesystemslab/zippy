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
package edu.uci.python.nodes.literal;

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.datatype.*;

public class DictLiteralNode extends LiteralNode {

    @Children final PNode[] keys;
    @Children final PNode[] values;

    public static LiteralNode create(PNode[] keys, PNode[] values) {
        if (keys.length == 0 && values.length == 0) {
            return new EmptyDictLiteralNode();
        } else if (keys.length == 1 && values.length == 1) {
            return new SimpleDictLiteralNode(keys[0], values[0]);
        }

        return new DictLiteralNode(keys, values);
    }

    public DictLiteralNode(PNode[] keys, PNode[] values) {
        this.keys = adoptChildren(keys);
        this.values = adoptChildren(values);
        assert keys.length == values.length;
    }

    @ExplodeLoop
    @Override
    public PDict executePDictionary(VirtualFrame frame) {
        final Map<Object, Object> map = new HashMap<>();

        for (int i = 0; i < values.length; i++) {
            final Object key = keys[i].execute(frame);
            final Object val = values[i].execute(frame);
            map.put(key, val);
        }

        return new PDict(map);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executePDictionary(frame);
    }

    @Override
    public String toString() {
        return "dict";
    }

    public static final class EmptyDictLiteralNode extends LiteralNode {

        @Override
        public Object execute(VirtualFrame frame) {
            return new PDict();
        }
    }

    public static final class SimpleDictLiteralNode extends LiteralNode {

        @Child protected PNode key;
        @Child protected PNode value;

        public SimpleDictLiteralNode(PNode key, PNode value) {
            this.key = adoptChild(key);
            this.value = adoptChild(value);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Map<Object, Object> map = new HashMap<>();
            map.put(key.execute(frame), value.execute(frame));
            return new PDict(map);
        }
    }

}
