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
package edu.uci.python.nodes.expressions;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.runtime.datatypes.*;

@NodeChildren({@NodeChild(value = "primary", type = PNode.class), @NodeChild(value = "slice", type = PNode.class), @NodeChild(value = "right", type = PNode.class)})
public abstract class SubscriptStoreNode extends StatementNode implements Amendable {

    @Override
    public StatementNode updateRhs(PNode newRhs) {
        return SubscriptStoreNodeFactory.create(getPrimary(), getSlice(), newRhs);
    }

    public abstract PNode getPrimary();

    public abstract PNode getSlice();

    public abstract PNode getRight();

    /*
     * As a right hand side expression
     */
    @Specialization(order = 0)
    public Object doPDictionary(PDictionary primary, Object slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 1)
    public Object doPSequence(PSequence primary, int slice, Object value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 2)
    public Object doPSequence(PSequence primary, PSlice slice, PSequence value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 3)
    public Object doPIntegerArray(PIntegerArray primary, int slice, int value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 4)
    public Object doPIntegerArray(PIntegerArray primary, PSlice slice, PIntegerArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 5)
    public Object doPDoubleArray(PDoubleArray primary, int slice, double value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 6)
    public Object doPDoubleArray(PDoubleArray primary, PSlice slice, PDoubleArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Specialization(order = 7)
    public Object doPCharArray(PCharArray primary, int slice, char value) {
        primary.setItem(slice, value);
        return null;
    }

    @Specialization(order = 8)
    public Object doPCharArray(PCharArray primary, PSlice slice, PCharArray value) {
        primary.setSlice(slice, value);
        return null;
    }

    @Generic
    public Object doGeneric(Object primary, Object slice, Object value) {
        if (primary instanceof PSequence) {
            PSequence prim = (PSequence) primary;
            if (slice instanceof Integer) {
                prim.setItem((int) slice, value);
            } else if (slice instanceof PSlice) {
                prim.setSlice((PSlice) slice, (PSequence) value);
            }
        } else if (primary instanceof PDictionary) {
            PDictionary prim = (PDictionary) primary;
            prim.setItem(slice, value);
        } else if (primary instanceof PArray) {
            PArray prim = (PArray) primary;
            prim.setItem((int) slice, value);
        } else {
            throw new RuntimeException("Unsupported Type!");
        }

        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " = " + getPrimary() + "[" + getSlice() + "]";
    }

}
