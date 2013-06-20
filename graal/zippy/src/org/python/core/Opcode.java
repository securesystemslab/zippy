package org.python.core;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

// derived from CPython 2.5.2 Include/opcode.h

public class Opcode {

    public static final int POP_TOP = 1;
    public static final int ROT_TWO = 2;
    public static final int ROT_THREE = 3;
    public static final int DUP_TOP = 4;
    public static final int ROT_FOUR = 5;
    public static final int NOP = 9;
    public static final int UNARY_POSITIVE = 10;
    public static final int UNARY_NEGATIVE = 11;
    public static final int UNARY_NOT = 12;
    public static final int UNARY_CONVERT = 13;
    public static final int UNARY_INVERT = 15;
    public static final int LIST_APPEND = 18;
    public static final int BINARY_POWER = 19;
    public static final int BINARY_MULTIPLY = 20;
    public static final int BINARY_DIVIDE = 21;
    public static final int BINARY_MODULO = 22;
    public static final int BINARY_ADD = 23;
    public static final int BINARY_SUBTRACT = 24;
    public static final int BINARY_SUBSCR = 25;
    public static final int BINARY_FLOOR_DIVIDE = 26;
    public static final int BINARY_TRUE_DIVIDE = 27;
    public static final int INPLACE_FLOOR_DIVIDE = 28;
    public static final int INPLACE_TRUE_DIVIDE = 29;
    public static final int SLICE = 30;
    /* Also uses 31-33 */
    public static final int STORE_SLICE = 40;
    /* Also uses 41-43 */
    public static final int DELETE_SLICE = 50;
    /* Also uses 51-53 */
    public static final int INPLACE_ADD = 55;
    public static final int INPLACE_SUBTRACT = 56;
    public static final int INPLACE_MULTIPLY = 57;
    public static final int INPLACE_DIVIDE = 58;
    public static final int INPLACE_MODULO = 59;
    public static final int STORE_SUBSCR = 60;
    public static final int DELETE_SUBSCR = 61;
    public static final int BINARY_LSHIFT = 62;
    public static final int BINARY_RSHIFT = 63;
    public static final int BINARY_AND = 64;
    public static final int BINARY_XOR = 65;
    public static final int BINARY_OR = 66;
    public static final int INPLACE_POWER = 67;
    public static final int GET_ITER = 68;
    public static final int PRINT_EXPR = 70;
    public static final int PRINT_ITEM = 71;
    public static final int PRINT_NEWLINE = 72;
    public static final int PRINT_ITEM_TO = 73;
    public static final int PRINT_NEWLINE_TO = 74;
    public static final int INPLACE_LSHIFT = 75;
    public static final int INPLACE_RSHIFT = 76;
    public static final int INPLACE_AND = 77;
    public static final int INPLACE_XOR = 78;
    public static final int INPLACE_OR = 79;
    public static final int BREAK_LOOP = 80;
    public static final int WITH_CLEANUP = 81;
    public static final int LOAD_LOCALS = 82;
    public static final int RETURN_VALUE = 83;
    public static final int IMPORT_STAR = 84;
    public static final int EXEC_STMT = 85;
    public static final int YIELD_VALUE = 86;
    public static final int POP_BLOCK = 87;
    public static final int END_FINALLY = 88;
    public static final int BUILD_CLASS = 89;
    public static final int HAVE_ARGUMENT = 90; /*
                                                 * Opcodes from here have an
                                                 * argument:
                                                 */

    public static final int STORE_NAME = 90; /* Index in name list */

    public static final int DELETE_NAME = 91; /* "" */

    public static final int UNPACK_SEQUENCE = 92; /* Number of sequence items */

    public static final int FOR_ITER = 93;
    public static final int STORE_ATTR = 95; /* Index in name list */

    public static final int DELETE_ATTR = 96; /* "" */

    public static final int STORE_GLOBAL = 97; /* "" */

    public static final int DELETE_GLOBAL = 98; /* "" */

    public static final int DUP_TOPX = 99; /* number of items to duplicate */

    public static final int LOAD_CONST = 100; /* Index in const list */

    public static final int LOAD_NAME = 101; /* Index in name list */

    public static final int BUILD_TUPLE = 102; /* Number of tuple items */

    public static final int BUILD_LIST = 103; /* Number of list items */

    public static final int BUILD_MAP = 104; /* Always zero for now */

    public static final int LOAD_ATTR = 105; /* Index in name list */

    public static final int COMPARE_OP = 106; /* Comparison operator */

    public static final int IMPORT_NAME = 107; /* Index in name list */

    public static final int IMPORT_FROM = 108; /* Index in name list */

    public static final int JUMP_FORWARD = 110; /* Number of bytes to skip */

    public static final int JUMP_IF_FALSE = 111; /* "" */

    public static final int JUMP_IF_TRUE = 112; /* "" */

