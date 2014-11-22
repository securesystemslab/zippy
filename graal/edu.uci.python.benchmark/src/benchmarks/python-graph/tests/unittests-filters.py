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
python-graph

Unit tests for python-graph
"""


# Imports
import unittest
import pygraph
from pygraph.algorithms.searching import depth_first_search, breadth_first_search
from pygraph.classes.graph import graph

from pygraph.algorithms.filters.radius import radius
from pygraph.algorithms.filters.find import find
import testlib


class test_find_filter(unittest.TestCase):

    def test_bfs_in_empty_graph(self):
        gr = graph()
        st, lo = breadth_first_search(gr, filter=find(5))
        assert st == {}
        assert lo == []
    
    def test_bfs_in_graph(self):
        gr = testlib.new_graph()
        gr.add_node('find-me')
        gr.add_edge((0, 'find-me'))
        st, lo = breadth_first_search(gr, root=0, filter=find('find-me'))
        assert st['find-me'] == 0
        for each in st:
            assert st[each] == None or st[each] == 0 or st[st[each]] == 0
    
    def test_bfs_in_digraph(self):
        gr = testlib.new_digraph()
        gr.add_node('find-me')
        gr.add_edge((0, 'find-me'))
        st, lo = breadth_first_search(gr, root=0, filter=find('find-me'))
        assert st['find-me'] == 0
        for each in st:
            assert st[each] == None or st[each] == 0 or st[st[each]] == 0
    
    def test_dfs_in_empty_graph(self):
        gr = graph()
        st, pre, post = depth_first_search(gr)
        assert st == {}
        assert pre == []
        assert post == []
    
    def test_dfs_in_graph(self):
        gr = testlib.new_graph()
        gr.add_node('find-me')
        gr.add_node('dont-find-me')
        gr.add_edge((0, 'find-me'))
        gr.add_edge(('find-me','dont-find-me'))
        st, pre, post = depth_first_search(gr, root=0, filter=find('find-me'))
        assert st['find-me'] == 0
        assert 'dont-find-me' not in st
    
    def test_dfs_in_digraph(self):
        gr = testlib.new_digraph()
        gr.add_node('find-me')
        gr.add_node('dont-find-me')
        gr.add_edge((0, 'find-me'))
        gr.add_edge(('find-me','dont-find-me'))
        st, pre, post = depth_first_search(gr, root=0, filter=find('find-me'))
        assert st['find-me'] == 0
        assert 'dont-find-me' not in st


class test_radius_filter(unittest.TestCase):

    def testbfs_in_empty_graph(self):
        gr = graph()
        st, lo = breadth_first_search(gr, filter=radius(2))
        assert st == {}
        assert lo == []
    
    def test_bfs_in_graph(self):
        gr = testlib.new_graph()
        st, lo = breadth_first_search(gr, root=0, filter=radius(3))
        for each in st:
            assert (st[each] == None or st[each] == 0
                    or st[st[each]] == 0 or st[st[st[each]]] == 0)
    
    def test_bfs_in_digraph(self):
        gr = testlib.new_digraph()
        st, lo = breadth_first_search(gr, root=0, filter=radius(3))
        for each in st:
            assert (st[each] == None or st[each] == 0
                    or st[st[each]] == 0 or st[st[st[each]]] == 0)

    def test_dfs_in_empty_graph(self):
        gr = graph()
        st, pre, post = depth_first_search(gr, filter=radius(2))
        assert st == {}
        assert pre == []
        assert post == []
    
    def test_dfs_in_graph(self):
        gr = testlib.new_graph()
        st, pre, post = depth_first_search(gr, root=0, filter=radius(3))
        for each in st:
            assert (st[each] == None or st[each] == 0
                    or st[st[each]] == 0 or st[st[st[each]]] == 0)
    
    def test_dfs_in_digraph(self):
        gr = testlib.new_graph()
        st, pre, post = depth_first_search(gr, root=0, filter=radius(3))
        for each in st:
            assert (st[each] == None or st[each] == 0
                    or st[st[each]] == 0 or st[st[st[each]]] == 0)
            
if __name__ == "__main__":
    unittest.main()