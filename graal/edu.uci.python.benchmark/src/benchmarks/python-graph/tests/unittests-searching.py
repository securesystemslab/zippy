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
Unittests for graph.algorithms.searching
"""


# Imports
import unittest
import pygraph
import pygraph.classes
from pygraph.algorithms.searching import depth_first_search, breadth_first_search
from sys import getrecursionlimit
import testlib


class test_depth_first_search(unittest.TestCase):

    def test_dfs_in_empty_graph(self):
        gr = pygraph.classes.graph.graph()
        st, pre, post = depth_first_search(gr)
        assert st == {}
        assert pre == []
        assert post == []
    
    def test_dfs_in_graph(self):
        gr = testlib.new_graph()
        st, pre, post = depth_first_search(gr)
        for each in gr:
            if (st[each] != None):
                assert pre.index(each) > pre.index(st[each])
                assert post.index(each) < post.index(st[each])
        for node in st:
            assert gr.has_edge((st[node], node)) or st[node] == None

    def test_dfs_in_empty_digraph(self):
        gr = pygraph.classes.digraph.digraph()
        st, pre, post = depth_first_search(gr)
        assert st == {}
        assert pre == []
        assert post == []
    
    def test_dfs_in_digraph(self):
        gr = testlib.new_digraph()
        st, pre, post = depth_first_search(gr)
        for each in gr:
            if (st[each] != None):
                assert pre.index(each) > pre.index(st[each])
                assert post.index(each) < post.index(st[each])
        for node in st:
            assert gr.has_edge((st[node], node)) or st[node] == None
    
    def test_dfs_very_deep_graph(self):
        gr = pygraph.classes.graph.graph()
        gr.add_nodes(range(0,20001))
        for i in range(0,20000):
            gr.add_edge((i,i+1))
        recursionlimit = getrecursionlimit()
        depth_first_search(gr, 0)
        assert getrecursionlimit() == recursionlimit

class test_breadth_first_search(unittest.TestCase):

    def test_bfs_in_empty_graph(self):
        gr = pygraph.classes.graph.graph()
        st, lo = breadth_first_search(gr)
        assert st == {}
        assert lo == []
    
    def test_bfs_in_graph(self):
        gr = pygraph.classes.graph.graph()
        gr = testlib.new_digraph()
        st, lo = breadth_first_search(gr)
        for each in gr:
            if (st[each] != None):
                assert lo.index(each) > lo.index(st[each])
        for node in st:
            assert gr.has_edge((st[node], node)) or st[node] == None

    def test_bfs_in_empty_digraph(self):
        gr = pygraph.classes.digraph.digraph()
        st, lo = breadth_first_search(gr)
        assert st == {}
        assert lo == []
    
    def test_bfs_in_digraph(self):
        gr = testlib.new_digraph()
        st, lo = breadth_first_search(gr)
        for each in gr:
            if (st[each] != None):
                assert lo.index(each) > lo.index(st[each])
        for node in st:
            assert gr.has_edge((st[node], node)) or st[node] == None
            
if __name__ == "__main__":
    unittest.main()