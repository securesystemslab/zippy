# Copyright (c) 2010 Pedro Matiello <pmatiello@gmail.com>
#               Juarez Bochi  <jbochi@gmail.com>
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


"""
Unittests for pygraph.algorithms.pagerank
"""

import unittest
from pygraph.classes.digraph import digraph
from pygraph.algorithms.pagerank import pagerank
import testlib

class test_pagerank(unittest.TestCase):

    # pagerank algorithm 
    
    def test_pagerank_empty(self):
        #Test if an empty dict is returned for an empty graph
        G = digraph()
        self.assertEqual(pagerank(G), {})
    
    def test_pagerank_cycle(self):
        #Test if all nodes in a cycle graph have the same value
        G = digraph()
        G.add_nodes([1, 2, 3, 4, 5])
        G.add_edge((1, 2))
        G.add_edge((2, 3))
        G.add_edge((3, 4))
        G.add_edge((4, 5))
        G.add_edge((5, 1))
        self.assertEqual(pagerank(G), {1: 0.2, 2: 0.2, 3: 0.2, 4: 0.2, 5: 0.2})

    def test_pagerank(self):
        #Test example from wikipedia: http://en.wikipedia.org/wiki/File:Linkstruct3.svg
        G = digraph()
        G.add_nodes([1, 2, 3, 4, 5, 6, 7])        
        G.add_edge((1, 2))
        G.add_edge((1, 3))
        G.add_edge((1, 4))
        G.add_edge((1, 5))
        G.add_edge((1, 7))
        G.add_edge((2, 1))
        G.add_edge((3, 1))
        G.add_edge((3, 2))
        G.add_edge((4, 2))
        G.add_edge((4, 3))
        G.add_edge((4, 5))
        G.add_edge((5, 1))
        G.add_edge((5, 3))
        G.add_edge((5, 4))
        G.add_edge((5, 6))
        G.add_edge((6, 1))
        G.add_edge((6, 5))
        G.add_edge((7, 5))
        expected_pagerank = {
            1: 0.280, 
            2: 0.159,
            3: 0.139,
            4: 0.108,
            5: 0.184,
            6: 0.061,
            7: 0.069,
        }
        pr = pagerank(G)
        for k in pr:
            self.assertAlmostEqual(pr[k], expected_pagerank[k], places=3)
    
    def test_pagerank_random(self):
        G = testlib.new_digraph()
        md = 0.00001
        df = 0.85
        pr = pagerank(G, damping_factor=df, min_delta=md)
        min_value = (1.0-df)/len(G)
        for node in G:
            expected = min_value
            for each in G.incidents(node):
                expected += (df * pr[each] / len(G.neighbors(each)))
            assert abs(pr[node] - expected) < md
        
if __name__ == "__main__":
    unittest.main()
