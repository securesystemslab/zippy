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
Unittests for graph.classes.Graph
"""


import unittest
import pygraph
from pygraph.algorithms.generators import generate
from pygraph.classes.exceptions import AdditionError
from pygraph.classes.graph import graph
import testlib
from copy import copy, deepcopy

class test_graph(unittest.TestCase):

    # Add/Remove nodes and edges
    
    def test_raise_exception_on_duplicate_node_addition(self):
        gr = graph()
        gr.add_node('a_node')
        try:
            gr.add_node('a_node')
        except AdditionError:
            pass
        else:
            fail()

    def test_raise_exception_on_duplicate_edge_addition(self):
        gr = graph()
        gr.add_node('a_node')
        gr.add_node('other_node')
        gr.add_edge(("a_node","other_node"))
        try:
            gr.add_edge(("a_node","other_node"))
        except AdditionError:
            pass
        else:
            fail()
    
    def test_raise_exception_when_edge_added_from_non_existing_node(self):
        gr = graph()
        gr.add_nodes([0,1])
        try:
            gr.add_edge((3,0))
        except KeyError:
            pass
        else:
            fail()
        assert gr.node_neighbors == {0: [], 1: []}
    
    def test_raise_exception_when_edge_added_to_non_existing_node(self):
        gr = graph()
        gr.add_nodes([0,1])
        try:
            gr.add_edge((0,3))
        except KeyError:
            pass
        else:
            fail()
        assert gr.node_neighbors == {0: [], 1: []}
    
    def test_remove_node(self):
        gr = testlib.new_graph()
        gr.del_node(0)
        self.assertTrue(0 not in gr)
        for each, other in gr.edges():
            self.assertTrue(each in gr)
            self.assertTrue(other in gr)
    
    def test_remove_edge_from_node_to_same_node(self):
        gr = graph()
        gr.add_node(0)
        gr.add_edge((0, 0))
        gr.del_edge((0, 0))
    
    def test_remove_node_with_edge_to_itself(self):
        gr = graph()
        gr.add_node(0)
        gr.add_edge((0, 0))
        gr.del_node(0)
    
    def test_edges_between_different_nodes_should_be_arrows_in_both_ways(self):
        gr = graph()
        gr.add_nodes([0,1])
        gr.add_edge((0,1), label="label", attrs=[('key','value')])
        assert (0,1) in gr.edges()
        assert (1,0) in gr.edges()
        assert len(gr.edges()) == 2
        assert gr.neighbors(0) == [1]
        assert gr.neighbors(1) == [0]
        assert (0,1) in gr.edge_properties.keys()
        assert (1,0) in gr.edge_properties.keys()
        assert (0,1) in gr.edge_attr.keys()
        assert (1,0) in gr.edge_attr.keys()
    
    def test_edges_between_different_nodes_should_be_a_single_arrow(self):
        gr = graph()
        gr.add_node(0)
        gr.add_edge((0,0), label="label", attrs=[('key','value')])
        assert (0,0) in gr.edges()
        assert len(gr.edges()) == 1
        assert gr.neighbors(0) == [0]
        assert (0,0) in gr.edge_properties.keys()
        assert (0,0) in gr.edge_attr.keys()
        assert len(gr.edge_attr[(0,0)]) == 1

    
    # Invert graph
    
    def test_invert_graph(self):
        gr = testlib.new_graph()
        inv = gr.inverse()
        for each in gr.edges():
            self.assertTrue(each not in inv.edges())
        for each in inv.edges():
            self.assertTrue(each not in gr.edges())
    
    def test_invert_empty_graph(self):
        gr = graph()
        inv = gr.inverse()
        self.assertTrue(gr.nodes() == [])
        self.assertTrue(gr.edges() == [])
    
    
    # Complete graph
    
    def test_complete_graph(self):
        gr = graph()
        gr.add_nodes(range(10))
        gr.complete()
        for i in range(10):
            for j in range(10):
                self.assertTrue((i, j) in gr.edges() or i == j)
    
    def test_complete_empty_graph(self):
        gr = graph()
        gr.complete()
        self.assertTrue(gr.nodes() == [])
        self.assertTrue(gr.edges() == [])
    
    def test_complete_graph_with_one_node(self):
        gr = graph()
        gr.add_node(0)
        gr.complete()
        self.assertTrue(gr.nodes() == [0])
        self.assertTrue(gr.edges() == [])
    
    
    # Add graph
    
    def test_add_graph(self):
        gr1 = testlib.new_graph()
        gr2 = testlib.new_graph()
        gr1.add_graph(gr2)
        for each in gr2.nodes():
            self.assertTrue(each in gr1)
        for each in gr2.edges():
            self.assertTrue(each in gr1.edges())
    
    def test_add_empty_graph(self):
        gr1 = testlib.new_graph()
        gr1c = copy(gr1)
        gr2 = graph()
        gr1.add_graph(gr2)
        self.assertTrue(gr1.nodes() == gr1c.nodes())
        self.assertTrue(gr1.edges() == gr1c.edges())
    
    
    # Add spanning tree
    
    def test_add_spanning_tree(self):
        gr = graph()
        st = {0: None, 1: 0, 2:0, 3: 1, 4: 2, 5: 3}
        gr.add_spanning_tree(st)
        for each in st:
            self.assertTrue((each, st[each]) in gr.edges() or (each, st[each]) == (0, None))
            self.assertTrue((st[each], each) in gr.edges() or (each, st[each]) == (0, None))

    def test_add_empty_spanning_tree(self):
        gr = graph()
        st = {}
        gr.add_spanning_tree(st)
        self.assertTrue(gr.nodes() == [])
        self.assertTrue(gr.edges() == [])
        
    def test_repr(self):
        """
        Validate the repr string
        """
        gr = testlib.new_graph()
        gr_repr = repr(gr)
        assert isinstance(gr_repr, str )
        assert gr.__class__.__name__ in gr_repr
    
    def test_order_len_equivlance(self):
        """
        Verify the behavior of G.order()
        """
        gr = testlib.new_graph()
        assert len(gr) == gr.order()
        assert gr.order() == len( gr.node_neighbors )
        
    def test_graph_equality_nodes(self):
        """
        Graph equality test. This one checks node equality. 
        """
        gr = graph()
        gr.add_nodes([0,1,2,3,4,5])
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.del_node(5)
        
        gr4 = deepcopy(gr)
        gr4.add_node(6)
        gr4.del_node(0)
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
        
    def test_graph_equality_edges(self):
        """
        Graph equality test. This one checks edge equality. 
        """
        gr = graph()
        gr.add_nodes([0,1,2,3,4])
        gr.add_edge((0,1), wt=1)
        gr.add_edge((0,2), wt=2)
        gr.add_edge((1,2), wt=3)
        gr.add_edge((3,4), wt=4)
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.del_edge((0,2))
        
        gr4 = deepcopy(gr)
        gr4.add_edge((2,4))
        
        gr5 = deepcopy(gr)
        gr5.del_edge((0,2))
        gr5.add_edge((2,4))
        
        gr6 = deepcopy(gr)
        gr6.del_edge((0,2))
        gr6.add_edge((0,2), wt=10)
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
        assert gr != gr5
        assert gr5 != gr
        assert gr != gr6
        assert gr6 != gr
    
    def test_graph_equality_labels(self):
        """
        Graph equality test. This one checks node equality. 
        """
        gr = graph()
        gr.add_nodes([0,1,2])
        gr.add_edge((0,1), label="l1")
        gr.add_edge((1,2), label="l2")
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.del_edge((0,1))
        gr3.add_edge((0,1))
        
        gr4 = deepcopy(gr)
        gr4.del_edge((0,1))
        gr4.add_edge((0,1), label="l3")
             
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
    
    def test_graph_equality_attributes(self):
        """
        Graph equality test. This one checks node equality. 
        """
        gr = graph()
        gr.add_nodes([0,1,2])
        gr.add_edge((0,1))
        gr.add_node_attribute(1, ('a','x'))
        gr.add_node_attribute(2, ('b','y'))
        gr.add_edge_attribute((0,1), ('c','z'))
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.del_edge((0,1))
        gr3.add_edge((0,1))
        
        gr4 = deepcopy(gr)
        gr4.del_edge((0,1))
        gr4.add_edge((0,1))
        gr4.add_edge_attribute((0,1), ('d','k'))
        
        gr5 = deepcopy(gr)
        gr5.del_node(2)
        gr5.add_node(2)
        gr5.add_node_attribute(0, ('d','k'))
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
        assert gr != gr5
        assert gr5 != gr
        
if __name__ == "__main__":
    unittest.main()