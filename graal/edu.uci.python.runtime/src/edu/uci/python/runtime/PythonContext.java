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
package edu.uci.python.runtime;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.source.*;

import edu.uci.python.runtime.builtin.*;
import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.standardtype.*;

public class PythonContext {

    private final PythonModule builtinsModule;
    private final PythonOptions options;
    private final PythonBuiltinsLookup lookup;

    private final PythonBuiltinClass typeClass;
    private final PythonBuiltinClass objectClass;
    private final PythonBuiltinClass moduleClass;

    private final SourceManager sourceManager;
    private final PythonParser parser;
    private final ImportManager importManager;

    // Parallel generators
    private final ExecutorService executorService;
    private final Map<String, long[]> generatorProfilingInfo;

    private static PythonContext currentContext;

    private RuntimeException currentException;

    public PythonContext(PythonOptions opts, PythonBuiltinsLookup lookup, PythonParser parser) {
        this.options = opts;
        this.lookup = lookup;
        this.typeClass = new PythonBuiltinClass(this, null, "type");
        this.objectClass = new PythonObjectClass(this);
        this.typeClass.unsafeSetSuperClass(objectClass);
        this.moduleClass = new PythonBuiltinClass(this, objectClass, "module");
        this.sourceManager = new SourceManager();
        this.parser = parser;
        this.importManager = new ImportManager(this);

        // The order matters.
        currentContext = this;

        this.builtinsModule = this.lookup.populateBuiltins(this);
        this.executorService = Executors.newCachedThreadPool();
        this.generatorProfilingInfo = new HashMap<>();
    }

    public PythonModule createMainModule() {
        PythonModule main = new PythonModule("__main__", this);
        main.setAttribute("__builtins__", getBuiltins());
        return main;
    }

    public PythonModule getBuiltins() {
        return builtinsModule;
    }

    public PythonOptions getPythonOptions() {
        return options;
    }

    public PythonBuiltinsLookup getPythonBuiltinsLookup() {
        return lookup;
    }

    public static PythonBuiltinClass getBuiltinTypeFor(Class<? extends PythonBuiltinObject> javaClass) {
        return currentContext.lookup.lookupType(javaClass);
    }

    public PrintStream getStandardOut() {
        return options.getStandardOut();
    }

    public boolean getUseUnsafe() {
        return PythonOptions.UseUnsafe;
    }

    public PythonBuiltinClass getTypeClass() {
        return typeClass;
    }

    public PythonBuiltinClass getObjectClass() {
        return objectClass;
    }

    public PythonClass getModuleClass() {
        return moduleClass;
    }

    public PythonBuiltinObject boxAsPythonBuiltinObject(Object obj) throws UnexpectedResultException {
        if (obj instanceof PythonBuiltinObject) {
            return (PythonBuiltinObject) obj;
        }

        /**
         * TODO: missing int, double, boolean... and maybe more.
         */
        if (obj instanceof String) {
            return new PString((String) obj);
        }

        throw new UnexpectedResultException(obj);
    }

    public PythonParser getParser() {
        return parser;
    }

    public SourceManager getSourceManager() {
        return sourceManager;
    }

    public ImportManager getImportManager() {
        return importManager;
    }

    public void setCurrentException(RuntimeException e) {
        currentException = e;
    }

    public RuntimeException getCurrentException() {
        assert currentException != null;
        return currentException;
    }

    public void submitParallelTask(Runnable task) {
        executorService.execute(task);
    }

    public void registerGeneratorProfilingInfo(String generatorId, long innerTime, long outerTime) {
        generatorProfilingInfo.put(generatorId, new long[]{innerTime, outerTime});
    }

    public void printGeneratorProfilingInfo() {
        PrintStream ps = System.out;
        ps.println("--------------- generator profiling info ---------------");
        for (String id : generatorProfilingInfo.keySet()) {
            long[] times = generatorProfilingInfo.get(id);
            double innerTime = (double) times[0] / 1000000000;
            double outerTime = (double) times[1] / 1000000000;
            ps.printf("%25s : ", id);
            ps.printf("inner time: %f10, outer time: %f10, in/out: %f6 \n", innerTime, outerTime, innerTime / outerTime);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

}
