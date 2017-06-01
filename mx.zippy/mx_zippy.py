from argparse import ArgumentParser
import re
import os
import sys
import subprocess
import urllib2
import mx
import mx_benchmark
import mx_gate
from mx_gate import Task
from mx_unittest import unittest

from mx_zippy_tools import _extract_zippy_internal_options, _zippy_help_options
from mx_zippy_tools import _graal_heuristics_options
import mx_zippy_benchmark
import mx_zippy_benchmark_asv
import mx_zippy_asv_chart
import mx_zippy_junit

_suite = mx.suite('zippy')
_mx_graal = mx.suite("compiler", fatalIfMissing=False)

def check_vm(vm_warning=True, must_be_jvmci=False):
    if not _mx_graal:
        if must_be_jvmci:
            print '** Error ** : graal compiler was not found!'
            sys.exit(1)

        if vm_warning:
            print '** warning ** : graal compiler was not found!! Executing using standard VM..'


def get_jdk():
    if _mx_graal:
        tag = 'jvmci'
    else:
        tag = None
    return mx.get_jdk(tag=tag)


def python(args):
    """run a Python program or shell"""
    do_run_python(args)


def do_run_python(args, extraVmArgs=None, env=None, jdk=None, **kwargs):
    if _zippy_help_options(args):
        sys.exit(1)

    if not env:
        env = os.environ

    if not 'ZIPPY_HOME' in env:
        env['ZIPPY_HOME'] = _suite.dir

    check_vm_env = env['ZIPPY_MUST_USE_GRAAL']
    if check_vm_env:
        if check_vm_env == '1':
            check_vm(must_be_jvmci=True)
        elif check_vm_env == '0':
            check_vm()

    vmArgs, zippyArgs = mx.extract_VM_args(args)
    internalZippyArgs, zippyArgs = _extract_zippy_internal_options(zippyArgs)
    vmArgs = internalZippyArgs + ['-cp', mx.classpath(["edu.uci.python"])]

    if not jdk:
        jdk = get_jdk()

    vmArgs += _graal_heuristics_options(_mx_graal)

    # default: assertion checking is enabled
    if extraVmArgs is None or not '-da' in extraVmArgs:
        vmArgs += ['-ea', '-esa']

    if extraVmArgs:
        vmArgs += extraVmArgs

    # vmArgs = _sanitize_vmArgs(jdk, vmArgs)
    # if len(zippyArgs) > 0:
    vmArgs.append("edu.uci.python.shell.ZipPyMain")
    # else:
    #     print 'Interactive shell is not implemented yet..'
    #     sys.exit(1)

    return mx.run_java(vmArgs + zippyArgs, jdk=jdk, **kwargs)

# mx gate --tags pythonbenchmarktest
# mx gate --tags pythontest
# mx gate --tags fulltest

class ZippyTags:
    test = ['pythontest', 'fulltest']
    benchmarktest = ['pythonbenchmarktest', 'fulltest']

def _gate_python_benchmarks_tests(name, iterations, extra_vmargs=[]):
    vmargs = extra_vmargs + ['-Xms2g', '-Xmx2g']
    run_java = mx.run_java

    if _mx_graal:
        vmargs += ['-Dgraal.TraceTruffleCompilation=true']
        run_java = mx_benchmark.get_java_vm('server', 'graal-core').run_java

    vmargs += ['-cp', mx.classpath(["edu.uci.python"]), "edu.uci.python.shell.ZipPyMain", name, str(iterations)]
    successRe = r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)"
    out = mx.OutputCapture()
    run_java(vmargs, out=mx.TeeOutputCapture(out), err=subprocess.STDOUT)

    if not re.search(successRe, out.data, re.MULTILINE):
        mx.abort('Benchmark "'+ name +'" doesn\'t match success pattern: ' + successRe)


def zippy_gate_runner(suites, tasks, extraVMarguments=None):
    vmargs = extraVMarguments
    if not vmargs or not any(vmargs):
        vmargs = []

    vmargs += _graal_heuristics_options(_mx_graal)
    # Run unit tests
    with Task('ZipPy UnitTests', tasks, tags=ZippyTags.test) as t:
        if t: unittest(['--suite', 'zippy', '--fail-fast'] + vmargs)

    pythonTestBenchmarks = {
        'binarytrees3'  : '12',
        'fannkuchredux3': '9',
        'fasta3'        : '250000',
        'mandelbrot3'   : '600',
        'meteor3'       : '2098',
        'nbody3'        : '100000',
        'spectralnorm3' : '500',
        'richards3'     : '3',
        'bm-ai'         : '0',
        'pidigits'      : '0',
        'pypy-go'       : '1',
    }
    for name, iterations in sorted(pythonTestBenchmarks.iteritems()):
        with Task('PythonBenchmarksTest:' + name, tasks, tags=ZippyTags.benchmarktest) as t:
            if t: _gate_python_benchmarks_tests("zippy/benchmarks/src/benchmarks/" + name + ".py", iterations, vmargs)


def _zippy_gate_runner(args, tasks):
    extra_args = None if not _mx_graal else args.extra_vm_argument
    zippy_gate_runner(['zippy'], tasks, extra_args)

mx_gate.add_gate_runner(_suite, _zippy_gate_runner)


mx.update_commands(_suite, {
    # new commands
    'python' : [python, '[Python args|@VM options]'],
})
