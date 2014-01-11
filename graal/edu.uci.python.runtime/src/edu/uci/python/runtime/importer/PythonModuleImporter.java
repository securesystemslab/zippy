package edu.uci.python.runtime.importer;

import java.io.*;
import org.python.core.*;

import com.oracle.truffle.api.*;
import edu.uci.python.runtime.*;

public class PythonModuleImporter {

    private PythonContext context;

    public PythonModuleImporter(PythonContext context) {
        this.context = context;
    }

    public PythonParseResult findModule(String name, String moduleName) {
        String workingDir = System.getProperty("user.dir");
        String path = workingDir + "/" + "lib-python/3";
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
                System.out.println("[ZipPy] parsing module " + modName);
                PythonParseResult parsedModule = parseModule(displayDirName, sourceName);
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

    private PythonParseResult parseModule(String dirName, String sourceName) {
        Source source = context.getSourceManager().get(dirName + "/" + sourceName);
        PythonContext moduleContext = new PythonContext(context);
        PythonParseResult parseResult = context.getParser().parse(moduleContext, source, CompileMode.exec, CompilerFlags.getCompilerFlags());
        return parseResult;
    }

}
