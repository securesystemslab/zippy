from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
import traceback
import json
import time
import re
import os
from os.path import join, exists
import mx
import mx_benchmark

# mx benchmark 'python:*' --results-file ./python.json
# mx benchmark 'python-nopeeling:*' --results-file ./python-nopeeling.json
# mx benchmark 'python-flex:*' --results-file ./python-flex.json
# mx benchmark 'python-flex-evol:*' --results-file ./python-flex-evol.json
# ...
_mx_graal = mx.suite("graal-core", fatalIfMissing=False)
_suite = mx.suite('zippy')
asv_results_dir = _suite.dir + '/asv/results/'
machine_name = os.environ.get("MACHINE_NAME")
if not machine_name:
    machine_name = os.uname()[1]
machine_results_dir = asv_results_dir + machine_name

def generate_asv():
    # TODO
    print("Not implemented yet!")


py = ".py"
pathBench = "zippy/benchmarks/src/benchmarks/"
pathMicro = "zippy/benchmarks/src/micro/"

extraVmOpts = ['-Dgraal.TraceTruffleCompilation=true']

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

# XXX: testing
# pythonBenchmarks = {
#     'binarytrees3t'   : '12',
#     'mandelbrot3t'    : '300',
# }


def athean_bench_shortcut(benchSuite, args):
    benchname = "*"
    if not args:
        vm_py_args = []
    elif args[0] == "--":
        vm_py_args = args # VM or Python options
    else:
        benchname = args[0]
        vm_py_args = args[1:]

    return zippy_benchmark([bench_suite + ":" + benchname] + vm_py_args)

class BaseASVBenchmarkSuite(mx_benchmark.JavaBenchmarkSuite):

    def group(self):
        return "ZipPy"

    def benchmarksIterations(self):
        raise NotImplementedError()

    def benchmarksType(self):
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
        iterations = self.benchmarksIterations()[benchmarks[0]]
        return [
          mx_benchmark.StdOutRule(
            r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<time>[0-9]+(\.[0-9]+)?$)", # pylint: disable=line-too-long
            {
              "benchmark": "".join(self.benchmarksType() + "." + benchmarks[0] + "." + iterations),
              "metric.name": "time",
              "peak": ("<time>", float),
              "metric.value": ("<time>", float),
              "metric.unit": "s",
              "metric.type": "numeric",
              "metric.score-function": "id",
              "metric.better": "lower",
              "metric.arg": " ".join(iterations),
            }
          ),
        ]


class ASVZipPyBenchmarkSuite(BaseASVBenchmarkSuite):

    def name(self):
        return "asv-zippy-normal"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def benchmarksIterations(self):
        return pythonBenchmarks

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
        if _mx_graal:
            # mx benchmark '<Benchmark>' -- --jvm-config graal-core
            return mx_benchmark.get_java_vm('server', 'graal-core')
        else:
            return mx_benchmark.DefaultJavaVm('server', 'default')

mx_benchmark.add_bm_suite(ASVZipPyBenchmarkSuite())

class ASVMicroZipPyBenchmarkSuite(ASVZipPyBenchmarkSuite):

    def name(self):
        return "asv-zippy-micro"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathMicro

    def benchmarksType(self):
        return "micro"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

mx_benchmark.add_bm_suite(ASVMicroZipPyBenchmarkSuite())

class BaseExternalBenchmarkSuite(BaseASVBenchmarkSuite):

    def externalInterpreter(self):
        raise NotImplementedError()

    def ext_version(self):
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


class ASVPyPy3BenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "asv-pypy3-normal"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def externalInterpreter(self):
        return "pypy3"

    def ext_version(self):
        out = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out))
        out = out.data.split("\n")
        out = out[1].split(" ")
        return out[out.index("[PyPy") + 1]


    def benchmarksIterations(self):
        return pythonBenchmarks


mx_benchmark.add_bm_suite(ASVPyPy3BenchmarkSuite())

class ASVMicroPyPy3BenchmarkSuite(ASVPyPy3BenchmarkSuite):

    def name(self):
        return "asv-pypy3-micro"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathMicro

    def benchmarksType(self):
        return "micro"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

