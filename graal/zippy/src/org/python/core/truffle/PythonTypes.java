package org.python.core.truffle;

import java.math.BigInteger;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.expressions.*;

import com.oracle.truffle.api.codegen.TypeCast;
import com.oracle.truffle.api.codegen.TypeCheck;
import com.oracle.truffle.api.codegen.TypeSystem;

@TypeSystem({
        int.class, BigInteger.class, double.class, PComplex.class, char.class, String.class,
        PDictionary.class, PList.class, PTuple.class, PSequence.class,
        PSet.class, PFrozenSet.class, PBaseSet.class,
        PIntegerArray.class, PDoubleArray.class, PCharArray.class, PArray.class,
        PSlice.class, PObject.class, boolean.class, PCallable.class })
public class PythonTypes {

    /**
     * The following type checking and casting methods are copied from Truffle's
     * Simple Language SLTypes.java
     */
    @TypeCheck
    public boolean isInteger(Object value) {
        return value instanceof Integer;
    }

    @TypeCast
    public int asInteger(Object value) {
        assert isInteger(value);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            int result = ((BigInteger) value).intValue();
            assert BigInteger.valueOf(result).equals(value) : "Loosing precision";
            return result;
        }
    }

    @TypeCheck
    public boolean isBigInteger(Object value) {
        return value instanceof Integer || value instanceof BigInteger;
    }

    @TypeCast
    public BigInteger asBigInteger(Object value) {
        if (value instanceof Integer) {
            return BigInteger.valueOf((int) value);
        } else {
            return (BigInteger) value;
        }
    }

    @TypeCast
    public BigInteger asBigInteger(int value) {
        return BigInteger.valueOf(value);
    }

    @TypeCheck
    public boolean isBigInteger(int value) {
        return true;
    }

    /**
     * Our own stuff
     */
    @TypeCheck
    public boolean isDouble(int value) {
        return true;
    }

    @TypeCast
    public double asDouble(int value) {
        return (double) value;
    }

    @TypeCheck
    public boolean isDouble(Object value) {
        return value instanceof Integer || value instanceof BigInteger || value instanceof Double;
    }

    @TypeCast
    public double asDouble(Object value) {
        if (value instanceof Integer) {
            Integer integer = (Integer) value;
            return integer.doubleValue();
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            return bigInteger.doubleValue();
        } else if (value instanceof Double) {
            return (double) value;
        }

        assert false : "should not be reached!";
        return -1;
    }

    @TypeCheck
    public boolean isCharacter(Object value) {
        return value instanceof Character;
    }

    @TypeCast
    public char asCharacter(Object value) {
        assert isCharacter(value);
        if (value instanceof Character) {
            return (char) value;
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPComplex(int value) {
        return true;
    }

    @TypeCast
    public PComplex asPComplex(int value) {
        return new PComplex(value, 0);
    }

    @TypeCheck
    public boolean isPComplex(double value) {
        return true;
    }

    @TypeCast
    public PComplex asPComplex(double value) {
        return new PComplex(value, 0);
    }

    @TypeCheck
    public boolean isPComplex(Object value) {
        return value instanceof Integer || value instanceof BigInteger || value instanceof Double || value instanceof PComplex;
    }

    @TypeCast
    public PComplex asPComplex(Object value) {
        if (value instanceof Integer) {
            PComplex complex = new PComplex((Integer) value, 0);
            return complex;
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            PComplex complex = new PComplex(bigInteger.doubleValue(), 0);
            return complex;
        } else if (value instanceof Double) {
            PComplex complex = new PComplex((Double) value, 0);
            return complex;
        } else if (value instanceof PComplex) {
            PComplex complex = (PComplex) value;
            return complex;
        }

        throw new RuntimeException("unexpected type" + value.getClass().getSimpleName());
    }

    @TypeCast
    public String asString(Object value) {
        if (value instanceof Integer) {
            return Integer.toString((int) value);
        } else if (value instanceof BigInteger) {
            return new String(((BigInteger) value).toByteArray());
        } else if (value instanceof Double) {
            return Double.toString((double) value);
        } else if (value instanceof Boolean) {
            boolean boolValue = (boolean) value;
            return (boolValue) ? "1" : "0";
        } else if (value instanceof String) {
            return (String) value;
        }

        throw new RuntimeException("unexpected type" + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isBoolean(int value) {
        return true;
    }

    @TypeCheck
    public boolean isBoolean(BigInteger value) {
        return true;
    }

    @TypeCheck
    public boolean isBoolean(double value) {
        return true;
    }

    @TypeCheck
    public boolean isBoolean(Object value) {
        return value instanceof Integer || value instanceof BigInteger || value instanceof Double || value instanceof Boolean;
    }

    @TypeCast
    public boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (boolean) value;
        } else if (value instanceof Integer) {
            Integer integer = (Integer) value;
            return ArithmeticUtil.isNotZero(integer);
        } else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            return ArithmeticUtil.isNotZero(bigInteger);
        } else if (value instanceof Double) {
            Double doubleValue = (Double) value;
            return ArithmeticUtil.isNotZero(doubleValue);
        }

        throw new RuntimeException("unexpected type" + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPDictionary(Object value) {
        return value instanceof PDictionary;// || value instanceof Integer;
    }

    @TypeCast
    public PDictionary asPDictionary(Object value) {
        if (value instanceof PDictionary) {
            return (PDictionary) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PDictionary();
            }
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPList(Object value) {
        return value instanceof PList;// || value instanceof Integer;
    }

    @TypeCast
    public PList asPList(Object value) {
        if (value instanceof PList) {
            return (PList) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PList();
            }
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPIntegerArray(Object value) {
        return value instanceof PIntegerArray;
    }

    @TypeCast
    public PIntegerArray asPIntegerArray(Object value) {
        if (value instanceof PIntegerArray) {
            return (PIntegerArray) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PIntegerArray();
            }
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPDoubleArray(Object value) {
        return value instanceof PDoubleArray;
    }

    @TypeCast
    public PDoubleArray asPDoubleArray(Object value) {
        if (value instanceof PDoubleArray) {
            return (PDoubleArray) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PDoubleArray();
            }
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPCharArray(Object value) {
        return value instanceof PCharArray;
    }

    @TypeCast
    public PCharArray asPCharArray(Object value) {
        if (value instanceof PCharArray) {
            return (PCharArray) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PCharArray();
            }
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPTuple(Object value) {
        return value instanceof PTuple;// || value instanceof Integer;
    }

    @TypeCast
    public PTuple asPTuple(Object value) {
        if (value instanceof PTuple) {
            return (PTuple) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PTuple();
            }
        }

        if (value == null) {
            return null;
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPSet(Object value) {
        return value instanceof PSet;
    }

    @TypeCast
    public PSet asPSet(Object value) {
        if (value instanceof PSet) {
            return (PSet) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PSet();
            }
        }

        if (value == null) {
            return null;
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    @TypeCheck
    public boolean isPFrozenSet(Object value) {
        return value instanceof PFrozenSet;
    }

    @TypeCast
    public PFrozenSet asPFrozenSet(Object value) {
        if (value instanceof PFrozenSet) {
            return (PFrozenSet) value;
        } else if (value instanceof Integer) {
            int intVal = (int) value;
            if (intVal == 0) {
                return new PFrozenSet();
            }
        }

        if (value == null) {
            return null;
        }

        throw new RuntimeException("unexpected type " + value.getClass().getSimpleName());
    }

    /*
     * Relaxes types that are lower than Object in the type lattice to be Object
     */
    public static Class<?> getRelaxedTyped(Object value) {
        Class<?> type = value.getClass();
        PythonTypesGen ptg = new PythonTypesGen();

        try {
            value = ptg.convertTo(type, value);
        } catch (IllegalArgumentException iae) {
            return Object.class;
        }

        return value.getClass();
    }

}
