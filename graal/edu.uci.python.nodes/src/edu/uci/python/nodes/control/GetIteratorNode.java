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
package edu.uci.python.nodes.control;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.expression.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;

public abstract class GetIteratorNode extends UnaryOpNode {

    @Specialization
    public Object doPList(PList value) {
        return value.__iter__();
    }

    @Specialization
    public Object doPTuple(PTuple value) {
        return value.__iter__();
    }

    @Specialization
    public Object doPSequence(PSequence value) {
        return value.__iter__();
    }

    @Specialization
    public Object doPBaseSet(PBaseSet value) {
        return value.__iter__();
    }

    @Specialization
    public Object doString(String value) {
        return new PStringIterator(value);
    }

    @Specialization
    public Object doPDictionary(PDict value) {
        return value.__iter__();
    }

    @Specialization
    public Object doPEnumerate(PEnumerate value) {
        return value.__iter__();
    }

    @Specialization
    public Object doPZip(PZip value) {
        return value.__iter__();
    }

    @Specialization
    public PIntegerIterator doPIntegerIterator(PIntegerIterator value) {
        return value;
    }

    @Specialization
    public PIterator doPIterable(PIterable value) {
        return value.__iter__();
    }

    @Specialization
    public PIterator doPIterator(PIterator value) {
        return value;
    }

}
