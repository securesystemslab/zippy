/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/* Execute testcases by reading names from a given file, due to limits of
 * the operating system regarding command line size (windows: 32k,
 * linux [depending on the settings]: ~2097k)
 * see http://msdn.microsoft.com/en-us/library/ms682425%28VS.85%29.aspx
 */

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.selectors.*;
import org.junit.runner.*;

import java.io.*;
import java.util.*;

public class JUnitWrapper {

    /**
     * @param args args[0] is the path where to read the names of the testclasses.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.printf("wrong usage. provide a filename\n");
            System.exit(1);
        }
        ArrayList<String> tests = new ArrayList<String>(1000);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(args[0]));

            String buf;
            while ((buf = br.readLine()) != null) {
                tests.add(buf);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(2);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(3);
            }
        }

        String[] strargs = tests.toArray(new String[tests.size()]);
        if (strargs.length == 1) {
            System.out.printf("executing junit test now... (%s)\n", strargs[0]);
        } else {
            System.out.printf("executing junit tests now... (%d test classes)\n", strargs.length);
        }

        JUnitTask task = new JUnitTask();
        task.setCloneVm(true);
        task.setProject(new Project());
        task.getProject().setSystemProperties();

        task.getProject().setBasedir(".");
        TypeAttribute ta = new TypeAttribute();
        ta.setValue("xml");
        FormatterElement fe = new FormatterElement();
        fe.setType(ta);
        task.addFormatter(fe);

        File reportDir = new File("report");
        reportDir.mkdirs();
        File xmldir = new File(reportDir, "xml");
        xmldir.mkdirs();
        Set<String> ignore = new HashSet<>();
        ignore.add("com.oracle.truffle.api.dsl.test.BinaryNodeTest");
        ignore.add("com.oracle.truffle.api.dsl.test.ExecuteEvaluatedTest");
        ignore.add("com.oracle.truffle.api.test.FrameTest");
        ignore.add("com.oracle.truffle.api.dsl.test.UnsupportedSpecializationTest");
        ignore.add("com.oracle.truffle.sl.test.SLSimpleTestSuite");
        ignore.add("com.oracle.graal.compiler.test.ea.UnsafeEATest");
        ignore.add("com.oracle.graal.hotspot.test.HotSpotNmethodTest");
        ignore.add("com.oracle.graal.hotspot.test.WriteBarrierAdditionTest");
        ignore.add("com.oracle.graal.hotspot.test.CompressedOopTest");
        ignore.add("com.oracle.graal.compiler.test.deopt.MonitorDeoptTest"); // Probably CString problem
        for (String name : tests) {
            JUnitTest t = new JUnitTest();
            t.setName(name);
            t.setTodir(xmldir);
            if (new File(xmldir, "TEST-" + name + ".xml").exists() || ignore.contains(name) 
		) {
                System.out.println("Ignoring testclass " + name);
                t.setIf("run.all");
            }
            t.setFork(false);
            t.setHaltonerror(false);
            t.setHaltonfailure(false);
            task.addTest(t);
        }
        SummaryAttribute sa = new SummaryAttribute();
        sa.setValue("withOutAndErr");
        task.setPrintsummary(sa);
        task.setFork(false);
        task.setShowOutput(true);
        task.setOutputToFormatters(true);
	task.setHaltonerror(false);

        task.execute();
        XMLResultAggregator report = new XMLResultAggregator();
        report.setProject(task.getProject());
        report.setTofile(new File(reportDir, "unittest-report-merged.xml").getPath());
        FileSet resultFileSet = new FileSet();
        resultFileSet.setDir(xmldir);
        resultFileSet.setIncludes("*");
        report.addFileSet(resultFileSet);
        report.execute();
        AggregateTransformer at = report.createReport();
        File htmlDir = new File(reportDir, "html");
        htmlDir.mkdirs();
        at.setTodir(htmlDir);
        at.transform();
    }
}
