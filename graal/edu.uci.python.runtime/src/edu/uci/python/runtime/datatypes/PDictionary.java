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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.truffle.api.CompilerDirectives.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.builtins.*;
import edu.uci.python.runtime.function.*;

public class PDictionary extends PythonBuiltinObject {

    @CompilationFinal private static PythonBuiltinClass __class__;

    private final Map<Object, Object> map;

    public PDictionary() {
        map = new ConcurrentHashMap<>();
    }

    public PDictionary(Map<Object, Object> map) {
        this();
        this.map.putAll(map);
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

    public boolean hasKey(Object[] args) {
        if (args.length == 1) {
            return this.map.containsKey(args[0]);
        } else {
            throw new RuntimeException("invalid arguments for has_key()");
        }
    }

    @Override
    public PythonBuiltinClass __class__(PythonContext context) {
        if (__class__ == null) {
            __class__ = context.getPythonBuiltinsLookup().lookupType(PDictionary.class);
        }

        return __class__;
    }

    @Override
    public PythonCallable __getattribute__(String name, PythonContext context) {
        if (__class__ == null) {
            __class__ = context.getPythonBuiltinsLookup().lookupType(PDictionary.class);
        }

        return (PythonCallable) __class__.getAttribute(name);
    }

    @Override
    public int __len__() {
        return map.size();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{");
        int length = map.size();
        int i = 0;

        for (Object key : map.keySet()) {
            buf.append(key.toString() + " : " + map.get(key));

            if (i < length - 1) {
                buf.append(", ");
            }

            i++;
        }

        buf.append("}");
        return buf.toString();
    }
}
