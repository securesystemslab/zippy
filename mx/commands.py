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

import os, sys, shutil, zipfile, tempfile, re, time, datetime, platform, subprocess, multiprocessing
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
    

def scaladacapo(args):
    """run one or all Scala DaCapo benchmarks
    
    Scala DaCapo options are distinguished from VM options by a '@' prefix.
    For example, '@--iterations @5' will pass '--iterations 5' to the
    DaCapo harness."""

    numTests = {}
    
    if len(args) > 0:
        level = getattr(sanitycheck.SanityCheckLevel, args[0], None)
        if level is not None:
            del args[0]
            for (bench, ns) in sanitycheck.dacapoScalaSanityWarmup.items():
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
                if bm not in sanitycheck.dacapoScalaSanityWarmup.keys():
                    mx.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(sanitycheck.dacapoScalaSanityWarmup.keys()))
                numTests[bm] = n
    
    if len(numTests) is 0:    
        for bench in sanitycheck.dacapoScalaSanityWarmup.keys():
            numTests[bench] = 1
    
    # Extract DaCapo options
    dacapoArgs = [(arg[1:]) for arg in args if arg.startswith('@')]
    
    # The remainder are VM options 
    vmOpts = [arg for arg in args if not arg.startswith('@')]
    
    failed = []
    for (test, n) in numTests.items():
        if not sanitycheck.getScalaDacapo(test, n, dacapoArgs).test('graal', opts=vmOpts):
            failed.append(test)
    
    if len(failed) != 0:
        mx.abort('Scala DaCapo failures: ' + str(failed))

def _jdk(build='product', create=False):
    """
    Get the JDK into which Graal is installed, creating it first if necessary.
    """
    jdk = join(_graal_home, 'jdk' + mx.java().version, build)
    jdkContents = ['bin', 'db', 'include', 'jre', 'lib']
    if mx.get_os() != 'windows':
        jdkContents.append('man')
    if create:
        if not exists(jdk):
            srcJdk = mx.java().jdk
            mx.log('Creating ' + jdk + ' from ' + srcJdk)
            os.makedirs(jdk)
            for d in jdkContents:
                src = join(srcJdk, d)
                dst = join(jdk, d)
                if not exists(src):
                    mx.abort('Host JDK directory is missing: ' + src)
                shutil.copytree(src, dst)
                
            # Make a copy of the default VM so that this JDK can be
            # reliably used as the bootstrap for a HotSpot build.                
            jvmCfg = join(jdk, 'jre', 'lib', 'amd64', 'jvm.cfg')
            if not exists(jvmCfg):
                mx.abort(jvmCfg + ' does not exist')
                
            lines = []
            defaultVM = None
            with open(jvmCfg) as f:
                for line in f:
                    if line.startswith('-') and defaultVM is None:
                        parts = line.split()
                        assert len(parts) == 2, parts
                        assert parts[1] == 'KNOWN', parts[1]
                        defaultVM = parts[0][1:]
                        lines.append('-' + defaultVM + '0 KNOWN\n')
                    lines.append(line)

            assert defaultVM is not None, 'Could not find default VM in ' + jvmCfg
            shutil.copytree(join(jdk, 'jre', 'lib', 'amd64', defaultVM), join(jdk, 'jre', 'lib', 'amd64', defaultVM + '0'))
            
            with open(jvmCfg, 'w') as f:
                for line in lines:
                    f.write(line)
                    
    else:
        if not exists(jdk):
            mx.abort('The ' + build + ' VM has not been created - run \'mx clean; mx make ' + build + '\'')
    return jdk 

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
    
