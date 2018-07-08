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
package edu.uci.python.shell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.util.InteractiveConsole;
import org.python.util.JLineConsole;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.source.Source;

import edu.uci.python.PythonLanguage;
import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.nodes.ModuleNode;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;
import edu.uci.python.runtime.function.PArguments;
import edu.uci.python.runtime.standardtype.PythonModule;

public class ZipPyConsole extends InteractiveConsole {

    /**
     * - TODO: deprecation
     */
    @SuppressWarnings("deprecation")
    public void execfile(Source source, InputStream in, OutputStream out, OutputStream err) {
        com.oracle.truffle.api.vm.PolyglotEngine.Builder builder = com.oracle.truffle.api.vm.PolyglotEngine.newBuilder();
        if (in != null)
            builder.setIn(in);

        if (out != null)
            builder.setOut(out);

        if (err != null)
            builder.setErr(err);

        com.oracle.truffle.api.vm.PolyglotEngine engine = builder.build();
        engine.eval(source);
        engine.dispose();
    }

    public void execfile(Source source) {
        execfile(source, null, null, null);
    }

    @Override
    public void execfile(java.io.InputStream s, String name) {
        try {
            execfile(Source.newBuilder(new File(name)).mimeType(PythonLanguage.MIME_TYPE).build(), null, null, null);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns a new python interpreter using the InteractiveConsole subclass from the
     * <tt>python.console</tt> registry key.
     * <p>
     *
     * When stdin is interactive the default is {@link JLineConsole}. Otherwise the featureless
     * {@link InteractiveConsole} is always used as alternative consoles cause unexpected behavior
     * with the std file streams.
     */
    public static InteractiveConsole newInterpreter(String[] args, String workingDir, boolean interactiveStdin) {
        PySystemState systemState = createPySystemState(args);
        InteractiveConsole console = null;
        if (!interactiveStdin) {
            console = new InteractiveConsole();
        } else {
            String interpClass = PySystemState.registry.getProperty("python.console", "");
            if (interpClass.length() > 0) {
                try {
                    console = (InteractiveConsole) Class.forName(interpClass).newInstance();
                } catch (Throwable t) {
                    // fall through
                }
            }
            console = (console == null) ? new ZipPyConsole() : console;
        }

        setPySystemState(console, systemState, workingDir);
        return console;
    }

    public void init(String[] args, String workingDir) {
        setPySystemState(this, createPySystemState(args), workingDir);
    }

    /**
     *****************************
     * Only for testing purposes *
     *****************************
     */

    public static PythonParseResult testAndRunZipPyAST(String[] args, Source source, PrintStream out, PrintStream err) {
        ZipPyConsole interp = new ZipPyConsole();
        interp.init(args, null);
        PythonParseResult ast = testZipPyAST(source, out, err);
        ModuleNode root = (ModuleNode) ast.getModuleRoot();
        RootCallTarget moduleCallTarget = Truffle.getRuntime().createCallTarget(root);
        moduleCallTarget.call(PArguments.empty());
        return ast;
    }

    public static PythonParseResult testZipPyAST(Source source, PrintStream out, PrintStream err) {
        PythonOptions opts = new PythonOptions();
        opts.setStandardOut(out);
        opts.setStandardErr(err);
        PythonContext context = new PythonContext(null, opts, new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
        PythonModule module = context.createMainModule(source.getPath());
        return context.getParser().parse(context, module, source);
    }

    /**
     ***************************************************
     * Jython front-end parser preparation and cleanup *
     ***************************************************
     */

    private static PyList passArgs(String[] args) {
        PyList argv = new PyList();
        if (args != null) {
            for (String arg : args) {
                argv.append(Py.newStringOrUnicode(arg));
            }
        }
        return argv;
    }

    private static PySystemState createPySystemState(String[] args) {

        // Setup the basic python system state from these options
        PySystemState systemState = null;
        if (args.length > 0) {
            Py.setSystemState(new PySystemState()).argv = passArgs(args);
            Py.getSystemState().argv = passArgs(args);
            systemState = Py.getSystemState();
        } else {
            PySystemState.initialize(PySystemState.getBaseProperties(), PySystemState.getBaseProperties(), args);
            systemState = Py.getSystemState();
        }

        return systemState;
    }

    private static void setPySystemState(InteractiveConsole console, PySystemState systemState, String workingDir) {

        systemState.__setattr__("_jy_interpreter", Py.java2py(console));

        try {
            // Modify verion info in Jython runtime
            Field field = PySystemState.class.getField("version_info");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, new PyTuple(Py.newInteger(3), Py.newInteger(5), Py.newInteger(0), Py.newString("zippy"), Py.newInteger(0)));
            modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        } catch (Exception e) {
            // pass through
        }

        String workingPath = System.getProperty("user.dir");
        systemState.path.insert(0, new PyString(workingPath));
        systemState.setCurrentWorkingDir(workingPath);
        Py.getSystemState().path.insert(0, new PyString(workingPath));
        Py.getSystemState().setCurrentWorkingDir(workingPath);
        if (workingDir != null) {
            workingPath = workingDir;
            PyString path = new PyString(workingPath);
            systemState.path.insert(0, path);
            systemState.setCurrentWorkingDir(workingPath);
            Py.getSystemState().path.insert(0, path);
            Py.getSystemState().setCurrentWorkingDir(workingPath);
        }
    }

    public static void dispose(InteractiveConsole console, Throwable t, boolean exit) {
        if (t != null) {
            if (t instanceof PyException && ((PyException) t).match(org.python.modules._systemrestart.SystemRestart)) {
                /**
                 * Run any finalizations on the current interpreter in preparation for a
                 * SytemRestart.
                 */
                // Stop all the active threads and signal the SystemRestart
                org.python.modules.thread.thread.interruptAllThreads();
                Py.getSystemState()._systemRestart = true;
                // Close all sockets -- not all of their operations are stopped by
                // Thread.interrupt (in particular pre-nio sockets)
                try {
                    org.python.core.imp.load("socket").__findattr__("_closeActiveSockets").__call__();
                } catch (PyException pye) {
                    // continue
                }
                console.cleanup();
                // ..reset the state...
                Py.setSystemState(new PySystemState());
                // ...and start again
            } else {
                Py.printException(t);
                console.cleanup();
                if (exit) {
                    System.exit(-1);
                }
            }
        }
    }

}
