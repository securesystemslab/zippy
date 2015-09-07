# Copyright (c) 2007-2009 Pedro Matiello <pmatiello@gmail.com>
#                         Salim Fadhley <sal@stodge.org>
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:

# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.

import sys
sys.path.append('..')
sys.path.append('../core')
import core.pygraph
import unittest
import tests.testlib
import testlib
import logging
from os import listdir

log = logging.getLogger(__name__)

def test_modules():
    modlist = []
    for each in listdir('.'):
        if (each[0:9] == "unittests" and each[-3:] == ".py"):
            modlist.append(each[0:-3])
    return modlist

def run_tests():
    for each_size in testlib.sizes:
        print ("Testing with %s graphs" % each_size)
        
        suite = unittest.TestSuite()
        testlib.use_size = each_size
        
        for each_module in test_modules():
            try:
                suite.addTests(unittest.TestLoader().loadTestsFromName(each_module))
            except ImportError as ie:
                log.exception(ie)
                continue
        
        tr = unittest.TextTestRunner(verbosity=2)
        print(suite)
        result = tr.run(suite)
        del suite

def main():
    try:
        rseed = sys.argv[1]
        testlib.random_seed = int(rseed)
    except:
        pass
    print ("")
    print ("--------------------------------------------------")
    print ("python-graph unit-tests")
    print ("Random seed: %s" % testlib.random_seed)
    print ("--------------------------------------------------")
    print ("")
    run_tests()
   
if __name__ == "__main__":
    main()
    