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
package edu.uci.python.runtime.modules;


import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;

public class ArrayModule extends PythonModule {

    public ArrayModule() {
        addBuiltInMethods();
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
            case 'c':
                return new PCharArray();
            case 'i':
                return new PIntegerArray();
            case 'd':
                return new PDoubleArray();
            default:
                return null;
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
            default:
                return null;
        }
    }

}
