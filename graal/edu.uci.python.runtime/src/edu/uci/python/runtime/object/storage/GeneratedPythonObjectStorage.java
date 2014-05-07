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
package edu.uci.python.runtime.object.storage;

import java.lang.invoke.*;

import org.python.core.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public final class GeneratedPythonObjectStorage {

    private final Class storageClass;
    private final MethodHandle ctor;

    public GeneratedPythonObjectStorage(Class storageClass, MethodHandle ctor) {
        this.storageClass = storageClass;
        this.ctor = ctor;
    }

    public static GeneratedPythonObjectStorage createFrom(PythonObject prev) {
        StorageClassGenerator scg = new StorageClassGenerator(prev.getObjectLayout(), prev.getPythonClass().getName());
        Class storageClass = BytecodeLoader.makeClass(scg.getValidClassName(), scg.generate(), PythonObject.class);
        MethodHandle ctor;

        try {
            MethodType mt = MethodType.methodType(PythonObject.class, PythonClass.class);
            ctor = MethodHandles.lookup().findStatic(storageClass, StorageClassGenerator.CREATE, mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException();
        }

        return new GeneratedPythonObjectStorage(storageClass, ctor);
    }

    public Class getStorageClass() {
        return storageClass;
    }

    public MethodHandle getConstructor() {
        return ctor;
    }

}
