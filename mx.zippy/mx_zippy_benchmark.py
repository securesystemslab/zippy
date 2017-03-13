import argparse
import re
from os.path import join, exists
import mx
import mx_benchmark

from mx_zippy_bench_param import *

# mx benchmark 'python:*' --results-file ./python.json
# mx benchmark 'python-nopeeling:*' --results-file ./python-nopeeling.json
# mx benchmark 'python-flex:*' --results-file ./python-flex.json
# mx benchmark 'python-flex-evol:*' --results-file ./python-flex-evol.json
# ...
_mx_graal = mx.suite("graal-core", fatalIfMissing=False)

extraGraalVmOpts = ['-Dgraal.TraceTruffleCompilation=true', '-Dgraal.TruffleCompileImmediately=true']

class BasePythonBenchmarkSuite(mx_benchmark.JavaBenchmarkSuite):

    def group(self):
        return "zippy"

    def benchmarksIterations(self):
        raise NotImplementedError()

    def benchmarksType(self):
        raise NotImplementedError()

    def interpreterName(self):
        raise NotImplementedError()

    def validateReturnCode(self, retcode):
        return retcode == 0

    def createCommandLineArgs(self, benchmarks, bmSuiteArgs):
        if benchmarks is None:
            raise RuntimeError("Suite runs only a single benchmark.")
        if len(benchmarks) != 1:
            raise RuntimeError("Suite runs only a single benchmark, got: {0}".format(benchmarks))
        return self.getArgs(benchmarks, bmSuiteArgs)

    def getArgs(self, benchmarks, bmSuiteArgs):
        raise NotImplementedError()

    def benchmarkList(self, bmSuiteArgs):
        _list = []
        for bench in sorted(self.benchmarksIterations().keys()):
            for i in range(0, len(self.benchmarksIterations()[bench]), 2):
                _list += [[bench, self.benchmarksIterations()[bench][i], self.benchmarksIterations()[bench][i + 1]]]

        return _list

    def successPatterns(self):
        return [ re.compile(r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)", re.MULTILINE) ]

    def failurePatterns(self):
        return [ re.compile(r"Exception") ]

    def get_timing(self):
        return ['peak']

    def rules(self, out, benchmarks, bmSuiteArgs):
        benchmark  = self.benchmarksType() + "." + benchmarks[0][0]
        arg        = benchmarks[0][1]
        return [
          mx_benchmark.StdOutRule(
            r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<time>[0-9]+(\.[0-9]+)?$)", # pylint: disable=line-too-long
            {
              "benchmark": "".join(benchmark),
              "metric.name": "time",
              "peak": ("<time>", float),
              "python.params": "".join(arg),
              "metric.value": ("<time>", float),
              "metric.unit": "s",
              "metric.type": "numeric",
              "metric.score-function": "id",
              "metric.better": "lower",
              "metric.arg": " ".join(arg),
            }
          ),
        ]


class BaseZippyBenchmarkSuite(BasePythonBenchmarkSuite):

    def subgroup(self):
        return "zippy"

    def getPath(self):
        raise NotImplementedError()

    def interpreterName(self):
        return "ZipPy"

    def getZippyOpts(self):
        return []

    def getArgs(self, benchmarks, bmSuiteArgs):
        extra_vmargs = []
        if _mx_graal:
            extra_vmargs = extraGraalVmOpts

        return (
            self.vmArgs(bmSuiteArgs + extra_vmargs) + ['-cp', mx.classpath(["edu.uci.python"]), "edu.uci.python.shell.Shell"] +
            [self.getPath() + benchmarks[0][0] + py ] + benchmarks[0][2] + self.getZippyOpts())

    def dimensions(self):
        return {
            "interpreter": "ZipPy",
        }


class ZipPyBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "zippy-normal"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def benchmarksIterations(self):
        return pythonBenchmarks

mx_benchmark.add_bm_suite(ZipPyBenchmarkSuite())

class ZipPyMicroBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "zippy-micro"

    def getPath(self):
        return pathMicro

    def benchmarksType(self):
        return "micro"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

mx_benchmark.add_bm_suite(ZipPyMicroBenchmarkSuite())


class ZipPyNoPeelingBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-nopeeling"

    def getPath(self):
        return pathBench

    def getZippyOpts(self):
        return ["-no-generator-peeling"]

    def benchmarksType(self):
        return "generator"

    def benchmarksIterations(self):
        return pythonGeneratorBenchmarks

mx_benchmark.add_bm_suite(ZipPyNoPeelingBenchmarkSuite())

class ZipPyFlexBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-flex"

    def getPath(self):
        return pathBench

    def getZippyOpts(self):
        return ["-flexible-object-storage"]

    def benchmarksType(self):
        return "object"

    def benchmarksIterations(self):
        return pythonObjectBenchmarks

