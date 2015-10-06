package edu.uci.python.nodes;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.iterator.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public abstract class PGuards {

    /**
     * Specialization guards.
     */
    public static boolean isNone(Object value) {
        return value == PNone.NONE || value == PNone.NONENode;
    }

    public static boolean isEmptyStorage(PList list) {
        return list.getStorage() instanceof EmptySequenceStorage;
    }

    public static boolean is2ndEmptyStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof EmptySequenceStorage;
    }

    public static boolean isBasicStorage(PList list) {
        return list.getStorage() instanceof BasicSequenceStorage;
    }

    public static boolean isIntStorage(PList list) {
        return list.getStorage() instanceof IntSequenceStorage;
    }

    public static boolean is2ndIntStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof IntSequenceStorage;
    }

    public static boolean areBothIntStorage(PList first, PList second) {
        return first.getStorage() instanceof IntSequenceStorage && second.getStorage() instanceof IntSequenceStorage;
    }

    public static boolean isDoubleStorage(PList list) {
        return list.getStorage() instanceof DoubleSequenceStorage;
    }

    public static boolean is2ndDoubleStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof DoubleSequenceStorage;
    }

    public static boolean isListStorage(PList list) {
        return list.getStorage() instanceof ListSequenceStorage;
    }

    public static boolean is2ndListStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof ListSequenceStorage;
    }

    public static boolean isTupleStorage(PList list) {
        return list.getStorage() instanceof TupleSequenceStorage;
    }

    public static boolean is2ndTupleStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof TupleSequenceStorage;
    }

    public static boolean isObjectStorage(PList list) {
        return list.getStorage() instanceof ObjectSequenceStorage;
    }

    public static boolean is2ndObjectStorage(@SuppressWarnings("unused") Object first, PList list) {
        return list.getStorage() instanceof ObjectSequenceStorage;
    }

    public static boolean areBothObjectStorage(PList first, PList second) {
        return first.getStorage() instanceof ObjectSequenceStorage && second.getStorage() instanceof ObjectSequenceStorage;
    }

    public static boolean isObjectStorageIterator(PSequenceIterator iterator) {
        PSequence sequence = iterator.getSeqence();

        if (sequence instanceof PList) {
            PList list = (PList) sequence;
            return list.getStorage() instanceof ObjectSequenceStorage;
        }

        return false;
    }

    public static boolean isNotPythonObject(Object obj) {
        return !(obj instanceof PythonObject);
    }

    public static boolean is2ndNotPythonObject(@SuppressWarnings("unused") Object first, Object second) {
        return !(second instanceof PythonObject);
    }

    public static final boolean isEitherOperandPythonObject(Object left, Object right) {
        return left instanceof PythonObject || right instanceof PythonObject;
    }

    /**
     * Argument guards.
     */
    public static boolean emptyArguments(VirtualFrame frame) {
        return PArguments.getUserArgumentLength(frame) == 0;
    }

    public static boolean emptyArguments(PNone none) {
        if (none == PNone.NONENode)
            return false;
        return true;
    }

    public static boolean emptyArguments(Object arg) {
        return arg instanceof PFrozenSet && ((PFrozenSet) arg).len() == 0;
    }

    public static boolean emptyArgument(PTuple args) {
        return args.len() == 0;
    }

    public static boolean oneArgument(PTuple args) {
        return args.len() == 1;
    }

    @SuppressWarnings("unused")
    public static boolean hasOneArgument(Object arg1, PTuple args, Object keywordArg) {
        return (args.len() == 0 && keywordArg instanceof PNone);
    }

    public static boolean firstArgIsDict(PTuple args) {
        return args.getItem(0) instanceof PDict;
    }

    public static boolean firstArgIsIterable(PTuple args) {
        return args.getItem(0) instanceof PIterable;
    }

    public static boolean firstArgIsIterator(PTuple args) {
        return args.getItem(0) instanceof PIterator;
    }

    @SuppressWarnings("unused")
    public static boolean isForJSON(Object obj, String id, Object defaultValue) {
        return id.equals("for_json");
    }

    public static boolean is2ndNotTuple(@SuppressWarnings("unused") Object first, Object second) {
        return !(second instanceof PTuple);
    }

    @SuppressWarnings("unused")
    public static boolean isIndexPositive(Object primary, int idx) {
        return idx >= 0;
    }

    @SuppressWarnings("unused")
    public static boolean noInitializer(String typeCode, Object initializer) {
        return (initializer instanceof PNone);
    }

    @SuppressWarnings("unused")
    public static boolean isIndexNegative(Object primary, int idx) {
        return idx < 0;
    }
}
