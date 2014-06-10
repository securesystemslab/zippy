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
package edu.uci.python.builtins.type;

import java.util.*;

import com.oracle.truffle.api.dsl.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.runtime.array.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.sequence.*;

/**
 * @author Gulfem
 * @author zwei
 */

public final class StringBuiltins extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return StringBuiltinsFactory.getFactories();
    }

    private static final PList idmap = new PList();

    static {
        for (int i = 0; i < 256; i++) {
            idmap.append(Character.toString((char) i));
        }
    }

    // str.startswith(prefix[, start[, end]])
    @Builtin(name = "startswith", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class StartsWithNode extends PythonBuiltinNode {

        @Specialization
        public Object startsWith(String self, String prefix) {
            if (self.startsWith(prefix)) {
                return true;
            }

            return false;
        }

        @Specialization
        public Object startsWith(Object self, Object prefix) {
            throw new RuntimeException("startsWith is not supported for " + self + " " + self.getClass() + " prefix " + prefix);
        }
    }

    // str.join(iterable)
    @Builtin(name = "join", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class JoinNode extends PythonBuiltinNode {

        @Specialization(order = 0)
        public String join(String string, String arg) {
            StringBuilder sb = new StringBuilder();
            char[] joinString = arg.toCharArray();

            for (int i = 0; i < joinString.length - 1; i++) {
                sb.append(Character.toString(joinString[i]));
                sb.append(string);
            }

            sb.append(Character.toString(joinString[joinString.length - 1]));
            return sb.toString();
        }

        @Specialization(order = 1)
        public String join(String string, PSequence seq) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < seq.len() - 1; i++) {
                sb.append(seq.getItem(i).toString());
                sb.append(string);
            }

            sb.append(seq.getItem(seq.len() - 1));
            return sb.toString();
        }

        @Specialization(order = 2)
        public String join(String string, PCharArray array) {
            StringBuilder sb = new StringBuilder();
            char[] stringList = array.getSequence();

            for (int i = 0; i < stringList.length - 1; i++) {
                sb.append(Character.toString(stringList[i]));
                sb.append(string);
            }

            sb.append(Character.toString(stringList[stringList.length - 1]));
            return sb.toString();
        }

        @Specialization(order = 3)
        public String join(String string, PSet arg) {
            if (arg.len() == 0) {
                return string.toString();
            }

            StringBuilder sb = new StringBuilder();
            Object[] joinString = arg.getSet().toArray();
            for (int i = 0; i < joinString.length - 1; i++) {
                sb.append(joinString[i]);
                sb.append(string);
            }

            sb.append(joinString[joinString.length - 1]);
            return sb.toString();
        }

        @Specialization(order = 5)
        public String join(Object self, Object arg) {
            throw new RuntimeException("invalid arguments type for join(): self " + self + ", arg " + arg);
        }
    }

    // str.upper()
    @Builtin(name = "upper", fixedNumOfArguments = 1, hasFixedNumOfArguments = true)
    public abstract static class UpperNode extends PythonBuiltinNode {

        @Specialization
        public String upper(String self) {
            return self.toUpperCase();
        }
    }

    // static str.maketrans()
    @Builtin(name = "maketrans", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class MakeTransNode extends PythonBuiltinNode {

        @Specialization
        public PDict maketrans(String from, String to) {
            if (from.length() != to.length()) {
                throw new RuntimeException("maketrans arguments must have same length");
            }

            PDict translation = new PDict();
            for (int i = 0; i < from.length(); i++) {
                int key = from.charAt(i);
                int value = to.charAt(i);
                translation.setItem(key, value);
            }

            return translation;
        }
    }

    // str.translate()
    @Builtin(name = "translate", fixedNumOfArguments = 2, hasFixedNumOfArguments = true)
    public abstract static class TranslateNode extends PythonBuiltinNode {

        @Specialization(order = 0)
        public String translate(String self, PDict table) {
            char[] translatedChars = new char[self.length()];

            for (int i = 0; i < self.length(); i++) {
                char original = self.charAt(i);
                Object translated = table.getItem((int) original);
                int ord = translated == null ? original : (int) translated;
                translatedChars[i] = (char) ord;
            }

            return new String(translatedChars);
        }
    }

}
