/*
 * Copyright (c) 2014, Regents of the University of California
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
package edu.uci.python.runtime.datatype;

import java.util.*;
import java.util.Map.Entry;

import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;

public class PDictView {

    private final PDict dict;

    public PDictView(PDict dict) {
        this.dict = dict;
    }

    protected final PDict getDict() {
        return dict;
    }

    public static final class PDictViewItems extends PDictView implements PIterable {

        private final int size;

        public PDictViewItems(PDict dict) {
            super(dict);
            this.size = dict.len();
        }

        public int len() {
            return size;
        }

        public Object getMax() {
            throw new UnsupportedOperationException();
        }

        public Object getMin() {
            throw new UnsupportedOperationException();
        }

        public PIterator __iter__() {
            return new PDictViewItemsIterator(getDict());
        }
    }

    public static final class PDictViewItemsIterator implements PIterator {

        private final Iterator<Entry<Object, Object>> iterator;
        @SuppressWarnings("unused") private final int size;

        public PDictViewItemsIterator(PDict dict) {
            iterator = dict.getMap().entrySet().iterator();
            size = dict.len();
        }

        @Override
        public Object __next__() throws StopIterationException {
            if (iterator.hasNext()) {
                Entry<Object, Object> entry = iterator.next();
                return new PTuple(new Object[]{entry.getKey(), entry.getValue()});
            }

            throw StopIterationException.INSTANCE;
        }
    }

}