mx_benchmark.add_bm_suite(ASVMicroPyPy3BenchmarkSuite())


class ASVCPython3BenchmarkSuite(BaseExternalBenchmarkSuite):

    def name(self):
        return "asv-cpython3.5-normal"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathBench

    def benchmarksType(self):
        return "normal"

    def externalInterpreter(self):
        return "python3.5"

    def benchmarksIterations(self):
        return pythonBenchmarks

    def ext_version(self):
        out = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out))
        out = out.data.split(" ")
        return out[out.index("Python") + 1]


mx_benchmark.add_bm_suite(ASVCPython3BenchmarkSuite())

class ASVMicroCPython3BenchmarkSuite(ASVCPython3BenchmarkSuite):

    def name(self):
        return "asv-cpython3.5-micro"

    def subgroup(self):
        return "all"

    def getPath(self):
        return pathMicro

    def benchmarksType(self):
        return "micro"

    def benchmarksIterations(self):
        return pythonMicroBenchmarks

mx_benchmark.add_bm_suite(ASVMicroCPython3BenchmarkSuite())


class ZipPyBenchmarkExecutor(mx_benchmark.BenchmarkExecutor):
    def dimensions(self, suite, mxBenchmarkArgs, bmSuiteArgs):
        standard = {
          "subgroup": suite.subgroup(),
          "bench-suite": suite.name(),
          "config.platform-version": "",
          "commit.rev": _suite.vc.parent(_suite.dir),
          "commit.committer-ts": _suite.vc.parent_info(_suite.dir)["committer-ts"],
          "warnings": "",
        }

        return standard

    def process_result(self, result):
        self.asv_pre_results["peak"][result['benchmark']] = result["peak"]
        self.asv_pre_results["other"][result['benchmark'] + ".args"] = "arg"

    def prepare_asv_dict(self, suite):
        with open(machine_results_dir + '/machine.json') as machine_info:
            info = json.load(machine_info)

        asv_dict = {
            # "commit_hash": "471d65a3f2f65a20d5b231d0d27220fbc2871e43",
            # "date": (1474711889 * 1000),
            # "commit_hash": "6c8a7ec21f7386b82643c0a7598834fb513bbd90",
            # "date": (1474626233 * 1000),
            # "commit_hash": "323e23c95468a379bce2b4ec9cec4f987c3bba98",
            # "date": (1476247753 * 1000),
            "commit_hash": _suite.vc.parent(_suite.dir),
            "date": int(_suite.vc.parent_info(_suite.dir)["committer-ts"]) * 1000,
            "params": {
                "interpreter": suite.name(),
                "timing": ""
                # from machine.json
            },
            "profiles": {},
            "python": "3.5",
            "requirements": {
            },
            "results": {
            },
            "version": 1
        }

        asv_dict['params'].update(info)
        asv_dict['params'].pop('version', None)
        return asv_dict

    def get_latest_run(self, file_tag):
        results_list = os.listdir(machine_results_dir)
        latest_run = 0
        for f in results_list:
            if f.startswith(file_tag):
                t = f.split("-")
                t = t[-1].split(".")[0]
                latest_run = max(latest_run, int(t))

        return str(latest_run + 1)

    def write_to_json(self, content, filename):
        filename_path = machine_results_dir + "/" + filename
        dump = json.dumps(content, sort_keys = True, indent = 4)
        with open(filename_path + ".json", "w") as txtfile:
            txtfile.write(dump)

    def write_asv_results(self, suite, results):
        template = self.prepare_asv_dict(suite)
        if "zippy" in suite.name():
            file_tag = template['commit_hash'][:8] + "-" + suite.name() + "-"
        else:
            file_tag = "version_" + suite.ext_version() + "-" + suite.name() + "-"

        run_num = "-run-" + self.get_latest_run(file_tag)

        timing = ['peak']
        for t in timing:
            _timing = dict(template)
            _timing['params']['timing'] = t
            _timing['results'].update(results[t])
            self.write_to_json(_timing, file_tag + t + run_num)



    def asv_benchmark(self, mxZipPyBenchmarkArgs, bmSuiteArgs):
        """Run ZipPy ASV benchmark suite."""
        parser = ArgumentParser(
            prog="mx asv-benchmark",
            add_help=False,
            usage="mx asv-benchmark <options> -- <benchmark-suite-args> -- <benchmark-args>",
            formatter_class=RawTextHelpFormatter)
        parser.add_argument(
            "benchmark", nargs="?", default=None,
            help="Benchmark to run, format: <suite>:<benchmark>.")
        parser.add_argument(
            "--list", default=None, action="store_true",
            help="Prints the list of all available benchmark suites.")
        parser.add_argument(
            "-h", "--help", action="store_true", default=None,
            help="Show usage information.")
        parser.add_argument(
            "--generate-asv", action="store_true", default=None,
            help="Generate benchmarks.json file for all available benchmarks.")
        mxZipPyBenchmarkArgs = parser.parse_args(mxZipPyBenchmarkArgs)

        if mxZipPyBenchmarkArgs.generate_asv != None:
            generate_asv()
            mx.abort("")

        if mxZipPyBenchmarkArgs.list:
            print "The following ZipPy asv benchmark suites are available:\n"
            for name in mx_benchmark._bm_suites:
                if name.startswith("asv-"):
                    print "  " + name
            mx.abort("")

        if mxZipPyBenchmarkArgs.help or mxZipPyBenchmarkArgs.benchmark is None:
            parser.print_help()
            for key, entry in mx_benchmark.parsers.iteritems():
                if mxZipPyBenchmarkArgs.benchmark is None or key in suite.parserNames():
                    print entry.description
                    entry.parser.print_help()
            mx.abort("")

        if mxZipPyBenchmarkArgs.benchmark:
            suite, benchNamesList = self.getSuiteAndBenchNames(mxZipPyBenchmarkArgs, bmSuiteArgs)

        self.checkEnvironmentVars()

        results = []

        failures_seen = False
        suite.before(bmSuiteArgs)
        start_time = time.time()
        self.asv_pre_results = {"peak":{},"other":{},}
        for benchnames in benchNamesList:
            suite.validateEnvironment()
            try:
                partialResults = self.execute(suite, benchnames, mxZipPyBenchmarkArgs, bmSuiteArgs)
                self.process_result(partialResults[0])
                results.extend(partialResults)
            except RuntimeError:
                failures_seen = True
                mx.log(traceback.format_exc())
        end_time = time.time()
        suite.after(bmSuiteArgs)

        self.write_asv_results(suite, self.asv_pre_results)

        topLevelJson = {
          "Q": results
        }
        dump = json.dumps(topLevelJson, sort_keys = True, indent = 4)
        with open("zippy-results.json", "w") as txtfile:
            txtfile.write(dump)
        if failures_seen:
            return 1
        return 0

