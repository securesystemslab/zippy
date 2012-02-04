#
# commands.py - the GraalVM specific commands
#
# ----------------------------------------------------------------------------------------------------
#
# Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
# ----------------------------------------------------------------------------------------------------

import os, sys, shutil, zipfile, tempfile, re, time, datetime, platform, subprocess, StringIO
from os.path import join, exists, dirname, basename
from argparse import ArgumentParser, REMAINDER
import mx
import sanitycheck
import json

_graal_home = dirname(dirname(__file__))

""" Used to distinguish an exported GraalVM (see 'mx export'). """
_vmSourcesAvailable = exists(join(_graal_home, 'make')) and exists(join(_graal_home, 'src')) 

""" The VM that will be run by the 'vm' command: graal(default), client or server.
    This can be set via the global '--vm' option. """
_vm = 'graal'

""" The VM build that will be run by the 'vm' command: product(default), fastdebug or debug.
    This can be set via the global '--fastdebug' and '--debug' options. """
_vmbuild = 'product'

_copyrightTemplate = """/*
 * Copyright (c) {0}, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
 
"""

def clean(args):
    """cleans the GraalVM source tree"""
    opts = mx.clean(args, parser=ArgumentParser(prog='mx clean'))
    if opts.native:
        os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='16')
        mx.run([mx.gmake_cmd(), 'clean'], cwd=join(_graal_home, 'make'))

def copyrightcheck(args):
    """run copyright check on the Mercurial controlled source files"""
    res = mx.run_java(['-cp', mx.classpath('com.oracle.max.base', resolve=False), 'com.sun.max.tools.CheckCopyright', '-cfp=' + join(mx.project('com.oracle.max.base').dir, '.copyright.regex')] + args)
    mx.log("copyright check result = " + str(res))
    return res

def export(args):
    """create a GraalVM zip file for distribution"""
    
    parser = ArgumentParser(prog='mx export');
    parser.add_argument('--omit-vm-build', action='store_false', dest='vmbuild', help='omit VM build step')
    parser.add_argument('--omit-dist-init', action='store_false', dest='distInit', help='omit class files and IDE configurations from distribution')
    parser.add_argument('zipfile', nargs=REMAINDER, metavar='zipfile')

    args = parser.parse_args(args)
    
    tmp = tempfile.mkdtemp(prefix='tmp', dir=_graal_home)
    if args.vmbuild:
        # Make sure the product VM binary is up to date
        build(['product'])
        
    mx.log('Copying Java sources and mx files...')
    mx.run(('hg archive -I graal -I mx -I mxtool -I mx.sh ' + tmp).split())
    
    # Copy the GraalVM JDK
    mx.log('Copying GraalVM JDK...')
    src = _jdk()
    dst = join(tmp, basename(src))
    shutil.copytree(src, dst)
    zfName = join(_graal_home, 'graalvm-' + mx.get_os() + '.zip')
    zf = zipfile.ZipFile(zfName, 'w')
    for root, _, files in os.walk(tmp):
        for f in files:
            name = join(root, f)
            arcname = name[len(tmp) + 1:]
            zf.write(join(tmp, name), arcname)

    # create class files and IDE configurations
    if args.distInit:
        mx.log('Creating class files...')
        mx.run('mx build'.split(), cwd=tmp)
        mx.log('Creating IDE configurations...')
        mx.run('mx ideinit'.split(), cwd=tmp)
        
    # clean up temp directory
    mx.log('Cleaning up...')
    shutil.rmtree(tmp)
    
    mx.log('Created distribution in ' + zfName)

