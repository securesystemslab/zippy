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

import os, stat, errno, sys, shutil, zipfile, tarfile, tempfile, re, time, datetime, platform, subprocess, multiprocessing, StringIO, socket
from os.path import join, exists, dirname, basename, getmtime
from argparse import ArgumentParser, RawDescriptionHelpFormatter, REMAINDER
from outputparser import OutputParser, ValuesMatcher
import mx
import xml.dom.minidom
import sanitycheck
import itertools
import json, textwrap
import fnmatch

# This works because when mx loads this file, it makes sure __file__ gets an absolute path
_graal_home = dirname(dirname(__file__))

""" Used to distinguish an exported GraalVM (see 'mx export'). """
_vmSourcesAvailable = exists(join(_graal_home, 'make')) and exists(join(_graal_home, 'src'))

""" The VMs that can be built and run along with an optional description. Only VMs with a
    description are listed in the dialogue for setting the default VM (see _get_vm()). """
_vmChoices = {
    'graal' : 'Normal compilation is performed with a tiered system (C1 + Graal), Truffle compilation is performed with Graal.',
    'server' : 'Normal compilation is performed with a tiered system (C1 + C2), Truffle compilation is performed with Graal. Use this for optimal Truffle performance.',
    'client' : None,  # normal compilation with client compiler, explicit compilation (e.g., by Truffle) with Graal
    'server-nograal' : None,  # all compilation with tiered system (i.e., client + server), Graal omitted
    'client-nograal' : None,  # all compilation with client compiler, Graal omitted
    'original' : None,  # default VM copied from bootstrap JDK
}

""" The VM that will be run by the 'vm' command and built by default by the 'build' command.
    This can be set via the global '--vm' option or the DEFAULT_VM environment variable.
    It can also be temporarily set by using of a VM context manager object in a 'with' statement. """
_vm = None

""" The VM builds that will be run by the 'vm' command - default is first in list """
_vmbuildChoices = ['product', 'fastdebug', 'debug', 'optimized']

""" The VM build that will be run by the 'vm' command.
    This can be set via the global '--vmbuild' option.
    It can also be temporarily set by using of a VM context manager object in a 'with' statement. """
_vmbuild = _vmbuildChoices[0]

_jacoco = 'off'

""" The current working directory to switch to before running the VM. """
_vm_cwd = None

""" The base directory in which the JDKs cloned from $JAVA_HOME exist. """
_installed_jdks = None

""" Prefix for running the VM. """
_vm_prefix = None

_make_eclipse_launch = False

_minVersion = mx.VersionSpec('1.8')

JDK_UNIX_PERMISSIONS = 0755

def isVMSupported(vm):
    if 'client' in vm and len(platform.mac_ver()[0]) != 0:
        # Client VM not supported: java launcher on Mac OS X translates '-client' to '-server'
        return False
    return True

def _get_vm():
    """
    Gets the configured VM, presenting a dialogue if there is no currently configured VM.
    """
    global _vm
    if _vm:
        return _vm
    vm = mx.get_env('DEFAULT_VM')
    if vm is None:
        if not sys.stdout.isatty():
            mx.abort('Need to specify VM with --vm option or DEFAULT_VM environment variable')
        envPath = join(_graal_home, 'mx', 'env')
        mx.log('Please select the VM to be executed from the following: ')
        items = [k for k in _vmChoices.keys() if _vmChoices[k] is not None]
        descriptions = [_vmChoices[k] for k in _vmChoices.keys() if _vmChoices[k] is not None]
        vm = mx.select_items(items, descriptions, allowMultiple=False)
        if mx.ask_yes_no('Persist this choice by adding "DEFAULT_VM=' + vm + '" to ' + envPath, 'y'):
            with open(envPath, 'a') as fp:
                print >> fp, 'DEFAULT_VM=' + vm
    _vm = vm
    return vm

"""
A context manager that can be used with the 'with' statement to set the VM
used by all VM executions within the scope of the 'with' statement. For example:

    with VM('server'):
        dacapo(['pmd'])
"""
class VM:
    def __init__(self, vm=None, build=None):
        assert vm is None or vm in _vmChoices.keys()
        assert build is None or build in _vmbuildChoices
        self.vm = vm if vm else _vm
        self.build = build if build else _vmbuild
        self.previousVm = _vm
        self.previousBuild = _vmbuild

    def __enter__(self):
        global _vm, _vmbuild
        _vm = self.vm
        _vmbuild = self.build

    def __exit__(self, exc_type, exc_value, traceback):
        global _vm, _vmbuild
        _vm = self.previousVm
        _vmbuild = self.previousBuild

def _chmodDir(chmodFlags, dirname, fnames):
    os.chmod(dirname, chmodFlags)
    for name in fnames:
        os.chmod(os.path.join(dirname, name), chmodFlags)

def chmodRecursive(dirname, chmodFlags):
    os.path.walk(dirname, _chmodDir, chmodFlags)

def clean(args):
    """clean the GraalVM source tree"""
    opts = mx.clean(args, parser=ArgumentParser(prog='mx clean'))

    if opts.native:
        def handleRemoveReadonly(func, path, exc):
            excvalue = exc[1]
            if mx.get_os() == 'windows' and func in (os.rmdir, os.remove) and excvalue.errno == errno.EACCES:
                os.chmod(path, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)  # 0777
                func(path)
            else:
                raise

        def rmIfExists(name):
            if os.path.isdir(name):
                shutil.rmtree(name, ignore_errors=False, onerror=handleRemoveReadonly)
            elif os.path.isfile(name):
                os.unlink(name)

        rmIfExists(join(_graal_home, 'build'))
        rmIfExists(join(_graal_home, 'build-nograal'))
        rmIfExists(_jdksDir())

def export(args):
    """create archives of builds split by vmbuild and vm"""

    parser = ArgumentParser(prog='mx export')
    args = parser.parse_args(args)

    # collect data about export
    infos = dict()
    infos['timestamp'] = time.time()

    hgcfg = mx.HgConfig()
    hgcfg.check()
    infos['revision'] = hgcfg.tip('.') + ('+' if hgcfg.isDirty('.') else '')
    # TODO: infos['repository']

    infos['jdkversion'] = str(mx.java().version)

    infos['architecture'] = _arch()
    infos['platform'] = mx.get_os()

    if mx.get_os != 'windows':
        pass
        # infos['ccompiler']
        # infos['linker']

    infos['hostname'] = socket.gethostname()

    def _writeJson(suffix, properties):
        d = infos.copy()
        for k, v in properties.iteritems():
            assert not d.has_key(k)
            d[k] = v

        jsonFileName = 'export-' + suffix + '.json'
        with open(jsonFileName, 'w') as f:
            print >> f, json.dumps(d)
        return jsonFileName


    def _genFileName(archivtype, middle):
        idPrefix = infos['revision'] + '_'
        idSuffix = '.tar.gz'
        return join(_graal_home, "graalvm_" + archivtype + "_" + idPrefix + middle + idSuffix)

    def _genFileArchPlatformName(archivtype, middle):
        return _genFileName(archivtype, infos['platform'] + '_' + infos['architecture'] + '_' + middle)


    # archive different build types of hotspot
    for vmBuild in _vmbuildChoices:
        jdkpath = join(_jdksDir(), vmBuild)
        if not exists(jdkpath):
            mx.logv("skipping " + vmBuild)
            continue

        tarName = _genFileArchPlatformName('basejdk', vmBuild)
        mx.logv("creating basejdk " + tarName)
        vmSet = set()
        with tarfile.open(tarName, 'w:gz') as tar:
            for root, _, files in os.walk(jdkpath):
                if basename(root) in _vmChoices.keys():
                    # TODO: add some assert to check path assumption
                    vmSet.add(root)
                    continue

                for f in files:
                    name = join(root, f)
                    # print name
                    tar.add(name, name)

            n = _writeJson("basejdk-" + vmBuild, {'vmbuild' : vmBuild})
            tar.add(n, n)

        # create a separate archive for each VM
        for vm in vmSet:
            bVm = basename(vm)
            vmTarName = _genFileArchPlatformName('vm', vmBuild + '_' + bVm)
            mx.logv("creating vm " + vmTarName)

            debugFiles = set()
            with tarfile.open(vmTarName, 'w:gz') as tar:
                for root, _, files in os.walk(vm):
                    for f in files:
                        # TODO: mac, windows, solaris?
                        if any(map(f.endswith, [".debuginfo"])):
                            debugFiles.add(f)
                        else:
                            name = join(root, f)
                            # print name
                            tar.add(name, name)

                n = _writeJson("vm-" + vmBuild + "-" + bVm, {'vmbuild' : vmBuild, 'vm' : bVm})
                tar.add(n, n)

            if len(debugFiles) > 0:
                debugTarName = _genFileArchPlatformName('debugfilesvm', vmBuild + '_' + bVm)
                mx.logv("creating debugfilesvm " + debugTarName)
                with tarfile.open(debugTarName, 'w:gz') as tar:
                    for f in debugFiles:
                        name = join(root, f)
                        # print name
                        tar.add(name, name)

                    n = _writeJson("debugfilesvm-" + vmBuild + "-" + bVm, {'vmbuild' : vmBuild, 'vm' : bVm})
                    tar.add(n, n)

    # graal directory
    graalDirTarName = _genFileName('classfiles', 'javac')
    mx.logv("creating graal " + graalDirTarName)
    with tarfile.open(graalDirTarName, 'w:gz') as tar:
        for root, _, files in os.walk("graal"):
            for f in [f for f in files if not f.endswith('.java')]:
                name = join(root, f)
                # print name
                tar.add(name, name)

        n = _writeJson("graal", {'javacompiler' : 'javac'})
        tar.add(n, n)


def _run_benchmark(args, availableBenchmarks, runBenchmark):

    vmOpts, benchmarksAndOptions = _extract_VM_args(args, useDoubleDash=availableBenchmarks is None)

    if availableBenchmarks is None:
        harnessArgs = benchmarksAndOptions
        return runBenchmark(None, harnessArgs, vmOpts)

    if len(benchmarksAndOptions) == 0:
        mx.abort('at least one benchmark name or "all" must be specified')
    benchmarks = list(itertools.takewhile(lambda x: not x.startswith('-'), benchmarksAndOptions))
    harnessArgs = benchmarksAndOptions[len(benchmarks):]

    if 'all' in benchmarks:
        benchmarks = availableBenchmarks
    else:
        for bm in benchmarks:
            if bm not in availableBenchmarks:
                mx.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(availableBenchmarks))

    failed = []
    for bm in benchmarks:
        if not runBenchmark(bm, harnessArgs, vmOpts):
            failed.append(bm)

    if len(failed) != 0:
        mx.abort('Benchmark failures: ' + str(failed))

def dacapo(args):
    """run one or more DaCapo benchmarks"""

    def launcher(bm, harnessArgs, extraVmOpts):
        return sanitycheck.getDacapo(bm, harnessArgs).test(_get_vm(), extraVmOpts=extraVmOpts)

    _run_benchmark(args, sanitycheck.dacapoSanityWarmup.keys(), launcher)

def scaladacapo(args):
    """run one or more Scala DaCapo benchmarks"""

    def launcher(bm, harnessArgs, extraVmOpts):
        return sanitycheck.getScalaDacapo(bm, harnessArgs).test(_get_vm(), extraVmOpts=extraVmOpts)

    _run_benchmark(args, sanitycheck.dacapoScalaSanityWarmup.keys(), launcher)

