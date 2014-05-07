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

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.iterator.*;

public final class PSet extends PBaseSet {

    private static final PythonBuiltinClass __class__ = PythonContext.getBuiltinTypeFor(PSet.class);

    public PSet() {
        super();
    }

    public PSet(Set<Object> elements) {
        super(elements);
    }

    public PSet(PIterator iterator) {
        super(iterator);
    }

    public PSet(PBaseSet pBaseSet) {
        super(pBaseSet);
    }

    @Override
    public PythonBuiltinClass __class__() {
        return __class__;
    }

    // update
    @Override
    public void update(PBaseSet other) {
        this.updateInternal(other);
    }

    @Override
    public void update(PIterator other) {
        this.updateInternal(other);
    }

    @Override
    protected PBaseSet cloneThisSet() {
        return new PSet(this);
    }

    // add
    public boolean add(Object o) {
        return set.add(o);
    }

    // remove
    @SuppressWarnings({"unused", "static-method"})
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    // discard
    @SuppressWarnings({"unused", "static-method"})
    public boolean discard(Object o) {
        throw new UnsupportedOperationException();
    }

    // pop
    @SuppressWarnings("static-method")
    public boolean pop() {
        throw new UnsupportedOperationException();
    }

    // clear
    public void clear() {
        set.clear();
    }

}
