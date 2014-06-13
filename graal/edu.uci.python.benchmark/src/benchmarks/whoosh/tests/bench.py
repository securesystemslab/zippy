__author__ = 'zwei'

from whoosh import fields, query, matching, scoring
from whoosh.compat import b, u, permutations
from whoosh.filedb.filestore import RamStorage
from whoosh import analysis, fields, qparser

import cProfile as prof

def test_filter():
    lm = lambda: matching.ListMatcher(list(range(2, 10)))

    fm = matching.FilterMatcher(lm(), frozenset([3, 9]))
    assert list(fm.all_ids()) == [3, 9]

    fm = matching.FilterMatcher(lm(), frozenset([1, 5, 9, 13]))
    assert list(fm.all_ids()) == [5, 9]


def test_exclude():
    em = matching.FilterMatcher(matching.ListMatcher([1, 2, 5, 9, 10]),
                                frozenset([2, 9]), exclude=True)
    assert list(em.all_ids()) == [1, 5, 10]

    em = matching.FilterMatcher(matching.ListMatcher([1, 2, 5, 9, 10]),
                                frozenset([2, 9]), exclude=True)
    assert list(em.all_ids()) == [1, 5, 10]

    em = matching.FilterMatcher(matching.ListMatcher([1, 2, 5, 9, 10]),
                                frozenset([2, 9]), exclude=True)
    em.next()
    em.next()
    em = em.copy()
    ls = []
    while em.is_active():
        ls.append(em.id())
        em.next()

    assert ls == [10]

test_exclude()

def test_simple_union():
    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    um = matching.UnionMatcher(lm1, lm2)
    ls = []
    while um.is_active():
        ls.append((um.id(), um.score()))
        um.next()
    assert ls == [(0, 1.0), (1, 1.0), (4, 2.0), (10, 1.0), (20, 2.0), (90, 1.0)]

    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    um = matching.UnionMatcher(lm1, lm2)
    assert list(um.all_ids()) == [0, 1, 4, 10, 20, 90]

    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    um = matching.UnionMatcher(lm1, lm2)
    um.next()
    um.next()
    um = um.copy()
    ls = []
    while um.is_active():
        ls.append(um.id())
        um.next()
    assert ls == [4, 10, 20, 90]

def test_inverse():
    s = matching.ListMatcher([1, 5, 10, 11, 13])
    inv = matching.InverseMatcher(s, 15)
    ids = []
    while inv.is_active():
        ids.append(inv.id())
        inv.next()
    assert ids == [0, 2, 3, 4, 6, 7, 8, 9, 12, 14]


def test_simple_intersection():
    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    im = matching.IntersectionMatcher(lm1, lm2)
    ls = []
    while im.is_active():
        ls.append((im.id(), im.score()))
        im.next()
    assert ls == [(4, 2.0), (20, 2.0)]

    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    im = matching.IntersectionMatcher(lm1, lm2)
    assert list(im.all_ids()) == [4, 20]

    lm1 = matching.ListMatcher([1, 4, 10, 20, 90])
    lm2 = matching.ListMatcher([0, 4, 20])
    im = matching.IntersectionMatcher(lm1, lm2)
    im.next()
    im.next()
    im = im.copy()
    ls = []
    while im.is_active():
        ls.append(im.id())
        im.next()
    assert not ls

def test_replacements():
    sc = scoring.WeightScorer(0.25)
    a = matching.ListMatcher([1, 2, 3], [0.25, 0.25, 0.25], scorer=sc)
    b = matching.ListMatcher([1, 2, 3], [0.25, 0.25, 0.25], scorer=sc)
    um = matching.UnionMatcher(a, b)

    a2 = a.replace(0.5)
    assert a2.__class__ == matching.NullMatcherClass

    um2 = um.replace(0.5)
    assert um2.__class__ == matching.IntersectionMatcher
    um2 = um.replace(0.6)
    assert um2.__class__ == matching.NullMatcherClass

    wm = matching.WrappingMatcher(um, boost=2.0)
    wm = wm.replace(0.5)
    assert wm.__class__ == matching.WrappingMatcher
    assert wm.boost == 2.0
    assert wm.child.__class__ == matching.IntersectionMatcher

    ls1 = matching.ListMatcher([1, 2, 3], [0.1, 0.1, 0.1],
                               scorer=scoring.WeightScorer(0.1))
    ls2 = matching.ListMatcher([1, 2, 3], [0.2, 0.2, 0.2],
                               scorer=scoring.WeightScorer(0.2))
    ls3 = matching.ListMatcher([1, 2, 3], [0.3, 0.3, 0.3],
                               scorer=scoring.WeightScorer(0.3))
    mm = matching.MultiMatcher([ls1, ls2, ls3], [0, 4, 8])
    mm = mm.replace(0.25)
    assert mm.current == 2

    dm = matching.DisjunctionMaxMatcher(ls1, ls2)
    dm = dm.replace(0.15)
    assert dm is ls2

test_replacements()

def create_matchers():
    id1 = [i for i in range(1000)]
    id2 = [i + 1 for i in range(1000)]
    id3 = [i * 2 + i % 5 for i in range(1000)]
    id4 = [i * i for i in range(1000)]
    id5 = [1001 - i for i in range(1000)]
    id6 = [i * 3 // 2 for i in range(1000)]
    vl1 = [0.1 for i in range(1000)]
    vl2 = [0.2 for i in range(1000)]
    vl3 = [0.3 for i in range(1000)]
    vl4 = [0.4 for i in range(1000)]
    vl5 = [0.5 for i in range(1000)]
    vl6 = [0.6 for i in range(1000)]
    sc1 = scoring.WeightScorer(0.15)
    sc2 = scoring.WeightScorer(0.25)
    sc3 = scoring.WeightScorer(0.35)
    sc4 = scoring.WeightScorer(0.45)
    sc5 = scoring.WeightScorer(0.55)
    sc6 = scoring.WeightScorer(0.65)
    ls1 = matching.ListMatcher(id1, vl1, sc1)
    ls2 = matching.ListMatcher(id2, vl2, sc2)
    ls3 = matching.ListMatcher(id3, vl3, sc3)
    ls4 = matching.ListMatcher(id4, vl4, sc4)
    ls5 = matching.ListMatcher(id5, vl5, sc5)
    ls6 = matching.ListMatcher(id6, vl6, sc6)
    um1 = matching.UnionMatcher(ls1, ls2)
    um2 = matching.UnionMatcher(ls3, ls4)
    um3 = matching.UnionMatcher(ls5, ls6)
    inv = matching.InverseMatcher(um3, 15)
    mm = matching.MultiMatcher([um1, um2, inv], [0, 9, 18])
    return mm

def domatch(matcher):
    return [_id for _id in matcher.all_ids()]

def main(n):
    for i in range(n):
        matchers = create_matchers()
        ret = domatch(matchers)

    return ret

print(main(1000))