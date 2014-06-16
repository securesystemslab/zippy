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
package edu.uci.python.runtime;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;

import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Gulfem
 * @author zwei
 * @author myq
 */

public class ImportManager {

    private final PythonContext context;

    private final List<String> paths;

    private final Map<String, PythonModule> importedModules;

    // Unsupported Imports:
    private final Map<String, Boolean> unsupportedImports;

    private static String getPythonLibraryPath() {
        String workingDir = System.getProperty("user.dir");

        // TODO: Fix this hack that supports proper standard lib import in unittest.
        if (workingDir.endsWith("/graal/edu.uci.python.test")) {
            workingDir = workingDir.replaceAll("/graal/edu.uci.python.test", "");
        }

        String librayPath = workingDir + File.separatorChar + "lib-python" + File.separatorChar + "3";
        return librayPath;
    }

    public ImportManager(PythonContext context) {
        this.context = context;
        this.paths = new ArrayList<>();
        this.importedModules = new HashMap<>();
        this.unsupportedImports = new HashMap<>();
        this.paths.add(getPythonLibraryPath());

        String[] unsupportedImportNames = {"re", "os", "posix", "io", "textwrap", "optparse", "functools", "struct", "decimal", "collections", "threading"};

        for (String lib : unsupportedImportNames) {
            this.unsupportedImports.put(lib, true);
        }
    }

    public Object importModule(String moduleName) {
        return importModule(context.getMainModule(), moduleName);
    }

    public Object importModule(PythonModule relativeto, String moduleName) {
        CompilerAsserts.neverPartOfCompilation();

        /**
         * Look up built-in modules supported by ZipPy
         */
        Object importedModule = context.getPythonBuiltinsLookup().lookupModule(moduleName);
        if (importedModule != null) {
            return importedModule;
        }

        /**
         * Go to Jython for blacklisted modules.
         */
        if (unsupportedImports.containsKey(moduleName)) {
            return importFromJython(moduleName);
        }

        /**
         * Try to find user module.
         */
        String path = getPathFromImporterPath(moduleName, relativeto.getModulePath());
        if (path != null) {
            return importAndCache(path, moduleName);
        }

        /**
         * Try to find from system paths.
         */
        updateSystemPathFromJython();
        for (String directoryPath : paths) {
            path = getPathFromLibrary(directoryPath, moduleName);

            if (path != null) {
                return importAndCache(path, moduleName);
            }
        }

        /**
         * Eventually fall back to Jython, and might return null.
         */
        return importFromJython(moduleName);
    }

    private void updateSystemPathFromJython() {
        PyList jythonSystemPaths = Py.getSystemState().path;

        for (Object path : jythonSystemPaths) {
            if (!(path instanceof String)) {
                continue;
            }

            String stringPath = (String) path;
            if (stringPath.contains("zippy/lib") || stringPath.contains("jython")) {
                continue;
            }

            if (!Files.exists(Paths.get(stringPath))) {
                continue;
            }

            if (!paths.contains(stringPath)) {
                paths.add(stringPath);
            }
        }
    }

    private static PyObject importFromJython(String moduleName) {
        if (PythonOptions.TraceImports) {
            // CheckStyle: stop system..print check
            System.out.println("[ZipPy] importing from jython runtime " + moduleName);
            // CheckStyle: resume system..print check
        }
        return __builtin__.__import__(moduleName);
    }

    private static String getPathFromImporterPath(String moduleName, String basePath) {
        String importingModulePath = basePath;
        String filename = moduleName + ".py";
        String path = null;

        try {
            path = new File(importingModulePath).getCanonicalFile().getParent();
        } catch (IOException ioe) {
            path = new File(importingModulePath).getAbsoluteFile().getParent();
        }

        if (path == null) {
            return null;
        }

        String importedModulePath = path + File.separatorChar + filename;
        File importingFile = new File(importedModulePath);

        if (importingFile.exists()) {
            return importedModulePath;
        }

        importedModulePath = path + File.separatorChar + moduleName;
        File importingDirectory = new File(importedModulePath);
        importingFile = new File(importingDirectory, "__init__.py");
        if (importingDirectory.isDirectory() && importingFile.exists()) {
            return importingFile.toString();
        }

        return null;
    }

    private static String getPathFromLibrary(String directoryPath, String moduleName) {
        if (moduleName.equals("unittest")) {
            String casePath = getPythonLibraryPath() + File.separatorChar + "unittest" + File.separatorChar + "__init__zippy.py";
            return casePath;
        }

        String sourceName = "__init__.py";

        // First check for packages
        File dir = new File(directoryPath, moduleName);
        File sourceFile = new File(dir, sourceName);

        boolean isPackage = false;
        try {
            isPackage = dir.isDirectory() && sourceFile.isFile();
        } catch (SecurityException e) {
            // ok
        }

        if (!isPackage) {
            sourceName = moduleName + ".py";
            sourceFile = new File(directoryPath, sourceName);
            if (sourceFile.isFile()) {
                String path = sourceFile.getPath();
                return path;
            }
        } else {
            sourceFile = new File(dir, sourceName);

            if (sourceFile.isFile()) {
                String path = sourceFile.getPath();
                return path;
            }
        }

        return null;
    }

    private PythonModule importAndCache(String path, String moduleName) {
        PythonModule importedModule = importedModules.get(path);
        if (importedModule == null) {
            importedModule = tryImporting(path, moduleName);
        }

        assert importedModule.getAttribute("__name__").equals(moduleName);
        return importedModule;
    }

    private PythonModule tryImporting(String path, String moduleName) {
        PythonParseResult parsedModule = parseModule(path, moduleName);

        if (parsedModule != null) {
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(parsedModule.getModuleRoot());
            callTarget.call(PArguments.empty());
            return parsedModule.getModule();
        }

        return null;
    }

    private PythonParseResult parseModule(String path, String moduleName) {
        File file = new File(path);

        if (file.exists()) {
            PythonModule importedModule = new PythonModule(context, moduleName, path);
            Source source = SourceFactory.fromFile(path);
            importedModules.put(path, importedModule);
            PythonParseResult parsedModule = context.getParser().parse(context, importedModule, source);
            if (parsedModule != null) {
                if (PythonOptions.TraceImports) {
                    // CheckStyle: stop system..print check
                    System.out.println("[ZipPy] parsed module " + path);
                    // CheckStyle: resume system..print check
                }
                if (PythonOptions.PrintAST) {
                    parsedModule.printAST();
                }
            }

            return parsedModule;
        }

        return null;
    }

}
