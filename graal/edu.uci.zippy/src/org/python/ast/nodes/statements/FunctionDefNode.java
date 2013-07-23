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
package org.python.ast.nodes.statements;

import org.python.ast.datatypes.PFunction;
import org.python.ast.nodes.FunctionRootNode;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.*;

public class FunctionDefNode extends StatementNode {

    private final FrameSlot slot;

    private final String name;

    @Child private final ParametersNode parameters;

    private final CallTarget callTarget;

    @Child private final RootNode funcRoot;

    public FunctionDefNode(FrameSlot slot, String name, ParametersNode parameters, CallTarget callTarget, RootNode funcRoot) {
        this.slot = slot;
        this.name = name;
        this.parameters = adoptChild(parameters);
        this.callTarget = callTarget;
        this.funcRoot = adoptChild(funcRoot);
    }

    public String getName() {
        return name;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        parameters.evaluateDefaults(frame);
        PFunction fn = new PFunction(name, parameters, callTarget);

        try {
            frame.setObject(slot, fn);
        } catch (FrameSlotTypeException e) {
            FrameUtil.setObjectSafe(frame, slot, fn);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        parameters.evaluateDefaults(frame);
        PFunction fn = new PFunction(name, parameters, callTarget);

        try {
            frame.setObject(slot, fn);
        } catch (FrameSlotTypeException e) {
            FrameUtil.setObjectSafe(frame, slot, fn);
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")" + funcRoot;
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

        parameters.visualize(level);
        ((FunctionRootNode) funcRoot).visualize(level);
    }

}
