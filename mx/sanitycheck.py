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
    'sunflow':    [0, 2,  5, 10, 15],
    'tomcat':     [0, 0,  5, 10, 15],
    'tradebeans': [0, 0,  5, 10, 13],
    'tradesoap':  [2, 4,  5, 10, 15],
    'xalan':      [0, 0,  5, 10, 18],
}

dacapoScalaSanityWarmup = {
    'actors':     [0, 0, 2,  8, 10],
# (lstadler) apparat was disabled due to a deadlock which I think is the benchmarks fault.
#    'apparat':    [0, 0, 1,  2,  3],
    'factorie':   [0, 0, 2,  5,  5],
    'kiama':      [0, 0, 3, 13, 15],
    'scalac':     [0, 0, 5, 15, 20],
    'scaladoc':   [0, 0, 5, 15, 15],
    'scalap':     [0, 0, 5, 15, 20],
    'scalariform':[0, 0, 6, 15, 20],
    'scalatest':  [0, 0, 2, 10, 12],
    'scalaxb':    [0, 0, 5, 15, 25],
    'specs':      [0, 0, 3, 13, 18],
    'tmt':        [0, 0, 3, 10, 12]
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
    'sunflow':    [           'fastdebug', 'debug'],
    'tomcat':     ['product', 'fastdebug', 'debug'],
    'tradebeans': ['product', 'fastdebug', 'debug'],
    'tradesoap':  ['product'],
    'xalan':      ['product', 'fastdebug', 'debug'],
}

dacapoScalaGateBuildLevels = {
    'actors':     ['product', 'fastdebug', 'debug'],
    'apparat':    ['product', 'fastdebug', 'debug'],
    'factorie':   ['product', 'fastdebug', 'debug'],
    'kiama':      ['product', 'fastdebug', 'debug'],
    'scalac':     ['product', 'fastdebug', 'debug'],
    'scaladoc':   ['product', 'fastdebug', 'debug'],
    'scalap':     ['product', 'fastdebug', 'debug'],
    'scalariform':['product', 'fastdebug', 'debug'],
    'scalatest':  ['product', 'fastdebug', 'debug'],
    'scalaxb':    ['product', 'fastdebug', 'debug'],
    'specs':      ['product', 'fastdebug', 'debug'],
    'tmt':        ['product', 'fastdebug', 'debug'],
}

class SanityCheckLevel:
    Fast, Gate, Normal, Extensive, Benchmark = range(5)
    
def getSPECjbb2005(benchArgs = []):
    
    specjbb2005 = mx.get_env('SPECJBB2005')
    if specjbb2005 is None or not exists(join(specjbb2005, 'jbb.jar')):
        mx.abort('Please set the SPECJBB2005 environment variable to a SPECjbb2005 directory')
    
    score = re.compile(r"^Valid run, Score is  (?P<score>[0-9]+)$")
    error = re.compile(r"VALIDATION ERROR")
    success = re.compile(r"^Valid run, Score is  [0-9]+$")
    matcher = Matcher(score, {'const:group' : "const:SPECjbb2005", 'const:name' : 'const:score', 'const:score' : 'score'})
    classpath = ['jbb.jar', 'check.jar']
    return Test("SPECjbb2005", ['spec.jbb.JBBmain', '-propfile', 'SPECjbb.props'] + benchArgs, [success], [error], [matcher], vmOpts=['-Xms3g', '-XX:+UseSerialGC', '-XX:-UseCompressedOops', '-cp', os.pathsep.join(classpath)], defaultCwd=specjbb2005)
    
