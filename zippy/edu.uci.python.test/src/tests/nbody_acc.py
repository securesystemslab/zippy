# Oct 19, 2015
# numba-benchmarks
# https://github.com/numba/numba-benchmark
#
# originally by Antoine Pitrou
# modified by myq
# remove classes and add pure nbody python

"""
Benchmark an implementation of the N-body simulation.

As in the CUDA version, we only compute accelerations and don't care to
update speeds and positions.
"""

from __future__ import division

import sys, time
import random

def run_nbody(n, positions, weights):
    for i in range(n):
        ax = 0.0
        ay = 0.0
        for j in range(n):
            rx = positions[j][0] - positions[i][0]
            ry = positions[j][1] - positions[i][1]
            sqr_dist = rx * rx + ry * ry + 1e-6
            sixth_dist = sqr_dist * sqr_dist * sqr_dist
            inv_dist_cube = 1.0 / (sixth_dist ** 0.5)
            s = weights[j] * inv_dist_cube
            ax += s * rx
            ay += s * ry
        accelerations[i][0] = ax
        accelerations[i][1] = ay


def make_nbody_samples(n_bodies):
    positions = [[random.uniform(-1.0, 1.0) for i in range(2)] for j in range(n_bodies)]
    weights   = [random.uniform(1.0, 2.0) for i in range(n_bodies)]
    return positions, weights

n_bodies = 2 ** 16

# Actual benchmark samples
positions, weights = make_nbody_samples(n_bodies)
accelerations = [[0.0 for i in range(2)] for j in range(n_bodies)]

print("Sample creation complete")

def time_nbody():
    run_nbody(n_bodies, positions, weights)


start = time.time()
time_nbody()
duration = "%.3f\n" % (time.time() - start)
print("nbody accelerations: " + duration)
