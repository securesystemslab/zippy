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
    public abstract static class SeedNode extends PythonBuiltinNode {

        @Specialization
        public PNone seed(PNone none) {
            javaRandom.setSeed(System.currentTimeMillis());
            return PNone.NONE;
        }

        // TODO: There should be a better way to seed long numbers
        @Specialization
        public PNone seed(double inputSeed) {
            javaRandom.setSeed((long) ((Long.MAX_VALUE - inputSeed) * 412316924));
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(int inputSeed) {
            javaRandom.setSeed(inputSeed);
            return PNone.NONE;
        }

        @Specialization
        public PNone seed(Object inputSeed) {
            javaRandom.setSeed(System.identityHashCode(inputSeed));
            return PNone.NONE;
        }

    }

    @Builtin(name = "jumpahead", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
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
    public abstract static class SetStateNode extends PythonBuiltinNode {

        @Specialization
        public PNone setstate(PTuple tuple) {

            try {
                Object arr[] = tuple.getArray();
                byte b[] = new byte[arr.length];
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
    public abstract static class RandRangeNode extends PythonBuiltinNode {

// @Specialization
// public int randrange(int start) {
// return javaRandom.nextInt() % start;
// }

        @Specialization
        public double randrange(double start) {
            return javaRandom.nextInt() % start;
        }
    }

    @Builtin(name = "getstate", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    public abstract static class GetStateNode extends PythonBuiltinNode {
        @Specialization
        public PTuple getstate(PNone none) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeObject(javaRandom);
                byte b[] = bout.toByteArray();
                Integer retarr[] = new Integer[b.length];
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
    public abstract static class RandomNode extends PythonBuiltinNode {

        @Specialization
        public double random(PNone none) {
            long a = javaRandom.nextInt() >>> 7;
            long b = javaRandom.nextInt() >>> 3;
            return (a * 671333224.0 + b) * (1.0 / 902292547333.0);
        }
    }

    @Builtin(name = "getrandbits", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class GetRandBitsNode extends PythonBuiltinNode {

        @Specialization
        public BigInteger getrandbits(int k) {
            return new BigInteger(k, javaRandom);
        }
    }

}
