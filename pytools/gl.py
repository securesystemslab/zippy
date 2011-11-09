#!/usr/bin/python
#
# gl.py - shell interface for Graal source code
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
#
# A launcher for Graal executables and tools.
#

import subprocess
from threading import Thread
from argparse import ArgumentParser, REMAINDER
from os.path import join, dirname, abspath, exists, isfile, isdir
import commands
import types
import sys
import os

class Env(ArgumentParser):

    # Override parent to append the list of available commands
    def format_help(self):
        msg = ArgumentParser.format_help(self) + '\navailable commands:\n\n'
        for cmd in sorted(commands.table.iterkeys()):
            c, _ = commands.table[cmd][:2]
            doc = c.__doc__
            if doc is None:
                doc = ''
            msg += ' {0:<20} {1}\n'.format(cmd, doc.split('\n', 1)[0])
        return msg + '\n'
    
    def __init__(self):
        self.dacapo = os.getenv('DACAPO')
        self.jdk7 = os.getenv('JDK7')
        self.jdk7g = os.getenv('JDK7G')
        self.maxine = os.getenv('MAXINE')
        
        ArgumentParser.__init__(self, prog='gl')
    
        self.add_argument('-v', action='store_true', dest='verbose', help='enable verbose output')
        self.add_argument('--dacapo', help='path to DaCapo 91.12 jar file')
        self.add_argument('--jdk7', help='JDK7 installation in which the GraalVM binary is installed', metavar='<path>')
        self.add_argument('--jdk7g', help='JDK7G installation in which the GraalVM binary is installed', metavar='<path>')
        self.add_argument('-M', '--maxine', dest='maxine', help='path to Maxine code base', metavar='<path>')
        
    def parse_cmd_line(self, configFile):
        
        self.add_argument('commandAndArgs', nargs=REMAINDER, metavar='command args...')
        
        self.parse_args(namespace=self)

        if self.jdk7 is None or not isdir(self.jdk7):
            self.log('JDK7 is required. Use --jdk7 option or set JDK7 environment variable (in ' + configFile + ')')
            self.abort(1)

        if self.jdk7g is None or not isdir(self.jdk7g):
            self.log('JDK7G is required. Use --jdk7g option or set JDK7G environment variable (in ' + configFile + ')')
            self.abort(1)

        self.graal_home = dirname(abspath(dirname(sys.argv[0])))
    
    def load_config_file(self, configFile):
        """ adds attributes to this object from a file containing key=value lines """
        if exists(configFile):
            with open(configFile) as f:
                #self.log('[loading vars from ' + configFile + ']')
                for line in f:
                    if not line.startswith('#'):
                        kv = line.split('=', 1)
                        if len(kv) == 2:
                            k = kv[0].strip().lower()
                            setattr(self, k, os.path.expandvars(kv[1].strip()))

    def get_os(self):
        if sys.platform.startswith('darwin'):
            return 'darwin'
        elif sys.platform.startswith('linux'):
            return 'linux'
        elif sys.platform.startswith('sunos'):
            return 'solaris'
        elif sys.platform.startswith('win32') or sys.platform.startswith('cygwin'):
            return 'windows'
        else:
            print 'Supported operating system could not be derived from', sys.platform
            self.abort(1)

        
    def exe(self, name):
        if self.get_os() == 'windows':
            return name + '.exe'
        return name

    def run_dacapo(self, args):
        if not isfile(self.dacapo) or not self.dacapo.endswith('.jar'):
            self.log('Specified DaCapo jar file does not exist or is not a jar file: ' + self.dacapo)
            self.abort(1)
        return self.run_vm(['-Xms1g', '-Xmx2g', '-esa', '-XX:-GraalBailoutIsFatal', '-G:-QuietBailout', '-cp', self.dacapo] + args)

    def run_vm(self, args):
        if self.maxine is None:
            configFile = join(dirname(sys.argv[0]), 'glrc')
            self.log('Path to Maxine code base must be specified with -M option or MAXINE environment variable (in ' + configFile + ')')
            self.abort(1)
        if not exists(join(self.maxine, 'com.oracle.max.graal.hotspot', 'bin', 'com', 'oracle', 'max', 'graal', 'hotspot', 'VMEntriesNative.class')):
            self.log('Maxine code base path specified -M option or MAXINE environment variable does not contain com.oracle.max.graal.hotspot/bin/com/oracle/max/graal/hotspot/VMEntriesNative.class: ' + self.maxine)
            self.abort(1)
            
        os.environ['MAXINE'] = self.maxine
        exe = join(self.jdk7, 'bin', self.exe('java'))
        return self.run([exe, '-graal'] + args)

    def run(self, args, nonZeroIsFatal=True, out=None, err=None, cwd=None):
        """
        
        Run a command in a subprocess, wait for it to complete and return the exit status of the process.
        If the exit status is non-zero and `nonZeroIsFatal` is true, then the program is exited with
        the same exit status.
        Each line of the standard output and error streams of the subprocess are redirected to the
        provided out and err functions if they are not None.
        
        """
        
        assert isinstance(args, types.ListType), "'args' must be a list: " + str(args)
        for arg in args:
            if not isinstance(arg, types.StringTypes):
                self.log('argument is not a string: ' + str(arg))
                self.abort(1)
        
        if self.verbose:
            self.log(' '.join(args))
            
        try:
            if out is None and err is None:
                retcode = subprocess.call(args, cwd=cwd)
            else:
                def redirect(stream, f):
                    for line in iter(stream.readline, ''):
                        f(line)
                    stream.close()
                p = subprocess.Popen(args, stdout=None if out is None else subprocess.PIPE, stderr=None if err is None else subprocess.PIPE)
                if out is not None:
                    t = Thread(target=redirect, args=(p.stdout, out))
                    t.daemon = True # thread dies with the program
                    t.start()
                if err is not None:
                    t = Thread(target=redirect, args=(p.stderr, err))
                    t.daemon = True # thread dies with the program
                    t.start()
                retcode = p.wait()
        except OSError as e:
            self.log('Error executing \'' + ' '.join(args) + '\': ' + str(e))
            if self.verbose:
                raise e
            self.abort(e.errno)
        

        if retcode and nonZeroIsFatal:
            if self.verbose:
                raise subprocess.CalledProcessError(retcode, ' '.join(args))
            self.abort(retcode)
            
        return retcode

    
    def log(self, msg=None):
        if msg is None:
            print
        else:
            print msg
           
    def abort(self, code):
        """ raises a SystemExit exception with the provided exit code """
        raise SystemExit(code)

def main(env):
    configFile = join(dirname(sys.argv[0]), 'glrc')
    env.load_config_file(configFile)
    env.parse_cmd_line(configFile)
    
    if len(env.commandAndArgs) == 0:
        env.print_help()
        return
    
    env.command = env.commandAndArgs[0]
    env.command_args = env.commandAndArgs[1:]
    
    if not commands.table.has_key(env.command):
        env.error('unknown command "' + env.command + '"')
        
    c, _ = commands.table[env.command][:2]
    try:
        retcode = c(env, env.command_args)
        if retcode is not None and retcode != 0:
            env.abort(retcode)
    except KeyboardInterrupt:
        env.abort(1)
        
#This idiom means the below code only runs when executed from command line
if __name__ == '__main__':
    main(Env())