def _arch():
    machine = platform.uname()[4]
    if machine in ['amd64', 'AMD64', 'x86_64', 'i86pc']:
        return 'amd64'
    if machine in ['sun4v', 'sun4u']:
        return 'sparcv9'
    if machine == 'i386' and mx.get_os() == 'darwin':
        try:
            # Support for Snow Leopard and earlier version of MacOSX
            if subprocess.check_output(['sysctl', '-n', 'hw.cpu64bit_capable']).strip() == '1':
                return 'amd64'
        except OSError:
            # sysctl is not available
            pass
    mx.abort('unknown or unsupported architecture: os=' + mx.get_os() + ', machine=' + machine)

def _vmLibDirInJdk(jdk):
    """
    Get the directory within a JDK where the server and client
    subdirectories are located.
    """
    if platform.system() == 'Darwin':
        return join(jdk, 'jre', 'lib')
    if platform.system() == 'Windows':
        return join(jdk, 'jre', 'bin')
    return join(jdk, 'jre', 'lib', _arch())

def _vmCfgInJdk(jdk):
    """
    Get the jvm.cfg file.
    """
    if platform.system() == 'Windows':
        return join(jdk, 'jre', 'lib', _arch(), 'jvm.cfg')
    return join(_vmLibDirInJdk(jdk), 'jvm.cfg')

def _jdksDir():
    return os.path.abspath(join(_installed_jdks if _installed_jdks else _graal_home, 'jdk' + str(mx.java().version)))

def _handle_missing_VM(bld, vm):
    mx.log('The ' + bld + ' ' + vm + ' VM has not been created')
    if sys.stdout.isatty():
        if mx.ask_yes_no('Build it now', 'y'):
            with VM(vm, bld):
                build([])
            return
    mx.abort('You need to run "mx --vm ' + vm + ' --vmbuild ' + bld + ' build" to build the selected VM')

def _jdk(build='product', vmToCheck=None, create=False, installGraalJar=True):
    """
    Get the JDK into which Graal is installed, creating it first if necessary.
    """
    jdk = join(_jdksDir(), build)
    if create:
        srcJdk = mx.java().jdk
        if not exists(jdk):
            mx.log('Creating ' + jdk + ' from ' + srcJdk)
            shutil.copytree(srcJdk, jdk)

            # Make a copy of the default VM so that this JDK can be
            # reliably used as the bootstrap for a HotSpot build.
            jvmCfg = _vmCfgInJdk(jdk)
            if not exists(jvmCfg):
                mx.abort(jvmCfg + ' does not exist')

            defaultVM = None
            jvmCfgLines = []
            with open(jvmCfg) as f:
                for line in f:
                    if line.startswith('-') and defaultVM is None:
                        parts = line.split()
                        if len(parts) == 2:
                            assert parts[1] == 'KNOWN', parts[1]
                            defaultVM = parts[0][1:]
                            jvmCfgLines += ['# default VM is a copy of the unmodified ' + defaultVM + ' VM\n']
                            jvmCfgLines += ['-original KNOWN\n']
                        else:
                            # skip lines which we cannot parse (e.g. '-hotspot ALIASED_TO -client')
                            mx.log("WARNING: skipping not parsable line \"" + line + "\"")
                    else:
                        jvmCfgLines += [line]

            assert defaultVM is not None, 'Could not find default VM in ' + jvmCfg
            if mx.get_os() != 'windows':
                chmodRecursive(jdk, JDK_UNIX_PERMISSIONS)
            shutil.move(join(_vmLibDirInJdk(jdk), defaultVM), join(_vmLibDirInJdk(jdk), 'original'))


            with open(jvmCfg, 'w') as fp:
                for line in jvmCfgLines:
                    fp.write(line)

            # patch 'release' file (append graalvm revision)
            releaseFile = join(jdk, 'release')
            if exists(releaseFile):
                releaseFileLines = []
                with open(releaseFile) as f:
                    for line in f:
                        releaseFileLines.append(line)

                with open(releaseFile, 'w') as fp:
                    for line in releaseFileLines:
                        if line.startswith("SOURCE="):
                            try:
                                sourceLine = line[0:-2]  # remove last char
                                hgcfg = mx.HgConfig()
                                hgcfg.check()
                                revision = hgcfg.tip('.')[:12]  # take first 12 chars
                                fp.write(sourceLine + ' graal:' + revision + '\"\n')
                            except:
                                fp.write(line)
                        else:
                            fp.write(line)

            # Install a copy of the disassembler library
            try:
                hsdis([], copyToDir=_vmLibDirInJdk(jdk))
            except SystemExit:
                pass
    else:
        if not exists(jdk):
            if _installed_jdks:
                mx.log("The selected JDK directory does not (yet) exist: " + jdk)
            _handle_missing_VM(build, vmToCheck if vmToCheck else 'graal')

    if installGraalJar:
        _installGraalJarInJdks(mx.distribution('GRAAL'))

    if vmToCheck is not None:
        jvmCfg = _vmCfgInJdk(jdk)
        found = False
        with open(jvmCfg) as f:
            for line in f:
                if line.strip() == '-' + vmToCheck + ' KNOWN':
                    found = True
                    break
        if not found:
            _handle_missing_VM(build, vmToCheck)

    return jdk

def _updateInstalledGraalOptionsFile(jdk):
    graalOptions = join(_graal_home, 'graal.options')
    jreLibDir = join(jdk, 'jre', 'lib')
    if exists(graalOptions):
        shutil.copy(graalOptions, join(jreLibDir, 'graal.options'))
    else:
        toDelete = join(jreLibDir, 'graal.options')
        if exists(toDelete):
            os.unlink(toDelete)

def _update_graalRuntime_inline_hpp(graalJar):
    p = mx.project('com.oracle.graal.hotspot.sourcegen')
    mainClass = 'com.oracle.graal.hotspot.sourcegen.GenGraalRuntimeInlineHpp'
    if exists(join(p.output_dir(), mainClass.replace('.', os.sep) + '.class')):
        hsSrcGenDir = join(mx.project('com.oracle.graal.hotspot').source_gen_dir(), 'hotspot')
        if not exists(hsSrcGenDir):
            os.makedirs(hsSrcGenDir)

        tmp = StringIO.StringIO()
        mx.run_java(['-cp', '{}{}{}'.format(graalJar, os.pathsep, p.output_dir()), mainClass], out=tmp.write)
        mx.update_file(join(hsSrcGenDir, 'graalRuntime.inline.hpp'), tmp.getvalue())

def _installGraalJarInJdks(graalDist):
    graalJar = graalDist.path
    _update_graalRuntime_inline_hpp(graalJar)
    jdks = _jdksDir()

    if exists(jdks):
        for e in os.listdir(jdks):
            jreLibDir = join(jdks, e, 'jre', 'lib')
            if exists(jreLibDir):
                def install(srcJar, dstDir):
                    name = os.path.basename(srcJar)
                    dstJar = join(dstDir, name)
                    if mx.get_env('SYMLINK_GRAAL_JAR', None) == 'true':
                        # Using symlinks is much faster than copying but may
                        # cause issues if graal.jar is being updated while
                        # the VM is running.
                        if not os.path.islink(dstJar) or not os.path.realpath(dstJar) == srcJar:
                            if exists(dstJar):
                                os.remove(dstJar)
                            os.symlink(srcJar, dstJar)
                    else:
                        # do a copy and then a move to get atomic updating (on Unix)
                        fd, tmp = tempfile.mkstemp(suffix='', prefix=name, dir=dstDir)
                        shutil.copyfile(srcJar, tmp)
                        os.close(fd)
                        shutil.move(tmp, dstJar)
                        os.chmod(dstJar, JDK_UNIX_PERMISSIONS)

                install(graalJar, jreLibDir)
                if graalDist.sourcesPath:
                    install(graalDist.sourcesPath, join(jdks, e))

