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
package org.python.core.truffle;

import java.util.*;

import org.python.core.__builtin__;
import org.python.modules.truffle.ArrayModule;
import org.python.modules.truffle.BisectModule;
import org.python.modules.truffle.PythonModule;
import org.python.modules.truffle.TimeModule;

public class BuiltIns {

    public static final HashMap<String, PythonModule> moduleMap = moduleMapInit();

    public static HashMap<String, PythonModule> moduleMapInit() {
        HashMap<String, PythonModule> map = new HashMap<>();
        map.put("array", new ArrayModule());
        map.put("bisect", new BisectModule());
        map.put("time", new TimeModule());
        return map;
    }

    public static Object importModule(String name) {
        Object importedModule = moduleMap.get(name);

        if (importedModule == null) {
            importedModule = __builtin__.__import__(name);
        }

        return importedModule;
    }
}