    public static final int JUMP_ABSOLUTE = 113; /*
                                                  * Target byte offset from
                                                  * beginning of code
                                                  */

    public static final int LOAD_GLOBAL = 116; /* Index in name list */

    public static final int CONTINUE_LOOP = 119; /* Start of loop (absolute) */

    public static final int SETUP_LOOP = 120; /* Target address (absolute) */

    public static final int SETUP_EXCEPT = 121; /* "" */

    public static final int SETUP_FINALLY = 122; /* "" */

    public static final int LOAD_FAST = 124; /* Local variable number */

    public static final int STORE_FAST = 125; /* Local variable number */

    public static final int DELETE_FAST = 126; /* Local variable number */

    public static final int RAISE_VARARGS = 130; /*
                                                  * Number of raise arguments
                                                  * (1, 2 or 3)
                                                  */
    /* CALL_FUNCTION_XXX opcodes defined below depend on this definition */

    public static final int CALL_FUNCTION = 131; /* #args + (#kwargs<<8) */

    public static final int MAKE_FUNCTION = 132; /* #defaults */

    public static final int BUILD_SLICE = 133; /* Number of items */

    public static final int MAKE_CLOSURE = 134; /* #free vars */

    public static final int LOAD_CLOSURE = 135; /*
                                                 * Load free variable from
                                                 * closure
                                                 */

    public static final int LOAD_DEREF = 136; /*
                                               * Load and dereference from
                                               * closure cell
                                               */

    public static final int STORE_DEREF = 137; /* Store into cell */

    /*
     * The next 3 opcodes must be contiguous and satisfy (CALL_FUNCTION_VAR -
     * CALL_FUNCTION) & 3 == 1
     */
    public static final int CALL_FUNCTION_VAR = 140; /* #args + (#kwargs<<8) */

    public static final int CALL_FUNCTION_KW = 141; /* #args + (#kwargs<<8) */

    public static final int CALL_FUNCTION_VAR_KW = 142; /* #args + (#kwargs<<8) */

    /* Support for opargs more than 16 bits long */
    public static final int EXTENDED_ARG = 143;

    /* specialization of compare operations */
    public static final int COMPARE_LT = 144;
    public static final int COMPARE_LE = 145;
    public static final int COMPARE_EQ = 146; /*
                                               * Comparison operator specialized
                                               * to equality
                                               */
    public static final int COMPARE_NE = 147; /*
                                               * Comparison operator specialized
                                               * to inequality
                                               */
    public static final int COMPARE_GT = 148;
    public static final int COMPARE_GE = 149;
    public static final int COMPARE_IN = 150;
    public static final int COMPARE_NOT_IN = 151;
    public static final int COMPARE_IS = 152;
    public static final int COMPARE_IS_NOT = 153;
    public static final int COMPARE_EXC_MATCH = 154;

    // comparison opcodes (on the oparg), just put in this class too
    public static final int PyCmp_LT = 0;
    public static final int PyCmp_LE = 1;
    public static final int PyCmp_EQ = 2;
    public static final int PyCmp_NE = 3;
    public static final int PyCmp_GT = 4;
    public static final int PyCmp_GE = 5;
    public static final int PyCmp_IN = 6;
    public static final int PyCmp_NOT_IN = 7;
    public static final int PyCmp_IS = 8;
    public static final int PyCmp_IS_NOT = 9;
    public static final int PyCmp_EXC_MATCH = 10;

    /*
     * maxine stuff
     */
    public static final int LAST = 154; // PL: added specialized compare
                                        // operations

    public static void countOpcode(int opcode) {
        if (sortedMap.containsKey(opcode)) {
            sortedMap.put(opcode, sortedMap.get(opcode) + 1);
        } else {
            sortedMap.put(opcode, new Long(1));
        }
    }

    public static void printHistogram() {
        System.out
                .println("=== Opcode Histogram ==================================");
        System.out
                .println("Name                             Count            % Total");
        System.out
                .println("----                             -----            -------");

        Set<Map.Entry<Integer, Long>> mapSorted = entriesSortedByValues(sortedMap);
        long total = 0;

        for (Map.Entry<Integer, Long> entry : mapSorted) {
            total += entry.getValue();
        }

        int counter = 0;
        for (Map.Entry<Integer, Long> entry : mapSorted) {
            double perTotal = (float) ((double) entry.getValue() / (double) total);
            String opname = PyBytecode.get_opname()
                    .__getitem__(Py.newInteger(entry.getKey())).toString();
            System.out.printf("%-32s %-10d %10.2f\n", opname, entry.getValue(),
                    perTotal * 100);

            if (counter == 50) {
                break;
            } else {
                counter++;
            }
        }

        System.out
                .println("=========================================================");
    }

    private final static TreeMap<Integer, Long> sortedMap = new TreeMap<Integer, Long>();

    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
            Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return -e1.getValue().compareTo(e2.getValue());
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
