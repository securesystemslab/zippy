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
package edu.uci.python.runtime.sequence;

import org.python.core.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.iterator.*;

/**
 * @author Gulfem
 */

public class PZip implements PIterable {

    private final PIterable[] iterables;

    public PZip(PIterable[] iterables) {
        this.iterables = iterables;
    }

    @Override
    public PIterator __iter__() {

        PIterator[] iterators = new PIterator[iterables.length];
        for (int i = 0; i < iterables.length; i++) {
            iterators[i] = iterables[i].__iter__();
        }

        return new PZipIterator(iterators);
    }

    @Override
    public int len() {
        throw Py.AttributeError("'zip'" + " object has no attribute " + "'len'");

    }

    @Override
    public Object getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "<zip object at " + hashCode() + ">";
    }

}
