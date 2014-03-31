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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.exception.*;

public class YieldNode extends StatementNode {

    @Child protected PNode right;

    public YieldNode(PNode right) {
        this.right = adoptChild(right);
    }

    public PNode getRhs() {
        return right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        right.execute(frame);
        throw YieldException.INSTANCE;
    }

    /**
     * Of course yield is for generators. The point of this node is to properly advance the index
     * flag of the parent block node (if the yield's parent is one).
     */
    public static final class GeneratorYieldNode extends YieldNode {

        private final int parentBlockNodeIndexSlot;

        public GeneratorYieldNode(PNode right, int indexSlot) {
            super(right);
            parentBlockNodeIndexSlot = indexSlot;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            right.execute(frame);
            final int index = GeneratorBlockNode.getIndex(frame, parentBlockNodeIndexSlot);
            GeneratorBlockNode.setIndex(frame, parentBlockNodeIndexSlot, index + 1);
            throw YieldException.INSTANCE;
        }
    }

}
