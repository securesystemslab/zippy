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
package edu.uci.python.nodes.statement;

import java.io.*;
import java.net.*;

import org.python.core.*;
import org.python.core.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.source.*;
import edu.uci.python.runtime.standardtype.*;

public class ImportNode extends PNode {

    private final PythonContext context;

    private final String fromModuleName;

    private final String importee;

    public ImportNode(PythonContext context, String fromModule, String importee) {
        this.context = context;
        this.fromModuleName = fromModule;
        this.importee = importee;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object importedModule = null;

        if (fromModuleName != null) {
            importedModule = importModule(frame, fromModuleName);
        }

        return doImport(frame, importedModule, importee);
    }

    private Object doImport(VirtualFrame frame, Object importedModule, String name) {
        try {
            if (importedModule != null && importedModule instanceof PythonModule) {
                return ((PythonModule) importedModule).getAttribute(name);
            } else if (importedModule != null) {
                return ((PyObject) importedModule).__getattr__(name);
            } else {
                return importModule(frame, name);
            }
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.ImportError(String.format("cannot import name %.230s", name));
            } else {
                throw pye;
            }
        }
    }

    private Object importModule(VirtualFrame frame, String name) {
        Object importedModule = context.getPythonBuiltinsLookup().lookupModule(name);
        PythonParseResult result = null;
        CallTarget callTarget = null;
        PythonContext moduleContext = null;
        if (importedModule == null) {

            FileInputStream inputStream;
            try {
                String filename = name + ".py";
                String path = context.getSourceManager().getPath();
                inputStream = new FileInputStream(new RelativeFile(path + File.separatorChar + filename));
                SourceManager sourceManager = new SourceManager(path, filename, inputStream);
                moduleContext = new PythonContext(context, sourceManager);
                importedModule = result = context.getParser().parse(moduleContext, CompileMode.exec, CompilerFlags.getCompilerFlags());
                inputStream.close();
            } catch (IOException e) {
                // do nothing and jython's importer will fix it.
            }

            if (importedModule != null) {
                callTarget = Truffle.getRuntime().createCallTarget(result.getModuleRoot(), frame.getFrameDescriptor());
                callTarget.call(null, new PArguments(null));
                moduleContext = ((PythonParseResult) importedModule).getContext();
                PythonModule module = moduleContext.getPythonBuiltinsLookup().lookupModule("__main__");
                importedModule = new PythonModule(importee, module);
            } else {
                /*
                 * This should be removed the soon we can import any module
                 */

                if (PythonOptions.useNewImportMechanism) {
                    PythonParseResult parsedModule = findModule(name, name);
                    if (parsedModule != null) {
                        importedModule = createModule(parsedModule, frame);
                        if (PythonOptions.PrintAST) {
                            parsedModule.printAST();
                        }
                        return importedModule;
                    }
                }

                if (importedModule == null) {
                    importedModule = __builtin__.__import__(name);
                }
            }
        }

        return importedModule;
    }

    private PythonParseResult findModule(String name, String moduleName) {
        PySystemState sys = Py.getSystemState();
        PyList path = sys.path;

        PythonParseResult parsedModule = null;
        for (int i = 0; i < path.__len__(); i++) {
            PyObject p = path.__getitem__(i);
            if (!(p instanceof PyUnicode)) {
                p = p.__str__();
            }

            parsedModule = loadFromSource(sys, name, moduleName, p.toString());
            if (parsedModule != null) {
                return parsedModule;
            }
        }

        return parsedModule;
    }

    /**
     * TODO Taken from Jython, needs to cleaned up.
     */
    @SuppressWarnings("unused")
    private PythonParseResult loadFromSource(PySystemState sys, String name, String modName, String entry) {
        String dirName = sys.getPath(entry);
        String sourceName = "__init__.py";
        // display names are for identification purposes (e.g. __file__): when entry is
        // null it forces java.io.File to be a relative path (e.g. foo/bar.py instead of
        // /tmp/foo/bar.py)
        String displayDirName = entry.equals("") ? null : entry.toString();
        String displaySourceName = new File(new File(displayDirName, name), sourceName).getPath();

        // First check for packages
        File dir = new File(dirName, name);
        File sourceFile = new File(dir, sourceName);

        boolean isPackage = false;
        try {
            isPackage = dir.isDirectory() && sourceFile.isFile();
        } catch (SecurityException e) {
            // ok
        }

        if (!isPackage) {
            sourceName = name + ".py";
            displaySourceName = new File(displayDirName, sourceName).getPath();
            sourceFile = new File(dirName, sourceName);
            URL url = createURL(displayDirName, sourceName);
            try {
                // System.out.println("MODULE NAME " + modName);
                InputStream inputStream = url.openStream();
                if (inputStream != null) {
                    System.out.println("[ZipPy] parsing module " + modName);
                    PythonParseResult parsedModule = parseModule(displayDirName, sourceName, inputStream);
                    inputStream.close();
                    return parsedModule;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        } else {
            // PyModule m = addModule(modName);
            // PyObject filename = new PyString(new File(displayDirName, name).getPath());
            // m.__dict__.__setitem__("__path__", new PyList(new PyObject[]{filename}));
        }

        return null;
    }

    private PythonParseResult parseModule(String dirName, String sourceName, InputStream inputStream) {
        SourceManager sourceManager = new SourceManager(dirName, sourceName, inputStream);
        PythonContext moduleContext = new PythonContext(context, sourceManager);
        PythonParseResult parseResult = context.getParser().parse(moduleContext, CompileMode.exec, CompilerFlags.getCompilerFlags());
        return parseResult;
    }

    private PythonModule createModule(PythonParseResult parseResult, Frame frame) {
        PythonModule importedModule = null;
        if (parseResult != null) {
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(parseResult.getModuleRoot(), frame.getFrameDescriptor());
            callTarget.call(null, new PArguments(null));
            PythonContext moduleContext = parseResult.getContext();
            PythonModule module = moduleContext.getPythonBuiltinsLookup().lookupModule("__main__");
            importedModule = new PythonModule(importee, module);
        }

        return importedModule;
    }

    private static URL createURL(String path, String fileName) {
        URL url = null;
        if (path.contains(".jar")) {
            int indexOfJar = path.indexOf(".jar");
            String pathOfJar = path.substring(0, indexOfJar + 4);
            String pathAfterJar = path.substring(indexOfJar + 4, path.length());
            String urlString = "jar:file:" + pathOfJar + "!" + pathAfterJar + "/" + fileName;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String urlString = "file:" + path + "/" + fileName;
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return url;
    }

}
