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
Unittests for graph.algorithms.cycles
"""


import unittest
import pygraph
from pygraph.algorithms.cycles import find_cycle
from pygraph.algorithms.searching import depth_first_search
from pygraph.classes.digraph import digraph
from pygraph.classes.graph import graph
from sys import getrecursionlimit
import testlib


def verify_cycle(graph, cycle):
    for i in range(len(cycle)):
        assert graph.has_edge((cycle[i],cycle[(i+1)%len(cycle)]))

class test_find_cycle(unittest.TestCase):

    # Graph
    
    def test_find_cycle_on_graph(self):
        gr = testlib.new_graph()
        cycle = find_cycle(gr)
        verify_cycle(gr, cycle)

    def test_find_cycle_on_graph_withot_cycles(self):
        gr = testlib.new_graph()
        st, pre, post = depth_first_search(gr)
        gr = graph()
        gr.add_spanning_tree(st)
        assert find_cycle(gr) == []

    # Digraph
    
    def test_find_cycle_on_digraph(self):
        gr = testlib.new_digraph()
        cycle = find_cycle(gr)
        verify_cycle(gr, cycle)
    
    def test_find_cycle_on_digraph_without_cycles(self):
        gr = testlib.new_digraph()
        st, pre, post = depth_first_search(gr)
        gr = digraph()
        gr.add_spanning_tree(st)
        assert find_cycle(gr) == []
    
    def test_find_small_cycle_on_digraph(self):
        gr = digraph()
        gr.add_nodes([1, 2, 3, 4, 5])
        gr.add_edge((1, 2))
        gr.add_edge((2, 3))
        gr.add_edge((2, 4))
        gr.add_edge((4, 5))
        gr.add_edge((2, 1))
        # Cycle: 1-2
        assert find_cycle(gr) == [1,2]
    
    def test_find_cycle_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,20001))
        for i in range(0,20000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        find_cycle(gr)
        assert getrecursionlimit() == recursionlimit

    # Regression
    
    def test_regression1(self):
        G = digraph()
        G.add_nodes([1, 2, 3, 4, 5])
        G.add_edge((1, 2))
        G.add_edge((2, 3))
        G.add_edge((2, 4))
        G.add_edge((4, 5))
        G.add_edge((3, 5))
        G.add_edge((3, 1))
        assert find_cycle(G) == [1, 2, 3]
        
if __name__ == "__main__":
    unittest.main()