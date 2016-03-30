# Oct 27, 2015
# Ising model
# numba-benchmarks
# https://github.com/numba/numba-benchmark
#
# originally by Antoine Pitrou
# modified by myq
# remove classes and add pure Ising model python implementation

"""
Ising model benchmark, adapted from
http://matthewrocklin.com/blog/work/2015/02/28/Ising/
"""

from math import exp, log, e, sqrt
import math
import random
import GPUZipPy

N = 6
N_iterations = 7

kT = 2 / (log(1 + sqrt(2))/ log(e))
# x_start = [[random.randint(0,2) for i in range(N)] for j in range(N)]
x_start = [[1, 2, 0, 1, 1, 2], [1, 1, 1, 1, 1, 2], [1, 0, 2, 1, 0, 0], [0, 0, 2, 2, 0, 1], [0, 0, 2, 2, 0, 1], [2, 2, 2, 0, 0, 1]]

for i in range(N):
    for j in range(N):
        if x_start[i][j] == 0:
            x_start[i][j] = -1

# randomlist = [[random.random() for i in range(N)] for j in range(N)]
randomlist = [[0.5374977580801349, 0.7288789981054569, 0.874337430246591, 0.5558498988546964, 0.13413388157672224, 0.13206981395884076],
                [0.5930613097441034, 0.46658989944531426, 0.46871224550863744, 0.23347494664173862, 0.6153568160424621, 0.45494620140370134],
                [0.21873372143183967, 0.9223178045634051, 0.9797410279545438, 0.5899465763215381, 0.2014846991927467, 0.27282121638798285],
                [0.966601690188758, 0.5008766725254007, 0.16115166557325422, 0.8107957646540611, 0.28721909764539866, 0.72061557114457],
                [0.7842802370453988, 0.569701256713242, 0.56997477038006, 0.21891065788309616, 0.7087897121308825, 0.8777160753231892],
                [0.32347218230684616, 0.4848425192652519, 0.9081707166907533, 0.6018514246338039, 0.20155297804037642, 0.7121923811153379]]

@GPUZipPy.ddtest(False)
def update(x):
    n = N
    m = N

    for i in range(n):
        for jjj in range(0, N // 2, 1):
            # Even columns first to avoid overlap
            j = 2 * jjj

            # Deal with negative indices when jitting
            ii = (i-1)%n
            if ii < 0:
                ii = n - 1

            jj = (j-1)%m
            if jj < 0:
                jj = m - 1

            dE = 2* x[i][ j] * (
                             x[ii][ jj]
                           + x[ii][  j     ]
                           + x[ii][ (j+1)%m]

                           + x[ i     ][ jj]
                           + x[ i     ][ (j+1)%m]

                           + x[(i+1)%n][ jj]
                           + x[(i+1)%n][  j     ]
                           + x[(i+1)%n][ (j+1)%m]
                           )
            if dE <= 0 or exp((-1*dE) / kT) > randomlist[i][j]:
                x[i][ j] *= -1

    for i in range(n):
        for jjj in range(0, N // 2, 1):
            # Odd columns second to avoid overlap
            j = 2 * jjj + 1

            # Deal with negative indices when jitting
            ii = (i-1)%n
            if ii < 0:
                ii = n + ii

            jj = (j-1)%m
            if jj < 0:
                jj = m + jj

            dE = 2* x[i][ j] * (
                             x[ii][ jj]
                           + x[ii][  j     ]
                           + x[ii][ (j+1)%m]

                           + x[ i     ][ jj]
                           + x[ i     ][ (j+1)%m]

                           + x[(i+1)%n][ jj]
                           + x[(i+1)%n][  j     ]
                           + x[(i+1)%n][ (j+1)%m]
                           )
            if dE <= 0 or exp((-1*dE) / kT) > randomlist[i][j]:
                x[i][ j] *= -1


def time_ising():
    for i in range(N_iterations):
        update(x_start)

time_ising()
print(x_start)
