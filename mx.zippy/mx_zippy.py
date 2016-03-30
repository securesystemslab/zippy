from argparse import ArgumentParser
import mx
import pymarks
import json
import os
import sys
import urllib2

_suite = mx.suite('zippy')
_mx_jvmci = mx.suite("jvmci", fatalIfMissing=False)

def check_vm(vm_warning=True, must_be_jvmci=False):
    if not _mx_jvmci:
        if must_be_jvmci:
            print '** Error ** : JVMCI was not found!!'
            sys.exit(1)

        if vm_warning:
            print '** warning ** : JVMCI was not found!! Executing using standard VM..'

def get_jdk(vm_warning=True, must_be_jvmci=False):
    if not _mx_jvmci:
        check_vm(vm_warning=True, must_be_jvmci=False)
        return mx.get_jdk()
    else:
        return _mx_jvmci.extensions.get_jvmci_jdk()

def python(args):
    """run a Python program or shell"""
    do_run_python(args)


def do_run_python(args, extraVmArgs=None, jdk=None, nonZeroIsFatal=True):

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

    if len(zippyArgs) > 0:
        vmArgs.append("edu.uci.python.shell.Shell")
    else:
        print 'Interactive shell is not implemented yet..'
        sys.exit(1)

    return mx.run_java(vmArgs + args, nonZeroIsFatal=nonZeroIsFatal, jdk=jdk)

# Graal/Truffle heuristics parameters
def _graal_heuristics_options():
    result = []
    if _mx_jvmci:
        # result += ['-Dgraal.InliningDepthError=500']
        # result += ['-Dgraal.EscapeAnalysisIterations=3']
        # result += ['-XX:JVMCINMethodSizeLimit=1000000']
        result += ['-Xms2g', '-Xmx2g']
        # result += ['-Dgraal.TraceTruffleCompilation=true']
        # result += ['-Dgraal.TruffleInliningMaxCallerSize=150']
        # result += ['-Dgraal.InliningDepthError=10']
        # result += ['-Dgraal.MaximumLoopExplosionCount=1000']
        # result += ['-Dgraal.TruffleCompilationThreshold=100000']
    return result


def bench(args):
    parser = ArgumentParser(prog='mx bench')
    parser.add_argument('-resultfile', action='store', help='result file')

    mx.bench(args, harness=_bench_harness_body, parser=parser)


def _bench_harness_body(args, vmArgs):
    # args is from ArgumentParser.parseArgs
    resultFile = args.resultfile
    check_vm(must_be_jvmci=True)
    vm = _mx_jvmci.extensions.get_vm()
    results = {}
    benchmarks = []
    bmargs = args.remainder

    if 'pythontest' in bmargs:
        benchmarks += pymarks.getPythonTestBenchmarks(vm)

    if 'python' in bmargs:
        benchmarks += pymarks.getPythonBenchmarks(vm)

    if 'python-nopeeling' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksNoPeeling(vm)

    if 'python-flex' in bmargs:
        benchmarks += pymarks.getPythonObjectBenchmarksFlex(vm)

    if 'python-flex-evol' in bmargs:
        benchmarks += pymarks.getPythonObjectBenchmarksFlexStorageEvolution(vm)

    if 'python-profile' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm)

    if 'python-profile-calls' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-calls")

    if 'python-profile-control-flow' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-control-flow")

    if 'python-profile-variable-accesses' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-variable-accesses")

    if 'python-profile-operations' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-operations")

    if 'python-profile-collection-operations' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-collection-operations")

    if 'python-profile-type-distribution' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm, "-profile-type-distribution")

    if 'cpython2' in bmargs:
        benchmarks += pymarks.getPython2Benchmarks(vm)
        vm = 'cpython2'

    if 'cpython' in bmargs:
        benchmarks += pymarks.getPythonBenchmarks(vm)
        vm = 'cpython'

    if 'cpython-profile' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm)
        vm = 'cpython-profile'

    if 'jython' in bmargs:
        benchmarks += pymarks.getPython2Benchmarks(vm)
        vm = 'jython'

    if 'pypy' in bmargs:
        benchmarks += pymarks.getPython2Benchmarks(vm)
        vm = 'pypy'

    if 'pypy3' in bmargs:
        benchmarks += pymarks.getPythonBenchmarks(vm)
        vm = 'pypy3'

    if 'pypy-profile' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm)
        vm = 'pypy-profile'

    if 'pypy3-profile' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksProfiling(vm)
        vm = 'pypy3-profile'

    if 'python-micro' in bmargs:
        benchmarks += pymarks.getPythonMicroBenchmarks(vm)

    if 'cpython-micro' in bmargs:
        benchmarks += pymarks.getPython2MicroBenchmarks(vm)
        vm = 'cpython'

    if 'jython-micro' in bmargs:
        benchmarks += pymarks.getPython2MicroBenchmarks(vm)
        vm = 'jython'

    if 'pypy-micro' in bmargs:
        benchmarks += pymarks.getPythonMicroBenchmarks(vm)
        vm = 'pypy'

    if 'pypy3-micro' in bmargs:
        benchmarks += pymarks.getPythonMicroBenchmarks(vm)
        vm = 'pypy3'

    for test in benchmarks:
        for (groupName, res) in test.bench(vm, extraVmOpts=vmArgs).items():
            group = results.setdefault(groupName, {})
            group.update(res)
    mx.log(json.dumps(results))
    if resultFile:
        with open(resultFile, 'w') as f:
            f.write(json.dumps(results))



mx.update_commands(_suite, {
    'bench' : [bench, ''],
    'python' : [python, '[Python args|@VM options]'],
})
