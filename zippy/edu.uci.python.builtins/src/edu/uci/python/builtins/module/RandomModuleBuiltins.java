/*
 * Copyright (c) 2014, Regents of the University of California
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
package edu.uci.python.builtins.module;

import java.io.*;
import java.math.*;
import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

/**
 * @author myq
 *
 */
public class RandomModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return RandomModuleBuiltinsFactory.getFactories();
    }

    protected static java.util.Random javaRandom = new java.util.Random();

    @Builtin(name = "seed", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class SeedNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization
        public PNone seed(PNone none) {
            javaRandom.setSeed(System.currentTimeMillis());
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(int inputSeed) {
            javaRandom.setSeed(inputSeed);
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(BigInteger inputSeed) {
            javaRandom.setSeed(inputSeed.longValue());
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(double inputSeed) {
            javaRandom.setSeed((long) ((Long.MAX_VALUE - inputSeed) * 412316924));
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(Object inputSeed) {
            javaRandom.setSeed(System.identityHashCode(inputSeed));
            return PNone.NONE;
        }
    }

    @Builtin(name = "jumpahead", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class JumpAheadNode extends PythonBuiltinNode {
        @Specialization
        public PNone jumpahead(int jumps) {
            for (int i = jumps; i > 0; i--) {
                javaRandom.nextInt();
            }
            return PNone.NONE;
        }

        @Specialization
        public PNone jumpahead(double jumps) {
            for (double i = jumps; i > 0; i--) {
                javaRandom.nextInt();
            }
            return PNone.NONE;
        }
    }

    @Builtin(name = "setstate", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class SetStateNode extends PythonBuiltinNode {

        @Specialization
        public PNone setstate(PTuple tuple) {

            try {
                Object[] arr = tuple.getArray();
                byte[] b = new byte[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] instanceof Integer) {
                        b[i] = ((Integer) arr[i]).byteValue();
                    } else {
                        throw Py.TypeError("state vector of unexpected type: " + arr[i].getClass());
                    }
                }
                ByteArrayInputStream bin = new ByteArrayInputStream(b);
                ObjectInputStream oin = new ObjectInputStream(bin);
                javaRandom = (java.util.Random) oin.readObject();
            } catch (IOException e) {
                throw Py.SystemError("state vector invalid: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw Py.SystemError("state vector invalid: " + e.getMessage());
            }
            return PNone.NONE;
        }
    }

    // TODO: randrange is not part of _random
    @Builtin(name = "randrange", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class RandRangeNode extends PythonBuiltinNode {

        @Specialization
        public int randrange(int stop) {
            double scaled = javaRandom.nextDouble() * stop;

            while (scaled > stop) {
                scaled = javaRandom.nextDouble() * stop;
            }

            assert scaled <= stop;
            return (int) scaled;
        }

        @Specialization
        public int randrange(BigInteger stop) {
            long stopLong = stop.longValue();
            double scaled = javaRandom.nextDouble() * stopLong;

            while (scaled > stopLong) {
                scaled = javaRandom.nextDouble() * stopLong;
            }

            assert scaled <= stopLong;
            return (int) scaled;
        }
    }

    @Builtin(name = "getstate", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class GetStateNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization
        public PTuple getstate(PNone none) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeObject(javaRandom);
                byte[] b = bout.toByteArray();
                Integer[] retarr = new Integer[b.length];
                for (int i = 0; i < b.length; i++) {
                    retarr[i] = new Integer(b[i]);
                }
                PTuple ret = new PTuple(retarr);
                return ret;
            } catch (IOException e) {
                throw Py.SystemError("creation of state vector failed: " + e.getMessage());
            }
        }
    }

    @Builtin(name = "random", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class RandomNode extends PythonBuiltinNode {

        @Specialization
        public double random() {
            return javaRandom.nextDouble();
        }
    }

    @Builtin(name = "getrandbits", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    @GenerateNodeFactory
    public abstract static class GetRandBitsNode extends PythonBuiltinNode {

        @Specialization
        public BigInteger getrandbits(int k) {
            return new BigInteger(k, javaRandom);
        }
    }

}
