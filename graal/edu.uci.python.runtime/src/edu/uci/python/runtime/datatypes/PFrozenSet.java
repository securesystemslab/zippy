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
package edu.uci.python.runtime.datatypes;

public class PFrozenSet extends PBaseSet {

    public PFrozenSet() {
        super();
    }

    public PFrozenSet(Iterable<?> iterable) {
        super(iterable);
    }

    public PFrozenSet(PBaseSet pBaseSet) {
        super(pBaseSet);
    }

    @Override
    public void update(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void symmetricDifferenceUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void symmetricDifferenceUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean discard(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected PBaseSet cloneThisSet() {
        return new PFrozenSet(this);
    }

    @Override
    public PythonBuiltinObject multiply(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PythonCallable findAttribute(String name) {
        throw new UnsupportedOperationException();
    }

}
