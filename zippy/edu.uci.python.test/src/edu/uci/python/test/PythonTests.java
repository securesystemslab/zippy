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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.MissingMIMETypeException;
import com.oracle.truffle.api.source.MissingNameException;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.Source.Builder;

import edu.uci.python.PythonLanguage;
import edu.uci.python.builtins.PythonDefaultBuiltinsLookup;
import edu.uci.python.parser.PythonParserImpl;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.PythonParseResult;
import edu.uci.python.runtime.ZippyEnvVars;
import edu.uci.python.shell.RunScript;
import edu.uci.python.shell.ZipPyConsole;

public class PythonTests {

    public static void assertPrintContains(String expected, String code) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        PythonContext context = getContext(printStream, System.err);
        Source source = getTestCode(code);
        RunScript.runScript(new String[0], source, context);
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertTrue(result.contains(expected));
    }

    public static PythonParseResult assertPrints(String expected, String code) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        PythonContext context = getContext(printStream, System.err);
        Source source = getTestCode(code);
        PythonParseResult parseResult = RunScript.runScript(new String[0], source, context);
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertEquals(expected, result);
        return parseResult;
    }

    public static String parseTest(String code) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        PythonContext context = getContext(printStream, System.err);
        Source source = getTestCode(code);
        new ZipPyConsole().parseFile(context, source);
        return byteArray.toString().replaceAll("\r\n", "\n");
    }

    public static PythonParseResult getParseResult(String code) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        PythonContext context = getContext(printStream, System.err);
        Source source = getTestCode(code);
        return new ZipPyConsole().parseFile(context, source);
    }

    public static void assertError(String expected, String code) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);
        String error = "no error!";

        try {
            PythonContext context = getContext(System.out, printStream);
            Source source = getTestCode(code);
            RunScript.runThrowableScript(new String[0], source, context);
        } catch (Throwable err) {
            error = err.toString();
        }

        assertEquals(expected, error);
    }

    public static void assertPrints(String expected, Path scriptName) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        Source source = getSource(getTestFile(scriptName));
        PythonContext context = getContext(printStream, System.err);
        RunScript.runScript(new String[0], source, context);
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertEquals(expected, result);
    }

    public static void assertPrints(Path expected, Path scriptName) {
        assertPrints(expected, scriptName, new String[0]);
    }

    public static void assertPrints(Path expected, Path scriptName, String[] args) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        File scriptFile = getTestFile(scriptName);
        Source source = getSource(scriptFile);
        String output = getFileContent(getTestFile(expected));
        PythonContext context = getContext(printStream, System.err);
        RunScript.runScript(args, source, scriptFile.getParent(), context);
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertEquals(output, result);
    }

    public static void assertNoError(Path scriptName) {
        final ByteArrayOutputStream byteArrayErr = new ByteArrayOutputStream();
        final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        final PrintStream printErrStream = new PrintStream(byteArrayErr);
        final PrintStream printOutStream = new PrintStream(byteArrayOut);

        Source source = getSource(getTestFile(scriptName));
        PythonContext context = getContext(printOutStream, printErrStream);
        RunScript.runScript(new String[0], source, context);
        String result = byteArrayErr.toString().replaceAll("\r\n", "\n");
        assertEquals("", result);
    }

    public static void assertBenchNoError(Path scriptName, String[] args) {
        final ByteArrayOutputStream byteArrayErr = new ByteArrayOutputStream();
        final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        final PrintStream printErrStream = new PrintStream(byteArrayErr);
        final PrintStream printOutStream = new PrintStream(byteArrayOut);

        Source source = getSource(getBenchFile(scriptName));
        PythonContext context = getContext(printOutStream, printErrStream);

        if (args == null)
            RunScript.runScript(new String[]{scriptName.toString()}, source, context);
        else
            RunScript.runScript(args, source, context);

        String err = byteArrayErr.toString().replaceAll("\r\n", "\n");
        String result = byteArrayOut.toString().replaceAll("\r\n", "\n");
        System.out.println(source.getName() + "\n" + result + "\n");
        assertEquals("", err);
        assertNotEquals("", result);
    }

    public static PythonParseResult assertPrintContains(String expected, Path scriptName) {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArray);

        Source source = getSource(getTestFile(scriptName));
        PythonContext context = getContext(printStream, System.err);
        PythonParseResult ast = RunScript.runScript(new String[0], source, context);
        String result = byteArray.toString().replaceAll("\r\n", "\n");
        assertTrue(result.contains(expected));
        return ast;
    }

    private static Source getTestCode(String code) {
        Builder<RuntimeException, MissingMIMETypeException, MissingNameException> builder = Source.newBuilder(code);
        builder.name("(test)");
        builder.mimeType(PythonLanguage.MIME_TYPE);
        Source source = null;
        try {
            source = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }

    public static File getTestFile(Path filename) {
        Path path = Paths.get(ZippyEnvVars.zippyHome(), "zippy", "edu.uci.python.test", "src", "tests", filename.toString());
        if (Files.isReadable(path)) {
            return new File(path.toString());
        } else
            throw new RuntimeException("Unable to locate " + path);
    }

    public static File getBenchFile(Path filename) {
        Path path = Paths.get(ZippyEnvVars.zippyHome(), "zippy", "benchmarks", "src");
        if (!Files.isDirectory(path))
            throw new RuntimeException("Unable to locate benchmarks/src/");

        Path fullPath = Paths.get(path.toString(), filename.toString());
        if (!Files.isReadable(fullPath)) {
            fullPath = Paths.get(path.toString(), "benchmarks", filename.toString());
            if (!Files.isReadable(fullPath)) {
                fullPath = Paths.get(path.toString(), "micro", filename.toString());
                if (!Files.isReadable(fullPath))
                    throw new IllegalStateException("Unable to locate " + path + " (benchmarks or micro) " + filename.toString());
            }
        }

        File file = new File(fullPath.toString());
        return file;
    }

    private static Source getSource(File file) {
        Source source = null;

        try {
            Builder<IOException, RuntimeException, RuntimeException> builder = Source.newBuilder(file);
            builder.mimeType(PythonLanguage.MIME_TYPE);
            source = builder.build();
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        return source;
    }

    private static String getFileContent(File file) {
        String ret = null;
        Reader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            final BufferedReader bufferedReader = new BufferedReader(reader);
            final StringBuilder content = new StringBuilder();
            final char[] buffer = new char[1024];

            try {
                int n = 0;
                while (n != -1) {
                    n = bufferedReader.read(buffer);
                    if (n != -1)
                        content.append(buffer, 0, n);
                }
            } finally {
                bufferedReader.close();
            }
            ret = content.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static PythonContext getContext() {
        PythonOptions opts = new PythonOptions();
        PythonContext context = new PythonContext(opts, new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
        return context;
    }

    public static PythonContext getContext(PrintStream stdout, PrintStream stderr) {
        PythonOptions opts = new PythonOptions();
        opts.setStandardOut(stdout);
        opts.setStandardErr(stderr);
        PythonContext context = new PythonContext(opts, new PythonDefaultBuiltinsLookup(), new PythonParserImpl());
        return context;
    }

    public static VirtualFrame createVirtualFrame() {
        return Truffle.getRuntime().createVirtualFrame(null, new FrameDescriptor());
    }

}
