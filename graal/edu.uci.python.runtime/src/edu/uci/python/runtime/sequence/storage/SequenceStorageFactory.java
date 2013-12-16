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
package edu.uci.python.runtime.sequence.storage;

import edu.uci.python.runtime.*;

public class SequenceStorageFactory {

    public static SequenceStorage createStorage(Object[] values) {
        if (!PythonOptions.UnboxSequenceStorage) {
            if (values == null) {
                return new ObjectSequenceStorage();
            } else {
                return new ObjectSequenceStorage(values);
            }
        }

        /**
         * Try to use unboxed SequenceStorage.
         */
        if (values == null || values.length == 0) {
            return EmptySequenceStorage.INSTANCE;
        }

        if (canSpecializeToInt(values)) {
            return new IntSequenceStorage(specializeToInt(values));
        } else if (canSpecializeToDouble(values)) {
            return new DoubleSequenceStorage(specializeToDouble(values));
        } else {
            return new ObjectSequenceStorage(values);
        }
    }

    protected static boolean canSpecializeToInt(Object[] values) {
        if (!(values[0] instanceof Integer)) {
            return false;
        }

        for (Object item : values) {
            if (!(item instanceof Integer)) {
                return false;
            }
        }

        return true;
    }

    protected static int[] specializeToInt(Object[] values) {
        final int[] intVals = new int[values.length];

        for (int i = 0; i < values.length; i++) {
            intVals[i] = (int) values[i];
        }

        return intVals;
    }

    protected static boolean canSpecializeToDouble(Object[] values) {
        if (!(values[0] instanceof Double)) {
            return false;
        }

        for (Object item : values) {
            if (!(item instanceof Double)) {
                return false;
            }
        }

        return true;
    }

    protected static double[] specializeToDouble(Object[] values) {
        final double[] doubles = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            doubles[i] = (double) values[i];
        }

        return doubles;
    }

}
