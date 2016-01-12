__author__ = 'zwei'

import sys
sys.path.append('..')
sys.path.append('../core')
import pygraph
from pygraph.algorithms.accessibility import accessibility
from pygraph.classes.graph import graph
from sys import getrecursionlimit

def test_accessibility_on_very_deep_graph():
    gr = graph()
    gr.add_nodes(range(0,2001))
    for i in range(0,2000):
        gr.add_edge((i,i+1))
    recursionlimit = getrecursionlimit()
    accessibility(gr)
    assert getrecursionlimit() == recursionlimit

test_accessibility_on_very_deep_graph()