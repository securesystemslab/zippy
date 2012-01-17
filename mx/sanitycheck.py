# ----------------------------------------------------------------------------------------------------
#
# Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
# ----------------------------------------------------------------------------------------------------

from outputparser import OutputParser, Matcher
import re
import mx
import os
import commands
from os.path import isfile, join, exists

dacapoSanityWarmup = {
    'avrora':     [0, 0,  3,  6, 13],
    'batik':      [0, 0,  5,  5, 20],
    'eclipse':    [2, 4,  5, 10, 16],
    'fop':        [4, 8, 10, 20, 30],
    'h2':         [0, 0,  5,  5,  8],
    'jython':     [0, 0,  5, 10, 13],
    'luindex':    [0, 0,  5, 10, 10],
    'lusearch':   [0, 4,  5,  5,  8],
    'pmd':        [0, 0,  5, 10, 13],
    'sunflow':    [0, 0,  5, 10, 15],
    'tomcat':     [0, 0,  5, 10, 15],
    'tradebeans': [0, 0,  5, 10, 13],
    'tradesoap':  [2, 4,  5, 10, 15],
    'xalan':      [0, 0,  5, 10, 18],
}

dacapoGateBuildLevels = {
    'avrora':     ['product', 'fastdebug', 'debug'],
    'batik':      ['product', 'fastdebug', 'debug'],
    'eclipse':    ['product'],
    'fop':        [           'fastdebug', 'debug'],
    'h2':         ['product', 'fastdebug', 'debug'],
    'jython':     ['product', 'fastdebug', 'debug'],
    'luindex':    ['product', 'fastdebug', 'debug'],
    'lusearch':   ['product'],
    'pmd':        ['product', 'fastdebug', 'debug'],
    'sunflow':    ['product', 'fastdebug', 'debug'],
    'tomcat':     ['product', 'fastdebug', 'debug'],
    'tradebeans': ['product', 'fastdebug', 'debug'],
    'tradesoap':  ['product'],
    'xalan':      ['product', 'fastdebug', 'debug'],
}

class SanityCheckLevel:
    Fast, Gate, Normal, Extensive, Benchmark = range(5)
    
def getSPECjvm2008(skipKitValidation=False, warmupTime=None, iterationTime=None):
    
    specjvm2008 = mx.get_env('SPECJVM2008')
    if specjvm2008 is None or not exists(join(specjvm2008, 'SPECjvm2008.jar')):
        mx.abort('Please set the SPECJVM2008 environment variable to a SPECjvm2008 directory')
    
    score = re.compile(r"^(Score on|Noncompliant) (?P<benchmark>[a-zA-Z0-9\.\-]+)( result)?: (?P<score>[0-9]+(,|\.)[0-9]+)( SPECjvm2008 Base)? ops/m$")
    error = re.compile(r"^Errors in benchmark: ")
    # The ' ops/m' at the end of the success string is important : it's how you can tell valid and invalid runs apart
    success = re.compile(r"^(Noncompliant c|C)omposite result: [0-9]+,[0-9]+( SPECjvm2008 (Base|Peak))? ops/m$")
    matcher = Matcher(score, {'const:name' : 'benchmark', 'const:score' : 'score'}, startNewLine=True)
    
    opts = []
    if warmupTime is not None:
        opts += ['-wt', str(warmupTime)]
    if iterationTime is not None:
        opts += ['-it', str(iterationTime)]
    if skipKitValidation:
        opts += ['-ikv']
    
    return Test("SPECjvm2008", "SPECjvm2008", ['-jar', 'SPECjvm2008.jar'] + opts, [success], [error], [matcher], vmOpts=['-Xms3g'], defaultCwd=specjvm2008)

def getDacapos(level=SanityCheckLevel.Normal, gateBuildLevel=None, dacapoArgs=[]):
    checks = []
    
    for (bench, ns) in dacapoSanityWarmup.items():
        if ns[level] > 0:
            if gateBuildLevel is None or gateBuildLevel in dacapoGateBuildLevels[bench]:
                checks.append(getDacapo(bench, ns[level], dacapoArgs))
    
    return checks

