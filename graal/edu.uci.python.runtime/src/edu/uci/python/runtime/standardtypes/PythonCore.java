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
package edu.uci.python.runtime.standardtypes;

import edu.uci.python.runtime.*;

public class PythonCore {

    private final PythonContext context;

    private PythonClass typeClass;

    private PythonClass objectClass;

    private PythonClass moduleClass;

    private PythonModule builtinsModule;

    private PythonModule mainModule;

    public PythonCore(PythonContext context) {
        this.context = context;
    }

    public void initialize() {
        assert context != null;

        typeClass = new PythonClass(context, null, "type");
        objectClass = new PythonClass(context, null, "object");
        typeClass.unsafeSetSuperClass(objectClass);
        moduleClass = new PythonClass(context, objectClass, "module");

        builtinsModule = new BuiltinsModule(moduleClass, "__builtins__");
        mainModule = new MainModule(moduleClass, "__main__");
        mainModule.setAttribute("__builtins__", builtinsModule);
    }

    public PythonClass getTypeClass() {
        return typeClass;
    }

    public PythonClass getObjectClass() {
        return objectClass;
    }

    public PythonClass getModuleClass() {
        return moduleClass;
    }

    public PythonModule getBuiltinsModule() {
        return builtinsModule;
    }

    public PythonModule getMainModule() {
        return mainModule;
    }
}