def build(args, vm=None):
    """build the VM binary
    
    The global '--vm' option selects which VM to build. This command also
    compiles the Graal classes irrespective of what VM is being built.
    The optional last argument specifies what build level is to be used
    for the VM binary."""
    
    # Call mx.build to compile the Java sources        
    opts2 = mx.build(['--source', '1.7'] + args, parser=ArgumentParser(prog='mx build'))

    if not _vmSourcesAvailable or not opts2.native:
        return

    builds = opts2.remainder
    if len(builds) == 0:
        builds = ['product']

    if vm is None:
        vm = _vm
        
    if vm == 'server':
        buildSuffix = ''
    elif vm == 'client':
        buildSuffix = '1'
    else:
        assert vm == 'graal', vm
        buildSuffix = 'graal'
        
    for build in builds:
        jdk = _jdk(build, create=True)
            
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

            variant = {'client': 'compiler1', 'server': 'compiler2'}.get(vm, vm)
            project_config = variant + '_' + build
            _runInDebugShell('msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcproj /p:Configuration=' + project_config + ' /target:clean', _graal_home)
            winCompileCmd = r'set HotSpotMksHome=' + mksHome + r'& set OUT_DIR=' + jdk + r'& set JAVA_HOME=' + jdk + r'& set path=%JAVA_HOME%\bin;%path%;%HotSpotMksHome%& cd /D "' +_graal_home + r'\make\windows"& call create.bat ' + _graal_home
            print(winCompileCmd)
            winCompileSuccess = re.compile(r"^Writing \.vcxproj file:")
            if not _runInDebugShell(winCompileCmd, _graal_home, compilelogfile, winCompileSuccess):
                mx.log('Error executing create command')
                return 
            winBuildCmd = 'msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcxproj /p:Configuration=' + project_config + ' /p:Platform=x64'
            winBuildSuccess = re.compile('Build succeeded.')
            if not _runInDebugShell(winBuildCmd, _graal_home, compilelogfile, winBuildSuccess):
                mx.log('Error building project')
                return 
        else:
            cpus = multiprocessing.cpu_count()
            if build == 'debug':
                build = 'jvmg'
            env = os.environ
            env.setdefault('ARCH_DATA_MODEL', '64')
            env.setdefault('LANG', 'C')
            env.setdefault('HOTSPOT_BUILD_JOBS', str(cpus))
            env['ALT_BOOTDIR'] = jdk
            env.setdefault('INSTALL', 'y')
            
            # Clear these 2 variables as having them set can cause very confusing build problems
            env.pop('LD_LIBRARY_PATH', None)
            env.pop('CLASSPATH', None)
            
            mx.run([mx.gmake_cmd(), build + buildSuffix], cwd=join(_graal_home, 'make'), err=filterXusage)
        
        jvmCfg = join(jdk, 'jre', 'lib', 'amd64', 'jvm.cfg')
        found = False
        if not exists(jvmCfg):
            mx.abort(jvmCfg + ' does not exist')
        
        prefix = '-' + vm
        vmKnown = prefix + ' KNOWN\n'
        lines = []
        with open(jvmCfg) as f:
            for line in f:
                if vmKnown in line:
                    found = True
                    break
                if not line.startswith(prefix):
                    lines.append(line)
        if not found:
            mx.log('Appending "' + prefix + ' KNOWN" to ' + jvmCfg)
            lines.append(vmKnown)
            with open(jvmCfg, 'w') as f:
                for line in lines:
                    f.write(line)
    
def vm(args, vm=None, nonZeroIsFatal=True, out=None, err=None, cwd=None, timeout=None, vmbuild=None):
    """run the VM selected by the '--vm' option"""

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
# containing '@Test'. These are then determined to be the classes defining
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
    
def buildvms(args):
    """build one or more VMs in various configurations"""
    
    parser = ArgumentParser(prog='mx buildvms');
    parser.add_argument('--vms', help='a comma separated list of VMs to build (default: server,client,graal)', default='server,client,graal')
    parser.add_argument('--builds', help='a comma separated list of build types (default: product,fastdebug,debug)', default='product,fastdebug,debug')

    args = parser.parse_args(args)
    vms = args.vms.split(',')
    builds = args.builds.split(',')
    
    allStart = time.time()
    for v in vms:
        for vmbuild in builds:
            logFile = join(v + '-' + vmbuild + '.log')
            log = open(join(_graal_home, logFile), 'wb')
            start = time.time()
            mx.log('BEGIN: ' + v + '-' + vmbuild + '\t(see: ' + logFile + ')')
            # Run as subprocess so that output can be directed to a file
            subprocess.check_call([sys.executable, '-u', join('mxtool', 'mx.py'), '--vm', v, 'build', vmbuild], cwd=_graal_home, stdout=log, stderr=subprocess.STDOUT)
            duration = datetime.timedelta(seconds=time.time() - start)
            mx.log('END:   ' + v + '-' + vmbuild + '\t[' + str(duration) + ']')
    allDuration = datetime.timedelta(seconds=time.time() - allStart)
    mx.log('TOTAL TIME:   ' + '[' + str(allDuration) + ']')

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
             
    parser = ArgumentParser(prog='mx gate');
    parser.add_argument('--omit-native-build', action='store_false', dest='buildNative', help='omit cleaning and building native code')

    args = parser.parse_args(args)

    tasks = []             
    total = Task('Gate')
    try:
        
        t = Task('Clean')
        clean([] if args.buildNative else ['--no-native'])
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
    
        t = Task('CleanAndBuildGraalVisualizer')
        mx.run(['ant', '-f', join(_graal_home, 'visualizer', 'build.xml'), '-q', 'clean', 'build'])
        tasks.append(t.stop())

        # Prevent Graal modifications from breaking the standard builds
        if args.buildNative:
            t = Task('BuildHotSpotVarieties')
            buildvms(['--vms', 'client,server', '--builds', 'fastdebug,product'])
            tasks.append(t.stop())
        
        for vmbuild in ['fastdebug', 'product']:
            global _vmbuild
            _vmbuild = vmbuild
            
            if args.buildNative:
                t = Task('BuildHotSpotGraal:' + vmbuild)
                buildvms(['--vms', 'graal', '--builds', vmbuild])
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
    
    except BaseException as e:
        import traceback
        traceback.print_exc()
        total.abort(str(e))

    total.stop()
    
    mx.log('Gate task times:')
    for t in tasks:
        mx.log('  ' + str(t.duration) + '\t' + t.title)
    mx.log('  =======')
    mx.log('  ' + str(total.duration))