# run a command in the windows SDK Debug Shell
def _runInDebugShell(cmd, workingDir, logFile=None, findInOutput=None, respondTo=None):
    if respondTo is None:
        respondTo = {}
    newLine = os.linesep
    startToken = 'RUNINDEBUGSHELL_STARTSEQUENCE'
    endToken = 'RUNINDEBUGSHELL_ENDSEQUENCE'

    winSDK = mx.get_env('WIN_SDK', 'C:\\Program Files\\Microsoft SDKs\\Windows\\v7.1\\')

    if not exists(winSDK):
        mx.abort("Could not find Windows SDK : '" + winSDK + "' does not exist")

    if not exists(join(winSDK, 'Bin', 'SetEnv.cmd')):
        mx.abort("Invalid Windows SDK path (" + winSDK + ") : could not find Bin/SetEnv.cmd (you can use the WIN_SDK environment variable to specify an other path)")

    p = subprocess.Popen('cmd.exe /E:ON /V:ON /K ""' + winSDK + '/Bin/SetEnv.cmd" & echo ' + startToken + '"', \
            shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
    stdout = p.stdout
    stdin = p.stdin
    if logFile:
        log = open(logFile, 'w')
    ret = False
    while True:

        # encoding may be None on windows plattforms
        if sys.stdout.encoding is None:
            encoding = 'utf-8'
        else:
            encoding = sys.stdout.encoding

        line = stdout.readline().decode(encoding)
        if logFile:
            log.write(line.encode('utf-8'))
        line = line.strip()
        mx.log(line)
        if line == startToken:
            stdin.write('cd /D ' + workingDir + ' & ' + cmd + ' & echo ' + endToken + newLine)
        for regex in respondTo.keys():
            match = regex.search(line)
            if match:
                stdin.write(respondTo[regex] + newLine)
        if findInOutput:
            match = findInOutput.search(line)
            if match:
                ret = True
        if line == endToken:
            if not findInOutput:
                stdin.write('echo ERRXXX%errorlevel%' + newLine)
            else:
                break
        if line.startswith('ERRXXX'):
            if line == 'ERRXXX0':
                ret = True
            break
    stdin.write('exit' + newLine)
    if logFile:
        log.close()
    return ret

def jdkhome(vm=None):
    """return the JDK directory selected for the 'vm' command"""
    build = _vmbuild if _vmSourcesAvailable else 'product'
    return _jdk(build, installGraalJar=False)

def print_jdkhome(args, vm=None):
    """print the JDK directory selected for the 'vm' command"""
    print jdkhome(vm)

def buildvars(args):
    """describe the variables that can be set by the -D option to the 'mx build' commmand"""

    buildVars = {
        'ALT_BOOTDIR' : 'The location of the bootstrap JDK installation (default: ' + mx.java().jdk + ')',
        'ALT_OUTPUTDIR' : 'Build directory',
        'HOTSPOT_BUILD_JOBS' : 'Number of CPUs used by make (default: ' + str(multiprocessing.cpu_count()) + ')',
        'INSTALL' : 'Install the built VM into the JDK? (default: y)',
        'ZIP_DEBUGINFO_FILES' : 'Install zipped debug symbols file? (default: 0)',
    }

    mx.log('HotSpot build variables that can be set by the -D option to "mx build":')
    mx.log('')
    for n in sorted(buildVars.iterkeys()):
        mx.log(n)
        mx.log(textwrap.fill(buildVars[n], initial_indent='    ', subsequent_indent='    ', width=200))

    mx.log('')
    mx.log('Note that these variables can be given persistent values in the file ' + join(_graal_home, 'mx', 'env') + ' (see \'mx about\').')

def build(args, vm=None):
    """build the VM binary

    The global '--vm' and '--vmbuild' options select which VM type and build target to build."""

    # Override to fail quickly if extra arguments are given
    # at the end of the command line. This allows for a more
    # helpful error message.
    class AP(ArgumentParser):
        def __init__(self):
            ArgumentParser.__init__(self, prog='mx build')
        def parse_args(self, args):
            result = ArgumentParser.parse_args(self, args)
            if len(result.remainder) != 0:
                firstBuildTarget = result.remainder[0]
                mx.abort('To specify the ' + firstBuildTarget + ' VM build target, you need to use the global "--vmbuild" option. For example:\n' +
                         '    mx --vmbuild ' + firstBuildTarget + ' build')
            return result

    # Call mx.build to compile the Java sources
    parser = AP()
    parser.add_argument('--export-dir', help='directory to which graal.jar and graal.options will be copied', metavar='<path>')
    parser.add_argument('-D', action='append', help='set a HotSpot build variable (run \'mx buildvars\' to list variables)', metavar='name=value')
    opts2 = mx.build(['--source', '1.7'] + args, parser=parser)
    assert len(opts2.remainder) == 0

    if opts2.export_dir is not None:
        if not exists(opts2.export_dir):
            os.makedirs(opts2.export_dir)
        else:
            assert os.path.isdir(opts2.export_dir), '{} is not a directory'.format(opts2.export_dir)

        shutil.copy(mx.distribution('GRAAL').path, opts2.export_dir)
        graalOptions = join(_graal_home, 'graal.options')
        if exists(graalOptions):
            shutil.copy(graalOptions, opts2.export_dir)

    if not _vmSourcesAvailable or not opts2.native:
        return

    builds = [_vmbuild]

    if vm is None:
        vm = _get_vm()

    if vm == 'original':
        pass
    elif vm.startswith('server'):
        buildSuffix = ''
    elif vm.startswith('client'):
        buildSuffix = '1'
    else:
        assert vm == 'graal', vm
        buildSuffix = 'graal'

    if _installed_jdks and _installed_jdks != _graal_home:
        if not mx.ask_yes_no("Warning: building while --installed-jdks is set (" + _installed_jdks + ") is not recommanded - are you sure you want to continue", 'n'):
            mx.abort(1)

    for build in builds:
        if build == 'ide-build-target':
            build = os.environ.get('IDE_BUILD_TARGET', None)
            if build is None or len(build) == 0:
                continue

        jdk = _jdk(build, create=True)

        if vm == 'original':
            if build != 'product':
                mx.log('only product build of original VM exists')
            continue

        if not isVMSupported(vm):
            mx.log('The ' + vm + ' VM is not supported on this platform - skipping')
            continue

        vmDir = join(_vmLibDirInJdk(jdk), vm)
        if not exists(vmDir):
            if mx.get_os() != 'windows':
                chmodRecursive(jdk, JDK_UNIX_PERMISSIONS)
            mx.log('Creating VM directory in JDK: ' + vmDir)
            os.makedirs(vmDir)

        def filterXusage(line):
            if not 'Xusage.txt' in line:
                sys.stderr.write(line + os.linesep)

        # Check if a build really needs to be done
        timestampFile = join(vmDir, '.build-timestamp')
        if opts2.force or not exists(timestampFile):
            mustBuild = True
        else:
            mustBuild = False
            timestamp = os.path.getmtime(timestampFile)
            sources = []
            for d in ['src', 'make', join('graal', 'com.oracle.graal.hotspot', 'src_gen', 'hotspot')]:
                for root, dirnames, files in os.walk(join(_graal_home, d)):
                    # ignore <graal>/src/share/tools
                    if root == join(_graal_home, 'src', 'share'):
                        dirnames.remove('tools')
                    sources += [join(root, name) for name in files]
            for f in sources:
                if len(f) != 0 and os.path.getmtime(f) > timestamp:
                    mustBuild = True
                    break

        if not mustBuild:
            mx.logv('[all files in src and make directories are older than ' + timestampFile[len(_graal_home) + 1:] + ' - skipping native build]')
            continue

        if platform.system() == 'Windows':
            compilelogfile = _graal_home + '/graalCompile.log'
            mksHome = mx.get_env('MKS_HOME', 'C:\\cygwin\\bin')

            variant = {'client': 'compiler1', 'server': 'compiler2'}.get(vm, vm)
            project_config = variant + '_' + build
            _runInDebugShell('msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcproj /p:Configuration=' + project_config + ' /target:clean', _graal_home)
            winCompileCmd = r'set HotSpotMksHome=' + mksHome + r'& set OUT_DIR=' + jdk + r'& set JAVA_HOME=' + jdk + r'& set path=%JAVA_HOME%\bin;%path%;%HotSpotMksHome%& cd /D "' + _graal_home + r'\make\windows"& call create.bat ' + _graal_home
            print winCompileCmd
            winCompileSuccess = re.compile(r"^Writing \.vcxproj file:")
            if not _runInDebugShell(winCompileCmd, _graal_home, compilelogfile, winCompileSuccess):
                mx.log('Error executing create command')
                return
            winBuildCmd = 'msbuild ' + _graal_home + r'\build\vs-amd64\jvm.vcxproj /p:Configuration=' + project_config + ' /p:Platform=x64'
            if not _runInDebugShell(winBuildCmd, _graal_home, compilelogfile):
                mx.log('Error building project')
                return
        else:
            cpus = multiprocessing.cpu_count()
            makeDir = join(_graal_home, 'make')
            runCmd = [mx.gmake_cmd(), '-C', makeDir]

            env = os.environ.copy()

            # These must be passed as environment variables
            env.setdefault('LANG', 'C')
            env['JAVA_HOME'] = jdk

            def setMakeVar(name, default, env=None):
                """Sets a make variable on the command line to the value
                   of the variable in 'env' with the same name if defined
                   and 'env' is not None otherwise to 'default'
                """
                runCmd.append(name + '=' + (env.get(name, default) if env else default))

            if opts2.D:
                for nv in opts2.D:
                    name, value = nv.split('=', 1)
                    setMakeVar(name.strip(), value)

            setMakeVar('ARCH_DATA_MODEL', '64', env=env)
            setMakeVar('HOTSPOT_BUILD_JOBS', str(cpus), env=env)
            setMakeVar('ALT_BOOTDIR', mx.java().jdk, env=env)

            setMakeVar('MAKE_VERBOSE', 'y' if mx._opts.verbose else '')
            if vm.endswith('nograal'):
                setMakeVar('INCLUDE_GRAAL', 'false')
                setMakeVar('ALT_OUTPUTDIR', join(_graal_home, 'build-nograal', mx.get_os()), env=env)
            else:
                # extract latest release tag for graal
                try:
                    tags = [x.split(' ')[0] for x in subprocess.check_output(['hg', '-R', _graal_home, 'tags']).split('\n') if x.startswith("graal-")]
                except:
                    # not a mercurial repository or hg commands are not available.
                    tags = None

                if tags:
                    # extract the most recent tag
                    tag = sorted(tags, key=lambda e: [int(x) for x in e[len("graal-"):].split('.')], reverse=True)[0]
                    setMakeVar('USER_RELEASE_SUFFIX', tag)
                    setMakeVar('GRAAL_VERSION', tag[len("graal-"):])
                else:
                    version = 'unknown-{}-{}'.format(platform.node(), time.strftime('%Y-%m-%d_%H-%M-%S_%Z'))
                    setMakeVar('USER_RELEASE_SUFFIX', 'graal-' + version)
                    setMakeVar('GRAAL_VERSION', version)
                setMakeVar('INCLUDE_GRAAL', 'true')
            setMakeVar('INSTALL', 'y', env=env)
            if mx.get_os() == 'solaris':
                # If using sparcWorks, setup flags to avoid make complaining about CC version
                cCompilerVersion = subprocess.Popen('CC -V', stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True).stderr.readlines()[0]
                if cCompilerVersion.startswith('CC: Sun C++'):
                    compilerRev = cCompilerVersion.split(' ')[3]
                    setMakeVar('ENFORCE_COMPILER_REV', compilerRev, env=env)
                    setMakeVar('ENFORCE_CC_COMPILER_REV', compilerRev, env=env)
                    if build == 'jvmg':
                        # We want ALL the symbols when debugging on Solaris
                        setMakeVar('STRIP_POLICY', 'no_strip')
            # This removes the need to unzip the *.diz files before debugging in gdb
            setMakeVar('ZIP_DEBUGINFO_FILES', '0', env=env)

            # Clear these 2 variables as having them set can cause very confusing build problems
            env.pop('LD_LIBRARY_PATH', None)
            env.pop('CLASSPATH', None)

            # Issue an env prefix that can be used to run the make on the command line
            if not mx._opts.verbose:
                mx.log('--------------- make command line ----------------------')

            envPrefix = ' '.join([key + '=' + env[key] for key in env.iterkeys() if not os.environ.has_key(key) or env[key] != os.environ[key]])
            if len(envPrefix):
                mx.log('env ' + envPrefix + ' \\')

            runCmd.append(build + buildSuffix)

            if not mx._opts.verbose:
                mx.log(' '.join(runCmd))
                mx.log('--------------------------------------------------------')
            mx.run(runCmd, err=filterXusage, env=env)

        jvmCfg = _vmCfgInJdk(jdk)
        if not exists(jvmCfg):
            mx.abort(jvmCfg + ' does not exist')

        prefix = '-' + vm + ' '
        vmKnown = prefix + 'KNOWN\n'
        lines = []
        found = False
        with open(jvmCfg) as f:
            for line in f:
                if line.strip() == vmKnown.strip():
                    found = True
                lines.append(line)

        if not found:
            mx.log('Appending "' + prefix + 'KNOWN" to ' + jvmCfg)
            if mx.get_os() != 'windows':
                os.chmod(jvmCfg, JDK_UNIX_PERMISSIONS)
            with open(jvmCfg, 'w') as f:
                for line in lines:
                    if line.startswith(prefix):
                        line = vmKnown
                        found = True
                    f.write(line)
                if not found:
                    f.write(vmKnown)

        if exists(timestampFile):
            os.utime(timestampFile, None)
        else:
            file(timestampFile, 'a')

def vmg(args):
    """run the debug build of VM selected by the '--vm' option"""
    return vm(args, vmbuild='debug')

def vmfg(args):
    """run the fastdebug build of VM selected by the '--vm' option"""
    return vm(args, vmbuild='fastdebug')

def _parseVmArgs(args, vm=None, cwd=None, vmbuild=None):
    """run the VM selected by the '--vm' option"""

    if vm is None:
        vm = _get_vm()

    if not isVMSupported(vm):
        mx.abort('The ' + vm + ' is not supported on this platform')

    if cwd is None:
        cwd = _vm_cwd
    elif _vm_cwd is not None and _vm_cwd != cwd:
        mx.abort("conflicting working directories: do not set --vmcwd for this command")

    build = vmbuild if vmbuild is not None else _vmbuild if _vmSourcesAvailable else 'product'
    jdk = _jdk(build, vmToCheck=vm, installGraalJar=False)
    _updateInstalledGraalOptionsFile(jdk)
    mx.expand_project_in_args(args)
    if _make_eclipse_launch:
        mx.make_eclipse_launch(args, 'graal-' + build, name=None, deps=mx.project('com.oracle.graal.hotspot').all_deps([], True))
    if _jacoco == 'on' or _jacoco == 'append':
        jacocoagent = mx.library("JACOCOAGENT", True)
        # Exclude all compiler tests and snippets
        excludes = ['com.oracle.graal.compiler.tests.*', 'com.oracle.graal.jtt.*']
        for p in mx.projects():
            excludes += _find_classes_with_annotations(p, None, ['@Snippet', '@ClassSubstitution', '@Test'], includeInnerClasses=True).keys()
            excludes += p.find_classes_with_matching_source_line(None, lambda line: 'JaCoCo Exclude' in line, includeInnerClasses=True).keys()

        includes = ['com.oracle.graal.*']
        agentOptions = {
                        'append' : 'true' if _jacoco == 'append' else 'false',
                        'bootclasspath' : 'true',
                        'includes' : ':'.join(includes),
                        'excludes' : ':'.join(excludes),
                        'destfile' : 'jacoco.exec'
        }
        args = ['-javaagent:' + jacocoagent.get_path(True) + '=' + ','.join([k + '=' + v for k, v in agentOptions.items()])] + args
    exe = join(jdk, 'bin', mx.exe_suffix('java'))
    pfx = _vm_prefix.split() if _vm_prefix is not None else []

    if '-version' in args:
        ignoredArgs = args[args.index('-version') + 1:]
        if  len(ignoredArgs) > 0:
            mx.log("Warning: The following options will be ignored by the vm because they come after the '-version' argument: " + ' '.join(ignoredArgs))

    args = mx.java().processArgs(args)
    return (pfx, exe, vm, args, cwd)

def vm(args, vm=None, nonZeroIsFatal=True, out=None, err=None, cwd=None, timeout=None, vmbuild=None):
    (pfx_, exe_, vm_, args_, cwd) = _parseVmArgs(args, vm, cwd, vmbuild)
    return mx.run(pfx_ + [exe_, '-' + vm_] + args_, nonZeroIsFatal=nonZeroIsFatal, out=out, err=err, cwd=cwd, timeout=timeout)

def _find_classes_with_annotations(p, pkgRoot, annotations, includeInnerClasses=False):
    """
    Scan the sources of project 'p' for Java source files containing a line starting with 'annotation'
    (ignoring preceding whitespace) and return the fully qualified class name for each Java
    source file matched in a list.
    """

    matches = lambda line: len([a for a in annotations if line == a or line.startswith(a + '(')]) != 0
    return p.find_classes_with_matching_source_line(pkgRoot, matches, includeInnerClasses)

def _extract_VM_args(args, allowClasspath=False, useDoubleDash=False, defaultAllVMArgs=True):
    """
    Partitions a command line into a leading sequence of HotSpot VM options and the rest.
    """
    for i in range(0, len(args)):
        if useDoubleDash:
            if args[i] == '--':
                vmArgs = args[:i]
                remainder = args[i + 1:]
                return vmArgs, remainder
        else:
            if not args[i].startswith('-'):
                if i != 0 and (args[i - 1] == '-cp' or args[i - 1] == '-classpath'):
                    if not allowClasspath:
                        mx.abort('Cannot supply explicit class path option')
                    else:
                        continue
                vmArgs = args[:i]
                remainder = args[i:]
                return vmArgs, remainder

    if defaultAllVMArgs:
        return args, []
    else:
        return [], args

def _run_tests(args, harness, annotations, testfile, whitelist, regex):


    vmArgs, tests = _extract_VM_args(args)
    for t in tests:
        if t.startswith('-'):
            mx.abort('VM option ' + t + ' must precede ' + tests[0])

    candidates = {}
    for p in mx.projects_opt_limit_to_suites():
        if mx.java().javaCompliance < p.javaCompliance:
            continue
        for c in _find_classes_with_annotations(p, None, annotations).keys():
            candidates[c] = p

    classes = []
    if len(tests) == 0:
        classes = candidates.keys()
        projectscp = mx.classpath([pcp.name for pcp in mx.projects_opt_limit_to_suites() if pcp.javaCompliance <= mx.java().javaCompliance])
    else:
        projs = set()
        for t in tests:
            found = False
            for c, p in candidates.iteritems():
                if t in c:
                    found = True
                    classes.append(c)
                    projs.add(p.name)
            if not found:
                mx.log('warning: no tests matched by substring "' + t)
        projectscp = mx.classpath(projs)

    if whitelist:
        classes = [c for c in classes if any((glob.match(c) for glob in whitelist))]

    if regex:
        classes = [c for c in classes if re.search(regex, c)]

    if len(classes) != 0:
        f_testfile = open(testfile, 'w')
        for c in classes:
            f_testfile.write(c + '\n')
        f_testfile.close()
        harness(projectscp, vmArgs)

def _unittest(args, annotations, prefixcp="", whitelist=None, verbose=False, enable_timing=False, regex=None, color=False, eager_stacktrace=False, gc_after_test=False):
    mxdir = dirname(__file__)
    name = 'JUnitWrapper'
    javaSource = join(mxdir, name + '.java')
    javaClass = join(mxdir, name + '.class')
    testfile = os.environ.get('MX_TESTFILE', None)
    if testfile is None:
        (_, testfile) = tempfile.mkstemp(".testclasses", "graal")
        os.close(_)
    corecp = mx.classpath(['com.oracle.graal.test'])

    if not exists(javaClass) or getmtime(javaClass) < getmtime(javaSource):
        subprocess.check_call([mx.java().javac, '-cp', corecp, '-d', mxdir, javaSource])

    coreArgs = []
    if verbose:
        coreArgs.append('-JUnitVerbose')
    if enable_timing:
        coreArgs.append('-JUnitEnableTiming')
    if color:
        coreArgs.append('-JUnitColor')
    if eager_stacktrace:
        coreArgs.append('-JUnitEagerStackTrace')
    if gc_after_test:
        coreArgs.append('-JUnitGCAfterTest')


    def harness(projectscp, vmArgs):
        if _get_vm() != 'graal':
            prefixArgs = ['-esa', '-ea']
        else:
            prefixArgs = ['-XX:-BootstrapGraal', '-esa', '-ea']
        if gc_after_test:
            prefixArgs.append('-XX:-DisableExplicitGC')
        with open(testfile) as fp:
            testclasses = [l.rstrip() for l in fp.readlines()]
        if len(testclasses) == 1:
            # Execute Junit directly when one test is being run. This simplifies
            # replaying the VM execution in a native debugger (e.g., gdb).
            vm(prefixArgs + vmArgs + ['-cp', prefixcp + corecp + ':' + projectscp, 'com.oracle.graal.test.GraalJUnitCore'] + coreArgs + testclasses)
        else:
            vm(prefixArgs + vmArgs + ['-cp', prefixcp + corecp + ':' + projectscp + os.pathsep + mxdir, name] + [testfile] + coreArgs)

    try:
        _run_tests(args, harness, annotations, testfile, whitelist, regex)
    finally:
        if os.environ.get('MX_TESTFILE') is None:
            os.remove(testfile)

_unittestHelpSuffix = """
    Unittest options:

      --whitelist <file>     run only testcases which are included
                             in the given whitelist
      --verbose              enable verbose JUnit output
      --enable-timing        enable JUnit test timing
      --regex <regex>        run only testcases matching a regular expression
      --color                enable colors output
      --eager-stacktrace     print stacktrace eagerly
      --gc-after-test        force a GC after each test

    To avoid conflicts with VM options '--' can be used as delimiter.

    If filters are supplied, only tests whose fully qualified name
    includes a filter as a substring are run.

    For example, this command line:

       mx unittest -G:Dump= -G:MethodFilter=BC_aload.* -G:+PrintCFG BC_aload

    will run all JUnit test classes that contain 'BC_aload' in their
    fully qualified name and will pass these options to the VM:

        -G:Dump= -G:MethodFilter=BC_aload.* -G:+PrintCFG

    To get around command line length limitations on some OSes, the
    JUnit class names to be executed are written to a file that a
    custom JUnit wrapper reads and passes onto JUnit proper. The
    MX_TESTFILE environment variable can be set to specify a
    file which will not be deleted once the unittests are done
    (unlike the temporary file otherwise used).

    As with all other commands, using the global '-v' before 'unittest'
    command will cause mx to show the complete command line
    it uses to run the VM.
"""

def unittest(args):
    """run the JUnit tests (all testcases){0}"""

    parser = ArgumentParser(prog='mx unittest',
          description='run the JUnit tests',
          add_help=False,
          formatter_class=RawDescriptionHelpFormatter,
          epilog=_unittestHelpSuffix,
        )
    parser.add_argument('--whitelist', help='run testcases specified in whitelist only', metavar='<path>')
    parser.add_argument('--verbose', help='enable verbose JUnit output', action='store_true')
    parser.add_argument('--enable-timing', help='enable JUnit test timing', action='store_true')
    parser.add_argument('--regex', help='run only testcases matching a regular expression', metavar='<regex>')
    parser.add_argument('--color', help='enable color output', action='store_true')
    parser.add_argument('--eager-stacktrace', help='print stacktrace eagerly', action='store_true')
    parser.add_argument('--gc-after-test', help='force a GC after each test', action='store_true')

    ut_args = []
    delimiter = False
    # check for delimiter
    while len(args) > 0:
        arg = args.pop(0)
        if arg == '--':
            delimiter = True
            break
        ut_args.append(arg)

    if delimiter:
        # all arguments before '--' must be recognized
        parsed_args = parser.parse_args(ut_args)
    else:
        # parse all know arguments
        parsed_args, args = parser.parse_known_args(ut_args)

    if parsed_args.whitelist:
        try:
            with open(join(_graal_home, parsed_args.whitelist)) as fp:
                parsed_args.whitelist = [re.compile(fnmatch.translate(l.rstrip())) for l in fp.readlines() if not l.startswith('#')]
        except IOError:
            mx.log('warning: could not read whitelist: ' + parsed_args.whitelist)

    _unittest(args, ['@Test', '@Parameters'], **parsed_args.__dict__)

def shortunittest(args):
    """alias for 'unittest --whitelist test/whitelist_shortunittest.txt'{0}"""

    args = ['--whitelist', 'test/whitelist_shortunittest.txt'] + args
    unittest(args)

def buildvms(args):
    """build one or more VMs in various configurations"""

    vmsDefault = ','.join(_vmChoices.keys())
    vmbuildsDefault = ','.join(_vmbuildChoices)

    parser = ArgumentParser(prog='mx buildvms')
    parser.add_argument('--vms', help='a comma separated list of VMs to build (default: ' + vmsDefault + ')', metavar='<args>', default=vmsDefault)
    parser.add_argument('--builds', help='a comma separated list of build types (default: ' + vmbuildsDefault + ')', metavar='<args>', default=vmbuildsDefault)
    parser.add_argument('-n', '--no-check', action='store_true', help='omit running "java -version" after each build')
    parser.add_argument('-c', '--console', action='store_true', help='send build output to console instead of log file')

    args = parser.parse_args(args)
    vms = args.vms.split(',')
    builds = args.builds.split(',')

    allStart = time.time()
    for v in vms:
        if not isVMSupported(v):
            mx.log('The ' + v + ' VM is not supported on this platform - skipping')
            continue

        for vmbuild in builds:
            if v == 'original' and vmbuild != 'product':
                continue
            if not args.console:
                logFile = join(v + '-' + vmbuild + '.log')
                log = open(join(_graal_home, logFile), 'wb')
                start = time.time()
                mx.log('BEGIN: ' + v + '-' + vmbuild + '\t(see: ' + logFile + ')')
                # Run as subprocess so that output can be directed to a file
                subprocess.check_call([sys.executable, '-u', join('mxtool', 'mx.py'), '--vm', v, '--vmbuild',
                                       vmbuild, 'build'], cwd=_graal_home, stdout=log, stderr=subprocess.STDOUT)
                duration = datetime.timedelta(seconds=time.time() - start)
                mx.log('END:   ' + v + '-' + vmbuild + '\t[' + str(duration) + ']')
            else:
                with VM(v, vmbuild):
                    build([])
            if not args.no_check:
                vmargs = ['-version']
                if v == 'graal':
                    vmargs.insert(0, '-XX:-BootstrapGraal')
                vm(vmargs, vm=v, vmbuild=vmbuild)
    allDuration = datetime.timedelta(seconds=time.time() - allStart)
    mx.log('TOTAL TIME:   ' + '[' + str(allDuration) + ']')

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

def _basic_gate_body(args, tasks):
    t = Task('BuildHotSpotGraal: fastdebug,product')
    buildvms(['--vms', 'graal,server', '--builds', 'fastdebug,product'])
    tasks.append(t.stop())

    with VM('graal', 'fastdebug'):
        t = Task('BootstrapWithSystemAssertions:fastdebug')
        vm(['-esa', '-XX:-TieredCompilation', '-version'])
        tasks.append(t.stop())

    with VM('graal', 'fastdebug'):
        t = Task('BootstrapWithSystemAssertionsNoCoop:fastdebug')
        vm(['-esa', '-XX:-TieredCompilation', '-XX:-UseCompressedOops', '-version'])
        tasks.append(t.stop())

    with VM('graal', 'product'):
        t = Task('BootstrapWithGCVerification:product')
        out = mx.DuplicateSuppressingStream(['VerifyAfterGC:', 'VerifyBeforeGC:']).write
        vm(['-XX:-TieredCompilation', '-XX:+UnlockDiagnosticVMOptions', '-XX:+VerifyBeforeGC', '-XX:+VerifyAfterGC', '-version'], out=out)
        tasks.append(t.stop())

    with VM('graal', 'product'):
        t = Task('BootstrapWithG1GCVerification:product')
        out = mx.DuplicateSuppressingStream(['VerifyAfterGC:', 'VerifyBeforeGC:']).write
        vm(['-XX:-TieredCompilation', '-XX:+UnlockDiagnosticVMOptions', '-XX:-UseSerialGC', '-XX:+UseG1GC', '-XX:+VerifyBeforeGC', '-XX:+VerifyAfterGC', '-version'], out=out)
        tasks.append(t.stop())

    with VM('graal', 'product'):
        t = Task('BootstrapWithRegisterPressure:product')
        vm(['-XX:-TieredCompilation', '-G:RegisterPressure=rbx,r11,r10,r14,xmm3,xmm11,xmm14', '-esa', '-version'])
        tasks.append(t.stop())

    with VM('graal', 'product'):
        t = Task('BootstrapWithImmutableCode:product')
        vm(['-XX:-TieredCompilation', '-G:+ImmutableCode', '-G:+VerifyPhases', '-esa', '-version'])
        tasks.append(t.stop())

    with VM('server', 'product'):  # hosted mode
        t = Task('UnitTests:hosted-product')
        unittest(['--enable-timing', '--verbose'])
        tasks.append(t.stop())

    with VM('server', 'product'):  # hosted mode
        t = Task('UnitTests-BaselineCompiler:hosted-product')
        unittest(['--enable-timing', '--verbose', '--whitelist', 'test/whitelist_baseline.txt', '-G:+UseBaselineCompiler'])
        tasks.append(t.stop())

    for vmbuild in ['fastdebug', 'product']:
        for test in sanitycheck.getDacapos(level=sanitycheck.SanityCheckLevel.Gate, gateBuildLevel=vmbuild) + sanitycheck.getScalaDacapos(level=sanitycheck.SanityCheckLevel.Gate, gateBuildLevel=vmbuild):
            t = Task(str(test) + ':' + vmbuild)
            if not test.test('graal'):
                t.abort(test.name + ' Failed')
            tasks.append(t.stop())

    if args.jacocout is not None:
        jacocoreport([args.jacocout])

    global _jacoco
    _jacoco = 'off'

    t = Task('CleanAndBuildIdealGraphVisualizer')
    mx.run(['ant', '-f', join(_graal_home, 'src', 'share', 'tools', 'IdealGraphVisualizer', 'build.xml'), '-q', 'clean', 'build'])
    tasks.append(t.stop())

    # Prevent Graal modifications from breaking the standard builds
    if args.buildNonGraal:
        t = Task('BuildHotSpotVarieties')
        buildvms(['--vms', 'client,server', '--builds', 'fastdebug,product'])
        buildvms(['--vms', 'server-nograal', '--builds', 'product'])
        buildvms(['--vms', 'server-nograal', '--builds', 'optimized'])
        tasks.append(t.stop())

        for vmbuild in ['product', 'fastdebug']:
            for theVm in ['client', 'server']:
                if not isVMSupported(theVm):
                    mx.log('The' + theVm + ' VM is not supported on this platform')
                    continue
                with VM(theVm, vmbuild):
                    t = Task('DaCapo_pmd:' + theVm + ':' + vmbuild)
                    dacapo(['pmd'])
                    tasks.append(t.stop())

                    t = Task('UnitTests:' + theVm + ':' + vmbuild)
                    unittest(['-XX:CompileCommand=exclude,*::run*', 'graal.api'])
                    tasks.append(t.stop())


def gate(args, gate_body=_basic_gate_body):
    """run the tests used to validate a push

    If this command exits with a 0 exit code, then the source code is in
    a state that would be accepted for integration into the main repository."""

    parser = ArgumentParser(prog='mx gate')
    parser.add_argument('-j', '--omit-java-clean', action='store_false', dest='cleanJava', help='omit cleaning Java native code')
    parser.add_argument('-n', '--omit-native-clean', action='store_false', dest='cleanNative', help='omit cleaning and building native code')
    parser.add_argument('-g', '--only-build-graalvm', action='store_false', dest='buildNonGraal', help='only build the Graal VM')
    parser.add_argument('--jacocout', help='specify the output directory for jacoco report')

    args = parser.parse_args(args)

    global _jacoco

    tasks = []
    total = Task('Gate')
    try:

        t = Task('Pylint')
        mx.pylint([])
        tasks.append(t.stop())

        def _clean(name='Clean'):
            t = Task(name)
            cleanArgs = []
            if not args.cleanNative:
                cleanArgs.append('--no-native')
            if not args.cleanJava:
                cleanArgs.append('--no-java')
            clean(cleanArgs)
            tasks.append(t.stop())
        _clean()

        t = Task('IDEConfigCheck')
        mx.ideclean([])
        mx.ideinit([])
        tasks.append(t.stop())

        eclipse_exe = mx.get_env('ECLIPSE_EXE')
        if eclipse_exe is not None:
            t = Task('CodeFormatCheck')
            if mx.eclipseformat(['-e', eclipse_exe]) != 0:
                t.abort('Formatter modified files - run "mx eclipseformat", check in changes and repush')
            tasks.append(t.stop())

        t = Task('Canonicalization Check')
        mx.log(time.strftime('%d %b %Y %H:%M:%S - Ensuring mx/projects files are canonicalized...'))
        if mx.canonicalizeprojects([]) != 0:
            t.abort('Rerun "mx canonicalizeprojects" and check-in the modified mx/projects files.')
        tasks.append(t.stop())

        if mx.get_env('JDT'):
            t = Task('BuildJavaWithEcj')
            build(['-p', '--no-native', '--jdt-warning-as-error'])
            tasks.append(t.stop())

            _clean('CleanAfterEcjBuild')

        t = Task('BuildJavaWithJavac')
        build(['-p', '--no-native', '--force-javac'])
        tasks.append(t.stop())

        t = Task('Checkheaders')
        if checkheaders([]) != 0:
            t.abort('Checkheaders warnings were found')
        tasks.append(t.stop())

        t = Task('FindBugs')
        if findbugs([]) != 0:
            t.abort('FindBugs warnings were found')
        tasks.append(t.stop())

        if exists('jacoco.exec'):
            os.unlink('jacoco.exec')

        if args.jacocout is not None:
            _jacoco = 'append'
        else:
            _jacoco = 'off'

        gate_body(args, tasks)

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

def deoptalot(args):
    """bootstrap a fastdebug Graal VM with DeoptimizeALot and VerifyOops on

    If the first argument is a number, the process will be repeated
    this number of times. All other arguments are passed to the VM."""
    count = 1
    if len(args) > 0 and args[0].isdigit():
        count = int(args[0])
        del args[0]

    for _ in range(count):
        if not vm(['-XX:+DeoptimizeALot', '-XX:+VerifyOops'] + args + ['-version'], vmbuild='fastdebug') == 0:
            mx.abort("Failed")

def longtests(args):

    deoptalot(['15', '-Xmx48m'])

    dacapo(['100', 'eclipse', '-esa'])

def igv(args):
    """run the Ideal Graph Visualizer"""
    with open(join(_graal_home, '.ideal_graph_visualizer.log'), 'w') as fp:
        # When the http_proxy environment variable is set, convert it to the proxy settings that ant needs
        env = os.environ
        proxy = os.environ.get('http_proxy')
        if not (proxy is None) and len(proxy) > 0:
            if '://' in proxy:
                # Remove the http:// prefix (or any other protocol prefix)
                proxy = proxy.split('://', 1)[1]
            # Separate proxy server name and port number
            proxyName, proxyPort = proxy.split(':', 1)
            proxyEnv = '-DproxyHost="' + proxyName + '" -DproxyPort=' + proxyPort
            env['ANT_OPTS'] = proxyEnv

        mx.logv('[Ideal Graph Visualizer log is in ' + fp.name + ']')
        nbplatform = join(_graal_home, 'src', 'share', 'tools', 'IdealGraphVisualizer', 'nbplatform')

        # Remove NetBeans platform if it is earlier than the current supported version
        if exists(nbplatform):
            dom = xml.dom.minidom.parse(join(nbplatform, 'platform', 'update_tracking', 'org-netbeans-core.xml'))
            currentVersion = mx.VersionSpec(dom.getElementsByTagName('module_version')[0].getAttribute('specification_version'))
            supportedVersion = mx.VersionSpec('3.43.1')
            if currentVersion < supportedVersion:
                mx.log('Replacing NetBeans platform version ' + str(currentVersion) + ' with version ' + str(supportedVersion))
                shutil.rmtree(nbplatform)
            elif supportedVersion < currentVersion:
                mx.log('Supported NetBeans version in igv command should be updated to ' + str(currentVersion))

        if not exists(nbplatform):
            mx.logv('[This execution may take a while as the NetBeans platform needs to be downloaded]')
        mx.run(['ant', '-f', join(_graal_home, 'src', 'share', 'tools', 'IdealGraphVisualizer', 'build.xml'), '-l', fp.name, 'run'], env=env)

def c1visualizer(args):
    """run the Cl Compiler Visualizer"""
    libpath = join(_graal_home, 'lib')
    if mx.get_os() == 'windows':
        executable = join(libpath, 'c1visualizer', 'bin', 'c1visualizer.exe')
    else:
        executable = join(libpath, 'c1visualizer', 'bin', 'c1visualizer')

    archive = join(libpath, 'c1visualizer_2014-04-22.zip')
    if not exists(executable) or not exists(archive):
        if not exists(archive):
            mx.download(archive, ['https://java.net/downloads/c1visualizer/c1visualizer_2014-04-22.zip'])
        zf = zipfile.ZipFile(archive, 'r')
        zf.extractall(libpath)

    if not exists(executable):
        mx.abort('C1Visualizer binary does not exist: ' + executable)

    if mx.get_os() != 'windows':
        # Make sure that execution is allowed. The zip file does not always specfiy that correctly
        os.chmod(executable, 0777)

    mx.run([executable])

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
    vm = _get_vm()
    if len(args) is 0:
        args = ['all']

    vmArgs = [arg for arg in args if arg.startswith('-')]

    def benchmarks_in_group(group):
        prefix = group + ':'
        return [a[len(prefix):] for a in args if a.startswith(prefix)]

    results = {}
    benchmarks = []
    # DaCapo
    if 'dacapo' in args or 'all' in args:
        benchmarks += sanitycheck.getDacapos(level=sanitycheck.SanityCheckLevel.Benchmark)
    else:
        dacapos = benchmarks_in_group('dacapo')
        for dacapo in dacapos:
            if dacapo not in sanitycheck.dacapoSanityWarmup.keys():
                mx.abort('Unknown DaCapo : ' + dacapo)
            iterations = sanitycheck.dacapoSanityWarmup[dacapo][sanitycheck.SanityCheckLevel.Benchmark]
            if iterations > 0:
                benchmarks += [sanitycheck.getDacapo(dacapo, iterations)]

    if 'scaladacapo' in args or 'all' in args:
        benchmarks += sanitycheck.getScalaDacapos(level=sanitycheck.SanityCheckLevel.Benchmark)
    else:
        scaladacapos = benchmarks_in_group('scaladacapo')
        for scaladacapo in scaladacapos:
            if scaladacapo not in sanitycheck.dacapoScalaSanityWarmup.keys():
                mx.abort('Unknown Scala DaCapo : ' + scaladacapo)
            iterations = sanitycheck.dacapoScalaSanityWarmup[scaladacapo][sanitycheck.SanityCheckLevel.Benchmark]
            if iterations > 0:
                benchmarks += [sanitycheck.getScalaDacapo(scaladacapo, ['-n', str(iterations)])]

    # Bootstrap
    if 'bootstrap' in args or 'all' in args:
        benchmarks += sanitycheck.getBootstraps()
    # SPECjvm2008
    if 'specjvm2008' in args or 'all' in args:
        benchmarks += [sanitycheck.getSPECjvm2008(['-ikv', '-wt', '120', '-it', '120'])]
    else:
        specjvms = benchmarks_in_group('specjvm2008')
        for specjvm in specjvms:
            benchmarks += [sanitycheck.getSPECjvm2008(['-ikv', '-wt', '120', '-it', '120', specjvm])]

    if 'specjbb2005' in args or 'all' in args:
        benchmarks += [sanitycheck.getSPECjbb2005()]

    if 'specjbb2013' in args:  # or 'all' in args //currently not in default set
        benchmarks += [sanitycheck.getSPECjbb2013()]

    if 'ctw-full' in args:
        benchmarks.append(sanitycheck.getCTW(vm, sanitycheck.CTWMode.Full))
    if 'ctw-noinline' in args:
        benchmarks.append(sanitycheck.getCTW(vm, sanitycheck.CTWMode.NoInline))
    if 'ctw-nocomplex' in args:
        benchmarks.append(sanitycheck.getCTW(vm, sanitycheck.CTWMode.NoComplex))

    # Python
    if 'pythontest' in args:
        benchmarks += sanitycheck.getPythonTestBenchmarks(vm)    

    if 'python' in args:
        benchmarks += sanitycheck.getPythonBenchmarks(vm)

    if 'python-nopeeling' in args:
        benchmarks += sanitycheck.getPythonBenchmarksNoPeeling(vm)

    if 'python-profile' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm)
        
    if 'python-profile-calls' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-calls")   
        print("benchmarks", benchmarks) 

    if 'python-profile-control-flow' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-control-flow")   
        
    if 'python-profile-variable-accesses' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-variable-accesses")   
        
    if 'python-profile-operations' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-operations")       

    if 'python-profile-attributes-elements' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-attributes-elements")   

    if 'python-profile-type-distribution' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm, "-profile-type-distribution")   

    if 'cpython2' in args:
        benchmarks += sanitycheck.getPython2Benchmarks(vm)
        vm = 'cpython2'

    if 'cpython' in args:
        benchmarks += sanitycheck.getPythonBenchmarks(vm)
        vm = 'cpython'
        
    if 'cpython-profile' in args:
        benchmarks += sanitycheck.getPythonBenchmarksProfiling(vm)
        vm = 'cpython-profile'

    if 'jython' in args:
        benchmarks += sanitycheck.getPython2Benchmarks(vm)
        vm = 'jython'

    if 'pypy' in args:
        benchmarks += sanitycheck.getPython2Benchmarks(vm)
        vm = 'pypy'

    if 'pypy3' in args:
        benchmarks += sanitycheck.getPythonBenchmarks(vm)
        vm = 'pypy3'

    if 'python-micro' in args:
        benchmarks += sanitycheck.getPythonMicroBenchmarks(vm)

    if 'cpython-micro' in args:
        benchmarks += sanitycheck.getPython2MicroBenchmarks(vm)
        vm = 'cpython'

    if 'jython-micro' in args:
        benchmarks += sanitycheck.getPython2MicroBenchmarks(vm)
        vm = 'jython'

    if 'pypy-micro' in args:
        benchmarks += sanitycheck.getPythonMicroBenchmarks(vm)
        vm = 'pypy'

    if 'pypy3-micro' in args:
        benchmarks += sanitycheck.getPythonMicroBenchmarks(vm)
        vm = 'pypy3'

    for test in benchmarks:
        for (groupName, res) in test.bench(vm, extraVmOpts=vmArgs).items():
            group = results.setdefault(groupName, {})
            group.update(res)
    mx.log(json.dumps(results))
    if resultFile:
        with open(resultFile, 'w') as f:
            f.write(json.dumps(results))

