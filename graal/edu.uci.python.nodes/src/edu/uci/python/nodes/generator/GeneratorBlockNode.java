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

import java.util.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.runtime.function.*;

public final class GeneratorBlockNode extends BlockNode implements GeneratorControlNode {

    private final int indexSlot;

    public GeneratorBlockNode(PNode[] statements, int indexSlot) {
        super(statements);
        this.indexSlot = indexSlot;
    }

    public int getIndexSlot() {
        return indexSlot;
    }

    public static int getIndex(VirtualFrame frame, int blockIndexSlot) {
        return PArguments.getControlData(frame).getBlockIndexAt(blockIndexSlot);
    }

    public static void setIndex(VirtualFrame frame, int blockIndexSlot, int value) {
        PArguments.getControlData(frame).setBlockIndexAt(blockIndexSlot, value);
    }

    public GeneratorBlockNode insertNodesBefore(PNode insertBefore, List<PNode> insertees) {
        int insertAt = -1;
        for (int i = 0; i < statements.length; i++) {
            PNode stmt = statements[i];

            if (stmt.equals(insertBefore)) {
                insertAt = i;
            }
        }

        assert insertAt != -1;
        PNode[] extendedStatements = new PNode[statements.length + insertees.size()];
        System.arraycopy(statements, 0, extendedStatements, 0, insertAt);

        for (int i = 0; i < insertees.size(); i++) {
            extendedStatements[i + insertAt] = insertees.get(i);
        }

        for (int i = insertAt; i < statements.length; i++) {
            extendedStatements[i + insertees.size()] = statements[i];
        }

        return new GeneratorBlockNode(extendedStatements, getIndexSlot());
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        Object result = null;

        for (int i = 0; i < statements.length; i++) {
            final int currentIndex = getIndex(frame, indexSlot);

            if (i < currentIndex) {
                continue;
            }

            result = statements[i].execute(frame);
            setIndex(frame, indexSlot, currentIndex + 1);
        }

        setIndex(frame, indexSlot, 0);
        return result;
    }

}
