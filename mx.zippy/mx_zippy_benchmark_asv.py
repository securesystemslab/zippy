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

from mx_zippy_bench_param import benchmarks_list

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

active_branch = _suite.vc.active_branch(_suite.vc_dir, abortOnError=False)
active_branch = ['master'] if not active_branch else [ active_branch ]
url = _suite.vc.default_pull(_suite.vc_dir).replace('.git','')
url = url if 'html' in url else "https://github.com/securesystemslab/zippy"
html_dir = asv_env + '/html'
asv_dir = asv_env + '/asv/'
asv_results_dir = asv_dir + '/results/'
machine_name = os.environ.get("MACHINE_NAME")
if not machine_name:
    machine_name = platform.node()

machine_name += '-no-graal' if not _mx_graal else '-graal'

machine_results_dir = asv_results_dir + machine_name

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
    gpu_brand = prompt("Unknown", "GPU Model (run: " + gpu_cmd + ")")
    gpu_ram_cmd = "$ clinfo | grep 'Global memory size'"
    gpu_ram = prompt("Unknown", "GPU Memory (run: " + gpu_ram_cmd + ")")
    machine_json = {
        "arch"      : arch,
        "cpu"       : processor_brand,
        "ram"       : ram,
        "gpu"       : gpu_brand,
        "gpu-ram"   : gpu_ram,
        "machine"   : machine_name,
        "os"        : os_name,
        "version"   : 1
    }

    write_to_json(machine_json, path)


def generate_asv_benchmarks(user_benchmarks_list, force=False):
    """benchmarks.json"""
    path_json = asv_results_dir + "benchmarks.json"
    if not exists(asv_results_dir):
        os.mkdir(asv_results_dir)

    if not force and exists(path_json):
        return

    benchmarks_json = {}
    single_benchmark_template = {
        "code"          : "",
        "goal_time"     : 2.0,
        "name"          : "",
        "number"        : 0,
        "param_names"   : [],
        "params"        : [],
        "repeat"        : 0,
        "timeout"       : 60.0,
        "type"          : "time",
        "unit"          : "seconds"
    }

    for bench_list in user_benchmarks_list:
        if bench_list in sorted(benchmarks_list):
            path = benchmarks_list[bench_list][0]
            bms = benchmarks_list[bench_list][1]
            for bench in bms:
                bench_json = copy.deepcopy(single_benchmark_template)
                bench_json['name'] = bench_list + "." + bench
                bench_json['code'] = "mx python " + path + bench + ".py " + " ".join(bms[bench][1])
                if len(bms[bench]) > 0 and bms[bench][0] != '':
                    params = []
                    for i in range(0, len(bms[bench]), 2):
                        params += [bms[bench][i]]
                    bench_json['params'] = [params]
                    bench_json['param_names'] = [["input"]]
                benchmarks_json[bench] = bench_json

    benchmarks_json["version"] = 1
    write_to_json(benchmarks_json, path_json)

def generate_asv_conf(force=False):
    """asv.conf.json"""
    path = asv_dir + "asv.conf.json"
    if not exists(asv_dir):
        os.mkdir(asv_dir)

    if not force and exists(path):
        return

    asv_conf_json = {
        "version"           : 1,
        "project"           : _suite.name,
        "project_url"       : url,
        "repo"              : url + ".git",
        "branches"          : active_branch,
        "environment_type"  : "conda",
        "show_commit_url"   : url + "/commit/",
        "pythons"           : ["3.4"],
        "matrix"            : {},
        "html_dir"          : html_dir,
        "hash_length"       : 8
    }

    write_to_json(asv_conf_json, path)

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

