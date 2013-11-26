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
package edu.uci.python.runtime.builtins;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtypes.*;

/**
 * Python built-in immutable class.
 * 
 * @author zwei
 * 
 */
// public class PythonBuiltinClass extends PythonClass implements PythonCallable {
public class PythonBuiltinClass extends PythonClass {

// protected CallTarget callTarget;
// protected Arity arity;

    public PythonBuiltinClass(PythonContext context, PythonClass superClass, String name) {
        super(context, superClass, name);
    }

// public PythonBuiltinClass(PythonContext context, PythonClass superClass, String name, Arity
// arity, CallTarget callTarget) {
// super(context, superClass, name);
// this.arity = arity;
// this.callTarget = callTarget;
// }

    @Override
    public void setAttribute(String name, Object value) {
        throw Py.TypeError("can't set attributes of built-in/extension type '" + name + "'");
    }

    /**
     * Modify attributes in an unsafe way, should only use when initializing.
     * 
     * @param name
     * @param value
     */
    public void setAttributeUnsafe(String name, Object value) {
        super.setAttribute(name, value);
    }

// @Override
// public Object call(PackedFrame caller, Object[] args) {
// // arity.arityCheck(args.length, 0, null);
// return callTarget.call(caller, new PArguments(PNone.NONE, null, args));
// }
//
// @Override
// public Object call(PackedFrame caller, Object[] args, PKeyword[] keywords) {
// // arity.arityCheck(args.length, keywords.length, keywords);
// return callTarget.call(caller, new PArguments(PNone.NONE, null, args, keywords));
// }
}
