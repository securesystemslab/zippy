package org.python.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import org.python.Version;
import org.python.core.CodeFlag;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFile;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.imp;
import org.python.core.util.RelativeFile;
import org.python.modules._systemrestart;
import org.python.modules.posix.PosixModule;

public class Zippy extends jython {
	
    public static void run(String[] args) {
        // Parse the command line options
        CommandLineOptions opts = new CommandLineOptions();
        if (!opts.parse(args)) {
            if (opts.version) {
                System.err.println("Jython " + Version.PY_VERSION);
                System.exit(0);
            }
            if (opts.help) {
                System.err.println(usage);
            } else if (!opts.runCommand && !opts.runModule) {
                System.err.print(usageHeader);
                System.err.println("Try `jython -h' for more information.");
            }

            int exitcode = opts.help ? 0 : -1;
            System.exit(exitcode);
        }

        // Setup the basic python system state from these options
        PySystemState.initialize(PySystemState.getBaseProperties(),
                opts.properties, opts.argv);
        PySystemState systemState = Py.getSystemState();

        // Decide if stdin is interactive
        if (!opts.fixInteractive || opts.interactive) {
            opts.interactive = ((PyFile) Py.defaultSystemState.stdin).isatty();
            if (!opts.interactive) {
                systemState.ps1 = systemState.ps2 = Py.EmptyString;
            }
        }

        // Now create an interpreter
        InteractiveConsole interp = newInterpreter(opts.interactive);
        systemState.__setattr__("_jy_interpreter", Py.java2py(interp));

        // Print banner and copyright information (or not)
        if (opts.interactive && opts.notice && !opts.runModule) {
            System.err.println(InteractiveConsole.getDefaultBanner());
        }

        if (Options.importSite) {
            try {
                imp.load("site");
                if (opts.interactive && opts.notice && !opts.runModule) {
                    System.err.println(COPYRIGHT);
                }
            } catch (PyException pye) {
                if (!pye.match(Py.ImportError)) {
                    System.err.println("error importing site");
                    Py.printException(pye);
                    System.exit(-1);
                }
            }
        }

        if (opts.division != null) {
            if ("old".equals(opts.division)) {
                Options.division_warning = 0;
            } else if ("warn".equals(opts.division)) {
                Options.division_warning = 1;
            } else if ("warnall".equals(opts.division)) {
                Options.division_warning = 2;
            } else if ("new".equals(opts.division)) {
                Options.Qnew = true;
                interp.cflags.setFlag(CodeFlag.CO_FUTURE_DIVISION);
            }
        }

        Options.subroutineInterpreter = opts.subroutineInterpreter;
        Options.subroutineInterpreterWithUnifiedStackFrame = opts.subroutineInterpreterWithUnifiedStackframe;
        Options.directThreadedInterpreter = opts.directThreadedInterpreter;
        Options.bytecodeInterpreterWithIops = opts.bytecodeInterpreterWithIops;
        Options.directThreadedInterpreterWithOperandStackMapping = opts.directThreadedInterpreterWithOperandStackMapping;
        Options.printMetrics = opts.printMetrics;
        Options.printThreadedCode = opts.printThreadedCode;
        Options.visualizeAST = opts.visualizeAST;
        Options.interpretAST = opts.interpretAST;
        Options.debug = opts.debug;
        Options.specialize = opts.specialize;
        Options.optimizeAST = opts.optimizeAST;
        Options.optimizeNode = opts.optimizeNode;

        /*
         * maxine time stuff
         */
        Options.initTimers(opts.timeRun, opts.timeParse, opts.timeCompile,
                opts.timeInterpret, opts.timeThreadGen);

        // was there a filename on the command line?
        if (opts.filename != null) {
            String path;
            try {
                path = new File(opts.filename).getCanonicalFile().getParent();
            } catch (IOException ioe) {
                path = new File(opts.filename).getAbsoluteFile().getParent();
            }
            if (path == null) {
                path = "";
            }
            Py.getSystemState().path.insert(0, new PyString(path));
            if (opts.jar) {
                try {
                    runJar(opts.filename);
                } catch (Throwable t) {
                    Py.printException(t);
                    System.exit(-1);
                }
            } else if (opts.filename.equals("-")) {
                try {
                    interp.globals.__setitem__(new PyString("__file__"),
                            new PyString("<stdin>"));
                    interp.execfile(System.in, "<stdin>", opts.timeRun);
                } catch (Throwable t) {
                    Py.printException(t);
                }
            } else {
                try {
                    interp.globals.__setitem__(new PyString("__file__"),
                            new PyString(opts.filename));

                    FileInputStream file;
                    try {
                        file = new FileInputStream(new RelativeFile(
                                opts.filename));
                    } catch (FileNotFoundException e) {
                        throw Py.IOError(e);
                    }
                    try {
                        if (PosixModule.getPOSIX().isatty(file.getFD())) {
                            opts.interactive = true;
                            interp.interact(null, new PyFile(file));
                            return;
                        } else {
                            interp.execfile(file, opts.filename, opts.timeRun);
                        }
                    } finally {
                        file.close();
                    }
                } catch (Throwable t) {
                    if (t instanceof PyException
                            && ((PyException) t)
                                    .match(_systemrestart.SystemRestart)) {
                        // Shutdown this instance...
                        shouldRestart = true;
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
        } else {
            // if there was no file name on the command line, then "" is the
            // first element
            // on sys.path. This is here because if there /was/ a filename on
            // the c.l.,
            // and say the -i option was given, sys.path[0] will have gotten
            // filled in
            // with the dir of the argument filename.
            Py.getSystemState().path.insert(0, Py.EmptyString);

            if (opts.command != null) {
                try {
                    interp.exec(opts.command);
                } catch (Throwable t) {
                    Py.printException(t);
                    System.exit(1);
                }
            }

            if (opts.moduleName != null) {
                // PEP 338 - Execute module as a script
                try {
                    interp.exec("import runpy");
                    interp.set("name", Py.newString(opts.moduleName));
                    interp.exec("runpy.run_module(name, run_name='__main__', alter_sys=True)");
                    interp.cleanup();
                    return;
                } catch (Throwable t) {
                    Py.printException(t);
                    interp.cleanup();
                    System.exit(-1);
                }
            }
        }

        if (opts.fixInteractive
                || (opts.filename == null && opts.command == null)) {
            if (opts.encoding == null) {
                opts.encoding = PySystemState.registry
                        .getProperty("python.console.encoding");
            }
            if (opts.encoding != null) {
                if (!Charset.isSupported(opts.encoding)) {
                    System.err
                            .println(opts.encoding
                                    + " is not a supported encoding on this JVM, so it can't "
                                    + "be used in python.console.encoding.");
                    System.exit(1);
                }
                interp.cflags.encoding = opts.encoding;
            }
            try {
                interp.interact(null, null);
            } catch (Throwable t) {
                Py.printException(t);
            }
        }
        interp.cleanup();
    }
	
    /**
     * Returns a new python interpreter using the InteractiveConsole subclass
     * from the <tt>python.console</tt> registry key.
     * <p>
     * 
     * When stdin is interactive the default is {@link JLineConsole}. Otherwise
     * the featureless {@link InteractiveConsole} is always used as alternative
     * consoles cause unexpected behavior with the std file streams.
     */
    private static InteractiveConsole newInterpreter(boolean interactiveStdin) {
        if (!interactiveStdin) {
            return new ZippyInteractiveConsole();
        }

        String interpClass = PySystemState.registry.getProperty(
                "python.console", "");
        if (interpClass.length() > 0) {
            try {
                return (InteractiveConsole) Class.forName(interpClass)
                        .newInstance();
            } catch (Throwable t) {
                // fall through
            }
        }
        return new JLineConsole();
    }
    
}
