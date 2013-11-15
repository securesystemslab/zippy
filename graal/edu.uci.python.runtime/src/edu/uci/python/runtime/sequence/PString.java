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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.*;

public class PString extends PImmutableSequence implements Iterable<Object> {

    private final String value;

    public PString(String value) {
        this.value = value;
    }

    @Override
    public PythonCallable findAttribute(String name) {
        return (PythonCallable) PythonModulesContainer.stringModule.getAttribute(name);
    }

    public List<String> getList() {
        ArrayList<String> list = new ArrayList<>();

        char[] array = value.toCharArray();
        for (int i = 0; i < array.length; i++) {
            list.add(String.valueOf(array[i]));
        }

        return list;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {

            private final Iterator<String> iter = getList().iterator();

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

    public String getValue() {
        return value;
    }

    @Override
    public int len() {
        return value.length();
    }

    @Override
    public Object getItem(int idx) {
        return value.charAt(idx);
    }

    @Override
    public Object getSlice(int start, int stop, int step, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSlice(PSlice slice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] getSequence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lessThan(PSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PSequence concat(PSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return value;
    }
}
