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
package edu.uci.python.test;

import java.io.*;
import java.nio.file.*;

import static org.junit.Assert.*;
import edu.uci.python.builtins.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtypes.*;
import edu.uci.python.shell.*;

public class PythonTests {

    public static void assertPrints(String expected, String source) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        RunScript.runScript(new String[0], source, getContext(printStream, System.err));
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertEquals(expected, result);
    }

    public static void assertError(String expected, String source) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);
        String error = "no error!";

        try {
            RunScript.runScript(new String[0], source, getContext(System.out, printStream));
        } catch (Throwable err) {
            error = err.toString();
        }

        assertEquals(expected, error);
    }

    public static void assertPrints(String expected, Path scriptName) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        Path scriptFile;
        if (Files.isDirectory(Paths.get("graal/edu.uci.python.test/src/tests"))) {
            scriptFile = Paths.get("graal/edu.uci.python.test/src/tests").resolve(scriptName.toString());
        } else if (Files.isDirectory(Paths.get("../../graal/edu.uci.python.test/src/tests"))) {
            scriptFile = Paths.get("../../graal/edu.uci.python.test/src/tests").resolve(scriptName.toString());
        } else {
            throw new RuntimeException("Unable to locate edu.uci.python.test/src/tests/");
        }

        InputStream scriptStream;
        try {
            scriptStream = Files.newInputStream(scriptFile);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        RunScript.runScript(new String[0], scriptStream, getContext(printStream, System.err));
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertEquals(expected, result);
    }

    public static PythonContext getContext() {
        PythonOptions opts = new PythonOptions();
        PythonBuiltinsInitializer.initialize();
        return new PythonContext(opts, new PythonBuiltinsLookup());
    }

    public static PythonContext getContext(PrintStream stdout, PrintStream stderr) {
        PythonOptions opts = new PythonOptions();
        opts.setStandardOut(stdout);
        opts.setStandardErr(stderr);
        PythonBuiltinsInitializer.initialize();
        return new PythonContext(opts, new PythonBuiltinsLookup());
    }
}
