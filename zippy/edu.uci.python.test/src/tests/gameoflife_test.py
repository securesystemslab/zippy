# Oct 27, 2015
# game of life
# numba-benchmarks
# https://github.com/numba/numba-benchmark
#
# originally by Antoine Pitrou
# modified by myq
# remove classes and add pure game of life python implementation

"""
Benchmark a game of life implementation.
"""
import time
import numpy as np


N = 10

ni = 300
nj = 200
start_state = np.random.RandomState(0).random_sample((ni, nj)) > 0.5
neighbors = np.zeros_like(X, dtype=np.int8)

def life_step(X):
    # Compute # of live neighbours per cell
    for i in range(X.shape[0]):
        for j in range(X.shape[1]):
            if X[i,j]:
                # increment_neighbors(i, j, neighbors)
                for delta_i in (-1, 0, 1):
                    # neighbor_i = wrap(i + delta_i, ni)
                    neighbor_i = 0
                    ki = i + delta_i
                    max_ki = ni
                    if ki == -1:
                        neighbor_i = max_ki - 1
                    elif ki == max_ki:
                        neighbor_i = 0
                    else:
                        neighbor_i = ki

                    for delta_j in (-1, 0, 1):
                        if delta_i != 0 or delta_j != 0:
                            # neighbor_j = wrap(j + delta_j, nj)
                            neighbor_j = 0
                            kj = j + delta_j
                            max_kj = nj
                            if kj == -1:
                                neighbor_j = max_kj - 1
                            elif kj == max_kj:
                                neighbor_j = 0
                            else:
                                neighbor_j = kj

                            neighbors[neighbor_i][neighbor_j] += 1


    # Return next iteration of the game state
    return (neighbors == 3) | (X & (neighbors == 2))



def run_game(nb_iters):
    state = start_state
    for i in range(nb_iters):
        state = life_step(state)
    return state


start = time.time()
run_game(N)
duration = "N, %d,  Time, %.3f" % (N,time.time() - start)
print("NumPy, game of life, " + duration)