def _get_jmh_path():
    path = mx.get_env('JMH_BENCHMARKS', None)
    if not path:
        probe = join(dirname(_graal_home), 'java-benchmarks')
        if exists(probe):
            path = probe

    if not path:
        mx.abort("Please set the JMH_BENCHMARKS environment variable to point to the java-benchmarks workspace")
    if not exists(path):
        mx.abort("The directory denoted by the JMH_BENCHMARKS environment variable does not exist: " + path)
    return path

def makejmhdeps(args):
    """creates and installs Maven dependencies required by the JMH benchmarks

    The dependencies are specified by files named pom.mxdeps in the
    JMH directory tree. Each such file contains a list of dependencies
    defined in JSON format. For example:

    '[{"artifactId" : "compiler.test", "groupId" : "com.oracle.graal", "deps" : ["com.oracle.graal.compiler.test"]}]'

    will result in a dependency being installed in the local Maven repository
    that can be referenced in a pom.xml file as follows:

          <dependency>
            <groupId>com.oracle.graal</groupId>
            <artifactId>compiler.test</artifactId>
            <version>1.0-SNAPSHOT</version>
          </dependency>"""

    parser = ArgumentParser(prog='mx makejmhdeps')
    parser.add_argument('-s', '--settings', help='alternative path for Maven user settings file', metavar='<path>')
    parser.add_argument('-p', '--permissive', action='store_true', help='issue note instead of error if a Maven dependency cannot be built due to missing projects/libraries')
    args = parser.parse_args(args)

    def makejmhdep(artifactId, groupId, deps):
        graalSuite = mx.suite("graal")
        path = artifactId + '.jar'
        if args.permissive:
            for name in deps:
                if not mx.project(name, fatalIfMissing=False):
                    if not mx.library(name, fatalIfMissing=False):
                        mx.log('Skipping ' + groupId + '.' + artifactId + '.jar as ' + name + ' cannot be resolved')
                        return
        d = mx.Distribution(graalSuite, name=artifactId, path=path, sourcesPath=path, deps=deps, mainClass=None, excludedDependencies=[], distDependencies=[])
        d.make_archive()
        cmd = ['mvn', 'install:install-file', '-DgroupId=' + groupId, '-DartifactId=' + artifactId,
               '-Dversion=1.0-SNAPSHOT', '-Dpackaging=jar', '-Dfile=' + d.path]
        if not mx._opts.verbose:
            cmd.append('-q')
        if args.settings:
            cmd = cmd + ['-s', args.settings]
        mx.run(cmd)
        os.unlink(d.path)

    jmhPath = _get_jmh_path()
    for root, _, filenames in os.walk(jmhPath):
        for f in [join(root, n) for n in filenames if n == 'pom.mxdeps']:
            mx.logv('[processing ' + f + ']')
            try:
                with open(f) as fp:
                    for d in json.load(fp):
                        artifactId = d['artifactId']
                        groupId = d['groupId']
                        deps = d['deps']
                        makejmhdep(artifactId, groupId, deps)
            except ValueError as e:
                mx.abort('Error parsing {}:\n{}'.format(f, e))

