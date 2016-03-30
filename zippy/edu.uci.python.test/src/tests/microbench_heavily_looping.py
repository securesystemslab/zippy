# Oct 19, 2015
# heavily looping function
# https://www.wakari.io/sharing/bundle/yves/Continuum_N_Body_Simulation_Numba
#
# originally by Dr. Yves J. Hilpisch
# modified by myq

import time
def f(n):
    t0 = time.time()
    result = 0.0
    for i in range(n):
        for j in range(n * i):
            result += sin(pi / 2)
    return int(result), time.time()-t0

    n = 250
res_py = f(n)

print "Number of Loops        %8d" % res_py[0]
print "Time in Sec for Python %8.3f" % res_py[1]

import numba as nb
f_nb = nb.autojit(f)

res_nb = f_nb(n)

print "Number of Loops        %8d" % res_nb[0]
print "Time in Sec for Python %8.3f" % res_nb[1]

print "Number of Loops        %8d" % res_py[0]
print "Speed-up of Numba      %8d" % (res_py[1] / res_nb[1])
