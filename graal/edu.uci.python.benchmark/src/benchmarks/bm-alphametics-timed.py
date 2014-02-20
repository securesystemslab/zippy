#! /usr/bin/python2.5

"""Wrapper script for testing the performance of simple AI systems.

bm_ai.py runs the following little solvers:
    - N-Queens
    - alphametics (e.g., "SEND + MORE = MONEY")
"""

# Wanted by the alphametics solver.


__author__ = "collinwinter@google.com (Collin Winter)"

# Python imports
import re
import time


# Pure-Python implementation of itertools.permutations().
def permutations(iterable, r=None):
    """permutations(range(3), 2) --> (0,1) (0,2) (1,0) (1,2) (2,0) (2,1)"""
    pool = tuple(iterable)
    n = len(pool)
    if r is None:
        r = n
    indices = list(range(n))
    cycles = list(range(n-r+1, n+1))[::-1]
    yield tuple(pool[i] for i in indices[:r])
    while n:
        for i in reversed(list(range(r))):
            cycles[i] -= 1
            if cycles[i] == 0:
                indices[i:] = indices[i+1:] + indices[i:i+1]
                cycles[i] = n - i
            else:
                j = cycles[i]
                indices[i], indices[-j] = indices[-j], indices[i]
                yield tuple(pool[i] for i in indices[:r])
                break
        else:
            return


# From http://code.activestate.com/recipes/576615/
def alphametics(s):
    """Find solutions to alphametic equations.

    >>> solve('SEND + MORE == MONEY')
    9567 + 1085 == 10652
    """
    words = re.findall("[A-Za-z]+", s)
    chars = set("".join(words))         # Characters to be substituted.
    assert len(chars) <= 10             # There are only ten possible digits.
    firsts = set(w[0] for w in words)   # First letters of each of word.
    chars = "".join(firsts) + "".join(chars - firsts)
    n = len(firsts)                     # chars[:n] cannot be assigned zero.
    for perm in permutations("0123456789", len(chars)):
        if "0" not in perm[:n]:
            trans = str.maketrans(chars, "".join(perm))
            equation = s.translate(trans)
            if eval(equation):
                yield equation

def main():
    # This is a fairly simple equation. More interesting ones like
    # SEND + MORE = MONEY take forever to solve, though.
    ## equation = "SEND + MORE == MONEY"
    ## equation = "CROSS + ROADS == DANGER"
    ## equation = "EED + BE == CCCC"
    equation = "SIX + SIX + SIX == NINE + NINE"
    return list(alphametics(equation))

# if __name__ == "__main__":
#     test_alphametics()

def measure():
    # queen_count = int(sys.argv[1])
    print("Start timing...")
    start = time.time()
    print(main())
    duration = "%.3f\n" % (time.time() - start)
    print("bm-alphametics: " + duration)

measure()