def buildjmh(args):
    """build the JMH benchmarks"""

    parser = ArgumentParser(prog='mx buildjmh')
    parser.add_argument('-s', '--settings', help='alternative path for Maven user settings file', metavar='<path>')
    parser.add_argument('-c', action='store_true', dest='clean', help='clean before building')
    args = parser.parse_args(args)

    jmhPath = _get_jmh_path()
    mx.log('JMH benchmarks: ' + jmhPath)

    # Ensure the mx injected dependencies are up to date
    makejmhdeps(['-p'] + (['-s', args.settings] if args.settings else []))

    timestamp = mx.TimeStampFile(join(_graal_home, 'mx', 'jmh', jmhPath.replace(os.sep, '_') + '.timestamp'))
    mustBuild = args.clean
    if not mustBuild:
        try:
            hgfiles = [join(jmhPath, f) for f in subprocess.check_output(['hg', '-R', jmhPath, 'locate']).split('\n')]
            mustBuild = timestamp.isOlderThan(hgfiles)
        except:
            # not a Mercurial repository or hg commands are not available.
            mustBuild = True

    if mustBuild:
        buildOutput = []
        def _redirect(x):
            if mx._opts.verbose:
                mx.log(x[:-1])
            else:
                buildOutput.append(x)
        env = os.environ.copy()
        env['JAVA_HOME'] = _jdk(vmToCheck='server')
        env['MAVEN_OPTS'] = '-server'
        mx.log("Building benchmarks...")
        cmd = ['mvn']
        if args.settings:
            cmd = cmd + ['-s', args.settings]
        if args.clean:
            cmd.append('clean')
        cmd.append('package')
        retcode = mx.run(cmd, cwd=jmhPath, out=_redirect, env=env, nonZeroIsFatal=False)
        if retcode != 0:
            mx.log(''.join(buildOutput))
            mx.abort(retcode)
        timestamp.touch()
    else:
        mx.logv('[all Mercurial controlled files in ' + jmhPath + ' are older than ' + timestamp.path + ' - skipping build]')

