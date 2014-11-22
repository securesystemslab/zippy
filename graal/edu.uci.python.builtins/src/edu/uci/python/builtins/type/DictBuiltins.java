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
package edu.uci.python.builtins.type;

import java.util.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

/**
 * @author Gulfem
 * @author zwei
 */

public final class DictBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return DictBuiltinsFactory.getFactories();
    }

    // setdefault(key[, default])
    @Builtin(name = "setdefault", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class SetDefaultNode extends PythonBuiltinNode {

        @Specialization
        public Object setDefault(PDict dict, Object arg0, Object arg1) {
            if (dict.getMap().containsKey(arg0)) {
                return dict.getMap().get(arg0);
            } else {
                dict.getMap().put(arg0, arg1);
                return arg1;
            }
        }
    }

    // pop(key[, default])
    @Builtin(name = "pop", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PopNode extends PythonBuiltinNode {

        @Specialization
        public Object pop(PDict dict, Object arg0, Object arg1) {
            Object retVal = dict.getMap().get(arg0);
            if (retVal != null) {
                dict.getMap().remove(arg0);
                return retVal;
            } else {
                return arg1;
            }
        }
    }

    // popitem()
    @Builtin(name = "popitem", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PopItemNode extends PythonBuiltinNode {

        @Specialization
        public Object popItem(PDict dict) {
            Object key = dict.__iter__().__next__();
            dict.delItem(key);
            Object nextKey = dict.__iter__().__next__();
            Object nextValue = dict.getItem(nextKey);
            return new PTuple(new Object[]{nextKey, nextValue});
        }
    }

    // keys()
    @Builtin(name = "keys", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class KeysNode extends PythonBuiltinNode {

        @ExplodeLoop
        @Specialization(rewriteOn = ClassCastException.class, order = 1)
        public PList keysPDictInt(PDict self) {
            IntSequenceStorage store = new IntSequenceStorage();

            for (Object key : self.keys()) {
                store.appendInt((int) key);
            }

            return new PList(store);
        }

        @Specialization(order = 2)
        public PList keys(PDict self) {
            return new PList(self.__iter__());
        }
    }

    // items()
    @Builtin(name = "items", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class ItemsNode extends PythonBuiltinNode {

        @Specialization
        public Object items(PDict self) {
            return new PDictView.PDictViewItems(self);
        }
    }

    // get(key[, default])
    @Builtin(name = "get", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class GetNode extends PythonBuiltinNode {

        @Specialization
        public Object get(PDict dict, Object key, Object defaultValue) {
            final Object value = dict.getMap().get(key);
            return value != null ? value : defaultValue;
        }
    }

    // copy()
    @Builtin(name = "copy", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class CopyNode extends PythonBuiltinNode {

        @Specialization
        public PDict copy(PDict dict) {
            return new PDict(dict.getMap());
        }
    }

    // clear()
    @Builtin(name = "clear", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class ClearNode extends PythonBuiltinNode {

        @Specialization
        public PDict copy(PDict dict) {
            dict.getMap().clear();
            return dict;
        }
    }

    // values()
    @Builtin(name = "values", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class ValuesNode extends PythonBuiltinNode {

        @Specialization
        public PList values(PDict dict) {
            return new PList(dict.values());
        }
    }

}
