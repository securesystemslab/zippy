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

import mx
import commands
import subprocess

class OutputParser:
    
    def __init__(self, nonZeroIsFatal=True):
        self.matchers = []
        self.nonZeroIsFatal = nonZeroIsFatal
        
    def addMatcher(self, matcher):
        self.matchers.append(matcher)
    
    def parse(self, vm, cmd, cwd=None, vmbuild=None):
        ret = [{}]
        
        def parseLine(line):
            anyMatch = False
            for matcher in self.matchers:
                parsed = matcher.parse(line.strip())
                if parsed:
                    anyMatch = True
                    if matcher.startNewLine and len(ret[0]) > 0:
                        ret.append({})
                    ret[len(ret)-1].update(parsed)
            if anyMatch :
                mx.log('>' + line.rstrip())
            else :
                mx.log( line.rstrip())
        
        retcode = commands.vm(cmd, vm, nonZeroIsFatal=self.nonZeroIsFatal, out=parseLine, err=subprocess.STDOUT, cwd=cwd, vmbuild=vmbuild)
        return {'parsed' : ret, 'retcode' : retcode}

class Matcher:
    
    def __init__(self, regex, valuesToParse, startNewLine=False):
        assert isinstance(valuesToParse, dict)
        self.regex = regex
        self.valuesToParse = valuesToParse
        self.startNewLine = startNewLine
        
    def parse(self, line):
        match = self.regex.search(line)
        if not match:
            return False
        ret = {}
        for key, value in self.valuesToParse.items():
            ret[self.parsestr(match, key)] = self.parsestr(match, value)
        return ret
        
    def parsestr(self, match, key):
        if isinstance(key, tuple):
            if len(key) != 2:
                raise Exception('Tuple arguments must have a length of 2')
            tup1, tup2 = key
            # check if key is a function
            if hasattr(tup1, '__call__'):
                return tup1(self.parsestr(match, tup2))
            elif hasattr(tup2, '__call__'):
                return tup2(self.parsestr(match, tup1))
            else:
                raise Exception('Tuple must contain a function pointer')
        elif key.startswith('const:'):
            return key.split(':')[1]
        else:
            return match.group(key)
