
"""
Generate test classes for benchmarks
"""

import subprocess
from pymarksparams import *


header="""/*
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
package edu.uci.python.test.benchmarks;

import static edu.uci.python.test.PythonTests.*;

import java.nio.file.*;

import org.junit.*;

"""

pathTestBench='../edu.uci.python.test/src/edu/uci/python/test/benchmarks/'
pathBench='edu.uci.python.benchmark//src//benchmarks//'

testClassNamep1 = 'public class '
testClassNamep2 = ' {\n'
decorationTest= '    @Test\n'
functionNamep1= '    public void '
functionNamep2= '() {\n'
funcBodyp1    = '        Path script = Paths.get("'
funcBodyp2    = '");\n'
funcBodyp3    = '        assertBenchNoError('
funcBodyp4    = 'script, '
funcBodyp5    = ');\n'
funcBodyp6    = '        }\n\n'
testClassNameEnd = '}\n\n'


def generateTest(c, d):
    className = c.replace("python","")
    comments = '/*\n'
    test = ''
    test += testClassNamep1 + className + testClassNamep2
    for key in d.keys():
        # testFile = path+key+'.py'
        test += decorationTest + functionNamep1
        test += key.replace('-','_') + functionNamep2
        test += funcBodyp1 + key+'.py' + funcBodyp2
        test += funcBodyp3
        comments += key+'.py ' + d[key] +'\n'
        # p = subprocess.Popen(['python3.4',testFile, d[key]], stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        # out, err = p.communicate()
        # test += out.decode("utf-8")
        test += funcBodyp4
        test += '"' + d[key] + '"'
        test += funcBodyp5
        test += funcBodyp6

    test += testClassNameEnd
    comments += '*/\n'
    text_file = open(pathTestBench+className+".java", "w")
    text_file.write(header)
    text_file.write(comments)
    text_file.write(test)
    text_file.close()
    print(header)
    print(comments)
    print(test)




generateTest('pythonTestBenchmarks', pythonTestBenchmarks)
# generateTest('python2MicroBenchmarks', python2MicroBenchmarks)
generateTest('pythonMicroBenchmarks', pythonMicroBenchmarks)
generateTest('pythonBenchmarks', pythonBenchmarks)
generateTest('pythonGeneratorBenchmarks', pythonGeneratorBenchmarks)
generateTest('pythonObjectBenchmarks', pythonObjectBenchmarks)