class ASVBenchmarkExecutor(mx_benchmark.BenchmarkExecutor):

    asv_pre_results = {}
    repeat_count = 1

    def dimensions(self, suite, mxBenchmarkArgs, bmSuiteArgs):
        standard = {
          "subgroup"                : suite.subgroup(),
          "bench-suite"             : suite.name(),
          "config.platform-version" : "",
          "commit.rev"              : _suite.vc.parent(_suite.dir),
          "commit.committer-ts"     : _suite.vc.parent_info(_suite.dir)["committer-ts"],
          "warnings"                : "",
        }

        return standard

    def process_result(self, suite, result):
        for t in suite.get_timing():

            if t not in self.asv_pre_results:
                self.asv_pre_results[ t ] = {}

            if result['python.params'] == '':
                self.asv_pre_results[ t ][result['benchmark']] = result[ t ]
            else:
                if result['benchmark'] not in self.asv_pre_results[ t ]:
                    self.asv_pre_results[ t ][result['benchmark']] = { "params": [[ ]], "result": [] }
                self.asv_pre_results[ t ][result['benchmark']]['params'][0] += [result['python.params']]
                self.asv_pre_results[ t ][result['benchmark']]['result'] += [result[ t ]]

    def prepare_asv_dict(self, suite):

        asv_dict = {
            "commit_hash"   : _suite.vc.parent(_suite.dir),
            "date"          : int(_suite.vc.parent_info(_suite.dir)["committer-ts"]) * 1000,
            "params"        : { "machine": machine_name,
                                "interpreter": suite.interpreterName(),
                                "timing": "" },
            "profiles"      : {},
            "python"        : "3.5",
            "requirements"  : {},
            "results"       : {},
            "profiling"     : {},
            "version"       : 1
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

        return latest_run

    def get_file_tag(self, suite):
        if _suite.name in suite.name():
            file_tag = _suite.vc.parent(_suite.dir)[:8] + "-" + suite.name() + "-"
        else:
            file_tag = "ver" + suite.ext_version() + "-" + suite.name() + "-"
        return file_tag

    def write_asv_results(self, suite, results):
        template = self.prepare_asv_dict(suite)
        file_tag = self.get_file_tag(suite)
        is_run   = "profile" not in suite.name()

        if not is_run:
            _profile = dict(template)
            _data    = suite.get_profile_info()
            _profile['params']['timing'] = "profile"
            _profile['profiling'].update(_data)
            write_to_json(_profile, machine_results_dir + "/" + file_tag + "-profile" + ".json")
            return

        last_run = self.get_latest_run(file_tag)
        run      = "-run-" + str( 1 + last_run)

        for t in suite.get_timing():
            _timing = dict(template)
            _timing['params']['timing'] = t
            _timing['results'].update(results[t])
            write_to_json(_timing, machine_results_dir + "/" + file_tag + t + run + ".json")


    def copy_previous_as_new(self, suite, benchNamesList):
        file_tag = self.get_file_tag(suite)
        last_run = self.get_latest_run(file_tag)
        count = self.repeat_count

        for t in suite.get_timing():
            try:
                for c in range(count):
                    run_num  = "-run-" + str(last_run - c)
                    with open(machine_results_dir + "/" + file_tag + t + run_num + ".json") as last_result:
                        last_result = json.load(last_result)
                        last_result['commit_hash'] = _suite.vc.parent(_suite.dir)
                        last_result['date'] = int(_suite.vc.parent_info(_suite.dir)["committer-ts"]) * 1000
                        for bench in benchNamesList:
                            iterations = suite.benchmarksIterations()[bench[0][0]]
                            formated_bench = suite.benchmarksType() + "." + bench[0][0]
                            if formated_bench not in last_result['results']:
                                return 1

                        run_num = "-run-" + str(1 + last_run + c)
                        write_to_json(last_result, machine_results_dir + "/" + file_tag + t + run_num + ".json")
                        self.repeat_count -= 1

                    mx.warn("Copied {0}/{1}".format(c, count))
            except:
                return 1

        return 0

    def asv_benchmark(self, mxASVBenchmarkArgs, bmSuiteArgs):
        """Run ASV benchmark suite."""
        parser = ArgumentParser(
            prog="mx asv-benchmark",
            add_help=False,
            usage="mx asv-benchmark <options> -- <benchmark-suite-args> -- <benchmark-args>",
            formatter_class=RawTextHelpFormatter)
        parser.add_argument(
            "benchmark", nargs="?", default=None,
            help="Benchmark to run, format: <suite>:<benchmark>.")
        parser.add_argument(
            "--list", action="store_true", default=None,
            help="Prints the list of all available benchmark suites.")
        parser.add_argument(
            "--generate-asv-benchmarks", nargs="*", default=None,
            help="Generate benchmarks.json file for all available benchmarks:\n" + " \n".join(benchmarks_list.keys()))
        parser.add_argument(
            "--generate-asv-conf", action="store_true", default=None,
            help="Generate asv.conf.json file for all available benchmarks.")
        parser.add_argument(
            "--generate-asv-machine", action="store_true", default=None,
            help="Generate machine.json file for all available benchmarks.")
        parser.add_argument(
            "--copy-last", action="store_true", default=None,
            help="Copy last run and use the current revision information")
        parser.add_argument(
            "--repeat", nargs="?", default=None,
            help="Repeat action <number> of times")
        parser.add_argument(
            "-h", "--help", action="store_true", default=None,
            help="Show usage information.")
        mxASVBenchmarkArgs = parser.parse_args(mxASVBenchmarkArgs)

        generate_asv_conf()
        generate_asv_machine(machine_name)

        if mxASVBenchmarkArgs.repeat:
            self.repeat_count = int(mxASVBenchmarkArgs.repeat)

        copy_previous = False
        if mxASVBenchmarkArgs.copy_last:
            copy_previous = True

        if mxASVBenchmarkArgs.generate_asv_conf != None:
            generate_asv_conf(force=True)
            mx.abort("")

        if mxASVBenchmarkArgs.generate_asv_benchmarks != None:
            generate_asv_benchmarks(mxASVBenchmarkArgs.generate_asv_benchmarks, force=True)
            mx.abort("")

        if mxASVBenchmarkArgs.generate_asv_machine != None:
            generate_asv_machine(machine_name, force=True)
            mx.abort("")

        if mxASVBenchmarkArgs.list:
            print "The following benchmark suites are available:\n"
            for name in sorted(mx_benchmark._bm_suites):
                if mx_benchmark._bm_suites[name].group() == "zippy":
                    print "  " + name
            mx.abort("")

        if mxASVBenchmarkArgs.help or mxASVBenchmarkArgs.benchmark is None:
            parser.print_help()
            for key, entry in mx_benchmark.parsers.iteritems():
                if mxASVBenchmarkArgs.benchmark is None or key in suite.parserNames():
                    print entry.description
                    entry.parser.print_help()
            mx.abort("")

        if mxASVBenchmarkArgs.benchmark:
            suite, benchNamesList = self.getSuiteAndBenchNames(mxASVBenchmarkArgs, bmSuiteArgs)

        self.checkEnvironmentVars()

        results = []

        if copy_previous:
            if self.copy_previous_as_new(suite, benchNamesList) == 0:
                mx.warn("copied successfully for suite: " + suite.name())
                return 0
            else:
                mx.warn("No matching (no more matching) available to copy from for suite: " + suite.name())

        failures_seen = False
        for c in range(self.repeat_count):
            mx.warn("Run {0}/{1}".format(c, self.repeat_count))
            suite.before(bmSuiteArgs)
            for benchnames in sorted(benchNamesList):
                suite.validateEnvironment()
                try:
                    partialResults = self.execute(suite, benchnames, mxASVBenchmarkArgs, bmSuiteArgs)
                    self.process_result(suite, partialResults[0])
                    results.extend(partialResults)
                except RuntimeError:
                    failures_seen = True
                    failedResults = {
                        "benchmark": "".join(suite.benchmarksType() + "." + benchnames[0][0]),
                        "python.params": "".join(benchnames[0][1]),
                    }
                    for t in suite.get_timing():
                        failedResults[t] = None
                    self.process_result(suite, failedResults)
                    mx.log(traceback.format_exc())
            suite.after(bmSuiteArgs)

            self.write_asv_results(suite, self.asv_pre_results)

        if failures_seen:
            return 1
        return 0


_asv_benchmark_executor = ASVBenchmarkExecutor()

def _asv_benchmark(args):
    """Run asv benchmark suite."""

    mxASVBenchmarkArgs, bmSuiteArgs = mx_benchmark.splitArgs(args, "--")
    if not _mx_graal and not mx_benchmark.java_vm_registry._vms:
        mx_benchmark.add_java_vm(mx_benchmark.DefaultJavaVm("server", "default"))

    if not bmSuiteArgs:
        if _mx_graal:
            bmSuiteArgs = ['--jvm-config', 'graal-core', '--jvm', 'server']
        else:
            bmSuiteArgs = ['--jvm-config', 'default', '--jvm', 'server']

    return _asv_benchmark_executor.asv_benchmark(mxASVBenchmarkArgs, bmSuiteArgs)

mx.update_commands(_suite, {
    'asv-benchmark' : [_asv_benchmark, '--vmargs [vmargs] --runargs [runargs] suite:benchname'],
})
