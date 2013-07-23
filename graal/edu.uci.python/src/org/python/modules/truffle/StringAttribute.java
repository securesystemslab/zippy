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
package org.python.modules.truffle;

import org.python.ast.datatypes.PCharArray;
import org.python.ast.datatypes.PSequence;
import org.python.modules.truffle.annotations.ModuleMethod;

public class StringAttribute extends PythonModule {

    public StringAttribute() {
        try {
            addAttributeMethods();
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @ModuleMethod
    public String join(Object[] args, Object self) {
        if (args.length == 1) {
            return join(args[0], self);
        } else {
            throw new RuntimeException("wrong number of arguments for join()");
        }
    }

    public String join(Object arg, Object self) {
        if (arg instanceof String) {
            StringBuilder sb = new StringBuilder();
            char[] joinString = ((String) arg).toCharArray();
            for (int i = 0; i < joinString.length - 1; i++) {
                sb.append(Character.toString(joinString[i]));
                sb.append((String) self);
            }
            sb.append(Character.toString(joinString[joinString.length - 1]));

            return sb.toString();
        } else if (arg instanceof PSequence) {
            StringBuilder sb = new StringBuilder();
            Object[] stringList = ((PSequence) arg).getSequence();
            for (int i = 0; i < stringList.length - 1; i++) {
                sb.append((String) stringList[i]);
                sb.append((String) self);
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

    public String join(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for join()");
    }
}
