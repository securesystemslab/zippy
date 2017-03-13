import mx
from mx_zippy_benchmark import *
from mx_zippy_bench_param import *

## Python 2 (disabled)

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

class PyPyBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "pypy"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy"

    def benchmarksIterations(self):
        return python2Benchmarks


# mx_benchmark.add_bm_suite(PyPyBenchmarkSuite())


class PyPyMicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "pypy-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "pypy"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


# mx_benchmark.add_bm_suite(PyPyMicroBenchmarkSuite())

class CPythonBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython2"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "python"

    def benchmarksIterations(self):
        return python2Benchmarks


# mx_benchmark.add_bm_suite(CPythonBenchmarkSuite())


class CPythonMicroBenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "cpython2-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "python"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


# mx_benchmark.add_bm_suite(CPythonMicroBenchmarkSuite())


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


# mx_benchmark.add_bm_suite(JythonBenchmarkSuite())


class JythonMicroBenchmarkSuite(BaseJythonBenchmarkSuite):

    def name(self):
        return "jython-micro"

    def getPath(self):
        return pathBench

    def externalInterpreter(self):
        return "jython"

    def benchmarksIterations(self):
        return python2MicroBenchmarks


# mx_benchmark.add_bm_suite(JythonMicroBenchmarkSuite())
