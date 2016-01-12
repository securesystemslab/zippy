# Copyright (c) Pedro Matiello <pmatiello@gmail.com>
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
Unittests for graph.algorithms.heuristics
"""


import unittest
import pygraph
from pygraph.classes.graph import graph
from pygraph.classes.digraph import digraph
from pygraph.algorithms.heuristics.euclidean import euclidean
from pygraph.algorithms.heuristics.chow import chow
from pygraph.classes import exceptions

from test_data import nations_of_the_world


class test_chow(unittest.TestCase):

    def setUp(self):
        self.G = graph()
        nations_of_the_world(self.G)

    def test_basic(self):
        """
        Test some very basic functionality
        """
        englands_neighbors = self.G.neighbors("England")
        assert set(['Wales', 'Scotland', 'France', 'Ireland']) == set( englands_neighbors )

    def test_chow(self):
        heuristic = chow( "Wales", "North Korea", "Russia" )
        heuristic.optimize(self.G)
        result = pygraph.algorithms.minmax.heuristic_search( self.G, "England", "India", heuristic )
        
    def test_chow_unreachable(self):
        heuristic = chow( "Wales", "North Korea", "Russia" )
        self.G.add_node("Sealand")
        self.G.add_edge(("England", "Sealand"))
        heuristic.optimize(self.G)
        self.G.del_edge(("England", "Sealand"))
        
        try:
            result = pygraph.algorithms.minmax.heuristic_search( self.G, "England", "Sealand" , heuristic )
        except exceptions.NodeUnreachable as _:
            return
        
        assert False, "This test should raise an unreachable error."


class test_euclidean(unittest.TestCase):

    def setUp(self):
        self.G = pygraph.classes.graph.graph()
        self.G.add_node('A', [('position',[0,0])])
        self.G.add_node('B', [('position',[2,0])])
        self.G.add_node('C', [('position',[2,3])])
        self.G.add_node('D', [('position',[1,2])])
        self.G.add_edge(('A', 'B'), wt=4)
        self.G.add_edge(('A', 'D'), wt=5)
        self.G.add_edge(('B', 'C'), wt=9)
        self.G.add_edge(('D', 'C'), wt=2)            

    def test_euclidean(self):
        heuristic = euclidean()
        heuristic.optimize(self.G)
        result = pygraph.algorithms.minmax.heuristic_search(self.G, 'A', 'C', heuristic )
        assert result == ['A', 'D', 'C']
        
if __name__ == "__main__":
    unittest.main()