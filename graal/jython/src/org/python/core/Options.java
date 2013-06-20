// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

/**
 * A class with static fields for each of the settable options. The options from
 * registry and command line is copied into the fields here and the rest of
 * Jython checks these fields.
 */
public class Options {
    // Jython options. Some of these can be set from the command line
    // options, but all can be controlled through the Jython registry

    /**
     * when an exception occurs in Java code, and it is not caught, should the
     * interpreter print out the Java exception in the traceback?
     */
    public static boolean showJavaExceptions = false;

    /**
     * If true, exceptions raised from Python code will include a Java stack
     * trace in addition to the Python traceback. This can slow raising
     * considerably.
     */
    public static boolean includeJavaStackInExceptions = true;

    /**
     * When true, python exception raised in overridden methods will be shown on
     * stderr. This option is remarkably useful when python is used for
     * implementing CORBA server. Some CORBA servers will turn python exception
     * (say a NameError) into an anonymous user exception without any
     * stacktrace. Setting this option will show the stacktrace.
     */
    public static boolean showPythonProxyExceptions = false;

    /**
     * If true, Jython respects Java the accessibility flag for fields, methods,
     * and constructors. This means you can only access public members. Set this
     * to false to access all members by toggling the accessible flag on the
     * member.
     */
    public static boolean respectJavaAccessibility = true;

    /**
     * When false the <code>site.py</code> will not be imported. This is only
     * honored by the command line main class.
     */
    public static boolean importSite = true;

    /**
     * Set verbosity to Py.ERROR, Py.WARNING, Py.MESSAGE, Py.COMMENT, or
     * Py.DEBUG for varying levels of informative messages from Jython. Normally
     * this option is set from the command line.
     */
    public static int verbose = Py.MESSAGE;

    /**
     * A directory where the dynamically generated classes are written. Nothing
     * is ever read from here, it is only for debugging purposes.
     */
    public static String proxyDebugDirectory;

    /**
     * If true, Jython will use the first module found on sys.path where java
     * File.isFile() returns true. Setting this to true have no effect on
     * unix-type filesystems. On Windows/HFS+ systems setting it to true will
     * enable Jython-2.0 behaviour.
     */
    public static boolean caseok = false;

    /**
     * If true, enable truedivision for the '/' operator.
     */
    public static boolean Qnew = false;

    /**
     * Force stdin, stdout and stderr to be unbuffered, and opened in binary
     * mode
     */
    public static boolean unbuffered = false;

    /** Whether -3 (py3k warnings) was enabled via the command line. */
    public static boolean py3k_warning = false;

    /**
     * Whether -B (don't write bytecode on import) was enabled via the command
     * line.
     */
    public static boolean dont_write_bytecode = false;

    /** Whether -E (ignore environment) was enabled via the command line. */
    // XXX: place holder, not implemented yet.
    public static boolean ignore_environment = false;

    // XXX: place holder, not implemented yet.
    public static boolean no_user_site = false;

    // XXX: place holder, not implemented yet.
    public static boolean no_site = false;

    // XXX: place holder
    public static int bytes_warning = 0;

    // Corresponds to -O (Python bytecode optimization), -OO (remove docstrings)
    // flags in CPython; it's not clear how Jython should expose its
    // optimization,
    // but this is user visible as of 2.7.
    public static int optimize = 0;

    /**
     * Enable division warning. The value maps to the registry values of
     * <ul>
     * <li>old: 0</li>
     * <li>warn: 1</li>
     * <li>warnall: 2</li>
     * </ul>
     */
    public static int division_warning = 0;

    /*
     * maxine timer flags
     */
    public static boolean timeRun = false;
    public static boolean timeParse = false;
    public static boolean timeCompile = false;
    public static boolean timeInterpret = false;
    public static boolean timeThreadGen = false;

    public static long runTime = 0;
    public static long parseTime = 0;
    public static long compileTime = 0;
    public static long interpretTime = 0;
    public static long threadGenTime = 0;

    public static boolean subroutineInterpreter = false;
    public static boolean subroutineInterpreterWithUnifiedStackFrame = false;
    public static boolean directThreadedInterpreter = false;
    public static boolean bytecodeInterpreterWithIops = false;
    public static boolean directThreadedInterpreterWithOperandStackMapping = false;
    public static boolean printMetrics = false;
    public static boolean printThreadedCode = false;
    public static boolean interpretAST = false;
    public static boolean debug = false;
    public static boolean specialize = false;
    public static boolean optimizeAST = false;
    public static boolean visualizeAST = false;
    public static boolean optimizeNode = false;

    public enum TIMER {
        RUN, PARSE, COMPILE, INTERPRET, THREADGEN
    }

    //
    // ####### END OF OPTIONS
    //

