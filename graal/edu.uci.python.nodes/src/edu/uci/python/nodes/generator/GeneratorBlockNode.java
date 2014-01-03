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
package edu.uci.python.nodes.generator;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;

public class GeneratorBlockNode extends BlockNode {

    protected int index;
    protected final int indexSlot;

    public GeneratorBlockNode(PNode[] statements, int indexSlot) {
        super(statements);
        this.indexSlot = indexSlot;
    }

    protected final int getIndex(VirtualFrame frame) {
        return PArguments.getGeneratorArguments(frame).getBlockIndexOf(indexSlot);
    }

    protected final void setIndex(VirtualFrame frame, int value) {
        PArguments.getGeneratorArguments(frame).setBlockIndexOf(indexSlot, value);
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        for (int i = 0; i < statements.length; i++) {
            if (i < getIndex(frame)) {
                continue;
            }

            statements[i].executeVoid(frame);
            setIndex(frame, getIndex(frame) + 1);
        }

        setIndex(frame, 0);
        return PNone.NONE;
    }

    public static final class InnerGeneratorBlockNode extends GeneratorBlockNode {

        public InnerGeneratorBlockNode(PNode[] statements, int indexSlot) {
            super(statements, indexSlot);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            for (int i = 0; i < statements.length; i++) {
                if (i < getIndex(frame)) {
                    continue;
                }

                setIndex(frame, getIndex(frame) + 1);
                statements[i].executeVoid(frame);
            }

            setIndex(frame, 0);
            return PNone.NONE;
        }
    }

}
