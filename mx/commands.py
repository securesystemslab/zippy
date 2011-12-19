#
# commands.py - the default commands available to gl.py
#
# ----------------------------------------------------------------------------------------------------
#
# Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

import os, sys, shutil, tarfile, StringIO
from os.path import join, exists, dirname, isfile, isdir, isabs
import mx

_graal_home = dirname(dirname(__file__))
_vmbuild = 'product'

def clean(args):
    """cleans the GraalVM source tree"""
    mx.clean(args)
    os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='16')
    mx.run([mx.gmake_cmd(), 'clean'], cwd=join(_graal_home, 'make'))

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
        res.append(vm(['-cp', cp, printArg] + sharedArgs, vm="-server"))
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
    """run one or all DaCapo benchmarks"""
    
    benchmarks = {
        'avrora': ['-n', '5'],
        'batik': ['-n', '5'],
        'eclipse': ['-n', '5'],
        'fop': ['-n', '5'],
        'h2': ['-n', '5'],
        'jython': ['-n', '5'],
        'luindex': ['-n', '5'],
        'lusearch': ['-n', '5'],
        'pmd': ['-n', '5'],
        'sunflow': ['-n', '5'],
        'tomcat': ['-n', '5'],
        'tradebeans': ['-n', '5'],
        'tradesoap': ['-n', '5'],
        'xalan': ['-n', '5'],
    }
    
    dacapo = mx.check_get_env('DACAPO_CP')
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        mx.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
            
    vmOpts = ['-Xms1g', '-Xmx2g', '-esa', '-cp', dacapo]

    runs = dict()    
    while len(args) != 0 and not args[0].startswith('-'):
        bm = args[0]
        del args[0]
        config = benchmarks.get(bm) 
        if (config is None):
            mx.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(benchmarks.keys()))
        runs[bm] = config
    
    if len(runs) == 0:
        runs = benchmarks
        
    vmOpts += args
    for bm in runs:
        config = benchmarks.get(bm)
        vm(vmOpts + ['Harness'] + config + [bm])
    
def _download_and_extract_targz_jdk7(url, dst):
    assert url.endswith('.tar.gz')
    dl = join(_graal_home, 'jdk7.tar.gz')
    try:
        if not exists(dl):
            mx.log('Downloading ' + url)
            mx.download(dl, [url])
        tmp = join(_graal_home, 'tmp')
        if not exists(tmp):
            os.mkdir(tmp)
        with tarfile.open(dl, mode='r:gz') as f:
            mx.log('Extracting ' + dl)
            f.extractall(path=tmp)
        jdk = os.listdir(tmp)[0]
        shutil.move(join(tmp, jdk), dst)
        os.rmdir(tmp)
        os.remove(dl)
    except SystemExit:
        mx.abort('Could not download JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html.\n' + 
                  'Please do this manually and install it at ' + dst + ' or set the JDK7 environment variable to the install location.')
    

def _jdk7(build='product', create=False):
    jdk7 = os.environ.get('JDK7')
    if jdk7 is None:
        jdk7 = join(_graal_home, 'jdk1.7.0')
        if not exists(jdk7):
            # Try to download it
            if mx.get_os() == 'linux':
                _download_and_extract_targz_jdk7('http://download.oracle.com/otn-pub/java/jdk/7u2-b13/jdk-7u2-linux-x64.tar.gz', jdk7)
            else:
                mx.abort('Download JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html\n' + 
                          'and install it at ' + jdk7 + ' or set the JDK7 environment variable to the JDK7 install location.')
        
    jre = join(jdk7, 'jre')
    if not exists(jre) or not isdir(jre):
        mx.abort(jdk7 + ' does not appear to be a valid JDK directory ("jre" sub-directory is missing)')
    
    if build == 'product':
        return jdk7
    elif build in ['debug', 'fastdebug', 'optimized']:
        res = join(jdk7, build)
        if not exists(res):
            if not create:
                mx.abort('The ' + build + ' VM has not been created - run \'mx clean; mx make ' + build + '\'') 
            mx.log('[creating ' + res + '...]')
            os.mkdir(res)
            for d in ['jre', 'lib', 'bin', 'include']:
                shutil.copytree(join(jdk7, d), join(res, d))
        return res
    else:
        mx.abort('Unknown build type: ' + build)
    