    private Options() {
        ;
    }

    private static boolean getBooleanOption(String name, boolean defaultValue) {
        String prop = PySystemState.registry.getProperty("python." + name);
        if (prop == null) {
            return defaultValue;
        }
        return prop.equalsIgnoreCase("true") || prop.equalsIgnoreCase("yes");
    }

    private static String getStringOption(String name, String defaultValue) {
        String prop = PySystemState.registry.getProperty("python." + name);
        if (prop == null) {
            return defaultValue;
        }
        return prop;
    }

    /**
     * Initialize the static fields from the registry options.
     */
    public static void setFromRegistry() {
        // Set the more unusual options
        Options.showJavaExceptions = getBooleanOption(
                "options.showJavaExceptions", Options.showJavaExceptions);

        Options.includeJavaStackInExceptions = getBooleanOption(
                "options.includeJavaStackInExceptions",
                Options.includeJavaStackInExceptions);

        Options.showPythonProxyExceptions = getBooleanOption(
                "options.showPythonProxyExceptions",
                Options.showPythonProxyExceptions);

        Options.respectJavaAccessibility = getBooleanOption(
                "security.respectJavaAccessibility",
                Options.respectJavaAccessibility);

        Options.proxyDebugDirectory = getStringOption(
                "options.proxyDebugDirectory", Options.proxyDebugDirectory);

        // verbosity is more complicated:
        String prop = PySystemState.registry.getProperty("python.verbose");
        if (prop != null) {
            if (prop.equalsIgnoreCase("error")) {
                Options.verbose = Py.ERROR;
            } else if (prop.equalsIgnoreCase("warning")) {
                Options.verbose = Py.WARNING;
            } else if (prop.equalsIgnoreCase("message")) {
                Options.verbose = Py.MESSAGE;
            } else if (prop.equalsIgnoreCase("comment")) {
                Options.verbose = Py.COMMENT;
            } else if (prop.equalsIgnoreCase("debug")) {
                Options.verbose = Py.DEBUG;
            } else {
                throw Py.ValueError("Illegal verbose option setting: '" + prop
                        + "'");
            }
        }

        Options.caseok = getBooleanOption("options.caseok", Options.caseok);

        Options.Qnew = getBooleanOption("options.Qnew", Options.Qnew);

        prop = PySystemState.registry.getProperty("python.division_warning");
        if (prop != null) {
            if (prop.equalsIgnoreCase("old")) {
                Options.division_warning = 0;
            } else if (prop.equalsIgnoreCase("warn")) {
                Options.division_warning = 1;
            } else if (prop.equalsIgnoreCase("warnall")) {
                Options.division_warning = 2;
            } else {
                throw Py.ValueError("Illegal division_warning option "
                        + "setting: '" + prop + "'");
            }
        }
    }

    public static void initTimers(boolean timeRun, boolean timeParse,
            boolean timeCompile, boolean timeInterpret, boolean timeThreadGen) {
        Options.timeRun = timeRun;
        Options.timeParse = timeParse;
        Options.timeCompile = timeCompile;
        Options.timeInterpret = timeInterpret;
        Options.timeThreadGen = timeThreadGen;
    }

    public static long startTimer(boolean isTimerOn) {
        return isTimerOn ? System.nanoTime() : 0;
    }

    public static void stopTimer(boolean isTimerOn, long startTime,
            Options.TIMER timer) {
        if (!isTimerOn) {
            return;
        }

        // System.err.println("timer stopped " + timer + " " +
        // (System.nanoTime() - startTime));

        if (timer == TIMER.RUN) {
            runTime += System.nanoTime() - startTime;
        } else if (timer == TIMER.PARSE) {
            parseTime += System.nanoTime() - startTime;
        } else if (timer == TIMER.COMPILE) {
            compileTime += System.nanoTime() - startTime;
        } else if (timer == TIMER.INTERPRET) {
            interpretTime += System.nanoTime() - startTime;
        } else if (timer == TIMER.THREADGEN) {
            threadGenTime += System.nanoTime() - startTime;
        }
    }

    public static void printTimes() {
        if (timeParse) {
            System.err.println("> parse time");
            System.err.println(parseTime / 1000000);
        }

        if (timeCompile) {
            System.err.println("> compile time");
            System.err.println(compileTime / 1000000);
        }

        if (timeInterpret) {
            System.err.println("> interpret time");
            System.err.println(interpretTime / 1000000);
        }

        if (timeThreadGen) {
            System.err.println("> threadGen time");
            System.err.println(threadGenTime / 1000000);
        }

        if (timeRun) {
            // To make a fair comparison with bytecode interpreter parse time is
            // excluded from runtime
            System.err.println("> run time");
            System.err.println((runTime - parseTime) / 1000000);
        }
    }
}
