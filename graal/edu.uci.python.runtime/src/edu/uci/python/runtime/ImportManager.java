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
import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

/**
 * @author Gulfem
 * @author zwei
 * @author myq
 */

public class ImportManager {

    private static final String PYTHON_LIB_PATH = getPythonLibraryPath();
    private final PythonContext context;
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
        this.importedModules = new HashMap<>();
        this.unsupportedImports = new HashMap<>();
        String[] unsupportedImportNames = {"re", "os", "posix", "io", "textwrap", "optparse", "functools"};
        for (String lib : unsupportedImportNames) {
            this.unsupportedImports.put(lib, true);
        }
    }

    public Object importModule(String moduleName) {
        return importModule(context.getMainModule(), moduleName);
    }

    public Object importModule(PythonModule relativeto, String moduleName) {

        Object importedModule = context.getPythonBuiltinsLookup().lookupModule(moduleName);

        /**
         * Use Jython's regex module.
         */
        if (unsupportedImports.containsKey(moduleName)) {
            return importFromJython(moduleName);
        }

        if (importedModule != null) {
            return importedModule;
        } else {
            String path = getPathFromImporterPath(moduleName, relativeto.getModulePath());

            if (path != null) {
                importedModule = importAndCache(path, moduleName);
            } else {
                path = getPathFromLibrary(moduleName);
                if (path != null) {
                    importedModule = importAndCache(path, moduleName);
                } else {
                    importedModule = importFromJython(moduleName);
                }
            }
        }

        return importedModule;
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

        if (path != null) {
            String importedModulePath = path + File.separatorChar + filename;
            File importingFile = new File(importedModulePath);
            if (importingFile.exists()) {
                return importedModulePath;
            }
        }

        return null;
    }

    private static String getPathFromLibrary(String moduleName) {
        if (moduleName.equals("unittest")) {
            String casePath = PYTHON_LIB_PATH + File.separatorChar + "unittest" + File.separatorChar + "__init__zippy.py";
            return casePath;
        }

        String dirPath = PYTHON_LIB_PATH;
        String sourceName = "__init__.py";

        // First check for packages
        File dir = new File(dirPath, moduleName);
        File sourceFile = new File(dir, sourceName);

        boolean isPackage = false;
        try {
            isPackage = dir.isDirectory() && sourceFile.isFile();
        } catch (SecurityException e) {
            // ok
        }

        if (!isPackage) {
            sourceName = moduleName + ".py";
            sourceFile = new File(dirPath, sourceName);
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
            Source source = context.getSourceManager().get(path);
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
