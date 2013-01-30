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

class OutputParser:
    
    def __init__(self):
        self.matchers = []
        
    def addMatcher(self, matcher):
        self.matchers.append(matcher)
    
    def parse(self, output):
        records = []
        for matcher in self.matchers:
            record = matcher.parse(output)
            if record:
                records.append(record)
        return records

class Matcher:
    
    def __init__(self, regex, valuesTemplate):
        assert isinstance(valuesTemplate, dict)
        self.regex = regex
        self.valuesTemplate = valuesTemplate
        
    def parse(self, text):
        match = self.regex.search(text)
        if not match:
            return False
        values = {}
        for key, value in self.valuesTemplate.items():
            values[self.get_value_or_const(match, key)] = self.get_value_or_const(match, value)
                    
        return values
    
        
    def get_value_or_const(self, match, name):
        if name.startswith('const:'):
            return name.split(':')[1]
        else:
            return match.group(name)
