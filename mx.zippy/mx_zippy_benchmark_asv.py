from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
import traceback
import json
import time
import re
import os
import copy
import platform, subprocess
import math
from os.path import join, exists
import mx
import mx_benchmark

# mx asv-benchmark "asv-zippy-normal:*"
# mx asv-benchmark "asv-pypy3-normal:*"
# mx asv-benchmark "asv-cpython3.5-micro:*"
# mx asv-benchmark --list
# mx asv-benchmark --generate-asv-conf
# ...


_mx_graal = mx.suite("graal-core", fatalIfMissing=False)
_suite = mx.suite('zippy')
asv_env = os.environ.get("ZIPPY_ASV_PATH")
if not asv_env:
    asv_env = _suite.dir

url = _suite.vc.default_pull(_suite.vc_dir).replace('.git','')
url = url if 'html' not in url else "https://github.com/securesystemslab/zippy"
html_dir = asv_env + '/html'
asv_dir = asv_env + '/asv/'
asv_results_dir = asv_dir + '/results/'
machine_name = os.environ.get("MACHINE_NAME")
if not machine_name:
    machine_name = platform.node()

machine_name += '-no-graal' if not _mx_graal else '-graal'

machine_results_dir = asv_results_dir + machine_name


py = ".py"
pathBench = "zippy/benchmarks/src/benchmarks/"
pathMicro = "zippy/benchmarks/src/micro/"

extraGraalVmOpts = ['-Dgraal.TraceTruffleCompilation=true']

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
#     'binarytrees3t'   : '18',
#     'mandelbrot3t'    : '4000',
# }



def write_to_json(content, filename):
    dump = json.dumps(content, sort_keys = True, indent = 4)
    with open(filename, "w") as txtfile:
        txtfile.write(dump)

def prompt(default, msg):
    py3 = int(platform.python_version_tuple()[0]) > 2
    value = ''
    if py3:
        value = input(msg + " ['" + default + "']: ")
    else:
        value = raw_input(msg + " [" + default + "]: ")
    return value if value != '' else default

def get_processor_brand():
    if platform.system() == "Darwin":
        return subprocess.check_output(['/usr/sbin/sysctl', "-n", "machdep.cpu.brand_string"]).strip()
    elif platform.system() == "Linux":
        command = "cat /proc/cpuinfo"
        all_info = subprocess.check_output(command, shell=True).strip()
        for line in all_info.split("\n"):
            if "model name" in line:
                return re.sub( ".*model name.*:", "", line,1)
    return ""

def generate_asv_machine(machine_name, force=False):
    """machine.json"""
    path = machine_results_dir + "/machine.json"
    if not exists(machine_results_dir):
        os.mkdir(machine_results_dir)

    if not force and exists(path):
        return

    arch = prompt(platform.machine(), "CPU Architecture")
    os_name = platform.platform() if platform.system() != "Darwin" else "MacOS " + platform.mac_ver()[0]
    os_name = prompt(os_name, "Operating System")
    processor_brand = prompt(str(get_processor_brand()), "CPU Model")
    try:
        from psutil import virtual_memory
        ram = prompt(str(math.ceil(virtual_memory().total/(1024.**3))) + "GB", "System Memory (RAM)")
    except:
        ram = prompt("??GB", "System Memory (RAM)")
    gpu_cmd = "$ clinfo | grep 'Device Name'"
    gpu_brand = prompt("Unknown", "GPU Model (run: "+gpu_cmd+")")
    gpu_ram_cmd = "$ clinfo | grep 'Global memory size'"
    gpu_ram = prompt("Unknown", "GPU Memory (run: "+gpu_ram_cmd+")")
    machine_json = {
        "arch": arch,
        "cpu": processor_brand,
        "ram": ram,
        "gpu": gpu_brand,
        "gpu-ram": gpu_ram,
        "machine": machine_name,
        "os": os_name,
        "version": 1
    }

    write_to_json(machine_json, path)


def generate_asv_benchmarks(force=False):
    """benchmarks.json"""
    path = asv_results_dir + "benchmarks.json"
    if not exists(asv_results_dir):
        os.mkdir(asv_results_dir)

    if not force and exists(path):
        return

    benchmarks_json = {}
    single_benchmark_template = {
        "code": "",
        "goal_time": 2.0,
        "name": "",
        "number": 0,
        "param_names": [],
        "params": [],
        "repeat": 0,
        "timeout": 60.0,
        "type": "time",
        "unit": "seconds"
    }

    for bench in pythonBenchmarks:
        bench_json = copy.deepcopy(single_benchmark_template)
        bench_json['name'] = "normal." + bench + "." + pythonBenchmarks[bench]
        bench_json['code'] = "mx python " + pathMicro + bench + ".py " + pythonBenchmarks[bench]
        benchmarks_json[bench] = bench_json

    for bench in pythonMicroBenchmarks:
        bench_json = copy.deepcopy(single_benchmark_template)
        bench_json['name'] = "micro." + bench + "." + pythonMicroBenchmarks[bench]
        bench_json['code'] = "mx python " + pathMicro + bench + ".py " + pythonMicroBenchmarks[bench]
        benchmarks_json[bench] = bench_json

    benchmarks_json["version"] = 1
    write_to_json(benchmarks_json, path)