def build(args):
    """builds the GraalVM binary and compiles the Graal classes
    
    The optional argument specifies what type of VM to build."""

    build = 'product'
    if len(args) != 0 and not args[0].startswith('-'):
        build = args.pop(0)

    # Call mx.build to compile the Java sources        
    mx.build(args + ['--source', '1.7'])

    def fix_jvm_cfg(jdk):
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

    
    jdk7 = _jdk7(build, True)
    if build == 'debug':
        build = 'jvmg'
    
    fix_jvm_cfg(jdk7)

    graalVmDir = join(jdk7, 'jre', 'lib', 'amd64', 'graal')
    if not exists(graalVmDir):
        mx.log('Creating Graal directory in JDK7: ' + graalVmDir)
        os.makedirs(graalVmDir)

    def filterXusage(line):
        if not 'Xusage.txt' in line:
            sys.stderr.write(line + os.linesep)
            
    os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='3', ALT_BOOTDIR=jdk7, INSTALL='y')
    mx.run([mx.gmake_cmd(), build + 'graal'], cwd=join(_graal_home, 'make'), err=filterXusage)
    
def vm(args, vm='-graal'):
    """run the GraalVM"""
  
    build = _vmbuild
    if mx.java().debug:
        args = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000'] + args
    os.environ['GRAAL'] = join(_graal_home, 'graal')
    exe = join(_jdk7(build), 'bin', mx.exe_suffix('java'))
    return mx.run([exe, vm] + args)

