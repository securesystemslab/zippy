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
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

/**
 * @author Gulfem
 * @author zwei
 */

public class ListBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return ListBuiltinsFactory.getFactories();
    }

    // list.append(x)
    @Builtin(name = "append", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListAppendNode extends PythonBuiltinNode {

        @Specialization(order = 0, guards = "isIntStore")
        public PList append(PList list, int arg) {
            IntSequenceStorage store = (IntSequenceStorage) list.getStorage();
            store.appendInt(arg);
            return list;
        }

        @Specialization(order = 1, guards = "isDoubleStore")
        public PList append(PList list, double arg) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) list.getStorage();
            store.appendDouble(arg);
            return list;
        }

        @Specialization
        public PList append(PList list, Object arg) {
            list.append(arg);
            return list;
        }
    }

    // list.extend(L)
    @Builtin(name = "extend", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListExtendNode extends PythonBuiltinNode {

        @Specialization
        public PList extend(PList list1, PList list2) {
            list1.extend(list2);
            return list1;
        }

        @SuppressWarnings("unused")
        @Specialization
        public PList extend(PList list1, Object list2) {
            throw new RuntimeException("invalid arguments for extend()");
        }
    }

    // list.insert(i, x)
    @Builtin(name = "insert", fixedNumOfArguments = 3, hasFixedNumOfArguments = true)
    public abstract static class PythonListInsertNode extends PythonBuiltinNode {

        @Specialization
        public PList insert(PList list, int index, Object value) {
            list.insert(index, value);
            return list;
        }

        @SuppressWarnings("unused")
        @Specialization
        public PList insert(PList list, Object i, Object arg1) {
            throw new RuntimeException("invalid arguments for insert()");
        }
    }

    // list.remove(x)
    @Builtin(name = "remove", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListRemoveNode extends PythonBuiltinNode {

        @Specialization
        public PList remove(PList list, Object arg) {
            int index = list.index(arg);
            list.delItem(index);
            return list;
        }
    }

    // list.pop([i])
    @Builtin(name = "pop", minNumOfArguments = 1, maxNumOfArguments = 2)
    public abstract static class PythonListPopNode extends PythonBuiltinNode {

        @Specialization(order = 0, guards = "isIntStore")
        public int popInt(PList list, @SuppressWarnings("unused") PNone none) {
            IntSequenceStorage store = (IntSequenceStorage) list.getStorage();
            return store.popInt();
        }

        @Specialization(order = 1, guards = "isDoubleStore")
        public double popDouble(PList list, @SuppressWarnings("unused") PNone none) {
            DoubleSequenceStorage store = (DoubleSequenceStorage) list.getStorage();
            return store.popDouble();
        }

        @Specialization(order = 2, guards = "isObjectStore")
        public Object popObject(PList list, @SuppressWarnings("unused") PNone none) {
            ObjectSequenceStorage store = (ObjectSequenceStorage) list.getStorage();
            return store.popObject();
        }

        @Specialization(order = 3)
        public Object popLast(PList list, @SuppressWarnings("unused") PNone none) {
            Object ret = list.getItem(list.len() - 1);
            list.delItem(list.len() - 1);
            return ret;
        }

        @Specialization(order = 4)
        public Object pop(PList list, int index) {
            Object ret = list.getItem(index);
            list.delItem(index);
            return ret;
        }

        @SuppressWarnings("unused")
        @Specialization(order = 5)
        public Object pop(PList list, Object arg) {
            throw new RuntimeException("invalid arguments for pop()");
        }
    }

    // list.index(x)
    @Builtin(name = "index", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListIndexNode extends PythonBuiltinNode {

        @Specialization
        public int index(PList list, Object arg) {
            return list.index(arg);
        }
    }

    // list.count(x)
    @Builtin(name = "count", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonListCountNode extends PythonBuiltinNode {

        @Specialization
        public int count(PList list, Object arg) {
            int count = 0;

            try {
                while (true) {
                    if (list.__iter__().__next__().equals(arg)) {
                        count++;
                    }
                }
            } catch (StopIterationException e) {
                // fall through
            }

            return count;
        }
    }

    // list.sort()
    @Builtin(name = "sort", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonListSortNode extends PythonBuiltinNode {

        @Specialization
        public PList sort(PList list) {
            list.sort();
            return list;
        }
    }

    // list.reverse()
    @Builtin(name = "reverse", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class PythonListReverseNode extends PythonBuiltinNode {

        @Specialization
        public PList reverse(PList list) {
            list.reverse();
            return list;
        }
    }
}
