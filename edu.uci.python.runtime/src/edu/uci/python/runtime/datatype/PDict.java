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
package edu.uci.python.runtime.datatype;

import java.util.*;
import java.util.Map.Entry;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

public final class PDict extends PythonBuiltinObject implements PIterable {

    public static final PythonBuiltinClass __class__ = PythonContext.getBuiltinTypeFor(PDict.class);

    private final Map<Object, Object> map;

    public PDict() {
        map = new TreeMap<>();
    }

    public PDict(Map<Object, Object> map) {
        this();
        addAll(map);
    }

    @TruffleBoundary
    private void addAll(Map<Object, Object> mapToAdd) {
        this.map.putAll(mapToAdd);
    }

    public PDict(PIterator iter) {
        map = new TreeMap<>();

        try {
            while (true) {
                unpackKeyValuePair(iter.__next__());
            }
        } catch (StopIterationException e) {
            // fall through
        }
    }

    private void unpackKeyValuePair(Object obj) {
        if (obj instanceof PSequence && ((PSequence) obj).len() == 2) {
            map.put(((PSequence) obj).getItem(0), ((PSequence) obj).getItem(1));
        } else {
            throw new RuntimeException("invalid args for dict()");
        }
    }

    @Override
    public PythonBuiltinClass __class__() {
        return __class__;
    }

    public Object getItem(Object key) {
        return map.get(key);
    }

    public void setItem(Object key, Object value) {
        map.put(key, value);
    }

    public void delItem(Object key) {
        map.remove(key);
    }

    public Collection<Object> items() {
        return map.values();
    }

    public Collection<Object> keys() {
        return map.keySet();
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    public boolean hasKey(Object key) {
        return this.map.containsKey(key);
    }

    public PIterator __iter__() {
        return new PDictIterator(map.keySet().iterator());
    }

    public PIterator values() {
        return new PDictIterator(map.values().iterator());
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{");
        int length = map.size();
        int i = 0;

        for (Entry<?, ?> entry : map.entrySet()) {
            buf.append(entry.getKey() + ": " + entry.getValue());

            if (i < length - 1) {
                buf.append(", ");
            }

            i++;
        }

        buf.append("}");
        return buf.toString();
    }

    @Override
    public int len() {
        return map.size();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PDict)) {
            return false;
        }

        PDict otherDict = (PDict) other;
        return map.equals(otherDict.getMap());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Object getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMin() {
        throw new UnsupportedOperationException();
    }

}
