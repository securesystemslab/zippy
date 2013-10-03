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

import java.util.*;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.PackedFrame;

public class PFunction extends PCallable {

    private final List<String> parameters;

    private final CallTarget callTarget;

    public PFunction(String name, List<String> parameters, CallTarget callTarget) {
        super(name);
        this.parameters = parameters;
        this.callTarget = callTarget;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args) {
        return callTarget.call(caller, new PArguments(args));
    }

    @Override
    public Object call(PackedFrame caller, Object[] arguments, Object[] keywords) {
        Object[] combined = new Object[parameters.size()];
        assert combined.length >= arguments.length : "Parameters size does not match for call " + callTarget;
        System.arraycopy(arguments, 0, combined, 0, arguments.length);

        // TODO: get rid of cast.
        for (int i = 0; i < keywords.length; i++) {
            PKeyword keyarg = (PKeyword) keywords[i];
            int keywordIdx = parameters.indexOf(keyarg.getName());
            combined[keywordIdx] = keyarg.getValue();
        }

        return callTarget.call(caller, new PArguments(combined));
    }

    /*
     * Specialized
     */
    @Override
    public Object call(PackedFrame caller, Object arg) {
        return callTarget.call(caller, new PArguments(new Object[]{arg}));
    }

    @Override
    public Object call(PackedFrame caller, Object arg0, Object arg1) {
        return callTarget.call(caller, new PArguments(new Object[]{arg0, arg1}));
    }

}
