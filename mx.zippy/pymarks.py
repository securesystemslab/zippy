from outputparser import OutputParser, ValuesMatcher
import re, mx, mx_graal, os, sys, StringIO, subprocess, time
from os.path import isfile, join, exists
from sanitycheck import Test, Tee, _noneAsEmptyList
from pymarksparams import *

benchVmOpts = ['-Xms2g', '-Xmx2g', '-G:+TraceTruffleCompilation']

def getPythonTestBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonTestBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPython2MicroBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = python2MicroBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/micro/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonMicroBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonMicroBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/micro/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonBenchmarksNoPeeling(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonGeneratorBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg, "-no-generator-peeling"]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonObjectBenchmarksFlex(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonObjectBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg, "-flexible-object-storage"]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonObjectBenchmarksFlexStorageEvolution(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonObjectBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg, "-flexible-storage-evolution"]
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=benchVmOpts))

    return tests

def getPythonBenchmarksProfiling(vm, profile_option=None):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonProfilerBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        if (profile_option is not None):
            cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg, profile_option, "-sort"]
        else :
            cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))

    return tests

def getPython2Benchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = python2Benchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "zippy/benchmarks/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath(["TRUFFLE_API", "edu.uci.python"]), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(ZippyTest("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))

    return tests

def getSuccessErrorMatcher():
    score = re.compile(r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)", re.MULTILINE)
    error = re.compile(r"Exception")
    success = score #re.compile(r"^Score \(version \d\): (?:[0-9]+(?:\.[0-9]+)?)", re.MULTILINE)
    matcher = ValuesMatcher(score, {'group' : 'Python', 'name' : '<benchmark>', 'score' : '<score>'})
    return success, error, matcher

class ZippyTest(Test):

    def bench(self, vm, cwd=None, extraVmOpts=None, vmbuild=None):
        """
        Run this program as a benchmark.
        Copied from sanitycheck.Test to extend benchmarking for non-JVMs.
        """
        if vm in self.ignoredVMs:
            return {}
        if cwd is None:
            cwd = self.defaultCwd
        parser = OutputParser()

        for successRE in self.successREs:
            parser.addMatcher(ValuesMatcher(successRE, {'passed' : '1'}))
        for failureRE in self.failureREs:
            parser.addMatcher(ValuesMatcher(failureRE, {'failed' : '1'}))
        for scoreMatcher in self.scoreMatchers:
            parser.addMatcher(scoreMatcher)

        if self.benchmarkCompilationRate:
            if vm == 'graal':
                bps = re.compile(r"ParsedBytecodesPerSecond@final: (?P<rate>[0-9]+)")
                ibps = re.compile(r"InlinedBytecodesPerSecond@final: (?P<rate>[0-9]+)")
                parser.addMatcher(ValuesMatcher(bps, {'group' : 'ParsedBytecodesPerSecond', 'name' : self.name, 'score' : '<rate>'}))
                parser.addMatcher(ValuesMatcher(ibps, {'group' : 'InlinedBytecodesPerSecond', 'name' : self.name, 'score' : '<rate>'}))
            else:
                ibps = re.compile(r"(?P<compiler>[\w]+) compilation speed: +(?P<rate>[0-9]+) bytes/s {standard")
                parser.addMatcher(ValuesMatcher(ibps, {'group' : 'InlinedBytecodesPerSecond', 'name' : '<compiler>:' + self.name, 'score' : '<rate>'}))

        startDelim = 'START: ' + self.name
        endDelim = 'END: ' + self.name

        outputfile = os.environ.get('BENCH_OUTPUT', None)
        if outputfile:
            # Used only to debug output parsing
            with open(outputfile) as fp:
                output = fp.read()
                start = output.find(startDelim)
                end = output.find(endDelim, start)
                if start == -1 and end == -1:
                    return {}
                output = output[start + len(startDelim + os.linesep): end]
                mx.log(startDelim)
                mx.log(output)
                mx.log(endDelim)
        else:
            tee = Tee()
            mx.log(startDelim)
            # zippy
            result = -1
            if vm == 'cpython2':
                result = mx.run(['python'] + self.cmd[-2:], out=tee.eat)
            elif vm == 'cpython':
                result = mx.run(['python3'] + self.cmd[-2:], out=tee.eat)
            elif vm == 'jython':
                result = mx_graal.vm(['-jar', mx.library('JYTHON').path] + self.cmd[-2:], vm = 'original', out=tee.eat)
            elif vm == 'pypy':
                result = mx.run(['pypy'] + self.cmd[-2:], out=tee.eat)
            elif vm == 'pypy3':
                result = mx.run(['pypy3'] + self.cmd[-2:], out=tee.eat)
            else:
                result = mx_graal.vm(self.vmOpts + _noneAsEmptyList(extraVmOpts) + self.cmd, vm, nonZeroIsFatal=False, out=tee.eat, err=subprocess.STDOUT, cwd=cwd, vmbuild=vmbuild)

            if result != 0:
                mx.abort("Benchmark failed (non-zero retcode)")
            # wait for subprocess to finish
            time.sleep(.5)
            mx.log(endDelim)
            output = tee.output.getvalue()

        groups = {}
        passed = False
        for valueMap in parser.parse(output):
            assert (valueMap.has_key('name') and valueMap.has_key('score') and valueMap.has_key('group')) or valueMap.has_key('passed') or valueMap.has_key('failed'), valueMap
            if valueMap.get('failed') == '1':
                mx.abort("Benchmark failed")
            if valueMap.get('passed') == '1':
                passed = True
            groupName = valueMap.get('group')
            if groupName:
                group = groups.setdefault(groupName, {})
                name = valueMap.get('name')
                score = valueMap.get('score')
                if name and score:
                    group[name] = score

        if not passed:
            mx.abort("Benchmark failed (not passed)")

        return groups