def getDacapo(name, n, dacapoArgs=[]):
    dacapo = mx.get_env('DACAPO_CP')
    if dacapo is None:
        l = mx.library('DACAPO', False)
        if l is not None:
            dacapo = l.get_path(True)
        else:
            mx.abort('DaCapo 9.12 jar file must be specified with DACAPO_CP environment variable or as DACAPO library')
    
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        mx.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
    
    dacapoSuccess = re.compile(r"^===== DaCapo 9\.12 ([a-zA-Z0-9_]+) PASSED in ([0-9]+) msec =====$")
    dacapoFail = re.compile(r"^===== DaCapo 9\.12 ([a-zA-Z0-9_]+) FAILED (warmup|) =====$")
    dacapoTime = re.compile(r"===== DaCapo 9\.12 (?P<benchmark>[a-zA-Z0-9_]+) PASSED in (?P<time>[0-9]+) msec =====")
    
    dacapoMatcher = Matcher(dacapoTime, {'const:name' : 'benchmark', 'const:score' : 'time'})
    
    return Test("DaCapo-" + name, "DaCapo", ['-jar', dacapo, name, '-n', str(n), ] + dacapoArgs, [dacapoSuccess], [dacapoFail], [dacapoMatcher], ['-Xms2g', '-XX:MaxPermSize=256m'])

def getBootstraps():
    time = re.compile(r"Bootstrapping Graal\.+ in (?P<time>[0-9]+) ms")
    scoreMatcher = Matcher(time, {'const:name' : 'const:BootstrapTime', 'const:score' : 'time'})
    tests = []
    tests.append(Test("Bootstrap", "Bootstrap", ['-version'], successREs=[time], scoreMatchers=[scoreMatcher]))
    tests.append(Test("Bootstrap-bigHeap", "Bootstrap-bigHeap", ['-version'], successREs=[time], scoreMatchers=[scoreMatcher], vmOpts=['-Xms2g']))
    return tests

"""
Encapsulates a single program that is a sanity test and/or a benchmark.
"""
class Test:
    def __init__(self, name, group, cmd, successREs=[], failureREs=[], scoreMatchers=[], vmOpts=[], defaultCwd=None):
        self.name = name
        self.group = group
        self.successREs = successREs
        self.failureREs = failureREs
        self.scoreMatchers = scoreMatchers
        self.vmOpts = vmOpts
        self.cmd = cmd
        self.defaultCwd = defaultCwd
        
    def __str__(self):
        return self.name
    
    def test(self, vm, cwd=None, opts=[], vmbuild=None):
        """
        Run this program as a sanity test.
        """
        if cwd is None:
            cwd = self.defaultCwd
        parser = OutputParser(nonZeroIsFatal = False)
        jvmError = re.compile(r"(?P<jvmerror>([A-Z]:|/).*[/\\]hs_err_pid[0-9]+\.log)")
        parser.addMatcher(Matcher(jvmError, {'const:jvmError' : 'jvmerror'}))
        
        for successRE in self.successREs:
            parser.addMatcher(Matcher(successRE, {'const:passed' : 'const:1'}))
        for failureRE in self.failureREs:
            parser.addMatcher(Matcher(failureRE, {'const:failed' : 'const:1'}))
        
        result = parser.parse(vm, self.vmOpts + opts + self.cmd, cwd, vmbuild)
        
        parsedLines = result['parsed']
        assert len(parsedLines) == 1, 'Test matchers should not return more than one line'
        
        parsed = parsedLines[0]
        
        if parsed.has_key('jvmError'):
            mx.log('/!\\JVM Error : dumping error log...')
            f = open(parsed['jvmError'], 'rb');
            for line in iter(f.readline, ''):
                mx.log(line.rstrip())
            f.close()
            os.unlink(parsed['jvmError'])
            return False
        
        if parsed.has_key('failed') and parsed['failed'] is 1:
            return False
        
        return result['retcode'] is 0 and parsed.has_key('passed') and parsed['passed'] is '1'
    
    def bench(self, vm, cwd=None, opts=[], vmbuild=None):
        """
        Run this program as a benchmark.
        """
        if cwd is None:
            cwd = self.defaultCwd
        parser = OutputParser(nonZeroIsFatal = False)
        
        for failureRE in self.failureREs:
            parser.addMatcher(Matcher(failureRE, {'const:failed' : 'const:1'}))
        for scoreMatcher in self.scoreMatchers:
            parser.addMatcher(scoreMatcher)
            
        result = parser.parse(vm, self.vmOpts + opts + self.cmd, cwd, vmbuild)
        if result['retcode'] is not 0:
            return {}
        
        parsed = result['parsed']
        
        ret = {}
        
        for line in parsed:
            assert line.has_key('name') and line.has_key('score')
            if line.has_key('failed') and parsed['failed'] is 1:
                return {}
            ret[line['name']] = line['score']
        
        return ret
