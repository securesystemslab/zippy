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

import os, sys
from os.path import join, exists, dirname, isfile

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
    """run a DaCapo benchmark"""
    
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
    
    if len(args) == 0:
        args = args[0:]
        for bm in benchmarks:
            run_dacapo(env, args + ['Harness', '-n', '2'] + [bm])
        return 
    else:
        bm = args[0]
        config = benchmarks.get(bm)
        if (config is None):
            env.abort('unknown benchmark: ' + bm + '\nselect one of: ' + str(benchmarks.keys()))
        args = args[1:]
        return run_dacapo(env, args + ['Harness'] + config + [bm])
    
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

def _jdk7(env, build='product'):
    jdk7 = env.check_get_env('JDK7')
    if build == 'product':
        pass
    elif build in ['debug', 'fastdebug', 'optimized']:
        jdk7 = join(jdk7, build)
    else:
        env.abort('Unknown build type: ' + build)
    return jdk7
    
def make(env, args):
    """builds the GraalVM binary"""

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
    jdk7 = _jdk7(env, build)
    if build == 'debug':
        build = 'jvmg'
    
    fix_jvm_cfg(env, jdk7)

    if env.os != 'windows':
        javaLink = join(graal_home, 'hotspot', 'java')
        if not exists(javaLink):
            javaExe = join(jdk7, 'jre', 'bin', 'java')
            env.log('Creating link: ' + javaLink + ' -> ' + javaExe)
            os.symlink(javaExe, javaLink)

    graalVmDir = join(jdk7, 'jre', 'lib', 'amd64', 'graal')
    if not exists(graalVmDir):
        env.log('Creating Graal directory in JDK7: ' + graalVmDir)
        os.makedirs(graalVmDir)

    def filterXusage(line):
        if not 'Xusage.txt' in line:
            sys.stderr.write(line + os.linesep)
            
    os.environ.update(ARCH_DATA_MODEL='64', LANG='C', HOTSPOT_BUILD_JOBS='3', ALT_BOOTDIR=jdk7, INSTALL='y')
    env.run([env.gmake_cmd(), build + 'graal'], cwd=join(graal_home, 'make'), err=filterXusage)
    
def vm(env, args, vm='-graal', build='product'):
    """run the GraalVM"""
    if env.java_dbg:
        args = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000'] + args
    os.environ['MAXINE'] = env.check_get_env('GRAAL_HOME')
    exe = join(_jdk7(env, build), 'bin', env.exe_suffix('java'))
    return env.run([exe, vm] + args)

def vm_g(env, args):
    """run the debug GraalVM"""
    return vm(env, args, build='debug')

def vm_f(env, args):
    """run the fastdebug GraalVM"""
    return vm(env, args, build='fastdebug')

def vm_o(env, args):
    """run the optimized GraalVM"""
    return vm(env, args, build='optimized')

def mx_init(env):
    commands = {
        'dacapo': [dacapo, 'benchmark [VM options]'],
        'example': [example, '[-v] example names...'],
        'clean': [clean, ''],
        'make': [make, ''],
        'tests': [tests, ''],
        'vm_g': [vm_g, ''],
        'vm_f': [vm_f, ''],
        'vm_o': [vm_o, ''],
        'vm': [vm, ''],
    }
    env.commands.update(commands)

def run_dacapo(env, args):
    dacapo = env.check_get_env('DACAPO_CP')
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        env.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
    return vm(env, ['-Xms1g', '-Xmx2g', '-esa', '-cp', dacapo] + args)
