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
package edu.uci.python.builtins;

import java.util.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.dsl.NodeFactory;

import edu.uci.python.runtime.sequence.*;

/**
 * @author Gulfem
 */

public final class BisectModuleBuiltins extends PythonBuiltins {

    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return BisectModuleBuiltinsFactory.getFactories();
    }

    // bisect.bisect(a, x, lo=0, hi=len(a))
    @Builtin(name = "bisect", hasFixedNumOfArguments = true, fixedNumOfArguments = 2, takesKeywordArguments = true, takesVariableKeywords = true, keywordNames = {"lo", "hi"})
    public abstract static class PythonBisectNode extends PythonBuiltinNode {

        public PythonBisectNode(String name) {
            super(name);
        }

        public PythonBisectNode(PythonBisectNode prev) {
            this(prev.getName());
        }

        @Specialization
        public int bisect(Object arg1, Object arg2, Object[] keywords) {
            return bisect(arg1, arg2);
        }

        public int bisect(Object arg0, Object arg1) {
            if (arg0 instanceof PList) {
                PList plist = (PList) arg0;
                Object[] list = plist.getSequence();

                if (list.length == 0) {
                    return 0;
                }

                return getIndexRight(list, arg1);
            } else {
                throw new RuntimeException("invalid arguments number for bisect() ");
            }
        }

        public int bisect(Object arg) {
            throw new RuntimeException("wrong number of arguments for bisect() ");
        }

        @SlowPath
        public int getIndexRight(Object[] args, Object key) {
            if (key instanceof String) {
                return binarySearchRightStr(args, 0, args.length - 1, (String) key);
            } else {
                return binarySearchRightDouble(args, 0, args.length - 1, (double) key);
            }
        }

        @SlowPath
        public int binarySearchRightDouble(Object[] args, int start, int stop, double key) {
            if (start <= stop) {
                int middle = (stop - start) / 2 + start;
                if (((double) args[middle]) > key) {
                    if (middle - 1 >= 0 && ((double) args[middle - 1]) < key) {
                        return middle;
                    } else if (middle - 1 <= 0) {
                        return 0;
                    } else {
                        return binarySearchRightDouble(args, start, middle - 1, key);
                    }
                } else if (((double) args[middle]) < key) {
                    if (middle + 1 < args.length && ((double) args[middle + 1]) > key) {
                        return middle + 1;
                    } else if (middle + 1 >= args.length - 1) {
                        return args.length;
                    } else {
                        return binarySearchRightDouble(args, middle + 1, stop, key);
                    }
                } else {
                    int i = middle + 1;
                    while (((double) args[i]) == key && i < args.length) {
                        i++;
                    }
                    return i;
                }
            }
            return -1; // should not happen
        }

        @SlowPath
        public int binarySearchRightStr(Object[] args, int start, int stop, String key) {
            if (start <= stop) {
                int middle = (stop - start) / 2 + start;
                if (((String) args[middle]).compareTo(key) > 0) {
                    if (middle - 1 >= 0 && ((String) args[middle - 1]).compareTo(key) < 0) {
                        return middle;
                    } else if (middle - 1 <= 0) {
                        return 0;
                    } else {
                        return binarySearchRightStr(args, start, middle - 1, key);
                    }
                } else if (((String) args[middle]).compareTo(key) < 0) {
                    if (middle + 1 < args.length && ((String) args[middle + 1]).compareTo(key) > 0) {
                        return middle + 1;
                    } else if (middle + 1 >= args.length - 1) {
                        return args.length;
                    } else {
                        return binarySearchRightStr(args, middle + 1, stop, key);
                    }
                } else {
                    int i = middle + 1;
                    while (((String) args[i]).compareTo(key) == 0 && i < args.length) {
                        i++;
                    }
                    return i;
                }
            }
            return -1; // should not happen
        }
    }

}
