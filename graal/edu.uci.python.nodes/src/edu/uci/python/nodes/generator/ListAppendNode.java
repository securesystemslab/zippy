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
package edu.uci.python.nodes.generator;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.expression.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

/**
 * Implements LIST_APPEND bytecode in CPython.
 */
public abstract class ListAppendNode extends BinaryOpNode {

    @Specialization
    public boolean doBoolean(PList list, boolean right) {
        list.append(right);
        return right;
    }

    @Specialization(order = 1, guards = "isEmptyStorage")
    public int doEmptyStorage(PList list, int right) {
        list.append(right);
        return right;
    }

    @Specialization(order = 2, guards = "isIntStorage")
    public int doIntStorage(PList list, int right) {
        IntSequenceStorage store = (IntSequenceStorage) list.getStorage();
        store.appendInt(right);
        return right;
    }

    @Specialization(order = 3)
    public int doInteger(PList list, int right) {
        SequenceStorage store = list.getStorage();

        if (store instanceof IntSequenceStorage) {
            ((IntSequenceStorage) store).appendInt(right);
        } else {
            list.append(right);
        }

        return right;
    }

    @Specialization(order = 4, guards = "isDoubleStorage")
    public double doDoubleStorage(PList list, double right) {
        DoubleSequenceStorage store = (DoubleSequenceStorage) list.getStorage();
        store.appendDouble(right);
        return right;
    }

    @Specialization(order = 5)
    public double doDouble(PList list, double right) {
        SequenceStorage store = list.getStorage();

        if (store instanceof DoubleSequenceStorage) {
            ((DoubleSequenceStorage) store).appendDouble(right);
        } else {
            list.append(right);
        }

        return right;
    }

    @Specialization
    public Object doObject(PList list, Object right) {
        list.append(right);
        return right;
    }

}
