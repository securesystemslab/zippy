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
Unittests for graph.algorithms.readwrite
"""


import unittest
import pygraph
from pygraph.readwrite import dot, markup
import testlib

def graph_equality(gr1, gr2):
    for each in gr1.nodes():
        assert each in gr2.nodes()
    for each in gr2.nodes():
        assert each in gr1.nodes()
    for each in gr1.edges():
        assert each in gr2.edges()
    for each in gr2.edges():
        assert each in gr1.edges()

class test_readwrite_dot(unittest.TestCase):

    def test_dot_for_graph(self):
        gr = testlib.new_graph()
        dotstr = dot.write(gr)
        gr1 = dot.read(dotstr)
        dotstr = dot.write(gr1)
        gr2 = dot.read(dotstr)  
        graph_equality(gr1, gr2)
        assert len(gr.nodes()) == len(gr1.nodes())
        assert len(gr.edges()) == len(gr1.edges())
    
    def test_dot_for_digraph(self):
        gr = testlib.new_digraph()
        dotstr = dot.write(gr)
        gr1 = dot.read(dotstr)
        dotstr = dot.write(gr1)
        gr2 = dot.read(dotstr)  
        graph_equality(gr1, gr2)
        assert len(gr.nodes()) == len(gr1.nodes())
        assert len(gr.edges()) == len(gr1.edges())
    
    def test_dot_for_hypergraph(self):
        gr = testlib.new_hypergraph()
        dotstr = dot.write(gr)
        gr1 = dot.read_hypergraph(dotstr)
        dotstr = dot.write(gr1)
        gr2 = dot.read_hypergraph(dotstr)  
        graph_equality(gr1, gr2)
    
    def test_output_names_in_dot(self):
        gr1 = testlib.new_graph()
        gr1.name = "Some name 1"
        gr2 = testlib.new_digraph()
        gr2.name = "Some name 2"
        gr3 = testlib.new_hypergraph()
        gr3.name = "Some name 3"
        assert "Some name 1" in dot.write(gr1)
        assert "Some name 2" in dot.write(gr2)
        assert "Some name 3" in dot.write(gr3)

class test_readwrite_markup(unittest.TestCase):

    def test_xml_for_graph(self):
        gr = testlib.new_graph()
        dotstr = markup.write(gr)
        gr1 = markup.read(dotstr)
        dotstr = markup.write(gr1)
        gr2 = markup.read(dotstr)  
        graph_equality(gr1, gr2)
        assert len(gr.nodes()) == len(gr1.nodes())
        assert len(gr.edges()) == len(gr1.edges())
    
    def test_xml_digraph(self):
        gr = testlib.new_digraph()
        dotstr = markup.write(gr)
        gr1 = markup.read(dotstr)
        dotstr = markup.write(gr1)
        gr2 = markup.read(dotstr)  
        graph_equality(gr1, gr2)
        assert len(gr.nodes()) == len(gr1.nodes())
        assert len(gr.edges()) == len(gr1.edges())
    
    def test_xml_hypergraph(self):
        gr = testlib.new_hypergraph()
        dotstr = markup.write(gr)
        gr1 = markup.read(dotstr)
        dotstr = markup.write(gr1)
        gr2 = markup.read(dotstr)  
        graph_equality(gr1, gr2)
         
if __name__ == "__main__":
    unittest.main()