def getSPECjvm2008(benchArgs = [], skipCheck=False, skipKitValidation=False, warmupTime=None, iterationTime=None):
    
    specjvm2008 = mx.get_env('SPECJVM2008')
    if specjvm2008 is None or not exists(join(specjvm2008, 'SPECjvm2008.jar')):
        mx.abort('Please set the SPECJVM2008 environment variable to a SPECjvm2008 directory')
    
    score = re.compile(r"^(Score on|Noncompliant) (?P<benchmark>[a-zA-Z0-9\._]+)( result)?: (?P<score>[0-9]+((,|\.)[0-9]+)?)( SPECjvm2008 Base)? ops/m$")
    error = re.compile(r"^Errors in benchmark: ")
    # The ' ops/m' at the end of the success string is important : it's how you can tell valid and invalid runs apart
    success = re.compile(r"^(Noncompliant c|C)omposite result: [0-9]+((,|\.)[0-9]+)?( SPECjvm2008 (Base|Peak))? ops/m$")
    matcher = Matcher(score, {'const:group' : "const:SPECjvm2008", 'const:name' : 'benchmark', 'const:score' : 'score'}, startNewLine=True)
    
    opts = []
    if warmupTime is not None:
        opts += ['-wt', str(warmupTime)]
    if iterationTime is not None:
        opts += ['-it', str(iterationTime)]
    if skipKitValidation:
        opts += ['-ikv']
    if skipCheck:
        opts += ['-ict']
    
    return Test("SPECjvm2008", ['-jar', 'SPECjvm2008.jar'] + opts + benchArgs, [success], [error], [matcher], vmOpts=['-Xms3g', '-XX:+UseSerialGC', '-XX:-UseCompressedOops'], defaultCwd=specjvm2008)

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
    dacapoTime1 = re.compile(r"===== DaCapo 9\.12 (?P<benchmark>[a-zA-Z0-9_]+) completed warmup 1 in (?P<time>[0-9]+) msec =====")
    
    dacapoMatcher = Matcher(dacapoTime, {'const:group' : "const:DaCapo", 'const:name' : 'benchmark', 'const:score' : 'time'}, startNewLine=True)
    dacapoMatcher1 = Matcher(dacapoTime1, {'const:group' : "const:DaCapo-1stRun", 'const:name' : 'benchmark', 'const:score' : 'time'})
    
    return Test("DaCapo-" + name, ['-jar', dacapo, name, '-n', str(n), ] + dacapoArgs, [dacapoSuccess], [dacapoFail], [dacapoMatcher, dacapoMatcher1], ['-Xms2g', '-XX:+UseSerialGC', '-XX:-UseCompressedOops'])

def getScalaDacapos(level=SanityCheckLevel.Normal, gateBuildLevel=None, dacapoArgs=[]):
    checks = []
    
    for (bench, ns) in dacapoScalaSanityWarmup.items():
        if ns[level] > 0:
            if gateBuildLevel is None or gateBuildLevel in dacapoScalaGateBuildLevels[bench]:
                checks.append(getScalaDacapo(bench, ns[level], dacapoArgs))
    
    return checks

def getScalaDacapo(name, n, dacapoArgs=[]):
    dacapo = mx.get_env('DACAPO_SCALA_CP')
    if dacapo is None:
        l = mx.library('DACAPO_SCALA', False)
        if l is not None:
            dacapo = l.get_path(True)
        else:
            mx.abort('Scala DaCapo 0.1.0 jar file must be specified with DACAPO_SCALA_CP environment variable or as DACAPO_SCALA library')
    
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        mx.abort('Specified Scala DaCapo jar file does not exist or is not a jar file: ' + dacapo)
    
    dacapoSuccess = re.compile(r"^===== DaCapo 0\.1\.0(-SNAPSHOT)? ([a-zA-Z0-9_]+) PASSED in ([0-9]+) msec =====$")
    dacapoFail = re.compile(r"^===== DaCapo 0\.1\.0(-SNAPSHOT)? ([a-zA-Z0-9_]+) FAILED (warmup|) =====$")
    dacapoTime = re.compile(r"===== DaCapo 0\.1\.0(-SNAPSHOT)? (?P<benchmark>[a-zA-Z0-9_]+) PASSED in (?P<time>[0-9]+) msec =====")
    
    dacapoMatcher = Matcher(dacapoTime, {'const:group' : "const:Scala-DaCapo", 'const:name' : 'benchmark', 'const:score' : 'time'})
    
    return Test("Scala-DaCapo-" + name, ['-jar', dacapo, name, '-n', str(n), ] + dacapoArgs, [dacapoSuccess], [dacapoFail], [dacapoMatcher], ['-Xms2g', '-XX:+UseSerialGC', '-XX:-UseCompressedOops'])

