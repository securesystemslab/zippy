# Copyright (c) Pedro Matiello <pmatiello@gmail.com>
#               Johannes Reinhardt <jreinhardt@ist-dein-freund.de>
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
Unittests for graph.algorithms.searching
"""

import unittest
import testlib

from pygraph.classes.graph import graph
from pygraph.classes.digraph import digraph

from pygraph.algorithms.searching import depth_first_search
from pygraph.algorithms.minmax import minimal_spanning_tree,\
shortest_path, heuristic_search, shortest_path_bellman_ford, maximum_flow, cut_tree
from pygraph.algorithms.heuristics.chow import chow
from pygraph.classes.exceptions import NegativeWeightCycleError

from copy import deepcopy

# helpers

def tree_weight(gr, tree):
    sum = 0;
    for each in tree:
        sum = sum + gr.edge_weight((each, tree[each]))
    return sum

def add_spanning_tree(gr, st):
    # A very tolerant implementation.
    gr.add_nodes(list(st.keys()))
    for each in st:
        if ((st[each] is not None) and (not gr.has_edge((st[each], each)))): # Accepts invalid STs
            gr.add_edge((st[each], each))

def bf_path(gr, root, target, remainder):
    if (remainder <= 0): return True
    if (root == target): return False
    for each in gr[root]:
        if (not bf_path(gr, each, target, remainder - gr.edge_weight((root, each)))):
            return False
    return True

def generate_fixture_digraph():
    #helper for bellman-ford algorithm
    G = digraph()
    G.add_nodes([1,2,3,4,5])
    G.add_edge((1,2), 6)
    G.add_edge((1,4), 7)
    G.add_edge((2,4), 8)
    G.add_edge((3,2), -2)
    G.add_edge((4,3), -3)
    G.add_edge((2,5), -4)
    G.add_edge((4,5), 9)
    G.add_edge((5,1), 2)
    G.add_edge((5,3), 7)
    return G

def generate_fixture_digraph_neg_weight_cycle():
    #graph with a neg. weight cycle
    G = generate_fixture_digraph()
    G.del_edge((2,4))
    G.add_edge((2,4), 2)#changed
    
    G.add_nodes([100,200]) #unconnected part
    G.add_edge((100,200),2)
    return G
    
def generate_fixture_digraph_unconnected():
    G = generate_fixture_digraph()
    G.add_nodes([100,200])
    G.add_edge((100,200),2)
    return G
    
# minimal spanning tree tests

class test_minimal_spanning_tree(unittest.TestCase):

    def test_minimal_spanning_tree_on_graph(self):
        gr = testlib.new_graph(wt_range=(1,10))
        mst = minimal_spanning_tree(gr, root=0)
        wt = tree_weight(gr, mst)
        len_dfs = len(depth_first_search(gr, root=0)[0])
        for each in mst:
            if (mst[each] != None):
                mst_copy = deepcopy(mst)
                del(mst_copy[each])
                for other in gr[each]:
                     mst_copy[each] = other
                     if (tree_weight(gr, mst_copy) < wt):
                         gr2 = graph()
                         add_spanning_tree(gr2, mst_copy)
                         assert len(depth_first_search(gr2, root=0)[0]) < len_dfs
    

# shortest path tests

class test_shortest_path(unittest.TestCase):
    
    def test_shortest_path_on_graph(self):
        gr = testlib.new_graph(wt_range=(1,10))
        st, dist = shortest_path(gr, 0)
        for each in gr:
            if (each in dist):
                assert bf_path(gr, 0, each, dist[each])
    
    def test_shortest_path_on_digraph(self):
        # Test stub: not checking for correctness yet
        gr = testlib.new_digraph(wt_range=(1,10))
        st, dist = shortest_path(gr, 0)
        for each in gr:
            if (each in dist):
                assert bf_path(gr, 0, each, dist[each])
    
    def test_shortest_path_should_fail_if_source_does_not_exist(self):    
        gr = testlib.new_graph()
        try:
            shortest_path(gr, 'invalid')
            assert False
        except (KeyError):
            pass
                
class test_shortest_path_bellman_ford(unittest.TestCase):
    
    def test_shortest_path_BF_on_empty_digraph(self):
        pre, dist  = shortest_path_bellman_ford(digraph(), 1)
        assert pre == {1:None} and dist == {1:0}
    
    def test_shortest_path_BF_on_digraph(self):
        #testing correctness on the fixture 
        gr = generate_fixture_digraph()
        pre,dist = shortest_path_bellman_ford(gr, 1)
        assert pre == {1: None, 2: 3, 3: 4, 4: 1, 5: 2} \
               and dist == {1: 0, 2: 2, 3: 4, 4: 7, 5: -2}
               
    def test_shortest_path_BF_on_digraph_with_negwcycle(self):
        #test negative weight cycle detection
        gr = generate_fixture_digraph_neg_weight_cycle()
        self.assertRaises(NegativeWeightCycleError,
                 shortest_path_bellman_ford, gr, 1)
        
    def test_shortest_path_BF_on_unconnected_graph(self):
        gr = generate_fixture_digraph_unconnected()
        pre,dist = shortest_path_bellman_ford(gr, 100)
        assert pre  == {200: 100, 100: None} and \
               dist == {200: 2, 100: 0}

class test_maxflow_mincut(unittest.TestCase):
    
    def test_trivial_maxflow(self):
         gr = digraph()
         gr.add_nodes([0,1,2,3])
         gr.add_edge((0,1), wt=5)
         gr.add_edge((1,2), wt=3)
         gr.add_edge((2,3), wt=7)
         flows, cuts = maximum_flow(gr, 0, 3)
         assert flows[(0,1)] == 3
         assert flows[(1,2)] == 3
         assert flows[(2,3)] == 3
    
    def test_random_maxflow(self):
         gr = testlib.new_digraph(wt_range=(1,20))
         flows, cuts = maximum_flow(gr, 0, 1)
         # Sanity test
         for each in flows:
             assert gr.edge_weight(each) >= flows[each]

# Tests for heuristic search are not necessary here as it's tested 
# in unittests-heuristics.py                                     

class test_cut_tree(unittest.TestCase):
    
    def test_cut_tree(self):
        #set up the graph (see example on wikipedia page for Gomory-Hu tree)
        gr = graph()
        gr.add_nodes([0,1,2,3,4,5])
        gr.add_edge((0,1), wt=1)
        gr.add_edge((0,2), wt=7)
        gr.add_edge((1,3), wt=3)
        gr.add_edge((1,2), wt=1)
        gr.add_edge((1,4), wt=2)
        gr.add_edge((2,4), wt=4)
        gr.add_edge((3,4), wt=1)
        gr.add_edge((3,5), wt=6)
        gr.add_edge((4,5), wt=2)

        ct = cut_tree(gr)

        #check ct
        assert ct[(2,0)] == 8
        assert ct[(4,2)] == 6
        assert ct[(1,4)] == 7
        assert ct[(3,1)] == 6
        assert ct[(5,3)] == 8
    
    def test_cut_tree_with_empty_graph(self):
        gr = graph()
        ct = cut_tree(gr)
        assert ct == {}
    
    def test_cut_tree_with_random_graph(self):
        gr = testlib.new_graph()
        ct = cut_tree(gr)


if __name__ == "__main__":
    unittest.main()
