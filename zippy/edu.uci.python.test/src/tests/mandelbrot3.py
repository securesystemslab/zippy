# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
# contributed by Tupteq
# 2to3 - fixed by Daniele Varrazzo

#from __future__ import print_function
import sys, time

def main():
    # cout = sys.stdout.write
    size = 10000
    # size = int(sys.argv[1])
    xr_size = range(size)
    xr_iter = range(50)
    # cout("P4\n%d %d\n" % (size, size))
    # print("P4\n%d %d\n" % (size, size))

    # byte_acc_inc_yx = [[0 for i in range(size)] for i in range(size)]

    size = float(size)
    for y in xr_size:
        bit = 128
        byte_acc = 0
        fy = 2j * y / size - 1j
        for x in xr_size:
            z = 0j
            c = 2. * x / size - 1.5 + fy
            for i in xr_iter:
                z = z * z + c
                if abs(z) >= 2.0:
                    # byte_acc_inc_yx[y][x] = 1
                    break
            else:
                # byte_acc_inc_yx[y][x] = 0
                byte_acc += bit

            if bit > 1:
                bit >>= 1
            else:
                # cout(chr(byte_acc))
                # byte_acc_inc_yx[y][x] = byte_acc
                bit = 128
                byte_acc = 0
        if bit != 128:
            # cout(chr(byte_acc))
            bit = 128
            byte_acc = 0

    # print("byte_acc_inc = ", sum([sum(i) for i in byte_acc_inc_yx]))
    # s = ""
    # for y in xr_size:
    #     for x in xr_size:
    #         if byte_acc_inc_yx[y][x] == 0:
    #             s += "*"
    #         else:
    #             s += " "
    #     s += "\n"
    # print(s)
    # with open("mandelbrot.pm", "a") as myfile:
    #     myfile.write("P4\n%d %d\n" % (size, size))
    #     for y in xr_size:
    #         for x in xr_size:
    #             myfile.write(chr(byte_acc_inc_yx[y][x]))
    #         myfile.write(chr(byte_acc_inc[y]))
    # print(byte_acc_inc_y)
start = time.time()
main()
duration = "%.3f\n" % (time.time() - start)
print("mandelbrot: " + duration)