mx_benchmark.add_bm_suite(ZipPyFlexBenchmarkSuite())


class ZipPyFlexEvolBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-flex-evol"

    def getPath(self):
        return pathBench

    def getZippyOpts(self):
        return ["-flexible-storage-evolution"]

    def benchmarksType(self):
        return "object"

    def benchmarksIterations(self):
        return pythonObjectBenchmarks

mx_benchmark.add_bm_suite(ZipPyFlexEvolBenchmarkSuite())



class BaseExternalBenchmarkSuite(BasePythonBenchmarkSuite):

    def subgroup(self):
        return self.externalInterpreter()

    def ext_version(self):
        raise NotImplementedError()

    def externalInterpreter(self):
        raise NotImplementedError()

    def getArgs(self, benchmarks, bmSuiteArgs):
        return ([self.getPath() + benchmarks[0][0] + py ] + benchmarks[0][2])

    def dimensions(self):
        return {
            "interpreter": self.externalInterpreter(),
        }

    def before(self, bmSuiteArgs):
        mx.run([self.externalInterpreter(), "--version"])

    def runAndReturnStdOut(self, benchmarks, bmSuiteArgs):
        args = self.createCommandLineArgs(benchmarks, bmSuiteArgs)
        cwd = self.workingDirectory(benchmarks, bmSuiteArgs)
        if args is None:
            return 0, "", {}
        out = mx.TeeOutputCapture(mx.OutputCapture())
        mx.log("Running "+ self.externalInterpreter() +" with args: {0}".format(args))
        code = mx.run([ self.externalInterpreter() ] + args, out=out, err=out, cwd=cwd, nonZeroIsFatal=False)
        out = out.underlying.data
        dims = self.dimensions()
        return code, out, dims

class PyPy3BenchmarkSuite(BaseExternalBenchmarkSuite):

    def interpreterName(self):
        return "PyPy3"

    def name(self):
        return "pypy3-normal"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def externalInterpreter(self):
        return "pypy3"

    def ext_version(self):
        out1 = mx.OutputCapture()
        out2 = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out2), out=mx.TeeOutputCapture(out1))
        out1 = [] if not out1 or len(out1.data) <= 1 else out1.data.split("\n")
        out1 = [] if not out1 or len(out1) <= 1 else out1[1].split(" ")
        out2 = [] if not out2 or len(out2.data) <= 1 else out2.data.split("\n")
        out2 = [] if not out2 or len(out2) <= 1 else out2[1].split(" ")
        if len(out1) > 1:
            return out1[out1.index("[PyPy") + 1].replace('-','_')
        elif len(out2) > 1:
            return out2[out2.index("[PyPy") + 1].replace('-','_')
        else:
            return "unknown"


    def benchmarksIterations(self):
        return pythonBenchmarks


mx_benchmark.add_bm_suite(PyPy3BenchmarkSuite())


class PyPy3MicroBenchmarkSuite(PyPy3BenchmarkSuite):

    def name(self):
        return "pypy3-micro"

    def getPath(self):
        return pathMicro

    def benchmarksType(self):
        return "micro"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks


mx_benchmark.add_bm_suite(PyPy3MicroBenchmarkSuite())



class CPython3BenchmarkSuite(BaseExternalBenchmarkSuite):

    def interpreterName(self):
        return "CPython"

    def name(self):
        return "cpython3.5-normal"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def externalInterpreter(self):
        return "python3.5"

    def benchmarksIterations(self):
        return pythonBenchmarks

    def ext_version(self):
        out1 = mx.OutputCapture()
        out2 = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out2), out=mx.TeeOutputCapture(out1))
        out1 = [] if not out1 or len(out1.data) <= 1 else out1.data.split("\n")
        out1 = [] if not out1 or len(out1) <= 1 else out1[0].split(" ")
        out2 = [] if not out2 or len(out2.data) <= 1 else out2.data.split("\n")
        out2 = [] if not out2 or len(out2) <= 1 else out2[0].split(" ")
        if len(out1) > 1:
            return out1[out1.index("Python") + 1].replace('-','_')
        elif len(out2) > 1:
            return out2[out2.index("Python") + 1].replace('-','_')
        else:
            return "unknown"


mx_benchmark.add_bm_suite(CPython3BenchmarkSuite())


class CPython3MicroBenchmarkSuite(CPython3BenchmarkSuite):

    def name(self):
        return "cpython3.5-micro"

    def getPath(self):
        return pathMicro

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

    def benchmarksType(self):
        return "micro"

mx_benchmark.add_bm_suite(CPython3MicroBenchmarkSuite())
