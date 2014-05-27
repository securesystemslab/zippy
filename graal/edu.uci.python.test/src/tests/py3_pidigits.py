#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Calculating (some of) the digits of π.  This stresses big integer
arithmetic."""

# Python imports
import optparse
import time
import itertools

# Local imports
import py3_util as util
from py3_compat import xrange, imap, next


NDIGITS = 2000

def test_pidgits(iterations):
    _map = imap
    _count = itertools.count
    _islice = itertools.islice

    def calc_ndigits(n):
        # Adapted from code on http://shootout.alioth.debian.org/
        def gen_x():
            return _map(lambda k: (k, 4*k + 2, 0, 2*k + 1), _count(1))

        def compose(a, b):
            aq, ar, as_, at = a
            bq, br, bs, bt = b
            return (aq * bq,
                    aq * br + ar * bt,
                    as_ * bq + at * bs,
                    as_ * br + at * bt)

        def extract(z, j):
            q, r, s, t = z
            return (q*j + r) // (s*j + t)

        def pi_digits():
            z = (1, 0, 0, 1)
            x = gen_x()
            while 1:
                y = extract(z, 3)
                while y != extract(z, 4):
                    z = compose(z, next(x))
                    y = extract(z, 3)
                z = compose((10, -10*y, 0, 1), z)
                yield y

        return list(_islice(pi_digits(), n))

    # Warm-up runs.
    calc_ndigits(NDIGITS)
    calc_ndigits(NDIGITS)

    times = []
    for _ in xrange(iterations):
        t0 = time.time()
        calc_ndigits(NDIGITS)
        t1 = time.time()
        times.append(t1 - t0)
    return times


if __name__ == "__main__":
    parser = optparse.OptionParser(
        usage="%prog [options]",
        description=("Test the performance of pi calculation."))
    util.add_standard_options_to(parser)
    options, args = parser.parse_args()

    util.run_benchmark(options, options.num_runs, test_pidgits)
