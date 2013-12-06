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

import java.util.*;

import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;

public class PEnumerate extends PIterator implements Iterable<Object> {

    private int index;
    private List<PTuple> list;

    public PEnumerate(Iterable<?> iterable) {
        this(iterable, 0);
    }

    @SuppressWarnings("unused")
    public PEnumerate(Iterable<?> iterable, int start) {
        this.list = new ArrayList<>();
        int count = 0;

        for (Object object : iterable) {
            this.list.add(new PTuple(new Object[]{count, object}));
            count++;
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {

            private final Iterator<PTuple> iter = list.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return iter.next();
            }
        };
    }

    @Override
    public Object __next__() {
        if (index < list.size()) {
            return list.get(index++);
        }

        throw StopIterationException.INSTANCE;
    }

    @Override
    public String toString() {
        return "<enumerate object at " + hashCode() + ">";
    }
}
