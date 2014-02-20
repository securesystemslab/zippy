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

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

/**
 * @author Gulfem
 * @author zwei
 */

public final class DictionaryBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return DictionaryBuiltinsFactory.getFactories();
    }

    // setdefault(key[, default])
    @Builtin(name = "setdefault", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionarySetDefaultNode extends PythonBuiltinNode {

        @Specialization
        public Object setDefalut(Object self, Object arg0, Object arg1) {
            PDict dict = (PDict) self;

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
    public abstract static class PythonDictionaryPopNode extends PythonBuiltinNode {

        @Specialization
        public Object pop(Object self, Object arg0, Object arg1) {
            PDict dict = (PDict) self;

            Object retVal = dict.getMap().get(arg0);
            if (retVal != null) {
                dict.getMap().remove(arg0);
                return retVal;
            } else {
                return arg1;
            }
        }
    }

    // keys()
    @Builtin(name = "keys", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryKeysNode extends PythonBuiltinNode {

        @Specialization
        public PList keys(PDict self) {
            return new PList(self.__iter__());
        }
    }

    // items()
    @Builtin(name = "items", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryItemsNode extends PythonBuiltinNode {

        @Specialization
        public PList items(PDict self) {
            return new PList(self.__iter__());
        }
    }

    // get(key[, default])
    @Builtin(name = "get", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryGetNode extends PythonBuiltinNode {

        @Specialization
        public Object get(Object self, Object arg0, Object arg1) {
            PDict dict = (PDict) self;

            if (dict.getMap().get(arg0) != null) {
                return dict.getMap().get(arg0);
            } else {
                return arg1;
            }
        }
    }

    // copy()
    @Builtin(name = "copy", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryCopyNode extends PythonBuiltinNode {

        @Specialization
        public PDict copy(Object self) {
            PDict dict = (PDict) self;
            return new PDict(dict.getMap());
        }
    }

    // clear()
    @Builtin(name = "clear", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryClearNode extends PythonBuiltinNode {

        @Specialization
        public PDict copy(Object self) {
            PDict dict = (PDict) self;
            dict.getMap().clear();
            return dict;
        }
    }

    // values()
    @Builtin(name = "values", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonDictionaryValuesNode extends PythonBuiltinNode {

        @Specialization
        public PList values(PDict self) {
            return new PList(self.values());
        }
    }
}
