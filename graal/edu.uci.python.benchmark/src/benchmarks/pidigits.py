# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
# contributed by Dani Nanz (2007-09-21)

import sys
import gmpy

def main(n):

    pi = str(gmpy.pi(int(3.35 * n)))
    pi_tmp = ''.join([pi[0], pi[2:]])
    pistr = pi_tmp[0 : n]
    w = 10
    out = []
    for i in xrange(0, n - w + 1, w):
        out.extend([pistr[i : i + w] , i + w])
    print ('%s\t:%d\n' * (len(out) / 2)) % tuple(out),
    if n % w > 0:
        print "%s\t:%d" % ((pistr[-(n % w):]).ljust(w), n)


main(int(sys.argv[1]))

