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
Unittests for graph.classes.hypergraph
"""


import unittest
import pygraph
from pygraph.algorithms.generators import generate
from pygraph.classes.exceptions import AdditionError
from pygraph.classes.hypergraph import hypergraph
import testlib
from copy import copy, deepcopy

class test_hypergraph(unittest.TestCase):

    # Add/Remove nodes and edges
    
    def test_raise_exception_on_duplicate_node_addition(self):
        gr = hypergraph()
        gr.add_node('a_node')
        try:
            gr.add_node('a_node')
        except AdditionError:
            pass
        else:
            fail()

    def test_raise_exception_on_duplicate_edge_link(self):
        gr = hypergraph()
        gr.add_node('a node')
        gr.add_hyperedge('an edge')
        gr.link('a node', 'an edge')
        try:
            gr.link('a node', 'an edge')
        except AdditionError:
            pass
        else:
            fail()
    
    def test_raise_exception_on_non_existing_link_removal(self):
        gr = hypergraph()
        gr.add_node(0)
        gr.add_hyperedge(1)
        try:
            gr.unlink(0, 1)
        except ValueError:
            pass
        else:
            fail()
    
    def test_raise_exception_when_edge_added_from_non_existing_node(self):
        gr = hypergraph()
        gr.add_nodes([0,1])
        try:
            gr.link(3,0)
        except KeyError:
            pass
        else:
            fail()
        assert gr.neighbors(0) == []
    
    def test_raise_exception_when_edge_added_to_non_existing_node(self):
        gr = hypergraph()
        gr.add_nodes([0,1])
        try:
            gr.link(0,3)
        except KeyError:
            pass
        else:
            fail()
        assert gr.neighbors(0) == []
    
    def test_remove_node(self):
        gr = testlib.new_hypergraph()
        gr.del_node(0)
        self.assertTrue(0 not in gr.nodes())
        for e in gr.hyperedges():
            for n in gr.links(e):
                self.assertTrue(n in gr.nodes())
    
    def test_remove_edge(self):
        h = hypergraph()
        h.add_nodes([1,2])
        h.add_edges(['a', 'b'])
        
        h.link(1,'a')
        h.link(2,'a')
        h.link(1,'b')
        h.link(2,'b')
        
        # Delete an edge
        h.del_edge('a')
        
        assert 1 == len(h.hyperedges())
        
        gr = testlib.new_hypergraph()
        edge_no = len(gr.nodes())+1
        gr.del_hyperedge(edge_no)
        self.assertTrue(edge_no not in gr.hyperedges())
    
    def test_remove_link_from_node_to_same_node(self):
        gr = hypergraph()
        gr.add_node(0)
        gr.add_hyperedge(0)
        gr.link(0, 0)
        gr.unlink(0, 0)
    
    def test_remove_node_with_edge_to_itself(self):
        gr = hypergraph()
        gr.add_node(0)
        gr.add_hyperedge(0)
        gr.link(0, 0)
        gr.del_node(0)

    def test_check_add_node_s(self):
        gr = hypergraph()
        nodes = [1,2,3]
        gr.add_nodes(nodes)
        gr.add_node(0)
        
        for n in [0] + nodes:
            assert n in gr
            assert gr.has_node(n)

    def test_rank(self):
        # Uniform case
        gr = testlib.new_uniform_hypergraph(3)
        assert 3 == gr.rank()
        
        # Non-uniform case
        gr = testlib.new_hypergraph()
        num = max([len(gr.links(e)) for e in gr.hyperedges()])
        assert num == gr.rank()
    
    def test_repr(self):
        """
        Validate the repr string
        """
        gr = testlib.new_hypergraph()
        gr_repr = repr(gr)
        assert isinstance(gr_repr, str )
        assert gr.__class__.__name__ in gr_repr
    
    def test_order_len_equivlance(self):
        """
        Verify the behavior of G.order()
        """
        gr = testlib.new_hypergraph()
        assert len(gr) == gr.order()
        assert gr.order() == len( gr.node_links )
    
    def test_hypergraph_equality_nodes(self):
        """
        Hyperaph equality test. This one checks node equality. 
        """
        gr = hypergraph()
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

    def test_hypergraph_equality_edges(self):
        """
        Hyperaph equality test. This one checks edge equality. 
        """
        gr = hypergraph()
        gr.add_nodes([0,1,2,3])
        gr.add_edge('e1')
        gr.add_edge('e2')
        gr.link(0, 'e1')
        gr.link(1, 'e1')
        gr.link(1, 'e2')
        gr.link(2, 'e2')
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.del_edge('e2')
        
        gr4 = deepcopy(gr)
        gr4.unlink(1, 'e2')
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
    
    def test_hypergraph_equality_labels(self):
        """
        Hyperaph equality test. This one checks edge equality. 
        """
        gr = hypergraph()
        gr.add_nodes([0,1,2,3])
        gr.add_edge('e1')
        gr.add_edge('e2')
        gr.add_edge('e3')
        gr.set_edge_label('e1', 'l1')
        gr.set_edge_label('e2', 'l2')
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.set_edge_label('e3', 'l3')
        
        gr4 = deepcopy(gr)
        gr4.set_edge_label('e1', 'lx')
        
        gr5 = deepcopy(gr)
        gr5.del_edge('e1')
        gr5.add_edge('e1')
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
        assert gr != gr5
        assert gr5 != gr
    
    def test_hypergraph_equality_attributes(self):
        """
        Hyperaph equality test. This one checks edge equality. 
        """
        gr = hypergraph()
        gr.add_nodes([0,1])
        gr.add_edge('e1')
        gr.add_edge('e2')
        gr.add_node_attribute(0, ('a',0))
        gr.add_edge_attribute('e1', ('b',1))
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.add_node_attribute(0, ('x','y'))
        
        gr4 = deepcopy(gr)
        gr4.add_edge_attribute('e1', ('u','v'))
        
        gr5 = deepcopy(gr)
        gr5.del_edge('e1')
        gr5.add_edge('e1')
        
        gr6 = deepcopy(gr)
        gr6.del_node(0)
        gr6.add_node(0)
        
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
        
    def test_hypergraph_equality_weight(self):
        """
        Hyperaph equality test. This one checks edge equality. 
        """
        gr = hypergraph()
        gr.add_nodes([0,1,2,3])
        gr.add_edge('e1')
        gr.add_edge('e2')
        gr.add_edge('e3')
        gr.set_edge_weight('e1', 2)
        
        gr2 = deepcopy(gr)
        
        gr3 = deepcopy(gr)
        gr3.set_edge_weight('e3', 2)
        
        gr4 = deepcopy(gr)
        gr4.set_edge_weight('e1', 1)
        
        assert gr == gr2
        assert gr2 == gr
        assert gr != gr3
        assert gr3 != gr
        assert gr != gr4
        assert gr4 != gr
        
    def test_hypergraph_link_unlink_link(self):
        """
        Hypergraph link-unlink-link test. It makes sure that unlink cleans 
        everything properly. No AdditionError should occur.
        """
        h = hypergraph()
        h.add_nodes([1,2])
        h.add_edges(['e1'])
        
        h.link(1, 'e1')
        h.unlink(1, 'e1')
        h.link(1,'e1')

            
if __name__ == "__main__":
    unittest.main()
