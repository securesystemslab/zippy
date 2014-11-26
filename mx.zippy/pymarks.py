from outputparser import OutputParser, ValuesMatcher
import re, mx, mx_graal, os, sys, StringIO, subprocess
from os.path import isfile, join, exists
from sanitycheck import Test

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
}

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
    'whoosh-bench'    : '5000',
    'pymaging-bench'  : '5000',
    'sympy-bench'     : '20000',
}

pythonProfilerBenchmarks = {
    'binarytrees3t'   : '18',
    'fannkuchredux3t' : '11',
    'mandelbrot3t'    : '4000',
    'nbody3t'         : '5000000',
    'spectralnorm3t'  : '3000',
    'pidigits-timed'  : '0',
    'richards3-timed' : '200'
}

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
    'whoosh-bench'    : '5000',
    'pymaging-bench'  : '5000',
    'sympy-bench'     : '20000',
}

pythonGeneratorBenchmarks = {
    'euler31-timed'   : '200',
    'euler11-timed'   : '10000',
    'ai-nqueen-timed' : '10',
    'pads-eratosthenes-timed' : '100000',
    'pads-integerpartitions' : '700',
    'pads-lyndon'     : '100000000',
    'python-graph-bench': '200',
    'simplejson-bench': '10000',
    'whoosh-bench'    : '5000',
    'pymaging-bench'  : '5000',
    'sympy-bench'     : '20000',
}

def getPythonTestBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonTestBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getPython2MicroBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = python2MicroBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/micro/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getPythonMicroBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonMicroBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/micro/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getPythonBenchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getPythonBenchmarksNoPeeling(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonGeneratorBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg, "-no-generator-peeling"]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))

    return tests

def getPythonBenchmarksProfiling(vm, profile_option=None):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = pythonProfilerBenchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/benchmarks/" + benchmark + ".py"
        if (profile_option is not None):
            cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg, profile_option, "-sort"]      
        else :
            cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]      
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getPython2Benchmarks(vm):
    success, error, matcher = getSuccessErrorMatcher()
    benchmarks = python2Benchmarks
    tests = []
    for benchmark, arg in benchmarks.iteritems():
        script = "edu.uci.python.benchmark/src/benchmarks/" + benchmark + ".py"
        cmd = ['-cp', mx.classpath("edu.uci.python.shell"), "edu.uci.python.shell.Shell", script, arg]
        vmOpts = ['-Xms2g', '-Xmx2g']
        tests.append(Test("Python-" + benchmark, cmd, successREs=[success], failureREs=[error], scoreMatchers=[matcher], vmOpts=vmOpts))
    
    return tests

def getSuccessErrorMatcher():
    score = re.compile(r"^(?P<benchmark>[a-zA-Z0-9\.\-]+): (?P<score>[0-9]+(\.[0-9]+)?$)", re.MULTILINE)
    error = re.compile(r"Exception")
    success = score #re.compile(r"^Score \(version \d\): (?:[0-9]+(?:\.[0-9]+)?)", re.MULTILINE)
    matcher = ValuesMatcher(score, {'group' : 'Python', 'name' : '<benchmark>', 'score' : '<score>'})
    return success, error, matcher

