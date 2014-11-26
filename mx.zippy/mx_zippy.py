from argparse import ArgumentParser
import mx
import mx_graal
import pymarks
import json

def pythonShellCp():
    return mx.classpath("edu.uci.python.shell");

def pythonShellClass():
    return "edu.uci.python.shell.Shell";

def python(args):
    """run a Python program or shell

    VM args should have a @ prefix."""

    vmArgs = [a[1:] for a in args if a[0] == '@']
    pythonArgs = [a for a in args if a[0] != '@']

    mx_graal.vm(vmArgs + ['-cp', pythonShellCp(), pythonShellClass()] + pythonArgs)

def _bench_harness_body(args, vmArgs):
    # args is from ArgumentParser.parseArgs
    resultFile = args.resultfile
    vm = mx_graal._get_vm()
    results = {}
    benchmarks = []
    bmargs = args.remainder

    if 'pythontest' in bmargs:
        benchmarks += pymarks.getPythonTestBenchmarks(vm)

    if 'python' in bmargs:
        benchmarks += pymarks.getPythonBenchmarks(vm)

    if 'python-nopeeling' in bmargs:
        benchmarks += pymarks.getPythonBenchmarksNoPeeling(vm)

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

def bench(args):
    parser = ArgumentParser(prog='mx bench')
    parser.add_argument('-resultfile', action='store', help='result file')

    vm = mx_graal.VM('server' if mx_graal._vm is None else mx_graal._vm)
    with vm:
        mx.bench(args, harness=_bench_harness_body, parser=parser)

def mx_init(suite):
    commands = {
        # new commands
        'python' : [python, '[Python args|@VM options]'],
        # core overrides
        'bench' : [bench, ''],
    }
    mx.update_commands(suite, commands)
