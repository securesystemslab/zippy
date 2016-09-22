from argparse import ArgumentParser
import re
import os
import sys
import urllib2
import mx
import mx_graal_core
import mx_gate
from mx_gate import Task
import mx_zippy_benchmark

_suite = mx.suite('zippy')
_mx_graal = mx.suite("graal-core", fatalIfMissing=False)


def check_vm(vm_warning=True, must_be_jvmci=False):
    if not _mx_graal:
        if must_be_jvmci:
            print '** Error ** : JVMCI was not found!!'
            sys.exit(1)

        if vm_warning:
            print '** warning ** : JVMCI was not found!! Executing using standard VM..'


def get_jdk():
    if _mx_graal:
        tag = 'jvmci'
    else:
        tag = None
    return mx.get_jdk(tag=tag)


def python(args):
    """run a Python program or shell"""
    do_run_python(args)


def do_run_python(args, extraVmArgs=None, jdk=None, **kwargs):

    vmArgs, zippyArgs = mx.extract_VM_args(args)
    vmArgs = ['-cp', mx.classpath(["edu.uci.python"])]

    if not jdk:
        jdk = get_jdk()

    vmArgs += _graal_heuristics_options()

    # default: assertion checking is enabled
    if extraVmArgs is None or not '-da' in extraVmArgs:
        vmArgs += ['-ea', '-esa']

    if extraVmArgs:
        vmArgs += extraVmArgs

    # vmArgs = _sanitize_vmArgs(jdk, vmArgs)
    # if len(zippyArgs) > 0:
    vmArgs.append("edu.uci.python.shell.Shell")
    # else:
    #     print 'Interactive shell is not implemented yet..'
    #     sys.exit(1)

    return mx.run_java(vmArgs + args, jdk=jdk, **kwargs)

def _sanitize_vmArgs(jdk, vmArgs):
    '''
    jdk dependent analysis of vmArgs to remove those that are not appropriate for the
    chosen jdk. It is easier to allow clients to set anything they want and filter them
    out here.
    '''
    jvmci_jdk = jdk.tag == 'jvmci'
    jvmci_disabled = '-XX:-EnableJVMCI' in vmArgs

    xargs = []
    i = 0
    while i < len(vmArgs):
        vmArg = vmArgs[i]
        if vmArg != '-XX:-EnableJVMCI':
            if vmArg.startswith("-") and '-Dgraal' in vmArg or 'JVMCI' in vmArg:
                if not jvmci_jdk or jvmci_disabled:
                    i = i + 1
                    continue
        xargs.append(vmArg)
        i = i + 1
    return xargs

# Graal/Truffle heuristics parameters
def _graal_heuristics_options():
    result = []
    if _mx_graal:
        # result += ['-Dgraal.InliningDepthError=500']
        # result += ['-Dgraal.EscapeAnalysisIterations=3']
        # result += ['-XX:JVMCINMethodSizeLimit=1000000']
        result += ['-XX:+UseJVMCICompiler', '-Djvmci.Compiler=graal']
        result += ['-Xms10g', '-Xmx16g']
        # result += ['-Dgraal.TraceTruffleCompilation=true']
        # result += ['-Dgraal.TruffleInliningMaxCallerSize=150']
        # result += ['-Dgraal.InliningDepthError=10']
        # result += ['-Dgraal.MaximumLoopExplosionCount=1000']
        # result += ['-Dgraal.TruffleCompilationThreshold=100000']
    return result


#mx gate --tags pythonbenchmarktest
#mx gate --tags pythontest
#mx gate --tags fulltest

class ZippyTags:
    test = ['pythontest', 'fulltest']
    benchmarktest = ['pythonbenchmarktest', 'fulltest']

def _gate_python_benchmarks_tests(name, iterations, extraVMarguments=None):
    vmargs = ['-Xms2g', '-Xmx2g', '-Dgraal.TraceTruffleCompilation=true'] + mx_graal_core._noneAsEmptyList(extraVMarguments)
    mx_graal_core._gate_java_benchmark(vmargs + ['-cp', mx.classpath(["edu.uci.python"]), "edu.uci.python.shell.Shell", name, str(iterations)], r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)")

def zippy_gate_runner(suites, unit_test_runs, tasks, extraVMarguments=None):

    # Run unit tests
    for r in unit_test_runs:
        r.run(suites, tasks, mx_graal_core._noneAsEmptyList(extraVMarguments))

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
            if t: _gate_python_benchmarks_tests("zippy/benchmarks/src/benchmarks/" + name + ".py", iterations, mx_graal_core._noneAsEmptyList(extraVMarguments) + ['-XX:+UseJVMCICompiler'])

zippy_unit_test_runs = [
    mx_graal_core.UnitTestRun('UnitTests', ['-XX:-UseJVMCICompiler'], tags=ZippyTags.test),
]

def _zippy_gate_runner(args, tasks):
    zippy_gate_runner(['zippy'], zippy_unit_test_runs, tasks, args.extra_vm_argument)

mx_gate.add_gate_runner(_suite, _zippy_gate_runner)


mx.update_commands(_suite, {
    # core overrides
    # new commands
    'python' : [python, '[Python args|@VM options]'],
})
