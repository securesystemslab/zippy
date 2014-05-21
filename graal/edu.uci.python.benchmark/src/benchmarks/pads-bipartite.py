# This is PADS, a library of Python Algorithms and Data Structures
# implemented by David Eppstein of the University of California, Irvine.
#
# The current version of PADS may be found at
# <http://www.ics.uci.edu/~eppstein/PADS/>, as individual files or as a
# git repository that may be copied by the command line
#
# 02/24/14 Modified by Wei Zhang

##############################
# DFS
##############################
# Types of edges in DFS traversal.
# The numerical values are used in DepthFirstSearcher, change with care.
forward = 1     # traversing edge (v,w) from v to w
reverse = -1    # returning backwards on (v,w) from w to v
nontree = 0     # edge (v,w) is not part of the DFS tree

def DFS_search(G):
    """
    Generate sequence of triples (v,w,edgetype) for DFS of graph G.
    The subsequence for each root of each tree in the DFS forest starts
    with (root,root,forward) and ends with (root,root,reverse).
    If the initial vertex is given, it is used as the root and vertices
    not reachable from it are not searched.
    """
    visited = set()
    initials = G

    for v in initials:
        if v not in visited:
            yield v,v,forward
            visited.add(v)
            stack = [(v, iter(G[v]))]
            while stack:
                parent,children = stack[-1]
                try:
                    child = next(children)
                    print('child', child)
                    if child in visited:
                        print('yielding1 child', child)
                        yield parent,child,nontree
                    else:
                        print('yielding2 child', child)
                        yield parent,child,forward
                        visited.add(child)
                        stack.append((child,iter(G[child])))
                except StopIteration:
                    stack.pop()
                    if stack:
                        print('yielding3 child', child)
                        yield stack[-1][0],parent,reverse

            yield v,v,reverse

##############################
# Bipartite
##############################
def TwoColor(G):
    """
    Find a bipartition of G, if one exists.
    Raises NonBipartite or returns dict mapping vertices
    to two colors (True and False).
    """
    color = {}
    for v,w,edgetype in DFS_search(G):
        print('v',v , 'w',w)
        if edgetype is forward:
            color[w] = not color.get(v,False)
        elif edgetype is nontree and color[v] == color[w]:
            return None
    return color

def isBipartite(G):
    """
    Return True if G is bipartite, False otherwise.
    """
    color = TwoColor(G)
    if color == None:
        return False

    return True

def create_cycle_graph(n):
    return {i:[(i-1)%n,(i+1)%n] for i in range(n)}

def main():
	graph = create_cycle_graph(100)
	result = isBipartite(graph)
	print(result)

main()
