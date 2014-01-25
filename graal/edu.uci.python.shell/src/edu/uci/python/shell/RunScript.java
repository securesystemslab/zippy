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

import java.io.*;

import org.python.core.*;
import org.python.core.imp;
import org.python.core.util.*;
import org.python.modules.*;
import org.python.modules.posix.*;
import org.python.modules.thread.*;
import org.python.util.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.*;

public class RunScript {

    public static void main(String[] args) {
        if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
            Shell.usage();
        } else {
            String scriptName = args[0];

            // Setup the basic python system state from these options
            PySystemState.initialize(PySystemState.getBaseProperties(), PySystemState.getBaseProperties(), args);
            PySystemState systemState = Py.getSystemState();

            // Now create an interpreter
            InteractiveConsole interp = newInterpreter(true);
            systemState.__setattr__("_jy_interpreter", Py.java2py(interp));

            // was there a filename on the command line?
            if (scriptName != null) {
                String path;
                try {
                    path = new File(scriptName).getCanonicalFile().getParent();
                } catch (IOException ioe) {
                    path = new File(scriptName).getAbsoluteFile().getParent();
                }
                if (path == null) {
                    path = "";
                }

                try {

                    Py.getSystemState().path.insert(0, new PyString(path));
                    FileInputStream file;

                    try {
                        file = new FileInputStream(new RelativeFile(scriptName));
                    } catch (FileNotFoundException e) {
                        throw Py.IOError(e);
                    }

                    try {
                        if (PosixModule.getPOSIX().isatty(file.getFD())) {
                            interp.interact(null, new PyFile(file));
                            return;
                        } else {
                            interp.execfile(file, scriptName);
                        }
                    } finally {
                        file.close();
                    }
                } catch (Throwable t) {
                    if (t instanceof PyException && ((PyException) t).match(_systemrestart.SystemRestart)) {
                        // Shutdown this instance...
                        shutdownInterpreter();
                        interp.cleanup();
                        // ..reset the state...
                        Py.setSystemState(new PySystemState());
                        // ...and start again
                        return;
                    } else {
                        Py.printException(t);
                        interp.cleanup();
                        System.exit(-1);
                    }
                }
            }
        }
    }

    public static void runTrowableScript(String[] args, Source source, PythonContext context) {
        // Setup the basic python system state from these options
        PySystemState.initialize(PySystemState.getBaseProperties(), PySystemState.getBaseProperties(), args);
        PySystemState systemState = Py.getSystemState();

        // Now create an interpreter
        CustomConsole interp = new CustomConsole();
        systemState.__setattr__("_jy_interpreter", Py.java2py(interp));

        if (source != null) {
            Py.getSystemState().path.insert(0, new PyString(System.getProperty("user.dir")));
            interp.execfile(context, source);
        }
    }

    public static void runScript(String[] args, Source source, PythonContext context) {
        // Setup the basic python system state from these options
        PySystemState.initialize(PySystemState.getBaseProperties(), PySystemState.getBaseProperties(), args);
        PySystemState systemState = Py.getSystemState();

        // Now create an interpreter
        CustomConsole interp = new CustomConsole();
        systemState.__setattr__("_jy_interpreter", Py.java2py(interp));

        if (source != null) {
            try {
                PyString path = new PyString(System.getProperty("user.dir"));
                Py.getSystemState().path.insert(0, path);

                interp.execfile(context, source);
            } catch (Throwable t) {
                if (t instanceof PyException && ((PyException) t).match(_systemrestart.SystemRestart)) {
                    // Shutdown this instance...
                    shutdownInterpreter();
                    interp.cleanup();
                    // ..reset the state...
                    Py.setSystemState(new PySystemState());
                    // ...and start again
                    return;
                } else {
                    Py.printException(t);
                    interp.cleanup();
                }
            }
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
    private static InteractiveConsole newInterpreter(boolean interactiveStdin) {
        if (!interactiveStdin) {
            return new InteractiveConsole();
        }

        String interpClass = PySystemState.registry.getProperty("python.console", "");
        if (interpClass.length() > 0) {
            try {
                return (InteractiveConsole) Class.forName(interpClass).newInstance();
            } catch (Throwable t) {
                // fall through
            }
        }
        return new CustomConsole();
    }

    /**
     * Run any finalizations on the current interpreter in preparation for a SytemRestart.
     */
    public static void shutdownInterpreter() {
        // Stop all the active threads and signal the SystemRestart
        thread.interruptAllThreads();
        Py.getSystemState()._systemRestart = true;
        // Close all sockets -- not all of their operations are stopped by
        // Thread.interrupt (in particular pre-nio sockets)
        try {
            imp.load("socket").__findattr__("_closeActiveSockets").__call__();
        } catch (PyException pye) {
            // continue
        }
    }

}
