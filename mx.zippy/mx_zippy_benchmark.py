
import argparse
import re
from os.path import join, exists

import mx
import mx_benchmark
import mx_graal_core
import mx_graal_benchmark

# mx benchmark 'python:*' --results-file ./python.json
# mx benchmark 'python-nopeeling:*' --results-file ./python-nopeeling.json
# mx benchmark 'python-flex:*' --results-file ./python-flex.json
# mx benchmark 'python-flex-evol:*' --results-file ./python-flex-evol.json
# ...
mx.update_commands(mx.suite('zippy'), {
    'python': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("python", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'python-nopeeling': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("python-nopeeling", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'python-flex': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("python-flex", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'python-flex-evol': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("python-flex-evol", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'cpython2': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("cpython2", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'cpython': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("cpython", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'jython': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("jython", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'pypy': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("pypy", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'pypy3': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("pypy3", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'python-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("python-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'cpython-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("cpython-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'cpython2-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("cpython2-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'jython-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("jython-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'pypy-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("pypy-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],
    'pypy3-micro': [
      lambda args: mx_graal_benchmark.createBenchmarkShortcut("pypy3-micro", args),
      '[<benchmarks>|*] [-- [VM options] [-- [Python options]]]'
    ],

})


py = ".py"
pathBench = "zippy/benchmarks/src/benchmarks/"
pathMicro = "zippy/benchmarks/src/micro/"

extraVmOpts = ['-Dgraal.TraceTruffleCompilation=true']


class BasePythonBenchmarkSuite(mx_benchmark.JavaBenchmarkSuite):

    def group(self):
        return "Python"

    def benchmarksIterations(self):
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
        return [key for key, value in self.benchmarksIterations().iteritems()]

    def successPatterns(self):
        return [ re.compile(r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)", re.MULTILINE) ]

    def failurePatterns(self):
        return [ re.compile(r"Exception") ]

    def rules(self, out, benchmarks, bmSuiteArgs):
        arg = int(self.benchmarksIterations()[benchmarks[0]])
        return [
          mx_benchmark.StdOutRule(
            r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<time>[0-9]+(\.[0-9]+)?$)", # pylint: disable=line-too-long
            {
              "benchmark": ("<benchmark>", str),
              "metric.name": "time",
              "metric.value": ("<time>", float),
              "metric.unit": "s",
              "metric.type": "numeric",
              "metric.score-function": "id",
              "metric.better": "lower",
              "metric.arg": arg,
            }
          ),
        ]


class BaseZippyBenchmarkSuite(BasePythonBenchmarkSuite):

    def subgroup(self):
        return "zippy"

    def getPath(self):
        raise NotImplementedError()

    def getZippyOpts(self):
        return []

    def getArgs(self, benchmarks, bmSuiteArgs):
        return (
            self.vmArgs(bmSuiteArgs + extraVmOpts) + ['-cp', mx.classpath(["edu.uci.python"]), "edu.uci.python.shell.Shell"] +
            [self.getPath() + benchmarks[0] + py, self.benchmarksIterations()[benchmarks[0]]] + self.getZippyOpts())

    def dimensions(self):
        return {
            "interpreter": "ZipPy",
        }

    def getJavaVm(self, bmSuiteArgs):
        return mx_benchmark.get_java_vm('server', 'graal-core') # mx benchmark '<Benchmark>' -- --jvm-config graal-core


pythonBenchmarks = {
    'binarytrees3t'   : '18',
    'fannkuchredux3t' : '11',
    'fasta3t'         : '25000000',
    'mandelbrot3t'    : '4000',
    'meteor3t'        : '2098',
    'nbody3t'         : '5000000',
    'spectralnorm3t'  : '3000',
    'pidigits-timed'  : '0',
    'euler31-timed'   : '200',
    'euler11-timed'   : '10000',
    'ai-nqueen-timed' : '10',
    'pads-eratosthenes-timed' : '100000',
    'pads-integerpartitions' : '700',
    'pads-lyndon'     : '100000000',
    'richards3-timed' : '200',
    'bm-float-timed'  : '1000',
    'pypy-chaos-timed': '1000',
    'pypy-go-timed'   : '50',
    'pypy-deltablue'  : '2000',
    'python-graph-bench': '200',
    'simplejson-bench': '10000',
    # 'whoosh-bench'    : '5000',
    # type not supported to adopt to Jython! <scoring.WeightScorer...
    # 'pymaging-bench'  : '5000',
    # Multiple super class is not supported yet! + File "JYTHON.jar/Lib/abc.py", line 32, in abstractmethod AttributeError: 'str' object has no attribute '__isabstractmethod__'
    # 'sympy-bench'     : '20000',
    # ImportError: No module named core
}



class ZipPyBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python"

    def getPath(self):
        return pathBench

    def benchmarksIterations(self):
        return pythonBenchmarks

mx_benchmark.add_bm_suite(ZipPyBenchmarkSuite())


pythonGeneratorBenchmarks = {
    'euler31-timed'   : '200',
    'euler11-timed'   : '10000',
    'ai-nqueen-timed' : '10',
    'pads-eratosthenes-timed' : '100000',
    'pads-integerpartitions' : '700',
    'pads-lyndon'     : '100000000',
    'python-graph-bench': '200',
    'simplejson-bench': '10000',
    # 'whoosh-bench'    : '5000',
    # 'pymaging-bench'  : '5000',
    # 'sympy-bench'     : '20000',
}

class ZipPyNoPeelingBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-nopeeling"

    def getPath(self):
        return pathBench

    def getZippyOpts(self):
        return ["-no-generator-peeling"]

    def benchmarksIterations(self):
        return pythonGeneratorBenchmarks

mx_benchmark.add_bm_suite(ZipPyNoPeelingBenchmarkSuite())

pythonObjectBenchmarks = {
    'richards3-timed' : '200',
    'bm-float-timed'  : '1000',
    'pypy-chaos-timed': '1000',
    'pypy-go-timed'   : '50',
    'pypy-deltablue'  : '2000',
}

class ZipPyFlexBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-flex"

    def getPath(self):
        return pathBench

    def getZippyOpts(self):
        return ["-flexible-object-storage"]

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

    def benchmarksIterations(self):
        return pythonObjectBenchmarks

mx_benchmark.add_bm_suite(ZipPyFlexEvolBenchmarkSuite())


pythonMicroBenchmarks = {
    'arith-binop'           : '0',
    'for-range'             : '0',
    'function-call'         : '0',
    'list-comp'             : '0',
    'list-indexing'         : '0',
    'list-iterating'        : '0',
    'builtin-len'           : '0',
    'builtin-len-tuple'     : '0',
    'math-sqrt'             : '0',
    'generator'             : '0',
    'generator-notaligned'  : '0',
    'generator-expression'  : '0',
    'genexp-builtin-call'   : '0',
    'attribute-access'      : '0',
    'attribute-access-polymorphic' : '0',
    'attribute-bool'        : '0',
    'call-method-polymorphic': '0',
    'boolean-logic'         : '0',
    'object-allocate'       : '0',
    'special-add'           : '0',
    'special-add-int'       : '0',
    'special-len'           : '0',
    'object-layout-change'  : '0',
}

class ZipPyMicroBenchmarkSuite(BaseZippyBenchmarkSuite):

    def name(self):
        return "python-micro"

    def getPath(self):
        return pathMicro

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

mx_benchmark.add_bm_suite(ZipPyMicroBenchmarkSuite())


class BaseExternalBenchmarkSuite(BasePythonBenchmarkSuite):

    def subgroup(self):
        return self.externalInterpreter()

    def externalInterpreter(self):
        raise NotImplementedError()

    def getArgs(self, benchmarks, bmSuiteArgs):
        return ([self.getPath() + benchmarks[0] + py, self.benchmarksIterations()[benchmarks[0]]])

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

    def name(self):
        return "pypy3"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy3"

    def benchmarksIterations(self):
        return pythonBenchmarks


mx_benchmark.add_bm_suite(PyPy3BenchmarkSuite())


class PyPy3MicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "pypy3-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy3"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks


mx_benchmark.add_bm_suite(PyPy3MicroBenchmarkSuite())

python2Benchmarks = {
    'binarytrees2t'   : '18',
    'fannkuchredux2t' : '11',
    'fasta3t'         : '25000000',
    'mandelbrot2t'    : '4000',
    'meteor3t'        : '2098',
    'nbody2t'         : '5000000',
    'spectralnorm2t'  : '3000',
    'pidigits-timed'  : '0',
    'euler31-timed'   : '200',
    'euler11-timed'   : '10000',
    'ai-nqueen-timed' : '10',
    'pads-eratosthenes-timed' : '100000',
    'pads-integerpartitions' : '700',
    'pads-lyndon'     : '100000000',
    'richards3-timed' : '200',
    'bm-float-timed'  : '1000',
    'pypy-chaos-timed': '1000',
    'pypy-go-timed'   : '50',
    'pypy-deltablue'  : '2000',
    'python-graph-bench': '200',
    'simplejson-bench': '10000',
    # 'whoosh-bench'    : '5000',
    # 'pymaging-bench'  : '5000',
    # 'sympy-bench'     : '20000',
}

class PyPyBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "pypy"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy"

    def benchmarksIterations(self):
        return python2Benchmarks


mx_benchmark.add_bm_suite(PyPyBenchmarkSuite())


python2MicroBenchmarks = {
    'arith-binop'           : '0',
    'for-range'             : '0',
    #'function-call'         : '0',
    'list-comp'             : '0',
    'list-indexing'         : '0',
    'list-iterating'        : '0',
    #'builtin-len'          : '0',
    #'builtin-len-tuple'    : '0',
    #'math-sqrt'            : '0',
    'generator'             : '0',
    'generator-notaligned'  : '0',
    'generator-expression'  : '0',
    'genexp-builtin-call'   : '0',
    'attribute-access'      : '0',
    'attribute-access-polymorphic' : '0',
    #'attribute-bool'        : '0',
    'call-method-polymorphic': '0',
    #'boolean-logic'         : '0',
    #'object-allocate'       : '0',
    'special-add'           : '0',
    'special-add-int'       : '0',
    #'special-len'           : '0',
}

class PyPyMicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "pypy-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


mx_benchmark.add_bm_suite(PyPyMicroBenchmarkSuite())


class CPython3BenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "python3.5"

    def benchmarksIterations(self):
        return pythonBenchmarks


mx_benchmark.add_bm_suite(CPython3BenchmarkSuite())


class CPython3MicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy3"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks


mx_benchmark.add_bm_suite(CPython3MicroBenchmarkSuite())


class CPythonBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython2"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "python"

    def benchmarksIterations(self):
        return python2Benchmarks


mx_benchmark.add_bm_suite(CPythonBenchmarkSuite())


class CPythonMicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython2-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "python"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


mx_benchmark.add_bm_suite(CPythonMicroBenchmarkSuite())


class BaseJythonBenchmarkSuite(BasePythonBenchmarkSuite):

    def subgroup(self):
        return self.externalInterpreter()

    def externalInterpreter(self):
        raise NotImplementedError()

    def dimensions(self):
        return {
            "interpreter": self.externalInterpreter(),
        }

    def getArgs(self, benchmarks, bmSuiteArgs):
        return (
            ['-jar', mx.library('JYTHON').path] +
            [self.getPath() + benchmarks[0] + py, self.benchmarksIterations()[benchmarks[0]]])


class JythonBenchmarkSuite(BaseJythonBenchmarkSuite):

    def name(self):
        return "jython"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "jython"

    def benchmarksIterations(self):
        return python2Benchmarks


mx_benchmark.add_bm_suite(JythonBenchmarkSuite())


class JythonMicroBenchmarkSuite(BaseJythonBenchmarkSuite):

    def name(self):
        return "jython-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "jython"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


mx_benchmark.add_bm_suite(JythonMicroBenchmarkSuite())
