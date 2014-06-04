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
Unittests for graph.algorithms.sorting
"""


import unittest
import pygraph.classes
from pygraph.algorithms.sorting import topological_sorting
from pygraph.algorithms.searching import depth_first_search
from sys import getrecursionlimit
import testlib


class test_topological_sorting(unittest.TestCase):

    def test_topological_sorting_on_tree(self):
        gr = testlib.new_graph()
        st, pre, post = depth_first_search(gr)
        tree = pygraph.classes.digraph.digraph()

        
        for each in st:
            if st[each]:
                if (each not in tree.nodes()):
                    tree.add_node(each)
                if (st[each] not in tree.nodes()):
                    tree.add_node(st[each])
                tree.add_edge((st[each], each))
        
        ts = topological_sorting(tree)
        for each in ts:
            if (st[each]):
                assert ts.index(each) > ts.index(st[each])
    
    def test_topological_sorting_on_digraph(self):
        
        def is_ordered(node, list):
            # Has parent on list
            for each in list:
                if gr.has_edge((each, node)):
                    return True
            # Has no possible ancestors on list
            st, pre, post = depth_first_search(gr, node)
            for each in list:
                if (each in st):
                    return False
            return True
            
        gr = testlib.new_digraph()
        ts = topological_sorting(gr)
        
        while (ts):
            x = ts.pop()
            assert is_ordered(x, ts)

    def test_topological_sort_on_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,20001))
        for i in range(0,20000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        topological_sorting(gr)
        assert getrecursionlimit() == recursionlimit
            
if __name__ == "__main__":
    unittest.main()