"""
result.json:
{
    "commit_hash": "fb16abd9a84c90cefa48411d2ad7728b5430d39f",
    "date": 1445617605000,
    "params": {
        "cpu": "Intel(R) 2.40GHz x8",
        "gpu": "AMD(R) R390 1000MHz x2560",
        "gpu-ram": "8GB"
        "machine": "maxine",
        "os": "Linux Ubuntu",
        "ram": "32GB",
        "interpreter": "zippy",              // zippy, cpython3.5, pypy3
        "timing": "peak"
    },
    "profiles": {},
    "python": "3.5",
    "requirements": {
    },
    "results": {
        "zippy.mandelbrot3t.nn": 1.1540190579999998e-05,
        "pypy.mandelbrot3t.nn": 1.1621265679999997e-05
    },
    "version": 1
}
"""

_zippy_benchmark_executor = ZipPyBenchmarkExecutor()

def zippy_benchmark(args):
    """Run zippy asv benchmark suite."""

    mxZipPyBenchmarkArgs, bmSuiteArgs = mx_benchmark.splitArgs(args, "--")
    return _zippy_benchmark_executor.asv_benchmark(mxZipPyBenchmarkArgs, bmSuiteArgs)

mx.update_commands(_suite, {
    'asv-benchmark' : [zippy_benchmark, '--vmargs [vmargs] --runargs [runargs] suite:benchname'],
})
