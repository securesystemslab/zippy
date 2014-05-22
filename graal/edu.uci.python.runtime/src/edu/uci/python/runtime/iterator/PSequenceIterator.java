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
package edu.uci.python.runtime.iterator;

import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.sequence.*;

public class PSequenceIterator implements PIterator {

    protected final PSequence sequence;
    protected int index;

    public PSequenceIterator(PSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public Object __next__() throws StopIterationException {
        if (index < sequence.len()) {
            return sequence.getItem(index++);
        }

        throw StopIterationException.INSTANCE;
    }

    public static final class PSequenceReverseIterator extends PSequenceIterator {

        public PSequenceReverseIterator(PSequence sequence) {
            super(sequence);
            this.index = sequence.len() - 1;
        }

        @Override
        public Object __next__() throws StopIterationException {
            if (index >= 0) {
                return sequence.getItem(index--);
            }

            throw StopIterationException.INSTANCE;
        }

    }
}
