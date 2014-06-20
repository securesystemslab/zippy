__author__ = 'zwei'

import os, sys, time

# setup path
whoosh_path = os.path.join(os.path.dirname(__file__), 'whoosh/src')
sys.path.append(whoosh_path)

from whoosh.matching.mcore import ListMatcher
from whoosh.matching.binary import UnionMatcher
from whoosh.matching.wrappers import InverseMatcher
from whoosh.matching.wrappers import MultiMatcher
from whoosh.scoring import WeightScorer

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
    sc1 = WeightScorer(0.15)
    sc2 = WeightScorer(0.25)
    sc3 = WeightScorer(0.35)
    sc4 = WeightScorer(0.45)
    sc5 = WeightScorer(0.55)
    sc6 = WeightScorer(0.65)
    ls1 = ListMatcher(id1, vl1, sc1)
    ls2 = ListMatcher(id2, vl2, sc2)
    ls3 = ListMatcher(id3, vl3, sc3)
    ls4 = ListMatcher(id4, vl4, sc4)
    ls5 = ListMatcher(id5, vl5, sc5)
    ls6 = ListMatcher(id6, vl6, sc6)
    um1 = UnionMatcher(ls1, ls2)
    um2 = UnionMatcher(ls3, ls4)
    um3 = UnionMatcher(ls5, ls6)
    inv = InverseMatcher(um3, 15)
    mm = MultiMatcher([um1, um2, inv], [0, 9, 18])
    return mm

def domatch(matcher):
    return [_id for _id in matcher.all_ids()]

def main(n):
    for i in range(n):
        matchers = create_matchers()
        ret = domatch(matchers)

    return ret

def measure():
    print("Start timing...")
    start = time.time()
    json = main(num)
    duration = "%.3f\n" % (time.time() - start)
    print("whoosh-match: " + duration)

# warm up
num = int(sys.argv[1]) # 1000
for i in range(50):
    main(100)

measure()