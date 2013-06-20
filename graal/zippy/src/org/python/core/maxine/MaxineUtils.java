package org.python.core.maxine;

import static com.sun.max.vm.intrinsics.MaxineIntrinsicIDs.*;

import org.python.core.PyObject;

import com.sun.max.annotate.INTRINSIC;
import com.sun.max.unsafe.*;

public class MaxineUtils {

    @INTRINSIC(UNSAFE_CAST)
    public static native PyObject toPyObject(Object object);

    @INTRINSIC(PUSH)
    public static native void push(PyObject operand);

    @INTRINSIC(POOP)
    public static native PyObject pop();

    @INTRINSIC(STACKSIZE)
    public static native int stackSize();

    /**
     * (TOP)
     * +--------------------+
     * | RSP -> 0 | operand |
     * +--------------------+
     * |        1 | operand |
     * +--------------------+
     * |        2 | operand |
     * +--------------------+
     * |        3 | operand |
     * +--------------------+
     * |     gap  |prologue |
     * +--------------------+
     * | RBP ->   |interpret|
     * +--------------------+
     * (BOTTOM)
     */
    @INTRINSIC(STACKAT)
    public static native PyObject stackAt(Address stackTop, int slot);

    @INTRINSIC(STACKTOP)
    public static native Address stackTop();
}