def jmh(args):
    """run the JMH benchmarks

    This command respects the standard --vm and --vmbuild options
    for choosing which VM to run the benchmarks with."""
    if '-h' in args:
        mx.help_(['jmh'])
        mx.abort(1)

    vmArgs, benchmarksAndJsons = _extract_VM_args(args)

    benchmarks = [b for b in benchmarksAndJsons if not b.startswith('{')]
    jmhArgJsons = [b for b in benchmarksAndJsons if b.startswith('{')]
    jmhOutDir = join(_graal_home, 'mx', 'jmh')
    if not exists(jmhOutDir):
        os.makedirs(jmhOutDir)
    jmhOut = join(jmhOutDir, 'jmh.out')
    jmhArgs = {'-rff' : jmhOut, '-v' : 'EXTRA' if mx._opts.verbose else 'NORMAL'}

    # e.g. '{"-wi" : 20}'
    for j in jmhArgJsons:
        try:
            for n, v in json.loads(j).iteritems():
                if v is None:
                    del jmhArgs[n]
                else:
                    jmhArgs[n] = v
        except ValueError as e:
            mx.abort('error parsing JSON input: {}\n{}'.format(j, e))

    jmhPath = _get_jmh_path()
    mx.log('Using benchmarks in ' + jmhPath)

    matchedSuites = set()
    numBench = [0]
    for micros in os.listdir(jmhPath):
        absoluteMicro = os.path.join(jmhPath, micros)
        if not os.path.isdir(absoluteMicro):
            continue
        if not micros.startswith("micros-"):
            mx.logv('JMH: ignored ' + absoluteMicro + " because it doesn't start with 'micros-'")
            continue

        microJar = os.path.join(absoluteMicro, "target", "microbenchmarks.jar")
        if not exists(microJar):
            mx.log('Missing ' + microJar + ' - please run "mx buildjmh"')
            continue
        if benchmarks:
            def _addBenchmark(x):
                if x.startswith("Benchmark:"):
                    return
                match = False
                for b in benchmarks:
                    match = match or (b in x)

                if match:
                    numBench[0] += 1
                    matchedSuites.add(micros)

            mx.run_java(['-jar', microJar, "-l"], cwd=jmhPath, out=_addBenchmark, addDefaultArgs=False)
        else:
            matchedSuites.add(micros)

    mx.logv("matchedSuites: " + str(matchedSuites))
    plural = 's' if not benchmarks or numBench[0] > 1 else ''
    number = str(numBench[0]) if benchmarks else "all"
    mx.log("Running " + number + " benchmark" + plural + '...')

    regex = []
    if benchmarks:
        regex.append(r".*(" + "|".join(benchmarks) + ").*")

    for suite in matchedSuites:
        absoluteMicro = os.path.join(jmhPath, suite)
        (pfx, exe, vm, forkedVmArgs, _) = _parseVmArgs(vmArgs)
        if pfx:
            mx.warn("JMH ignores prefix: \"" + pfx + "\"")
        javaArgs = ['-jar', os.path.join(absoluteMicro, "target", "microbenchmarks.jar"),
                    '--jvm', exe,
                    '--jvmArgs', ' '.join(["-" + vm] + forkedVmArgs)]
        for k, v in jmhArgs.iteritems():
            javaArgs.append(k)
            if len(str(v)):
                javaArgs.append(str(v))
        mx.run_java(javaArgs + regex, addDefaultArgs=False, cwd=jmhPath)