def example(args):
    """run some or all Graal examples"""
    examples = {
        'safeadd': ['com.oracle.max.graal.examples.safeadd', 'com.oracle.max.graal.examples.safeadd.Main'],
        'vectorlib': ['com.oracle.max.graal.examples.vectorlib', 'com.oracle.max.graal.examples.vectorlib.Main'],
    }

    def run_example(verbose, project, mainClass):
        cp = mx.classpath(project)
        sharedArgs = ['-Xcomp', '-XX:CompileOnly=Main', mainClass]
        
        res = []
        mx.log("=== Server VM ===")
        printArg = '-XX:+PrintCompilation' if verbose else '-XX:-PrintCompilation'
        res.append(vm(['-cp', cp, printArg] + sharedArgs, vm='server'))
        mx.log("=== Graal VM ===")
        printArg = '-G:+PrintCompilation' if verbose else '-G:-PrintCompilation'
        res.append(vm(['-cp', cp, printArg, '-G:-Extend', '-G:-Inline'] + sharedArgs))
        mx.log("=== Graal VM with extensions ===")
        res.append(vm(['-cp', cp, printArg, '-G:+Extend', '-G:-Inline'] + sharedArgs))
        
        if len([x for x in res if x != 0]) != 0:
            return 1
        return 0

    verbose = False
    if '-v' in args:
        verbose = True
        args = [a for a in args if a != '-v']

    if len(args) == 0:
        args = examples.keys()
    for a in args:
        config = examples.get(a)
        if config is None:
            mx.log('unknown example: ' + a + '  {available examples = ' + str(examples.keys()) + '}')
        else:
            mx.log('--------- ' + a + ' ------------')
            project, mainClass = config
            run_example(verbose, project, mainClass)

def dacapo(args):
    """run one or all DaCapo benchmarks
    
    DaCapo options are distinguished from VM options by a '@' prefix.
    For example, '@--iterations @5' will pass '--iterations 5' to the
    DaCapo harness."""

    numTests = {}
    
    if len(args) > 0:
        level = getattr(sanitycheck.SanityCheckLevel, args[0], None)
        if level is not None:
            del args[0]
            for (bench, ns) in sanitycheck.dacapoSanityWarmup.items():
                if ns[level] > 0:
                    numTests[bench] = ns[level]
        else:
            while len(args) != 0 and args[0][0] not in ['-', '@']:
                n = 1
                if args[0].isdigit():
                    n = int(args[0])
                    assert len(args) > 1 and args[1][0] not in ['-', '@'] and not args[1].isdigit()
                    bm = args[1]
                    del args[0]
                else:
                    bm = args[0]
                
                del args[0]
                if bm not in sanitycheck.dacapoSanityWarmup.keys():
                    mx.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(sanitycheck.dacapoSanityWarmup.keys()))
                numTests[bm] = n
    
    if len(numTests) is 0:    
        for bench in sanitycheck.dacapoSanityWarmup.keys():
            numTests[bench] = 1
    
    # Extract DaCapo options
    dacapoArgs = [(arg[1:]) for arg in args if arg.startswith('@')]
    
    # The remainder are VM options 
    vmOpts = [arg for arg in args if not arg.startswith('@')]
    
    failed = []
    for (test, n) in numTests.items():
        if not sanitycheck.getDacapo(test, n, dacapoArgs).test('graal', opts=vmOpts):
            failed.append(test)
    
    if len(failed) != 0:
        mx.abort('DaCapo failures: ' + str(failed))
 
