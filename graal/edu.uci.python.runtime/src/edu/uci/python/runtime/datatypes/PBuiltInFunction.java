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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

public class PBuiltInFunction extends PCallable {

    private final CallTarget callTarget;

    public PBuiltInFunction(String name, CallTarget callTarget) {
        super(name, true);
        this.callTarget = callTarget;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args) {
        return callTarget.call(caller, new PArguments(args));
    }

    @Override
    public Object call(PackedFrame caller, Object[] args, Object[] keywords) {
        // TODO Auto-generated method stub
        return null;
    }

// @Override
// public Object call(PackedFrame caller, Object[] args, Object[] keywords) {
// return callTarget.call(caller, new PArguments(args, keywords));
// }

// @Override
// public Object call(PackedFrame caller, Object arg) {
// return callTarget.call(caller, new PArguments(new Object[]{arg}));
// }
//
// @Override
// public Object call(PackedFrame caller, Object arg0, Object arg1) {
// return callTarget.call(caller, new PArguments(new Object[]{arg0, arg1}));
// }

}
