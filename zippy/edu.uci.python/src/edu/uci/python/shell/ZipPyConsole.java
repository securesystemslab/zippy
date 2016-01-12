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
import org.python.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;

import edu.uci.python.builtins.*;
import edu.uci.python.nodes.*;
import edu.uci.python.parser.*;
//import edu.uci.python.profiler.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public class ZipPyConsole extends InteractiveConsole {

    @Override
    public void execfile(java.io.InputStream s, String name) {
        PythonParser parser = new PythonParserImpl();
        PythonContext context = null;

        try {
            Source source = Source.fromFileName(name);
            context = new PythonContext(new PythonOptions(), new PythonDefaultBuiltinsLookup(), parser);
            execfile(context, source);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public PythonParseResult execfile(PythonContext context, Source source) {
        setSystemState();

        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("Before Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        ModuleNode root = (ModuleNode) result.getModuleRoot();
        RootCallTarget moduleCallTarget = Truffle.getRuntime().createCallTarget(root);

        /**
         * Profiler translation, i.e. generating wrapper nodes happens after creating call target
         * because createCallTarget adopts all children, i.e. adds all parent relationships. In
         * order to be able create wrapper nodes, and replace nodes with wrapper nodes, we need
         * child parent relationship
         */

// ProfilerTranslator profilerTranslator = null;

        if (PythonOptions.ProfileCalls || PythonOptions.ProfileControlFlow || PythonOptions.ProfileVariableAccesses || PythonOptions.ProfileOperations || PythonOptions.ProfileCollectionOperations) {
// profilerTranslator = new ProfilerTranslator(result, result.getContext());
//
// profilerTranslator.translate();

            if (PythonOptions.PrintAST) {
                // CheckStyle: stop system..print check
                System.out.println("============= " + "After Adding Wrapper Nodes" + " ============= ");
                result.printAST();
                // CheckStyle: resume system..print check
            }
        }

        moduleCallTarget.call(PArguments.empty());

        if (PythonOptions.PrintAST) {
            printBanner("After Specialization");
            result.printAST();
        }

        if (PythonOptions.VisualizedAST) {
            result.visualizeToNetwork();
        }

        if (PythonOptions.InstrumentObjectStorageAllocation) {
            PythonObjectAllocationInstrumentor.getInstance().printAllocations();
        }

        if (PythonOptions.ProfileCalls) {
// profilerTranslator.getProfilerResultPrinter().printCallProfilerResults();
        }

        if (PythonOptions.ProfileControlFlow) {
// profilerTranslator.getProfilerResultPrinter().printControlFlowProfilerResults();
        }

        if (PythonOptions.ProfileVariableAccesses) {
// profilerTranslator.getProfilerResultPrinter().printVariableAccessProfilerResults();
        }

        if (PythonOptions.ProfileOperations) {
// profilerTranslator.getProfilerResultPrinter().printOperationProfilerResults();
        }

        if (PythonOptions.ProfileCollectionOperations) {
// profilerTranslator.getProfilerResultPrinter().printCollectionOperationsProfilerResults();
        }

        if (PythonOptions.TraceNodesWithoutSourceSection) {
// profilerTranslator.getProfilerResultPrinter().printNodesEmptySourceSections();
        }

        if (PythonOptions.TraceNodesUsingExistingProbe) {
// profilerTranslator.getProfilerResultPrinter().printNodesUsingExistingProbes();
        }

        Py.flushLine();
        return result;
    }

    public PythonParseResult parseFile(PythonContext context, Source source) {
        PythonModule module = context.createMainModule(source.getPath());
        PythonParseResult result = context.getParser().parse(context, module, source);

        if (PythonOptions.PrintAST) {
            printBanner("After Parsing");
            result.printAST();
        }

        return result;
    }

    public static void printBanner(String phase) {
        // CheckStyle: stop system..print check
        System.out.println("============= " + phase + " ============= ");
        // CheckStyle: resume system..print check
    }
}