def specjvm2008(args):
    """run one or more SPECjvm2008 benchmarks"""

    def launcher(bm, harnessArgs, extraVmOpts):
        return sanitycheck.getSPECjvm2008(harnessArgs + [bm]).bench(_get_vm(), extraVmOpts=extraVmOpts)

    availableBenchmarks = set(sanitycheck.specjvm2008Names)
    for name in sanitycheck.specjvm2008Names:
        parts = name.rsplit('.', 1)
        if len(parts) > 1:
            assert len(parts) == 2
            group = parts[0]
            availableBenchmarks.add(group)

    _run_benchmark(args, sorted(availableBenchmarks), launcher)

def specjbb2013(args):
    """runs the composite SPECjbb2013 benchmark"""

    def launcher(bm, harnessArgs, extraVmOpts):
        assert bm is None
        return sanitycheck.getSPECjbb2013(harnessArgs).bench(_get_vm(), extraVmOpts=extraVmOpts)

    _run_benchmark(args, None, launcher)

def specjbb2005(args):
    """runs the composite SPECjbb2005 benchmark"""

    def launcher(bm, harnessArgs, extraVmOpts):
        assert bm is None
        return sanitycheck.getSPECjbb2005(harnessArgs).bench(_get_vm(), extraVmOpts=extraVmOpts)

    _run_benchmark(args, None, launcher)

def hsdis(args, copyToDir=None):
    """download the hsdis library

    This is needed to support HotSpot's assembly dumping features.
    By default it downloads the Intel syntax version, use the 'att' argument to install AT&T syntax."""
    flavor = 'intel'
    if 'att' in args:
        flavor = 'att'
    lib = mx.add_lib_suffix('hsdis-' + _arch())
    path = join(_graal_home, 'lib', lib)
    if not exists(path):
        mx.download(path, ['http://lafo.ssw.uni-linz.ac.at/hsdis/' + flavor + "/" + lib])
    if copyToDir is not None and exists(copyToDir):
        shutil.copy(path, copyToDir)

def hcfdis(args):
    """disassemble HexCodeFiles embedded in text files

    Run a tool over the input files to convert all embedded HexCodeFiles
    to a disassembled format."""

    parser = ArgumentParser(prog='mx hcfdis')
    parser.add_argument('-m', '--map', help='address to symbol map applied to disassembler output')
    parser.add_argument('files', nargs=REMAINDER, metavar='files...')

    args = parser.parse_args(args)

    path = join(_graal_home, 'lib', 'hcfdis-1.jar')
    if not exists(path):
        mx.download(path, ['http://lafo.ssw.uni-linz.ac.at/hcfdis-2.jar'])
    mx.run_java(['-jar', path] + args.files)

    if args.map is not None:
        addressRE = re.compile(r'0[xX]([A-Fa-f0-9]+)')
        with open(args.map) as fp:
            lines = fp.read().splitlines()
        symbols = dict()
        for l in lines:
            addressAndSymbol = l.split(' ', 1)
            if len(addressAndSymbol) == 2:
                address, symbol = addressAndSymbol
                if address.startswith('0x'):
                    address = long(address, 16)
                    symbols[address] = symbol
        for f in args.files:
            with open(f) as fp:
                lines = fp.read().splitlines()
            updated = False
            for i in range(0, len(lines)):
                l = lines[i]
                for m in addressRE.finditer(l):
                    sval = m.group(0)
                    val = long(sval, 16)
                    sym = symbols.get(val)
                    if sym:
                        l = l.replace(sval, sym)
                        updated = True
                        lines[i] = l
            if updated:
                mx.log('updating ' + f)
                with open('new_' + f, "w") as fp:
                    for l in lines:
                        print >> fp, l

def jacocoreport(args):
    """create a JaCoCo coverage report

    Creates the report from the 'jacoco.exec' file in the current directory.
    Default output directory is 'coverage', but an alternative can be provided as an argument."""
    jacocoreport = mx.library("JACOCOREPORT", True)
    out = 'coverage'
    if len(args) == 1:
        out = args[0]
    elif len(args) > 1:
        mx.abort('jacocoreport takes only one argument : an output directory')
    mx.run_java(['-jar', jacocoreport.get_path(True), '--in', 'jacoco.exec', '--out', out] + [p.dir for p in mx.projects()])

def sl(args):
    """run an SL program"""
    vmArgs, slArgs = _extract_VM_args(args)
    vm(vmArgs + ['-cp', mx.classpath("com.oracle.truffle.sl"), "com.oracle.truffle.sl.SLMain"] + slArgs)

def isGraalEnabled(vm):
    return vm != 'original' and not vm.endswith('nograal')

def pythonShellCp():
    return mx.classpath("edu.uci.python.shell");

def pythonShellClass():
    return "edu.uci.python.shell.Shell";

def python(args):
    """run a Python program or shell
    
    VM args should have a @ prefix."""
    
    vmArgs = [a[1:] for a in args if a[0] == '@']
    pythonArgs = [a for a in args if a[0] != '@']

    vm(vmArgs + ['-cp', pythonShellCp(), pythonShellClass()] + pythonArgs)

def jol(args):
    """Java Object Layout"""
    jolurl = "http://lafo.ssw.uni-linz.ac.at/truffle/jol/jol-internals.jar"
    joljar = "lib/jol-internals.jar"
    if not exists(joljar):
        mx.download(joljar, [jolurl])

    candidates = mx.findclass(args, logToConsole=False, matcher=lambda s, classname: s == classname or classname.endswith('.' + s) or classname.endswith('$' + s))
    if len(candidates) > 10:
        print "Found %d candidates. Please be more precise." % (len(candidates))
        return

    vm(['-javaagent:' + joljar, '-cp', os.pathsep.join([mx.classpath(), joljar]), "org.openjdk.jol.MainObjectInternals"] + candidates)

def site(args):
    """create a website containing javadoc and the project dependency graph"""

    return mx.site(['--name', 'Graal',
                    '--jd', '@-tag', '--jd', '@test:X',
                    '--jd', '@-tag', '--jd', '@run:X',
                    '--jd', '@-tag', '--jd', '@bug:X',
                    '--jd', '@-tag', '--jd', '@summary:X',
                    '--jd', '@-tag', '--jd', '@vmoption:X',
                    '--overview', join(_graal_home, 'graal', 'overview.html'),
                    '--title', 'Graal OpenJDK Project Documentation',
                    '--dot-output-base', 'projects'] + args)

def generateZshCompletion(args):
    """generate zsh completion for mx"""
    try:
        from genzshcomp import CompletionGenerator
    except ImportError:
        mx.abort("install genzshcomp (pip install genzshcomp)")

    # need to fake module for the custom mx arg parser, otherwise a check in genzshcomp fails
    originalModule = mx._argParser.__module__
    mx._argParser.__module__ = "argparse"
    generator = CompletionGenerator("mx", mx._argParser)
    mx._argParser.__module__ = originalModule

    # strip last line and define local variable "ret"
    complt = "\n".join(generator.get().split('\n')[0:-1]).replace('context state line', 'context state line ret=1')

    # add array of possible subcommands (as they are not part of the argument parser)
    complt += '\n  ": :->command" \\\n'
    complt += '  "*::args:->args" && ret=0\n'
    complt += '\n'
    complt += 'case $state in\n'
    complt += '\t(command)\n'
    complt += '\t\tlocal -a main_commands\n'
    complt += '\t\tmain_commands=(\n'
    for cmd in sorted(mx._commands.iterkeys()):
        c, _ = mx._commands[cmd][:2]
        doc = c.__doc__
        complt += '\t\t\t"{0}'.format(cmd)
        if doc:
            complt += ':{0}'.format(_fixQuotes(doc.split('\n', 1)[0]))
        complt += '"\n'
    complt += '\t\t)\n'
    complt += '\t\t_describe -t main_commands command main_commands && ret=0\n'
    complt += '\t\t;;\n'

    complt += '\t(args)\n'
    # TODO: improve matcher: if mx args are given, this doesn't work
    complt += '\t\tcase $line[1] in\n'
    complt += '\t\t\t(vm)\n'
    complt += '\t\t\t\tnoglob \\\n'
    complt += '\t\t\t\t\t_arguments -s -S \\\n'
    complt += _appendOptions("graal", r"G\:")
    # TODO: fix -XX:{-,+}Use* flags
    complt += _appendOptions("hotspot", r"XX\:")
    complt += '\t\t\t\t\t"-version" && ret=0 \n'
    complt += '\t\t\t\t;;\n'
    complt += '\t\tesac\n'
    complt += '\t\t;;\n'
    complt += 'esac\n'
    complt += '\n'
    complt += 'return $ret'
    print complt

