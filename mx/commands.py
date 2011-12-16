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

import os, sys, shutil
from os.path import join, exists, dirname, isfile, isdir

graal_home = dirname(dirname(__file__))

def clean(env, args):
    """cleans the GraalVM source tree"""
    os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='16')
    env.run([env.gmake_cmd(), 'clean'], cwd=join(graal_home, 'make'))

def example(env, args):
    """run some or all Graal examples"""
    examples = {
        'safeadd': ['com.oracle.max.graal.examples.safeadd', 'com.oracle.max.graal.examples.safeadd.Main'],
        'vectorlib': ['com.oracle.max.graal.examples.vectorlib', 'com.oracle.max.graal.examples.vectorlib.Main'],
    }

    def run_example(env, verbose, project, mainClass):
        cp = env.pdb.classpath(project)
        sharedArgs = ['-Xcomp', '-XX:CompileOnly=Main', mainClass]
        
        res = []
        env.log("=== Server VM ===")
        printArg = '-XX:+PrintCompilation' if verbose else '-XX:-PrintCompilation'
        res.append(vm(env, ['-cp', cp, printArg] + sharedArgs, vm="-server"))
        env.log("=== Graal VM ===")
        printArg = '-G:+PrintCompilation' if verbose else '-G:-PrintCompilation'
        res.append(vm(env, ['-cp', cp, printArg, '-G:-Extend', '-G:-Inline'] + sharedArgs))
        env.log("=== Graal VM with extensions ===")
        res.append(vm(env, ['-cp', cp, printArg, '-G:+Extend', '-G:-Inline'] + sharedArgs))
        
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
            env.log('unknown example: ' + a + '  {available examples = ' + str(examples.keys()) + '}')
        else:
            env.log('--------- ' + a + ' ------------')
            project, mainClass = config
            run_example(env, verbose, project, mainClass)

def dacapo(env, args):
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
    
    dacapo = env.check_get_env('DACAPO_CP')
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        env.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
            
    vmOpts = ['-Xms1g', '-Xmx2g', '-esa', '-cp', dacapo]

    runs = dict()    
    while len(args) != 0 and not args[0].startswith('-'):
        bm = args[0]
        del args[0]
        config = benchmarks.get(bm) 
        if (config is None):
            env.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(benchmarks.keys()))
        runs[bm] = config
    
    if len(runs) == 0:
        runs = benchmarks
        
    vmOpts += args
    for bm in runs:
        config = benchmarks.get(bm)
        vm(env, vmOpts + ['Harness'] + config + [bm])
    
def tests(env, args):
    """run a selection of the Maxine JTT tests in Graal"""
    
    maxine = env.check_get_env('MAXINE_HOME')
    def jtt(name):
        return join(maxine, 'com.oracle.max.vm', 'test', 'jtt', name)
    
    return vm(env, ['-ea', '-esa', '-Xcomp', '-XX:+PrintCompilation', '-XX:CompileOnly=jtt'] + args +
                       ['-Xbootclasspath/p:' + join(maxine, 'com.oracle.max.vm', 'bin'), 
                        '-Xbootclasspath/p:' + join(maxine, 'com.oracle.max.base', 'bin'),
                        'test.com.sun.max.vm.compiler.JavaTester',
                        '-verbose=1', '-gen-run-scheme=false', '-run-scheme-package=all',
                        jtt('bytecode'),
                        jtt('except'), 
                        jtt('jdk'), 
                        jtt('hotpath'), 
                        jtt('jdk'), 
                        jtt('lang'), 
                        jtt('loop'), 
                        jtt('micro'), 
                        jtt('optimize'), 
                        jtt('reflect'), 
                        jtt('threads'), 
                        jtt('hotspot')])

def _jdk7(env, build='product', create=False):
    jdk7 = env.check_get_env('JDK7')
    jre = join(jdk7, 'jre')
    if not exists(jre) or not isdir(jre):
        env.abort(jdk7 + ' does not appear to be a valid JDK directory ("jre" sub-directory is missing)')
    
    if build == 'product':
        return jdk7
    elif build in ['debug', 'fastdebug', 'optimized']:
        res = join(jdk7, build)
        if not exists(res):
            if not create:
                env.abort('The ' + build + ' VM has not been created - run \'mx clean; mx make ' + build + '\'') 
            env.log('[creating ' + res + '...]')
            os.mkdir(res)
            for d in ['jre', 'lib', 'bin', 'include']:
                shutil.copytree(join(jdk7, d), join(res, d))
        return res
    else:
        env.abort('Unknown build type: ' + build)
    
def make(env, args):
    """builds the GraalVM binary
    
    The optional argument specifies what type of VM to build."""

    def fix_jvm_cfg(env, jdk):
        jvmCfg = join(jdk, 'jre', 'lib', 'amd64', 'jvm.cfg')
        found = False
        if not exists(jvmCfg):
            env.abort(jvmCfg + ' does not exist')
            
        with open(jvmCfg) as f:
            for line in f:
                if '-graal KNOWN' in line:
                    found = True
                    break
        if not found:
            env.log('Appending "-graal KNOWN" to ' + jvmCfg)
            with open(jvmCfg, 'a') as f:
                f.write('-graal KNOWN\n')

    build = 'product' if len(args) == 0 else args[0]
    jdk7 = _jdk7(env, build, True)
    if build == 'debug':
        build = 'jvmg'
    
    fix_jvm_cfg(env, jdk7)

    graalVmDir = join(jdk7, 'jre', 'lib', 'amd64', 'graal')
    if not exists(graalVmDir):
        env.log('Creating Graal directory in JDK7: ' + graalVmDir)
        os.makedirs(graalVmDir)

    def filterXusage(line):
        if not 'Xusage.txt' in line:
            sys.stderr.write(line + os.linesep)
            
    os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='3', ALT_BOOTDIR=jdk7, INSTALL='y')
    env.run([env.gmake_cmd(), build + 'graal'], cwd=join(graal_home, 'make'), err=filterXusage)
    
def vm(env, args, vm='-graal'):
    """run the GraalVM"""
  
    build = env.vmbuild
    if env.java_dbg:
        args = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000'] + args
    os.environ['MAXINE'] = env.check_get_env('GRAAL_HOME')
    exe = join(_jdk7(env, build), 'bin', env.exe_suffix('java'))
    return env.run([exe, vm] + args)

def mx_init(env):
    env.vmbuild = 'product'
    env.add_argument('--product', action='store_const', dest='vmbuild', const='product', help='select the product VM')
    env.add_argument('--debug', action='store_const', dest='vmbuild', const='debug', help='select the debug VM')
    env.add_argument('--fastdebug', action='store_const', dest='vmbuild', const='fastdebug', help='select the fast debug VM')
    env.add_argument('--optimized', action='store_const', dest='vmbuild', const='optimized', help='select the optimized VM')
    commands = {
        'dacapo': [dacapo, '[benchmark] [VM options]'],
        'example': [example, '[-v] example names...'],
        'clean': [clean, ''],
        'make': [make, '[product|debug|fastdebug|optimized]'],
        'tests': [tests, ''],
        'vm': [vm, '[-options] class [args...]'],
    }
    env.commands.update(commands)