def _jdk(build='product', create=False):
    """
    Get the JDK into which Graal is installed, creating it first if necessary.
    """
    jdk = join(_graal_home, 'jdk' + mx.java().version)
    jdkContents = ['bin', 'db', 'include', 'jre', 'lib']
    if mx.get_os() != 'windows':
        jdkContents.append('man')
    if not exists(jdk):
        srcJdk = mx.java().jdk
        mx.log('Creating ' + jdk + ' from ' + srcJdk)
        os.mkdir(jdk)
        for d in jdkContents:
            src = join(srcJdk, d)
            dst = join(jdk, d)
            if not exists(src):
                mx.abort('Host JDK directory is missing: ' + src)
            shutil.copytree(src, dst)
    
    jvmCfg = join(jdk, 'jre', 'lib', 'amd64', 'jvm.cfg')
    found = False
    if not exists(jvmCfg):
        mx.abort(jvmCfg + ' does not exist')
        
    with open(jvmCfg) as f:
        for line in f:
            if '-graal KNOWN' in line:
                found = True
                break
    if not found:
        mx.log('Appending "-graal KNOWN" to ' + jvmCfg)
        with open(jvmCfg, 'a') as f:
            f.write('-graal KNOWN\n')
    
    if build == 'product':
        return jdk
    elif build in ['debug', 'fastdebug']:
        res = join(jdk, build)
        if not exists(res):
            if not create:
                mx.abort('The ' + build + ' VM has not been created - run \'mx clean; mx make ' + build + '\'') 
            mx.log('Creating ' + res)
            os.mkdir(res)
            for d in jdkContents:
                shutil.copytree(join(jdk, d), join(res, d))
        return res
    else:
        mx.abort('Unknown build type: ' + build)

