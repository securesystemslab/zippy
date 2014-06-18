__author__ = 'zwei'

import sys, os, time

# setup path
path = os.path.join(os.path.dirname(__file__), 'sympy')
sys.path.append(path)

from sympy.unify.core import Compound, Variable, CondVariable, allcombinations
from sympy.unify import core

a, b, c, d, e, f, g, h, i, j, k, l, m, n = 'abcdefghijklmn'
o, p, q, r, s, t, u, v, w, x, y, z = map(Variable, 'opqrstuvwxyz')

C = Compound

def is_associative(x):
    return isinstance(x, Compound) and (x.op in ('Add', 'Mul', 'CAdd', 'CMul'))
def is_commutative(x):
    return isinstance(x, Compound) and (x.op in ('CAdd', 'CMul'))


def unify(a, b, s={}):
    return core.unify(a, b, s)

def test_basic():
    assert list(unify(a, x, {})) == [{x: a}]
    assert list(unify(a, x, {x: 10})) == []
    assert list(unify(1, x, {})) == [{x: 1}]
    assert list(unify(a, a, {})) == [{}]
    assert list(unify((w, x), (y, z), {})) == [{w: y, x: z}]
    assert list(unify(x, (a, b), {})) == [{x: (a, b)}]

    assert list(unify((a, b), (x, x), {})) == []
    assert list(unify((y, z), (x, x), {}))!= []
    assert list(unify((a, (b, c)), (a, (x, y)), {})) == [{x: b, y: c}]

# test_basic()

def test_ops():
    assert list(unify(C('Add', (a,b,c)), C('Add', (a,x,y)), {})) == \
            [{x:b, y:c}]
    cmul = C('Mul', (1,2))
    assert list(unify(C('Add', (cmul, b,c)), C('Add', (x,y,c)), {})) == \
            [{x: cmul, y:b}]

# for i in range(1000):
# test_ops()

def main(n):
    C1 = C('Add', [i for i in range(8)])
    C2 = C('Add', [Variable(i) for i in range(8)])

    for idx in range(n):
        lst = []
        for elem in core.unify(C1, C2, {}):
            lst.append(elem)

    return lst

def measure():
    print("Start timing...")
    start = time.time()
    lst = main(num)
    duration = "%.3f\n" % (time.time() - start)
    # print(lst)
    print("sympy-unify: " + duration)

# warm up
num = int(sys.argv[1]) # 10000
for idx in range(100):
    main(1000)

measure()



