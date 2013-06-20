package org.python.modules.truffle;

import org.python.ast.datatypes.*;
import org.python.modules.truffle.annotations.ModuleMethod;

public class ArrayModule extends Module {

    public ArrayModule() {
        try {
            addBuiltInMethods();
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @ModuleMethod
    public PArray array(Object[] args, Object[] keywords) {
        if (args.length == 1) {
            return makeEmptyArray(((String) args[0]).toCharArray()[0]);
        } else if (args.length == 2) {
            return makeArray(((String) args[0]).toCharArray()[0], args[1]); 
        } else {
            throw new RuntimeException("wrong number of arguments for array() ");
        }
    }
    
    public PArray array(Object arg) {
        return makeEmptyArray(((String) arg).toCharArray()[0]);
    }
    
    public PArray array(Object arg0, Object arg1) {
        return makeArray(((String) arg0).toCharArray()[0], arg1); 
    }
    
    private PArray makeEmptyArray(char type) {
        switch (type) {
            case 'c': return new PCharArray();
            case 'i': return new PIntegerArray();
            case 'd': return new PDoubleArray();
            default : return null;
        }
    }
    
    private PArray makeArray(char type, Object initializer) {
        Object[] copyArray;
        switch (type) {
            case 'c': 
                if (initializer instanceof String) {
                    return new PCharArray(((String) initializer).toCharArray());
                } else {
                    throw new RuntimeException("Unexpected argument type for array() ");
                }
            case 'i': 
                copyArray = ((PSequence) initializer).getSequence();
                int[] intArray = new int[copyArray.length];
                for (int i = 0; i < intArray.length; i++) {
                    if (copyArray[i] instanceof Integer) {
                        intArray[i] = (int) copyArray[i];
                    } else {
                        throw new RuntimeException("Unexpected argument type for array() ");
                    }
                }
                return new PIntegerArray(intArray);
            case 'd': 
                copyArray = ((PSequence) initializer).getSequence();
                double[] doubleArray = new double[copyArray.length];
                for (int i = 0; i < doubleArray.length; i++) {
                    if (copyArray[i] instanceof Integer) {
                        doubleArray[i] = (int) copyArray[i];
                    } else if (copyArray[i] instanceof Double) {
                        doubleArray[i] = (double) copyArray[i];
                    } else {
                        throw new RuntimeException("Unexpected argument type for array() ");
                    }
                }
                return new PDoubleArray(doubleArray);
            default : return null;
        }
    }

}