# run a command in the windows SDK Debug Shell
def _runInDebugShell(cmd, workingDir, logFile=None, findInOutput=None, respondTo={}):
    newLine = os.linesep
    STARTTOKEN = 'RUNINDEBUGSHELL_STARTSEQUENCE'
    ENDTOKEN = 'RUNINDEBUGSHELL_ENDSEQUENCE'
    
    winSDK = mx.get_env('WIN_SDK', 'C:\\Program Files\\Microsoft SDKs\\Windows\\v7.1\\')

    p = subprocess.Popen('cmd.exe /E:ON /V:ON /K ""' + winSDK + '/Bin/SetEnv.cmd" & echo ' + STARTTOKEN + '"', \
            shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
    stdout = p.stdout
    stdin = p.stdin
    if logFile:
        log = open(logFile, 'w')
    ret = False
    while True:
        line = stdout.readline().decode()
        if logFile:
            log.write(line)
        line = line.strip()
        mx.log(line)
        if line == STARTTOKEN:
            stdin.write('cd /D ' + workingDir + ' & ' + cmd + ' & echo ' + ENDTOKEN + newLine)
        for regex in respondTo.keys():
            match = regex.search(line)
            if match:
                stdin.write(respondTo[regex] + newLine)
        if findInOutput:
            match = findInOutput.search(line)
            if match:
                ret = True
        if line == ENDTOKEN:
            break
    stdin.write('exit' + newLine)
    if logFile:
        log.close()
    return ret
    
def build(args):
    """builds the GraalVM binary and compiles the Graal classes
    
    The optional last argument specifies what type of VM to build."""


    parser = ArgumentParser(prog='mx build');
    parser.add_argument('--vm', action='store', dest='vm', default='graal', choices=['graal', 'server', 'client'], help='the VM to be built')
    
    # Call mx.build to compile the Java sources        
    opts = mx.build(['--source', '1.7'] + args, parser=parser)

    if not _vmSourcesAvailable or not opts.native:
        return

    builds = opts.remainder
    if len(builds) == 0:
        builds = ['product']

    vm = opts.vm
    if vm == 'server':
        buildSuffix = ''
    elif vm == 'client':
        buildSuffix = '1'
    else:
        assert vm is 'graal'
        buildSuffix = 'graal'
        
    for build in builds:

        jdk = _jdk(build, True)
        if build == 'debug':
            build = 'jvmg'
            
        vmDir = join(jdk, 'jre', 'lib', 'amd64', vm)
        if not exists(vmDir):
            mx.log('Creating VM directory in JDK7: ' + vmDir)
            os.makedirs(vmDir)
    
        def filterXusage(line):
            if not 'Xusage.txt' in line:
                sys.stderr.write(line + os.linesep)
                
        if platform.system() == 'Windows':
            compilelogfile = _graal_home + '/graalCompile.log'
            mksHome = mx.get_env('MKS_HOME', 'C:\\cygwin\\bin')

            _runInDebugShell('msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcproj /p:Configuration=compiler1_product /target:clean', _graal_home)
            winCompileCmd = r'set HotSpotMksHome=' + mksHome + r'& set OUT_DIR=' + jdk + r'& set JAVA_HOME=' + jdk + r'& set path=%JAVA_HOME%\bin;%path%;%HotSpotMksHome%& cd /D "' +_graal_home + r'\make\windows"& call create.bat ' + _graal_home + ''
            print(winCompileCmd)
            winCompileSuccess = re.compile(r"^Writing \.vcxproj file:")
            if not _runInDebugShell(winCompileCmd, _graal_home, compilelogfile, winCompileSuccess):
                mx.log('Error executing create command')
                return 
            winBuildCmd = 'msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcxproj /p:Configuration=compiler1_product /p:Platform=x64'
            winBuildSuccess = re.compile('Build succeeded.')
            if not _runInDebugShell(winBuildCmd, _graal_home, compilelogfile, winBuildSuccess):
                mx.log('Error building project')
                return 
        else:
            env = os.environ
            env.setdefault('ARCH_DATA_MODEL', '64')
            env.setdefault('LANG', 'C')
            env.setdefault('HOTSPOT_BUILD_JOBS', '3')
            env['ALT_BOOTDIR'] = jdk
            env.setdefault('INSTALL', 'y')
            mx.run([mx.gmake_cmd(), build + buildSuffix], cwd=join(_graal_home, 'make'), err=filterXusage)
    
def vm(args, vm=None, nonZeroIsFatal=True, out=None, err=None, cwd=None, timeout=None, vmbuild=None):
    """run the GraalVM"""

    if vm is None:
        vm = _vm
        
    build = vmbuild if vmbuild is not None else _vmbuild if _vmSourcesAvailable else 'product'
    mx.expand_project_in_args(args)  
    if mx.java().debug:
        args = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000'] + args
    exe = join(_jdk(build), 'bin', mx.exe_suffix('java'))
    return mx.run([exe, '-' + vm] + args, nonZeroIsFatal=nonZeroIsFatal, out=out, err=err, cwd=cwd, timeout=timeout)


# Table of unit tests.
# Keys are project names, values are package name lists.
# All source files in the given (project,package) pairs are scanned for lines
# containing '@Test'. These are then detemrined to be the classes defining
# unit tests.
_unittests = {
    'com.oracle.max.graal.tests': ['com.oracle.max.graal.compiler.tests'],
}

def _add_test_classes(testClassList, searchDir, pkgRoot):
    pkgDecl = re.compile(r"^package\s+([a-zA-Z_][\w\.]*)\s*;$")
    for root, _, files in os.walk(searchDir):
        for name in files:
            if name.endswith('.java') and name != 'package-info.java':
                hasTest = False
                with open(join(root, name)) as f:
                    pkg = None
                    for line in f:
                        if line.startswith("package "):
                            match = pkgDecl.match(line)
                            if match:
                                pkg = match.group(1)
                        else:
                            if line.strip().startswith('@Test'):
                                hasTest = True
                                break
                if hasTest:
                    assert pkg is not None
                    testClassList.append(pkg + '.' + name[:-len('.java')])

def unittest(args):
    """run the Graal Compiler Unit Tests in the GraalVM
    
    If filters are supplied, only tests whose fully qualified name
    include a filter as a substring are run. Negative filters are
    those with a '-' prefix."""
    
    pos = [a for a in args if a[0] != '-']
    neg = [a[1:] for a in args if a[0] == '-']

    def containsAny(c, substrings):
        for s in substrings:
            if s in c:
                return True
        return False
    
    for proj in _unittests.iterkeys():
        p = mx.project(proj)
        classes = []
        for pkg in _unittests[proj]:
            _add_test_classes(classes, join(p.dir, 'src'), pkg)
    
        if len(pos) != 0:
            classes = [c for c in classes if containsAny(c, pos)]
        if len(neg) != 0:
            classes = [c for c in classes if not containsAny(c, neg)]
            
        # (ds) The boot class path must be used for some reason I don't quite understand
        vm(['-XX:-BootstrapGraal', '-esa', '-Xbootclasspath/a:' + mx.classpath(proj), 'org.junit.runner.JUnitCore'] + classes)
    
def gate(args):
    """run the tests used to validate a push

    If this command exits with a 0 exit code, then the source code is in
    a state that would be accepted for integration into the main repository."""
    
    
    
    class Task:
        def __init__(self, title):
            self.start = time.time()
            self.title = title
            self.end = None
            self.duration = None
            mx.log(time.strftime('gate: %d %b %Y %H:%M:%S: BEGIN: ') + title)
        def stop(self):
            self.end = time.time()
            self.duration = datetime.timedelta(seconds=self.end - self.start)
            mx.log(time.strftime('gate: %d %b %Y %H:%M:%S: END:   ') + self.title + ' [' + str(self.duration) + ']')
            return self
        def abort(self, codeOrMessage):
            self.end = time.time()
            self.duration = datetime.timedelta(seconds=self.end - self.start)
            mx.log(time.strftime('gate: %d %b %Y %H:%M:%S: ABORT: ') + self.title + ' [' + str(self.duration) + ']')
            mx.abort(codeOrMessage)
            return self
             
    tasks = []             
    total = Task('Gate')
    try:
        
        t = Task('Clean')
        clean([])
        tasks.append(t.stop())
        
        t = Task('Checkstyle')
        if mx.checkstyle([]) != 0:
            t.abort('Checkstyle warnings were found')
        tasks.append(t.stop())
    
        t = Task('Canonicalization Check')
        mx.log(time.strftime('%d %b %Y %H:%M:%S - Ensuring mx/projects files are canonicalized...'))
        if mx.canonicalizeprojects([]) != 0:
            t.abort('Rerun "mx canonicalizeprojects" and check-in the modified mx/projects files.')
        tasks.append(t.stop())
    
        t = Task('BuildJava')
        build(['--no-native'])
        tasks.append(t.stop())
    
        for vmbuild in ['product', 'fastdebug']:
            global _vmbuild
            _vmbuild = vmbuild
            
            t = Task('BuildHotSpot:' + vmbuild)
            build(['--no-java', vmbuild])
            tasks.append(t.stop())
            
            t = Task('BootstrapWithSystemAssertions:' + vmbuild)
            vm(['-esa', '-version'])
            tasks.append(t.stop())
            
            t = Task('UnitTests:' + vmbuild)
            unittest([])
            tasks.append(t.stop())
            
            for test in sanitycheck.getDacapos(level=sanitycheck.SanityCheckLevel.Gate, gateBuildLevel=vmbuild):
                t = Task(str(test) + ':' + vmbuild)
                if not test.test('graal'):
                    t.abort(test.group + ' ' + test.name + ' Failed')
                tasks.append(t.stop())
    except KeyboardInterrupt:
        total.abort(1)
    
    except Exception as e:
        import traceback
        traceback.print_exc()
        total.abort(str(e))

    total.stop()
    
    mx.log('Gate task times:')
    for t in tasks:
        mx.log('  ' + str(t.duration) + '\t' + t.title)
    mx.log('  =======')
    mx.log('  ' + str(total.duration))

def bench(args):
    """run benchmarks and parse their output for results

    Results are JSON formated : {group : {benchmark : score}}."""
    resultFile = None
    if '-resultfile' in args:
        index = args.index('-resultfile')
        if index + 1 < len(args):
            resultFile = args[index + 1]
            del args[index]
            del args[index]
        else:
            mx.abort('-resultfile must be followed by a file name')
    vm = 'graal'
    if '-vm' in args:
        index = args.index('-vm')
        if index + 1 < len(args):
            vm = args[index + 1]
            del args[index]
            del args[index]
        else:
            mx.abort('-vm must be followed by a vm name (graal, server, client..)')
    if len(args) is 0:
        args += ['all']

    results = {}
    benchmarks = []
    #DaCapo
    if ('dacapo' in args or 'all' in args):
        benchmarks += sanitycheck.getDacapos(level=sanitycheck.SanityCheckLevel.Benchmark)
    else:
        dacapos = [a[7:] for a in args if a.startswith('dacapo:')]
        for dacapo in dacapos:
            if dacapo not in sanitycheck.dacapoSanityWarmup.keys():
                mx.abort('Unknown dacapo : ' + dacapo)
            benchmarks += [sanitycheck.getDacapo(dacapo, sanitycheck.dacapoSanityWarmup[dacapo][sanitycheck.SanityCheckLevel.Benchmark])]
        
    #Bootstrap
    if ('bootstrap' in args or 'all' in args):
        benchmarks += sanitycheck.getBootstraps()
    #SPECjvm2008
    if ('specjvm2008' in args or 'all' in args):
        benchmarks += [sanitycheck.getSPECjvm2008([], True, 120, 120)]
    else:
        specjvms = [a[12:] for a in args if a.startswith('specjvm2008:')]
        for specjvm in specjvms:
            benchmarks += [sanitycheck.getSPECjvm2008([specjvm], True, 120, 120)]
    
    for test in benchmarks:
        if not results.has_key(test.group):
            results[test.group] = {}
        results[test.group].update(test.bench(vm))
    mx.log(json.dumps(results))
    if resultFile:
        with open(resultFile, 'w') as f:
            f.write(json.dumps(results))
    
def specjvm2008(args):
    benchArgs = [a[1:] for a in args if a[0] == '@']
    vmArgs = [a for a in args if a[0] != '@']
    sanitycheck.getSPECjvm2008(benchArgs).bench('graal', opts=vmArgs)
    
def mx_init():
    _vmbuild = 'product'
    commands = {
        'build': [build, '[-options]'],
        'clean': [clean, ''],
        'copyrightcheck': [copyrightcheck, ''],
        'dacapo': [dacapo, '[[n] benchmark] [VM options|@DaCapo options]'],
        'specjvm2008': [specjvm2008, ''],
        'example': [example, '[-v] example names...'],
        'gate' : [gate, ''],
        'bench' : [bench, '[-vm vm] [-resultfile file] [all(default)|dacapo|specjvm2008|bootstrap]'],
        'unittest' : [unittest, '[filters...]'],
        'vm': [vm, '[-options] class [args...]']
    }

    if (_vmSourcesAvailable):
        mx.add_argument('--vm', action='store', dest='vm', default='graal', choices=['graal', 'server', 'client'], help='the VM to run (default: graal)')
        mx.add_argument('--product', action='store_const', dest='vmbuild', const='product', help='select the product build of the VM')
        mx.add_argument('--debug', action='store_const', dest='vmbuild', const='debug', help='select the debug build of the VM')
        mx.add_argument('--fastdebug', action='store_const', dest='vmbuild', const='fastdebug', help='select the fast debug build of the VM')
        
        commands.update({
            'export': [export, '[-options] [zipfile]'],
            'build': [build, '[-options] [product|debug|fastdebug]...']
        })
    
    mx.commands.update(commands)

def mx_post_parse_cmd_line(opts):
    version = mx.java().version
    parts = version.split('.')
    assert len(parts) >= 2
    assert parts[0] == '1'
    major = int(parts[1])
    if not major >= 7:
        mx.abort('Requires Java version 1.7 or greater, got version ' + version)
    
    if (_vmSourcesAvailable):
        global _vm
        _vm = opts.vm
        if hasattr(opts, 'vmbuild') and opts.vmbuild is not None:
            global _vmbuild
            _vmbuild = opts.vmbuild
