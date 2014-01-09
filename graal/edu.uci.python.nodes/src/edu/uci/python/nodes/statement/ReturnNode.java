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
package edu.uci.python.nodes.statement;

import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.exception.*;

public class ReturnNode extends StatementNode {

    private static final ImplicitReturnException IMPLICIT_RETURN = new ImplicitReturnException();

    @Override
    public Object execute(VirtualFrame frame) {
        throw IMPLICIT_RETURN;
    }

    public static class ExplicitReturnNode extends ReturnNode {

        @Child protected PNode right;

        public ExplicitReturnNode(PNode right) {
            this.right = adoptChild(right);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object returnValue = right.execute(frame);
            throw new ExplicitReturnException(returnValue);
        }
    }

    public static final class FrameReturnNode extends ExplicitReturnNode {

        private static final ExplicitReturnException RETURN_EXCEPTION = new ExplicitReturnException(null);

        public FrameReturnNode(PNode right) {
            super(right);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            right.execute(frame);
            throw RETURN_EXCEPTION;
        }
    }
}