def getBootstraps():
    time = re.compile(r"Bootstrapping Graal\.+ in (?P<time>[0-9]+) ms")
    scoreMatcher = Matcher(time, {'const:group' : 'const:Bootstrap', 'const:name' : 'const:BootstrapTime', 'const:score' : 'time'})
    scoreMatcherBig = Matcher(time, {'const:group' : 'const:Bootstrap-bigHeap', 'const:name' : 'const:BootstrapTime', 'const:score' : 'time'})
    
    tests = []
    tests.append(Test("Bootstrap", ['-version'], successREs=[time], scoreMatchers=[scoreMatcher], ignoredVMs=['client', 'server']))
    tests.append(Test("Bootstrap-bigHeap", ['-version'], successREs=[time], scoreMatchers=[scoreMatcherBig], vmOpts=['-Xms2g'], ignoredVMs=['client', 'server']))
    return tests

"""
Encapsulates a single program that is a sanity test and/or a benchmark.
"""
class Test:
    def __init__(self, name, cmd, successREs=[], failureREs=[], scoreMatchers=[], vmOpts=[], defaultCwd=None, ignoredVMs=[]):
        self.name = name
        self.successREs = successREs
        self.failureREs = failureREs + [re.compile(r"Exception occured in scope: ")]
        self.scoreMatchers = scoreMatchers
        self.vmOpts = vmOpts
        self.cmd = cmd
        self.defaultCwd = defaultCwd
        self.ignoredVMs = ignoredVMs;
        
        
    def __str__(self):
        return self.name
    
    def test(self, vm, cwd=None, opts=[], vmbuild=None):
        """
        Run this program as a sanity test.
        """
        if (vm in self.ignoredVMs):
            return True;
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
        if len(parsedLines) == 0:
            return False
        
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
        
        if parsed.has_key('failed') and parsed['failed'] is '1':
            return False
        
        return result['retcode'] is 0 and parsed.has_key('passed') and parsed['passed'] is '1'
    
    def bench(self, vm, cwd=None, opts=[], vmbuild=None):
        """
        Run this program as a benchmark.
        """
        if (vm in self.ignoredVMs):
            return {};
        if cwd is None:
            cwd = self.defaultCwd
        parser = OutputParser(nonZeroIsFatal = False)
        
        for successRE in self.successREs:
            parser.addMatcher(Matcher(successRE, {'const:passed' : 'const:1'}))
        for failureRE in self.failureREs:
            parser.addMatcher(Matcher(failureRE, {'const:failed' : 'const:1'}))
        for scoreMatcher in self.scoreMatchers:
            parser.addMatcher(scoreMatcher)
            
        result = parser.parse(vm, self.vmOpts + opts + self.cmd, cwd, vmbuild)
        if result['retcode'] is not 0:
            mx.abort("Benchmark failed (non-zero retcode)")
        
        parsed = result['parsed']
        
        ret = {}
        
        passed = False;
        
        for line in parsed:
            assert (line.has_key('name') and line.has_key('score') and line.has_key('group')) or line.has_key('passed') or line.has_key('failed')
            if line.has_key('failed') and line['failed'] is '1':
                mx.abort("Benchmark failed")
            if line.has_key('passed') and line['passed'] is '1':
                passed = True
            if line.has_key('name') and line.has_key('score') and line.has_key('group'):
                if not ret.has_key(line['group']):
                    ret[line['group']] = {};
                ret[line['group']][line['name']] = line['score']
        
        if not passed:
            mx.abort("Benchmark failed (not passed)")
        
        return ret
