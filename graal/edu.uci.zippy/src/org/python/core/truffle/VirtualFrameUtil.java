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
package org.python.core.truffle;

import java.util.ArrayList;

import org.python.antlr.ast.List;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Tuple;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import static org.python.core.truffle.ASTInterpreter.*;

public class VirtualFrameUtil {

    public static void setName(VirtualFrame frame, Name name, Object value) {
        FrameSlot slot = name.getSlot();

        if (debug) {
            System.out.println("SET " + frame + " slot " + slot + " name " + (slot == null ? "" : slot.getIdentifier()));
        }

        if (slot != null) {
            try {
                frame.setObject(slot, value);
            } catch (FrameSlotTypeException e) {
                FrameUtil.setObjectSafe(frame, slot, value);
            }
        } else {
            GlobalScope.getInstance().set(name.getInternalId(), value);
        }
    }

    public static void recursiveUnpackAndAssign(VirtualFrame frame, PyObject val, java.util.List<expr> targetExpressions) {
        ArrayList<PyObject> values = new ArrayList<PyObject>();
        for (PyObject value : val.asIterable()) {
            values.add(value);
        }

        int index = 0;
        for (expr e : targetExpressions) {
            if (e instanceof Name) {
                setName(frame, (Name) e, values.get(index));
            } else if (e instanceof List) {
                List list = (List) e;
                recursiveUnpackAndAssign(frame, values.get(index), list.getInternalElts());
            } else if (e instanceof Tuple) {
                Tuple tuple = (Tuple) e;
                recursiveUnpackAndAssign(frame, values.get(index), tuple.getInternalElts());
            } else if (e instanceof Subscript) {
                ((Subscript) e).doUpdate(frame, (PyObject) values.get(index));
            }
            index++;
        }
    }

}
