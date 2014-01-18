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
package edu.uci.python.nodes.loop;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.generator.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.exception.*;
import edu.uci.python.runtime.function.*;

public final class BreakNode extends StatementNode {

    @Override
    public Object execute(VirtualFrame frame) {
        throw BreakException.INSTANCE;
    }

    public static final class GeneratorBreakNode extends StatementNode {

        private final int iteratorSlot;
        private final int[] indexSlots;

        public GeneratorBreakNode(int iteratorSlot, int[] indexSlots) {
            this.iteratorSlot = iteratorSlot;
            this.indexSlots = indexSlots;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            PArguments.getGeneratorArguments(frame).setIteratorAt(iteratorSlot, null);

            for (int indexSlot : indexSlots) {
                GeneratorBlockNode.setIndex(frame, indexSlot, 0);
            }

            throw BreakException.INSTANCE;
        }
    }

}