def gv(args):
    """run the Graal Visualizer"""
    mx.run(['ant', '-f', join(_graal_home, 'visualizer', 'build.xml'), '-q', 'run'])
    
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
    vm = _vm
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
    
    if ('scaladacapo' in args or 'all' in args):
        benchmarks += sanitycheck.getScalaDacapos(level=sanitycheck.SanityCheckLevel.Benchmark)
    else:
        dacapos = [a[7:] for a in args if a.startswith('scaladacapo:')]
        for dacapo in dacapos:
            if dacapo not in sanitycheck.dacapoScalaSanityWarmup.keys():
                mx.abort('Unknown dacapo : ' + dacapo)
            benchmarks += [sanitycheck.getScalaDacapo(dacapo, sanitycheck.dacapoScalaSanityWarmup[dacapo][sanitycheck.SanityCheckLevel.Benchmark])]
        
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
    """run one or all SPECjvm2008 benchmarks
    
    All options begining with - will be passed to the vm except for -ikv -wt and -it.
    Other options are supposed to be benchmark names and will be passed to SPECjvm2008."""
    benchArgs = [a for a in args if a[0] != '-']
    vmArgs = [a for a in args if a[0] == '-']
    wt = None
    it = None
    skipValid = False
    if '-ikv' in vmArgs:
        skipValid = True
        vmArgs.remove('-ikv')
    if '-wt' in vmArgs:
        wtIdx = args.index('-wt')
        try:
            wt = int(args[wtIdx+1])
        except:
            mx.abort('-wt (Warmup time) needs a numeric value (seconds)')
        vmArgs.remove('-wt')
        benchArgs.remove(args[wtIdx+1])
    if '-it' in vmArgs:
        itIdx = args.index('-it')
        try:
            it = int(args[itIdx+1])
        except:
            mx.abort('-it (Iteration time) needs a numeric value (seconds)')
        vmArgs.remove('-it')
        benchArgs.remove(args[itIdx+1])
    sanitycheck.getSPECjvm2008(benchArgs, skipValid, wt, it).bench('graal', opts=vmArgs)
    
def hsdis(args):
    """install the hsdis library

    This is needed to support HotSpot's assembly dumping features.
    By default it installs the Intel syntax version, use the 'att' argument to install AT&T syntax."""
    flavor = 'intel'
    if 'att' in args:
        flavor = 'att'
    build = _vmbuild if _vmSourcesAvailable else 'product'
    lib = mx.lib_suffix('hsdis-amd64')
    path = join(_jdk(build), 'jre', 'lib', 'amd64', lib)
    mx.download(path, ['http://lafo.ssw.uni-linz.ac.at/hsdis/' + flavor + "/" + lib])
    
def mx_init():
    _vmbuild = 'product'
    commands = {
        'build': [build, '[-options]'],
        'buildvms': [buildvms, '[-options]'],
        'clean': [clean, ''],
        'copyrightcheck': [copyrightcheck, ''],
        'hsdis': [hsdis, '[att]'],
        'dacapo': [dacapo, '[[n] benchmark] [VM options|@DaCapo options]'],
        'scaladacapo': [scaladacapo, '[[n] benchmark] [VM options|@Scala DaCapo options]'],
        'specjvm2008': [specjvm2008, '[VM options|@specjvm2008 options]'],
        'example': [example, '[-v] example names...'],
        'gate' : [gate, '[-options]'],
        'gv' : [gv, ''],
        'bench' : [bench, '[-resultfile file] [all(default)|dacapo|specjvm2008|bootstrap]'],
        'unittest' : [unittest, '[filters...]'],
        'vm': [vm, '[-options] class [args...]']
    }

    if (_vmSourcesAvailable):
        mx.add_argument('--vm', action='store', dest='vm', default='graal', choices=['graal', 'server', 'client'], help='the VM to build/run (default: graal)')
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
        if hasattr(opts, 'vm') and opts.vm is not None:
            global _vm
            _vm = opts.vm
        if hasattr(opts, 'vmbuild') and opts.vmbuild is not None:
            global _vmbuild
            _vmbuild = opts.vmbuild
