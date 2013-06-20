package org.python.core.truffle;

import java.util.ArrayList;

import org.python.antlr.ast.List;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Tuple;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import static org.python.core.truffle.ASTInterpreter.*;

public class VirtualFrameUtil {

    public static void setName(VirtualFrame frame, Name name, Object value) {
        FrameSlot slot = name.getSlot();

        if (debug) {
            System.out.println("SET " + frame + " slot " + slot + " name " + (slot == null ? "" : slot.getIdentifier()));
        }

        if (slot != null) {
            frame.setObject(slot, value);
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
