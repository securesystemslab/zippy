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

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.sequence.*;

public final class StringBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return StringBuiltinsFactory.getFactories();
    }

    // str.join(iterable)
    @Builtin(name = "join", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class PythonStringJoinNode extends PythonBuiltinNode {

        public PythonStringJoinNode(String name) {
            super(name);
        }

        public PythonStringJoinNode(PythonStringJoinNode prev) {
            this(prev.getName());
        }

        @Specialization
        public String join(Object self, Object arg) {
            if (arg instanceof String) {
                StringBuilder sb = new StringBuilder();
                char[] joinString = ((String) arg).toCharArray();
                for (int i = 0; i < joinString.length - 1; i++) {
                    sb.append(Character.toString(joinString[i]));
                    sb.append(self.toString());
                }
                sb.append(Character.toString(joinString[joinString.length - 1]));

                return sb.toString();
            } else if (arg instanceof PSequence) {
                StringBuilder sb = new StringBuilder();
                Object[] stringList = ((PSequence) arg).getSequence();
                for (int i = 0; i < stringList.length - 1; i++) {
                    sb.append(stringList[i].toString());
                    sb.append(self.toString());
                }
                sb.append((String) stringList[stringList.length - 1]);

                return sb.toString();
            } else if (arg instanceof PCharArray) {
                StringBuilder sb = new StringBuilder();
                char[] stringList = ((PCharArray) arg).getSequence();
                for (int i = 0; i < stringList.length - 1; i++) {
                    sb.append(Character.toString(stringList[i]));
                    sb.append((String) self);
                }
                sb.append(Character.toString(stringList[stringList.length - 1]));

                return sb.toString();
            } else {
                throw new RuntimeException("invalid arguments type for join()");
            }
        }
    }

    // str.upper()
    @Builtin(name = "upper", fixedNumOfArguments = 0, hasFixedNumOfArguments = true)
    public abstract static class PythonStringUpperNode extends PythonBuiltinNode {

        public PythonStringUpperNode(String name) {
            super(name);
        }

        public PythonStringUpperNode(PythonStringUpperNode prev) {
            this(prev.getName());
        }

        @Specialization
        public String upper(String self) {
            return self.toUpperCase();
        }
    }

}