def ideinit(args):
    """(re)generate Eclipse project configurations

    The exit code of this command reflects how many files were updated."""


    def println(out, obj):
        out.write(str(obj) + '\n')
        
    for p in mx.projects():
        if p.native:
            continue
        
        if not exists(p.dir):
            os.makedirs(p.dir)

        changedFiles = 0

        out = StringIO.StringIO()
        
        println(out, '<?xml version="1.0" encoding="UTF-8"?>')
        println(out, '<classpath>')
        for src in p.srcDirs:
            srcDir = join(p.dir, src)
            if not exists(srcDir):
                os.mkdir(srcDir)
            println(out, '\t<classpathentry kind="src" path="' + src + '"/>')
    
        # Every Java program depends on the JRE
        println(out, '\t<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>')
        
        for dep in p.all_deps([], True):
            if dep == p:
                continue;
            
            if dep.isLibrary():
                if hasattr(dep, 'eclipse.container'):
                    println(out, '\t<classpathentry exported="true" kind="con" path="' + getattr(dep, 'eclipse.container') + '"/>')
                elif hasattr(dep, 'eclipse.project'):
                    println(out, '\t<classpathentry combineaccessrules="false" exported="true" kind="src" path="/' + getattr(dep, 'eclipse.project') + '"/>')
                else:
                    path = dep.path
                    if dep.mustExist:
                        if isabs(path):
                            println(out, '\t<classpathentry exported="true" kind="lib" path="' + path + '"/>')
                        else:
                            println(out, '\t<classpathentry exported="true" kind="lib" path="/' + path + '"/>')
            else:
                println(out, '\t<classpathentry combineaccessrules="false" exported="true" kind="src" path="/' + dep.name + '"/>')
                        
        println(out, '\t<classpathentry kind="output" path="' + getattr(p, 'eclipse.output', 'bin') + '"/>')
        println(out, '</classpath>')
        
        if mx.update_file(join(p.dir, '.classpath'), out.getvalue()):
            changedFiles += 1
            
        out.close()

        csConfig = join(mx.project(p.checkstyleProj).dir, '.checkstyle_checks.xml')
        if exists(csConfig):
            out = StringIO.StringIO()
            
            dotCheckstyle = join(p.dir, ".checkstyle")
            checkstyleConfigPath = '/' + p.checkstyleProj + '/.checkstyle_checks.xml'
            println(out, '<?xml version="1.0" encoding="UTF-8"?>')
            println(out, '<fileset-config file-format-version="1.2.0" simple-config="true">')
            println(out, '\t<local-check-config name="Graal Checks" location="' + checkstyleConfigPath + '" type="project" description="">')
            println(out, '\t\t<additional-data name="protect-config-file" value="false"/>')
            println(out, '\t</local-check-config>')
            println(out, '\t<fileset name="all" enabled="true" check-config-name="Graal Checks" local="true">')
            println(out, '\t\t<file-match-pattern match-pattern="." include-pattern="true"/>')
            println(out, '\t</fileset>')
            println(out, '\t<filter name="FileTypesFilter" enabled="true">')
            println(out, '\t\t<filter-data value="java"/>')
            println(out, '\t</filter>')

            exclude = join(p.dir, '.checkstyle.exclude')
            if exists(exclude):
                println(out, '\t<filter name="FilesFromPackage" enabled="true">')
                with open(exclude) as f:
                    for line in f:
                        if not line.startswith('#'):
                            line = line.strip()
                            exclDir = join(p.dir, line)
                            assert isdir(exclDir), 'excluded source directory listed in ' + exclude + ' does not exist or is not a directory: ' + exclDir
                        println(out, '\t\t<filter-data value="' + line + '"/>')
                println(out, '\t</filter>')
                        
            println(out, '</fileset-config>')
            
            if mx.update_file(dotCheckstyle, out.getvalue()):
                changedFiles += 1
                
            out.close()
        

        out = StringIO.StringIO()
        
        println(out, '<?xml version="1.0" encoding="UTF-8"?>')
        println(out, '<projectDescription>')
        println(out, '\t<name>' + p.name + '</name>')
        println(out, '\t<comment></comment>')
        println(out, '\t<projects>')
        println(out, '\t</projects>')
        println(out, '\t<buildSpec>')
        println(out, '\t\t<buildCommand>')
        println(out, '\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>')
        println(out, '\t\t\t<arguments>')
        println(out, '\t\t\t</arguments>')
        println(out, '\t\t</buildCommand>')
        if exists(csConfig):
            println(out, '\t\t<buildCommand>')
            println(out, '\t\t\t<name>net.sf.eclipsecs.core.CheckstyleBuilder</name>')
            println(out, '\t\t\t<arguments>')
            println(out, '\t\t\t</arguments>')
            println(out, '\t\t</buildCommand>')
        println(out, '\t</buildSpec>')
        println(out, '\t<natures>')
        println(out, '\t\t<nature>org.eclipse.jdt.core.javanature</nature>')
        if exists(csConfig):
            println(out, '\t\t<nature>net.sf.eclipsecs.core.CheckstyleNature</nature>')
        println(out, '\t</natures>')
        println(out, '</projectDescription>')
        
        if mx.update_file(join(p.dir, '.project'), out.getvalue()):
            changedFiles += 1
            
        out.close()

        out = StringIO.StringIO()
        
        settingsDir = join(p.dir, ".settings")
        if not exists(settingsDir):
            os.mkdir(settingsDir)

        myDir = dirname(__file__)
        
        with open(join(myDir, 'org.eclipse.jdt.core.prefs')) as f:
            content = f.read()
        if mx.update_file(join(settingsDir, 'org.eclipse.jdt.core.prefs'), content):
            changedFiles += 1
            
        with open(join(myDir, 'org.eclipse.jdt.ui.prefs')) as f:
            content = f.read()
        if mx.update_file(join(settingsDir, 'org.eclipse.jdt.ui.prefs'), content):
            changedFiles += 1
        
    if changedFiles != 0:
        mx.abort(changedFiles)

def mx_init():
    _vmbuild = 'product'
    mx.add_argument('--product', action='store_const', dest='vmbuild', const='product', help='select the product VM')
    mx.add_argument('--debug', action='store_const', dest='vmbuild', const='debug', help='select the debug VM')
    mx.add_argument('--fastdebug', action='store_const', dest='vmbuild', const='fastdebug', help='select the fast debug VM')
    mx.add_argument('--optimized', action='store_const', dest='vmbuild', const='optimized', help='select the optimized VM')
    commands = {
        'build': [build, '[product|debug|fastdebug|optimized]'],
        'dacapo': [dacapo, '[benchmark] [VM options]'],
        'example': [example, '[-v] example names...'],
        'clean': [clean, ''],
        'vm': [vm, '[-options] class [args...]'],
	    'ideinit': [ideinit, ''],
    }
    mx.commands.update(commands)

def mx_post_parse_cmd_line(opts):
    global _vmbuild
    if not opts.vmbuild is None:
        _vmbuild = opts.vmbuild