def _fixQuotes(arg):
    return arg.replace('\"', '').replace('\'', '').replace('`', '').replace('{', '\\{').replace('}', '\\}').replace('[', '\\[').replace(']', '\\]')

def _appendOptions(optionType, optionPrefix):
    def isBoolean(vmap, field):
        return vmap[field] == "Boolean" or vmap[field] == "bool"

    def hasDescription(vmap):
        return vmap['optDefault'] or vmap['optDoc']

    complt = ""
    for vmap in _parseVMOptions(optionType):
        complt += '\t\t\t\t\t-"'
        complt += optionPrefix
        if isBoolean(vmap, 'optType'):
            complt += '"{-,+}"'
        complt += vmap['optName']
        if not isBoolean(vmap, 'optType'):
            complt += '='
        if hasDescription(vmap):
            complt += "["
        if vmap['optDefault']:
            complt += r"(default\: " + vmap['optDefault'] + ")"
        if vmap['optDoc']:
            complt += _fixQuotes(vmap['optDoc'])
        if hasDescription(vmap):
            complt += "]"
        complt += '" \\\n'
    return complt

def _parseVMOptions(optionType):
    parser = OutputParser()
    # TODO: the optDoc part can wrapped accross multiple lines, currently only the first line will be captured
    # TODO: fix matching for float literals
    jvmOptions = re.compile(
        r"^[ \t]*"
        r"(?P<optType>(Boolean|Integer|Float|Double|String|bool|intx|uintx|ccstr|double)) "
        r"(?P<optName>[a-zA-Z0-9]+)"
        r"[ \t]+=[ \t]*"
        r"(?P<optDefault>([\-0-9]+(\.[0-9]+(\.[0-9]+\.[0-9]+))?|false|true|null|Name|sun\.boot\.class\.path))?"
        r"[ \t]*"
        r"(?P<optDoc>.+)?",
        re.MULTILINE)
    parser.addMatcher(ValuesMatcher(jvmOptions, {
        'optType' : '<optType>',
        'optName' : '<optName>',
        'optDefault' : '<optDefault>',
        'optDoc' : '<optDoc>',
        }))

    # gather graal options
    output = StringIO.StringIO()
    vm(['-XX:-BootstrapGraal', '-G:+PrintFlags' if optionType == "graal" else '-XX:+PrintFlagsWithComments'],
       vm="graal",
       vmbuild="optimized",
       nonZeroIsFatal=False,
       out=output.write,
       err=subprocess.STDOUT)

    valueMap = parser.parse(output.getvalue())
    return valueMap

def findbugs(args):
    '''run FindBugs against non-test Java projects'''
    findBugsHome = mx.get_env('FINDBUGS_HOME', None)
    if findBugsHome:
        findbugsJar = join(findBugsHome, 'lib', 'findbugs.jar')
    else:
        findbugsLib = join(_graal_home, 'lib', 'findbugs-3.0.0')
        if not exists(findbugsLib):
            tmp = tempfile.mkdtemp(prefix='findbugs-download-tmp', dir=_graal_home)
            try:
                findbugsDist = join(tmp, 'findbugs.zip')
                mx.download(findbugsDist, ['http://sourceforge.net/projects/findbugs/files/findbugs/3.0.0/findbugs-3.0.0-dev-20131204-e3cbbd5.zip'])
                with zipfile.ZipFile(findbugsDist) as zf:
                    candidates = [e for e in zf.namelist() if e.endswith('/lib/findbugs.jar')]
                    assert len(candidates) == 1, candidates
                    libDirInZip = os.path.dirname(candidates[0])
                    zf.extractall(tmp)
                shutil.copytree(join(tmp, libDirInZip), findbugsLib)
            finally:
                shutil.rmtree(tmp)
        findbugsJar = join(findbugsLib, 'findbugs.jar')
    assert exists(findbugsJar)
    nonTestProjects = [p for p in mx.projects() if not p.name.endswith('.test') and not p.name.endswith('.jtt')]
    outputDirs = [p.output_dir() for p in nonTestProjects]
    findbugsResults = join(_graal_home, 'findbugs.results')

    cmd = ['-jar', findbugsJar, '-textui', '-low', '-maxRank', '15']
    if sys.stdout.isatty():
        cmd.append('-progress')
    cmd = cmd + ['-auxclasspath', mx.classpath([p.name for p in nonTestProjects]), '-output', findbugsResults, '-exitcode'] + args + outputDirs
    exitcode = mx.run_java(cmd, nonZeroIsFatal=False)
    if exitcode != 0:
        with open(findbugsResults) as fp:
            mx.log(fp.read())
    os.unlink(findbugsResults)
    return exitcode

def checkheaders(args):
    """check Java source headers against any required pattern"""
    failures = {}
    for p in mx.projects():
        if p.native:
            continue

        csConfig = join(mx.project(p.checkstyleProj).dir, '.checkstyle_checks.xml.disabled')
        dom = xml.dom.minidom.parse(csConfig)
        for module in dom.getElementsByTagName('module'):
            if module.getAttribute('name') == 'RegexpHeader':
                for prop in module.getElementsByTagName('property'):
                    if prop.getAttribute('name') == 'header':
                        value = prop.getAttribute('value')
                        matcher = re.compile(value, re.MULTILINE)
                        for sourceDir in p.source_dirs():
                            for root, _, files in os.walk(sourceDir):
                                for name in files:
                                    if name.endswith('.java') and name != 'package-info.java':
                                        f = join(root, name)
                                        with open(f) as fp:
                                            content = fp.read()
                                        if not matcher.match(content):
                                            failures[f] = csConfig
    for n, v in failures.iteritems():
        mx.log('{}: header does not match RegexpHeader defined in {}'.format(n, v))
    return len(failures)

def mx_init(suite):
    commands = {
        'build': [build, ''],
        'buildjmh': [buildjmh, '[-options]'],
        'buildvars': [buildvars, ''],
        'buildvms': [buildvms, '[-options]'],
        'c1visualizer' : [c1visualizer, ''],
        'checkheaders': [checkheaders, ''],
        'clean': [clean, ''],
        'findbugs': [findbugs, ''],
        'generateZshCompletion' : [generateZshCompletion, ''],
        'hsdis': [hsdis, '[att]'],
        'hcfdis': [hcfdis, ''],
        'igv' : [igv, ''],
        'jdkhome': [print_jdkhome, ''],
        'jmh': [jmh, '[VM options] [filters|JMH-args-as-json...]'],
        'dacapo': [dacapo, '[VM options] benchmarks...|"all" [DaCapo options]'],
        'scaladacapo': [scaladacapo, '[VM options] benchmarks...|"all" [Scala DaCapo options]'],
        'specjvm2008': [specjvm2008, '[VM options] benchmarks...|"all" [SPECjvm2008 options]'],
        'specjbb2013': [specjbb2013, '[VM options] [-- [SPECjbb2013 options]]'],
        'specjbb2005': [specjbb2005, '[VM options] [-- [SPECjbb2005 options]]'],
        'gate' : [gate, '[-options]'],
        'bench' : [bench, '[-resultfile file] [all(default)|dacapo|specjvm2008|bootstrap]'],
        'unittest' : [unittest, '[unittest options] [--] [VM options] [filters...]', _unittestHelpSuffix],
        'makejmhdeps' : [makejmhdeps, ''],
        'shortunittest' : [shortunittest, '[unittest options] [--] [VM options] [filters...]', _unittestHelpSuffix],
        'jacocoreport' : [jacocoreport, '[output directory]'],
        'python' : [python, '[Python args|@VM options]'],
        'site' : [site, '[-options]'],
        'vm': [vm, '[-options] class [args...]'],
        'vmg': [vmg, '[-options] class [args...]'],
        'vmfg': [vmfg, '[-options] class [args...]'],
        'deoptalot' : [deoptalot, '[n]'],
        'longtests' : [longtests, ''],
        'sl' : [sl, '[SL args|@VM options]'],
        'jol' : [jol, ''],
    }

    mx.add_argument('--jacoco', help='instruments com.oracle.* classes using JaCoCo', default='off', choices=['off', 'on', 'append'])
    mx.add_argument('--vmcwd', dest='vm_cwd', help='current directory will be changed to <path> before the VM is executed', default=None, metavar='<path>')
    mx.add_argument('--installed-jdks', help='the base directory in which the JDKs cloned from $JAVA_HOME exist. ' +
                    'The VM selected by --vm and --vmbuild options is under this directory (i.e., ' +
                    join('<path>', '<jdk-version>', '<vmbuild>', 'jre', 'lib', '<vm>', mx.add_lib_prefix(mx.add_lib_suffix('jvm'))) + ')', default=None, metavar='<path>')

    if _vmSourcesAvailable:
        mx.add_argument('--vm', action='store', dest='vm', choices=_vmChoices.keys(), help='the VM type to build/run')
        mx.add_argument('--vmbuild', action='store', dest='vmbuild', choices=_vmbuildChoices, help='the VM build to build/run (default: ' + _vmbuildChoices[0] + ')')
        mx.add_argument('--ecl', action='store_true', dest='make_eclipse_launch', help='create launch configuration for running VM execution(s) in Eclipse')
        mx.add_argument('--vmprefix', action='store', dest='vm_prefix', help='prefix for running the VM (e.g. "/usr/bin/gdb --args")', metavar='<prefix>')
        mx.add_argument('--gdb', action='store_const', const='/usr/bin/gdb --args', dest='vm_prefix', help='alias for --vmprefix "/usr/bin/gdb --args"')

        commands.update({
            'export': [export, '[-options] [zipfile]'],
        })

    mx.update_commands(suite, commands)

def mx_post_parse_cmd_line(opts):  #
    # TODO _minVersion check could probably be part of a Suite in mx?
    if mx.java().version < _minVersion:
        mx.abort('Requires Java version ' + str(_minVersion) + ' or greater, got version ' + str(mx.java().version))

    if _vmSourcesAvailable:
        if hasattr(opts, 'vm') and opts.vm is not None:
            global _vm
            _vm = opts.vm
        if hasattr(opts, 'vmbuild') and opts.vmbuild is not None:
            global _vmbuild
            _vmbuild = opts.vmbuild
        global _make_eclipse_launch
        _make_eclipse_launch = getattr(opts, 'make_eclipse_launch', False)
    global _jacoco
    _jacoco = opts.jacoco
    global _vm_cwd
    _vm_cwd = opts.vm_cwd
    global _installed_jdks
    _installed_jdks = opts.installed_jdks
    global _vm_prefix
    _vm_prefix = opts.vm_prefix

    mx.distribution('GRAAL').add_update_listener(_installGraalJarInJdks)
