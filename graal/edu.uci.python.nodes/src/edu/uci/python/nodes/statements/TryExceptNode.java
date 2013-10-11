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
package edu.uci.python.nodes.statements;

import org.python.core.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.datatypes.*;

public class TryExceptNode extends StatementNode {

    @Child protected BlockNode body;

    @Child protected BlockNode orelse;

    @Child protected ExceptNode[] excepts;

    protected TryExceptNode(BlockNode body, BlockNode orelse, ExceptNode[] excepts) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
        this.excepts = adoptChildren(excepts);
    }

    public static TryExceptNode create(BlockNode body, BlockNode orelse, ExceptNode[] excepts) {
        return new TryOnlyNode(body, orelse, excepts);
    }

    @SuppressWarnings("unused")
    protected Object executeExcept(VirtualFrame frame, RuntimeException ex) {
        throw new UnsupportedOperationException("cannot execute executeExcept on TryOnlyNode");
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return null;
    }
}

class TryOnlyNode extends TryExceptNode {

    protected TryOnlyNode(BlockNode body, BlockNode orelse, ExceptNode[] excepts) {
        super(body, orelse, excepts);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            body.execute(frame);
            return orelse.execute(frame);
        } catch (RuntimeException ex) {
            return this.replace(new GenericTryExceptNode(body, orelse, excepts)).executeExcept(frame, ex);
        }
    }
}

class GenericTryExceptNode extends TryExceptNode {

    protected GenericTryExceptNode(BlockNode body, BlockNode orelse, ExceptNode[] excepts) {
        super(body, orelse, excepts);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            body.execute(frame);
            return orelse.execute(frame);
        } catch (RuntimeException ex) {
            return executeExcept(frame, ex);
        }
    }

    private Object findExcept(VirtualFrame frame, String catchedExcept) {
        for (ExceptNode except : excepts) {
            Object type = except.getExceptType().execute(frame);
            if (type instanceof PyType) {
                if (catchedExcept.compareTo(((PyType) type).getName().toString()) == 0) {
                    return except.execute(frame);
                }
            } else {
                throw new PException(type.getClass() + ": is unknown exception type!");
            }
        }
        throw new PException(catchedExcept);
    }

    @Override
    protected Object executeExcept(VirtualFrame frame, RuntimeException excep) {
// String exception = TranslationUtil.isCompatibleException(excep);
// if (exception != null) {
// return findExcept(frame, exception);
// } else {
// throw new UnsupportedOperationException("Cannot execute this exception! " + excep.getClass());
// }
        return null;
    }
}
