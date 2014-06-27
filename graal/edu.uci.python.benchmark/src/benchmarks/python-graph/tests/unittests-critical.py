# Copyright (c) Pedro Matiello <pmatiello@gmail.com>
#               Tomaz Kovacic  <tomaz.kovacic@gmail.com>
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
Unittests for pygraph.algorithms.critical
"""

import unittest
from pygraph.algorithms.critical import critical_path
from pygraph.algorithms.critical import transitive_edges,_intersection
from pygraph.classes.digraph import digraph

def generate_test_graph():
    '''
    Generates & returns a weighted digraph with 
    one transitive edge and no cycles.
    '''
    G = digraph()
    G.add_nodes([1,2,3,4,5,6])
    G.add_edge((1,2), 1)
    G.add_edge((2,4), 4)
    G.add_edge((1,3), 1)#transitive edge
    G.add_edge((2,3), 20)
    G.add_edge((3,5), 3)
    G.add_edge((4,6), 5)
    G.add_edge((5,6), 4)
    return G

class test_critical_path_and_transitive_edges(unittest.TestCase):

    # critical path algorithm 
    
    def test_critical_path_with_cycle(self):
        G = generate_test_graph()
        G.add_edge((5,2),3)#add cycle
        assert critical_path(G) == []
        
    def test_critical_path(self):
        G = generate_test_graph()
        assert critical_path(G) == [1,2,3,5,6]
        
    # transitive edge detection algorithm     
    
    def test_transitivity_with_cycle(self):
        G = generate_test_graph()
        G.add_edge((5,2),3)#add cycle
        assert transitive_edges(G) == []
    
    def test_transitivity(self):
        G = generate_test_graph()
        G.add_edge((2,5),1)#add another transitive edge
        assert transitive_edges(G) == [(1,3),(2,5)]
        
    # intersection testing (used internally)
        
    def test_partial_intersection(self):
        list1 = [1,2,3,4]
        list2 = [3,4,5,6]
        assert _intersection(list1, list2) == [3,4]
        
    def test_empty_intersection(self):
        list1 = [1,2,3,4]
        list2 = [5,6]
        assert _intersection(list1, list2) == []


if __name__ == "__main__":
    unittest.main()