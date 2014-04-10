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
package edu.uci.python.nodes.attribute;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.standardtype.*;

public abstract class PrimaryCheckUnboxedNode extends Node {

    public abstract boolean accept(PythonBuiltinObject primaryObj);

    public static PrimaryCheckUnboxedNode create(Object primaryObj) {
        if (primaryObj instanceof PythonBuiltinObject) {
            return new BuiltinObjectCheckNode((PythonBuiltinObject) primaryObj);
        } else {
            return new PrimitiveCheckNode(primaryObj);
        }
    }

    /**
     * Checks if the unboxed primary object is still of the same type as the cached one.
     * <p>
     * The purpose of {@link PrimaryCheckUnboxedNode} is to avoid boxing Java primitive types like
     * int, boolean, double or String.
     *
     */
    public static final class PrimitiveCheckNode extends PrimaryCheckUnboxedNode {

        private final Class cachedClass;

        public PrimitiveCheckNode(Object primaryObj) {
            this.cachedClass = primaryObj.getClass();
        }

        @Override
        public boolean accept(PythonBuiltinObject primaryObj) {
            return primaryObj.getClass() == cachedClass;
        }
    }

    public static final class BuiltinObjectCheckNode extends PrimaryCheckUnboxedNode {

        private final Class cachedClass;

        public BuiltinObjectCheckNode(PythonBuiltinObject primaryObj) {
            this.cachedClass = primaryObj.getClass();
        }

        @Override
        public boolean accept(PythonBuiltinObject primaryObj) {
            return cachedClass == primaryObj.getClass();
        }
    }

}
