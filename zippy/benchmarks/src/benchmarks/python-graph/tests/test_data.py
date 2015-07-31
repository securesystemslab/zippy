"""
Misc functions used for testing, including the generation of test-data.
"""

EDGES = [
    ("China", "Russia"),
    ("Afghanistan", "Iran"),
    ("China", "Russia"),
    ("China", "Mongolia"),
    ("Mongolia", "Russia"),
    ("Mongolia", "China"),
    ("Nepal", "China"),
    ("India", "Pakistan"),
    ("India", "Nepal"),
    ("Afghanistan", "Pakistan"),
    ("North Korea", "China"),
    ("Romania", "Bulgaria"),
    ("Romania", "Moldova"),
    ("Romania", "Ukraine"),
    ("Romania", "Hungary"),
    ("North Korea", "South Korea"),
    ("Portugal", "Spain"),
    ("Spain","France"),
    ("France","Belgium"),
    ("France","Germany"),
    ("France","Italy",),
    ("Belgium","Netherlands"),
    ("Germany","Belgium"),
    ("Germany","Netherlands"),
    ("Germany","Denmark"),
    ("Germany","Luxembourg"),
    ("Germany","Czech Republic"),
    ("Belgium","Luxembourg"),
    ("France","Luxembourg"),
    ("England","Wales"),
    ("England","Scotland"),
    ("England","France"),
    ("Scotland","Wales"),
    ("Scotland","Ireland"),
    ("England","Ireland"),
    ("Switzerland","Austria"),
    ("Switzerland","Germany"),
    ("Switzerland","France"),
    ("Switzerland","Italy"),
    ("Austria","Germany"),
    ("Austria","Italy"),
    ("Austria","Czech Republic"),
    ("Austria","Slovakia"),
    ("Austria","Hungary"),
    ("Austria","Slovenia"),
    ("Denmark","Germany"),
    ("Poland","Czech Republic"),
    ("Poland","Slovakia"),
    ("Poland","Germany"),
    ("Poland","Russia"),
    ("Poland","Ukraine"),
    ("Poland","Belarus"),
    ("Poland","Lithuania"),
    ("Czech Republic","Slovakia"),
    ("Czech Republic","Germany"),
    ("Slovakia","Hungary")]


def nations_of_the_world( G ):
    """
    This is intended to simplify the unit-tests. Given a graph add the nations of the world to it.
    """
    for a,b in EDGES:
        
        for n in [a,b,]:
            if not n in G.nodes():
                G.add_node(n)
                
        if (not G.has_edge((a,b))):
            G.add_edge( (a,b) )
        
    return G
            
            
    