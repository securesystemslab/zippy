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
package edu.uci.python.nodes.frame;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class ReadGlobalNode extends PNode implements ReadNode, HasPrimaryNode {

    protected final String attributeId;
    protected final PythonContext context;
    protected final PythonModule globalScope;

    public ReadGlobalNode(PythonContext context, PythonModule globalScope, String attributeId) {
        this.attributeId = attributeId;
        this.context = context;
        this.globalScope = globalScope;
    }

    public static ReadGlobalNode create(PythonContext context, PythonModule globalScope, String attributeId) {
        return new UninitializedReadGlobalNode(context, globalScope, attributeId);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return new SetAttributeNode.UninitializedSetAttributeNode(attributeId, new ObjectLiteralNode(globalScope), rhs);
    }

    @Override
    public PNode extractPrimary() {
        return new ObjectLiteralNode(globalScope);
    }

    public abstract ShapeCheckNode extractShapeCheckNode();

    @Override
    public String getAttributeId() {
        return attributeId;
    }

    public PythonModule extractGlobaScope() {
        return globalScope;
    }

    protected final Object specializeAndExecute(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return replace(new UninitializedReadGlobalNode(context, globalScope, attributeId)).execute(frame);
    }

    public static final class ReadGlobalDirectNode extends ReadGlobalNode {

        @Child protected ShapeCheckNode check;
        @Child protected AttributeReadNode read;

        public ReadGlobalDirectNode(PythonContext context, PythonModule globalScope, String attributeId) {
            super(context, globalScope, attributeId);
            this.check = ShapeCheckNode.create(globalScope, globalScope.getObjectLayout(), 0);
            this.read = AttributeReadNode.create(globalScope.getOwnValidLocation(attributeId));
        }

        @Override
        public ShapeCheckNode extractShapeCheckNode() {
            return NodeUtil.cloneNode(check);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                check.accept(globalScope);
                return read.getValueUnsafe(globalScope);
            } catch (InvalidAssumptionException e) {
                return specializeAndExecute(frame);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                check.accept(globalScope);
                return read.getIntValueUnsafe(globalScope);
            } catch (InvalidAssumptionException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(specializeAndExecute(frame));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                check.accept(globalScope);
                return read.getDoubleValueUnsafe(globalScope);
            } catch (InvalidAssumptionException e) {
                return PythonTypesGen.PYTHONTYPES.expectDouble(specializeAndExecute(frame));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                check.accept(globalScope);
                return read.getBooleanValueUnsafe(globalScope);
            } catch (InvalidAssumptionException e) {
                return PythonTypesGen.PYTHONTYPES.expectBoolean(specializeAndExecute(frame));
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return execute(frame);
        }
    }

    public static final class ReadBuiltinDirectNode extends ReadGlobalNode {

        @Child protected ShapeCheckNode check;
        @Child protected AttributeReadNode read;
        private final PythonModule builtinsModule;

        public ReadBuiltinDirectNode(PythonContext context, PythonModule globalScope, String attributeId) {
            super(context, globalScope, attributeId);
            this.builtinsModule = context.getPythonBuiltinsLookup().lookupModule("__builtins__");
            this.check = ShapeCheckNode.create(globalScope, builtinsModule.getObjectLayout(), 1);
            this.read = AttributeReadNode.create(builtinsModule.getOwnValidLocation(attributeId));
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                check.accept(globalScope);
                return read.getValueUnsafe(builtinsModule);
            } catch (InvalidAssumptionException e) {
                return specializeAndExecute(frame);
            }
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return execute(frame);
        }

        @Override
        public ShapeCheckNode extractShapeCheckNode() {
            return NodeUtil.cloneNode(check);
        }
    }

    public static final class UninitializedReadGlobalNode extends ReadGlobalNode {

        public UninitializedReadGlobalNode(PythonContext context, PythonModule globalScope, String attributeId) {
            super(context, globalScope, attributeId);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            Object value = globalScope.getAttribute(attributeId);

            if (value == PNone.NONE) {
                value = context.getPythonBuiltinsLookup().lookupModule("__builtins__").getAttribute(attributeId);
            } else {
                replace(new ReadGlobalDirectNode(context, globalScope, attributeId));
                return value;
            }

            if (value == PNone.NONE) {
                value = slowPathLookup();
            } else {
                replace(new ReadBuiltinDirectNode(context, globalScope, attributeId));
            }

            return value;
        }

        @Override
        public Object executeWithPrimary(VirtualFrame frame, Object primary) {
            return execute(frame);
        }

        @SlowPath
        protected Object slowPathLookup() {
            Object value = PySystemState.builtins.__finditem__(attributeId);

            if (value == null) {
                throw Py.NameError("name \'" + attributeId + "\' is not defined");
            }

            return value;
        }

        @Override
        public ShapeCheckNode extractShapeCheckNode() {
            throw new UnsupportedOperationException();
        }

    }

}
