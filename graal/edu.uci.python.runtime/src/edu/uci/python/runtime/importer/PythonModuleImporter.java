package edu.uci.python.runtime.importer;

import java.io.*;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

public class PythonModuleImporter {

    private final PythonContext context;
    private final String moduleName;

    public PythonModuleImporter(PythonContext context, String moduleName) {
        this.context = context;
        this.moduleName = moduleName;
    }

    public Object importModule(VirtualFrame frame) {
        Object importedModule = context.getPythonBuiltinsLookup().lookupModule(moduleName);

        if (importedModule != null) {
            return importedModule;
        } else {
            String filename = moduleName + ".py";
            String path = getImporterPath();
            String fullPath = path + filename;
            PythonParseResult parsedModule = parseModule(fullPath);
            if (parsedModule != null) {
                importedModule = createModule(parsedModule, frame);
            } else {
                parsedModule = findModule(moduleName, moduleName);
                if (parsedModule != null) {
                    importedModule = createModule(parsedModule, frame);
                } else {
                    importedModule = __builtin__.__import__(moduleName);
                }
            }
        }

        return importedModule;

    }

    private PythonParseResult parseModule(String path) {
        File file = new File(path);

        if (file.exists()) {
            Source source = context.getSourceManager().get(path);
            PythonContext moduleContext = new PythonContext(context, moduleName);
            PythonParseResult parsedModule = context.getParser().parse(moduleContext, source, CompileMode.exec, CompilerFlags.getCompilerFlags());
            if (parsedModule != null) {
                System.out.println("[ZipPy] parsing module " + moduleName);
                if (PythonOptions.PrintAST) {
                    parsedModule.printAST();
                }
            }

            return parsedModule;
        }

        return null;
    }

    private PythonModule createModule(PythonParseResult parsedModule, Frame frame) {
        PythonModule importedModule = null;
        if (parsedModule != null) {
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(parsedModule.getModuleRoot(), frame.getFrameDescriptor());
            callTarget.call(null, new PArguments(null));
            PythonContext moduleContext = parsedModule.getContext();
            PythonModule module = moduleContext.getPythonBuiltinsLookup().lookupModule(moduleName);
            return module;
        }

        return importedModule;
    }

    private String getImporterPath() {
        String path = ".";

        // TODO: After adding support to SourceSection, this what we should use:
        // String name = this.getSourceSection().getSource().getPath();
        String name = context.getParser().getSource().getPath();
        String fileName = new StringBuilder(name).reverse().toString();
        int separtorLoc = name.length() - fileName.indexOf(File.separatorChar);
        int filenameln = name.length() - separtorLoc;
        fileName = new StringBuilder(fileName).reverse().toString().substring(separtorLoc, name.length());
        final File file = new File(name);
        if (file.exists()) {
            try {
                path = file.getCanonicalPath();
                path = path.substring(0, path.length() - filenameln);
            } catch (IOException e) {
            }
        }

        return path;
    }

    public PythonParseResult findModule(String name, String moduleName) {
        String workingDir = System.getProperty("user.dir");
        String path = workingDir + File.separatorChar + "lib-python" + File.separatorChar + "3";
        PythonParseResult parsedModule = null;
        parsedModule = loadFromSource(name, moduleName, path);

        if (parsedModule != null) {
            return parsedModule;
        }

        return parsedModule;
    }

    @SuppressWarnings("unused")
    private PythonParseResult loadFromSource(String name, String modName, String path) {
        String dirName = path;
        String sourceName = "__init__.py";
        // display names are for identification purposes (e.g. __file__): when entry is
        // null it forces java.io.File to be a relative path (e.g. foo/bar.py instead of
        // /tmp/foo/bar.py)
        String displayDirName = path.equals("") ? null : path.toString();
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
            if (sourceFile.isFile()) {
                String fullPath = displayDirName + File.separatorChar + sourceName;
                PythonParseResult parsedModule = parseModule(fullPath);
                if (parsedModule != null) {
                    return parsedModule;
                }
            }
        } else {
            /**
             * TODO Code is not implemented for modules that are in a package such as unittest
             */
        }

        return null;
    }
}
