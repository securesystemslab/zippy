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
Unittests for graph.algorithms.accessibility
"""


import unittest
import pygraph
from pygraph.algorithms.searching import depth_first_search
from pygraph.algorithms.accessibility import accessibility
from pygraph.algorithms.accessibility import mutual_accessibility
from pygraph.algorithms.accessibility import connected_components
from pygraph.algorithms.accessibility import cut_nodes
from pygraph.algorithms.accessibility import cut_edges
from pygraph.classes.hypergraph import hypergraph
from copy import deepcopy
from sys import getrecursionlimit
import testlib

def number_of_connected_components(cc):
    n = 0
    for each in cc:
        if cc[each] > n:
            n = cc[each]
    return n

class test_accessibility(unittest.TestCase):

    def setUp(self):
        pass
    
    def test_accessibility_in_graph(self):
        gr = testlib.new_graph()
        gr.add_nodes(['a','b','c'])
        gr.add_edge(('a','b'))
        gr.add_edge(('a','c'))
        
        ac = accessibility(gr)
        
        for n in gr:
            for m in gr:
                if (m in ac[n]):
                    assert m in depth_first_search(gr, n)[0]
                    assert n in depth_first_search(gr, m)[0]
                else:
                    assert m not in depth_first_search(gr, n)[0]
    
    def test_accessibility_in_digraph(self):
        gr = testlib.new_digraph()
        gr.add_nodes(['a','b','c'])
        gr.add_edge(('a','b'))
        gr.add_edge(('a','c'))
        
        ac = accessibility(gr)
        
        for n in gr:
            for m in gr:
                if (m in ac[n]):
                    assert m in depth_first_search(gr, n)[0]
                else:
                    assert m not in depth_first_search(gr, n)[0]

    
    def test_accessibility_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,2001))
        for i in range(0,2000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        accessibility(gr)
        assert getrecursionlimit() == recursionlimit

    def test_mutual_accessibility_in_graph(self):
        gr = testlib.new_graph()
        gr.add_nodes(['a','b','c'])
        gr.add_edge(('a','b'))
        gr.add_edge(('a','c'))
        
        ma = mutual_accessibility(gr)
        for n in gr:
            for m in gr:
                if (m in ma[n]):
                    assert m in depth_first_search(gr, n)[0]
                    assert n in depth_first_search(gr, m)[0]
                else:
                    assert m not in depth_first_search(gr, n)[0] or n not in depth_first_search(gr, m)[0]
    
    def test_mutual_accessibility_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,5001))
        for i in range(0,5000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        mutual_accessibility(gr)
        assert getrecursionlimit() == recursionlimit
    
    def test_mutual_accessibility_in_digraph(self):
        gr = testlib.new_digraph()
        gr.add_nodes(['a','b','c'])
        gr.add_edge(('a','b'))
        gr.add_edge(('b','a'))
        gr.add_edge(('a','c'))
        
        ma = mutual_accessibility(gr)
        for n in gr:
            for m in gr:
                if (m in ma[n]):
                    assert m in depth_first_search(gr, n)[0]
                    assert n in depth_first_search(gr, m)[0]
                else:
                    assert m not in depth_first_search(gr, n)[0] or n not in depth_first_search(gr, m)[0]
                    
    def test_connected_components_in_graph(self):
        gr = testlib.new_graph()
        gr.add_nodes(['a','b','c'])
        gr.add_edge(('a','b'))
        
        cc = connected_components(gr)
        
        for n in gr:
            for m in gr:
                if (cc[n] == cc[m]):
                    assert m in depth_first_search(gr, n)[0]
                else:
                    assert m not in depth_first_search(gr, n)[0]

    def test_connected_components_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,5001))
        for i in range(0,5000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        connected_components(gr)
        assert getrecursionlimit() == recursionlimit
    
    def test_cut_nodes_in_graph(self):
        gr = testlib.new_graph()
        gr.add_nodes(['x','y'])
        gr.add_edge(('x','y'))
        gr.add_edge(('x',0))
        
        gr_copy = deepcopy(gr)
        
        cn = cut_nodes(gr)
        
        for each in cn:
            before = number_of_connected_components(connected_components(gr))
            gr.del_node(each)
            number_of_connected_components(connected_components(gr)) > before
            gr = gr_copy
    
    def test_cut_nodes_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,5001))
        for i in range(0,5000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        cut_nodes(gr)
        assert getrecursionlimit() == recursionlimit
    
    def test_cut_edges_in_graph(self):
        gr = testlib.new_graph()
        gr.add_nodes(['x','y'])
        gr.add_edge(('x','y'))
        gr.add_edge(('x',0))
        
        gr_copy = deepcopy(gr)
        
        ce = cut_edges(gr)
        
        for each in ce:
            before = number_of_connected_components(connected_components(gr))
            gr.del_edge(each)
            number_of_connected_components(connected_components(gr)) > before
            gr = gr_copy

    def test_cut_edges_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,5001))
        for i in range(0,5000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        cut_edges(gr)
        assert getrecursionlimit() == recursionlimit

    def test_accessibility_hypergraph(self):
        gr = hypergraph()
        
        # Add some nodes / edges
        gr.add_nodes(range(8))
        gr.add_hyperedges(['a', 'b', 'c'])
        
        # Connect the 9 nodes with three size-3 hyperedges
        for node_set in [['a',0,1,2], ['b',2,3,4], ['c',5,6,7]]:
            for node in node_set[1:]:
                gr.link(node, node_set[0])
        
        access = accessibility(gr)
        
        assert 8 == len(access)
        
        for i in range(5):
            assert set(access[i]) == set(range(5))
        
        for i in range(5,8):
            assert set(access[i]) == set(range(5,8))
        
    def test_connected_components_hypergraph(self):
        gr = hypergraph()
        
        # Add some nodes / edges
        gr.add_nodes(range(9))
        gr.add_hyperedges(['a', 'b', 'c'])
        
        # Connect the 9 nodes with three size-3 hyperedges
        for node_set in [['a',0,1,2], ['b',3,4,5], ['c',6,7,8]]:
            for node in node_set[1:]:
                gr.link(node, node_set[0])
        
        cc = connected_components(gr)
        
        assert 3 == len(set(cc.values()))
        
        assert cc[0] == cc[1] and cc[1] == cc[2]
        assert cc[3] == cc[4] and cc[4] == cc[5]
        assert cc[6] == cc[7] and cc[7] == cc[8]
        
        
        # Do it again with two components and more than one edge for each
        gr = hypergraph()
        gr.add_nodes(range(9))
        gr.add_hyperedges(['a', 'b', 'c', 'd'])
        
        for node_set in [['a',0,1,2], ['b',2,3,4], ['c',5,6,7], ['d',6,7,8]]:
            for node in node_set[1:]:
                gr.link(node, node_set[0])
        
        cc = connected_components(gr)
        
        assert 2 == len(set(cc.values()))
        
        for i in [0,1,2,3]:
            assert cc[i] == cc[i+1]
        
        for i in [5,6,7]:
            assert cc[i] == cc[i+1]
            
        assert cc[4] != cc[5]
    
    def test_cut_nodes_in_hypergraph(self):
        gr = hypergraph()
        
        # Add some nodes / edges
        gr.add_nodes(range(9))
        gr.add_hyperedges(['a', 'b', 'c'])
        
        # Connect the 9 nodes with three size-3 hyperedges
        for node_set in [['a',0,1,2], ['b',3,4,5], ['c',6,7,8]]:
            for node in node_set[1:]:
                gr.link(node, node_set[0])
        
        # Connect the groups
        gr.add_hyperedges(['l1','l2'])
        gr.link(0, 'l1')
        gr.link(3, 'l1')
        gr.link(5, 'l2')
        gr.link(8, 'l2')
        
        cn = cut_nodes(gr);
        
        assert 0 in cn
        assert 3 in cn
        assert 5 in cn
        assert 8 in cn
        assert len(cn) == 4
    
    def test_cut_edges_in_hypergraph(self):
        gr = hypergraph()
        
        # Add some nodes / edges
        gr.add_nodes(range(9))
        gr.add_hyperedges(['a1', 'b1', 'c1'])
        gr.add_hyperedges(['a2', 'b2', 'c2'])
        
        # Connect the 9 nodes with three size-3 hyperedges
        for node_set in [['a1',0,1,2], ['b1',3,4,5], ['c1',6,7,8], ['a2',0,1,2], ['b2',3,4,5], ['c2',6,7,8]]:
            for node in node_set[1:]:
                gr.link(node, node_set[0])
        
        # Connect the groups
        gr.add_hyperedges(['l1','l2'])
        gr.link(0, 'l1')
        gr.link(3, 'l1')
        gr.link(5, 'l2')
        gr.link(8, 'l2')
        
        ce = cut_edges(gr)
        
        assert 'l1' in ce
        assert 'l2' in ce
        assert len(ce) == 2
        
if __name__ == "__main__":
    unittest.main()
