/**
 *
 */
package edu.uci.python.nodes.interop;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;

import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.datatype.PNone;

/**
 * @author Yeoul Na
 *
 */
public abstract class PForeignToPTypeNode extends Node {

    public abstract Object executeConvert(VirtualFrame frame, Object value);

    @Specialization
    protected static Object fromString(String value) {
        return value;
    }

    @Specialization
    protected static Object fromBoolean(boolean value) {
        return value;
    }

    @Specialization
    protected static Object fromChar(char value) {
        return String.valueOf(value);
    }

    @Specialization(guards = "isBoxedPrimitive(frame, value)")
    public Object unbox(VirtualFrame frame, TruffleObject value) {
        Object unboxed = doUnbox(frame, value);
        return fromObject(unboxed);
    }

    @Specialization(guards = "!isBoxedPrimitive(frame, value)")
    public Object fromTruffleObject(@SuppressWarnings("unused") VirtualFrame frame, TruffleObject value) {
        return value;
    }

    @Specialization
    protected static Object fromObject(Object value) {
        if (value instanceof Long || value instanceof BigInteger || value instanceof String) {
            return value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof TruffleObject) {
            return value;
        } else if (value instanceof PythonContext) {
            return value;
        }
        CompilerDirectives.transferToInterpreter();
        throw new IllegalStateException(value + " is not a Truffle value");
    }

    @Child private Node isBoxed;

    protected final boolean isBoxedPrimitive(@SuppressWarnings("unused") VirtualFrame frame, TruffleObject object) {
        if (isBoxed == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isBoxed = insert(Message.IS_BOXED.createNode());
        }
        return ForeignAccess.sendIsBoxed(isBoxed, object);
    }

    @Child private Node unbox;

    protected final Object doUnbox(@SuppressWarnings("unused") VirtualFrame frame, TruffleObject value) {
        if (unbox == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            unbox = insert(Message.UNBOX.createNode());
        }
        try {
            return ForeignAccess.sendUnbox(unbox, value);
        } catch (UnsupportedMessageException e) {
            return PNone.NONE;
        }
    }

}
