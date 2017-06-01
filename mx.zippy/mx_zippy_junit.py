from argparse import ArgumentParser
from os.path import join, sep
import os, tempfile
import mx
import mx_unittest

_suite = mx.suite('zippy')
_mx_graal = mx.suite("compiler", fatalIfMissing=False)

def _test_srcdir():
    tp = 'edu.uci.python.test'
    return join(mx.project(tp).dir, 'src', tp.replace('.', sep))

def _test_package():
    return 'edu.uci.python.test'

def _test_subpackage(name):
    return '.'.join((_test_package(), name))

def _default_generated_unit_tests():
    return ','.join(map(_test_subpackage, ['basic', 'builtin','datatype', 'decorator', 'generator', 'grammar', 'module', 'runtime']))

def _default_unit_tests():
    return ','.join([_default_generated_unit_tests()])

def junit_default(args):
    return mx.command_function('junit')(['--tests', _default_unit_tests()] + args)

def junit(args):
    '''run ZipPy Junit tests'''
    parser = ArgumentParser(prog='mx junit')
    parser.add_argument('--tests', action='store', help='patterns to match test classes (specify multiple patterns using \',\')')
    parser.add_argument('--J', dest='vm_args', action='append', help='Java VM arguments (e.g. --J @-dsa)', metavar='@<args>')
    parser.add_argument('--jdk', action='store', help='JDK to use for the "java" command')
    args = parser.parse_args(args)

    vmArgs = ['-ea', '-esa']

    # enable when necessary
    # vmArgs += ['-Xss12m']

    if args.vm_args:
        vmArgs = vmArgs + mx_fastr.split_j_args(args.vm_args)

    testfile = os.environ.get('MX_TESTFILE', None)
    if testfile is None:
        (_, testfile) = tempfile.mkstemp(".testclasses", "mx")
        os.close(_)

    if args.jdk:
        jdk = mx.get_jdk(tag=args.jdk)
        if not jdk:
            mx.abort("JDK '" + args.jdk + "' not found!")
    else:
        tag = 'jvmci' if _mx_graal else None
        jdk = mx.get_jdk(tag=tag)

    candidates = []
    for p in mx.projects(opt_limit_to_suite=True, limit_to_primary=True):
        if not p.isJavaProject() or jdk.javaCompliance < p.javaCompliance:
            continue
        candidates += mx_unittest._find_classes_with_annotations(p, None, ['@Test']).keys()

    tests = [] if args.tests is None else [name for name in args.tests.split(',')]
    classes = []
    if len(tests) == 0:
        classes = candidates
    else:
        for test in tests:
            exists = False
            for candidate in candidates:
                if test in candidate:
                    exists = True
                    classes.append(candidate)
            if not exists:
                mx.warn('no tests matched by substring "' + test + '"')

    vmArgs += mx.get_runtime_jvm_args(['ZIPPY', 'ZIPPY_UNIT_TESTS'], jdk=jdk)

    if len(classes) != 0:
        if len(classes) == 1:
            testClassArgs = ['--testclass', classes[0]]
        else:
            with open(testfile, 'w') as f:
                for c in classes:
                    f.write(c + '\n')
            testClassArgs = ['--testsfile', testfile]
        junitArgs = ['edu.uci.python.test.ZipPyJUnitRunner'] + testClassArgs
        rc = mx.run_java(vmArgs + junitArgs, nonZeroIsFatal=False, jdk=jdk)
        return rc
    else:
        return 0

mx.update_commands(_suite, {
    'junit' : [junit, ['options']],
    'junit-default' : [junit_default, ['options']],
})