def generate_asv_conf(force=False):
    """asv.conf.json"""
    path = asv_dir + "asv.conf.json"
    if not exists(asv_dir):
        os.mkdir(asv_dir)

    if not force and exists(path):
        return

    asv_conf_json = {
        "version": 1,
        "project": _suite.name,
        "project_url": url,
        "repo": url + ".git",
        "branches": ["master"],
        "environment_type": "conda",
        "show_commit_url": url + "/commit/",
        "pythons": ["3.4"],
        "matrix": {},
        "html_dir": html_dir,
        "hash_length": 8
    }

    write_to_json(asv_conf_json, path)


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

    def interpreterName(self):
        return "ZipPy"

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
        extra_vmargs = []
        if _mx_graal:
            extra_vmargs = extraGraalVmOpts

        return (
            self.vmArgs(bmSuiteArgs + extra_vmargs) + ['-cp', mx.classpath(["edu.uci.python"]), "edu.uci.python.shell.Shell"] +
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
        print(self.externalInterpreter() + " (version: " + self.ext_version() + ")")
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

    def interpreterName(self):
        return "PyPy3"

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
        out1 = mx.OutputCapture()
        out2 = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out2), out=mx.TeeOutputCapture(out1))
        out1 = [] if not out1 or len(out1.data) <= 1 else out1.data.split("\n")
        out1 = [] if not out1 or len(out1) <= 1 else out1[1].split(" ")
        out2 = [] if not out2 or len(out2.data) <= 1 else out2.data.split("\n")
        out2 = [] if not out2 or len(out2) <= 1 else out2[1].split(" ")
        if len(out1) > 1:
            return out1[out1.index("[PyPy") + 1]
        elif len(out2) > 1:
            return out2[out2.index("[PyPy") + 1]
        else:
            return "unknown"


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

    def interpreterName(self):
        return "CPython"

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
        out1 = mx.OutputCapture()
        out2 = mx.OutputCapture()
        mx.run([self.externalInterpreter(), "--version"], err=mx.TeeOutputCapture(out2), out=mx.TeeOutputCapture(out1))
        out1 = [] if not out1 or len(out1.data) <= 1 else out1.data.split("\n")
        out1 = [] if not out1 or len(out1) <= 1 else out1[0].split(" ")
        out2 = [] if not out2 or len(out2.data) <= 1 else out2.data.split("\n")
        out2 = [] if not out2 or len(out2) <= 1 else out2[0].split(" ")
        if len(out1) > 1:
            return out1[out1.index("Python") + 1]
        elif len(out2) > 1:
            return out2[out2.index("Python") + 1]
        else:
            return "unknown"


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

""" result.json:
{
    "commit_hash": "fb16abd9a84c90cefa48411d2ad7728b5430d39f",
    "date": 1445617605000,
    "params": {
        "machine": "maxine",
        "interpreter": "zippy",              // zippy, CPython, PyPy3
        "timing": "peak"
    },
    "profiles": {},
    "python": "3.5",
    "requirements": {
    },
    "results": {
        "normal.mandelbrot3t.400": 1.1540190579999998e-05,
        "normal.mandelbrot3t.4000": 1.1621265679999997e-05
    },
    "version": 1
}
"""

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

        asv_dict = {
            "commit_hash": _suite.vc.parent(_suite.dir),
            "date": int(_suite.vc.parent_info(_suite.dir)["committer-ts"]) * 1000,
            "params": {
                "machine": machine_name,
                "interpreter": suite.interpreterName(),
                "timing": ""
            },
            "profiles": {},
            "python": "3.5",
            "requirements": {
            },
            "results": {
            },
            "version": 1
        }

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
            write_to_json(_timing, machine_results_dir + "/" + file_tag + t + run_num + ".json")



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
            "--generate-asv-benchmarks", action="store_true", default=None,
            help="Generate benchmarks.json file for all available benchmarks.")
        parser.add_argument(
            "--generate-asv-conf", action="store_true", default=None,
            help="Generate asv.conf.json file for all available benchmarks.")
        parser.add_argument(
            "--generate-asv-machine", action="store_true", default=None,
            help="Generate machine.json file for all available benchmarks.")
        mxZipPyBenchmarkArgs = parser.parse_args(mxZipPyBenchmarkArgs)

        generate_asv_conf()
        generate_asv_benchmarks()
        generate_asv_machine(machine_name)

        if mxZipPyBenchmarkArgs.generate_asv_conf != None:
            generate_asv_conf(force=True)
            mx.abort("")

        if mxZipPyBenchmarkArgs.generate_asv_benchmarks != None:
            generate_asv_benchmarks(force=True)
            mx.abort("")

        if mxZipPyBenchmarkArgs.generate_asv_machine != None:
            generate_asv_machine(machine_name, force=True)
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


_zippy_benchmark_executor = ZipPyBenchmarkExecutor()

def zippy_benchmark(args):
    """Run zippy asv benchmark suite."""

    mxZipPyBenchmarkArgs, bmSuiteArgs = mx_benchmark.splitArgs(args, "--")
    if not _mx_graal and not mx_benchmark.java_vm_registry._vms:
        mx_benchmark.add_java_vm(mx_benchmark.DefaultJavaVm("server", "default"))

    if not bmSuiteArgs:
        if _mx_graal:
            bmSuiteArgs = ['--jvm-config', 'graal-core', '--jvm', 'server']
        else:
            bmSuiteArgs = ['--jvm-config', 'default', '--jvm', 'server']

    return _zippy_benchmark_executor.asv_benchmark(mxZipPyBenchmarkArgs, bmSuiteArgs)

mx.update_commands(_suite, {
    'asv-benchmark' : [zippy_benchmark, '--vmargs [vmargs] --runargs [runargs] suite:benchname'],
})
