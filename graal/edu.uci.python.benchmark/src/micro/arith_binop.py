# zwei 10/09/13
# arithmetic ops (partially extracted from spectralnorm)
import time

def eval_A (i, j):
    return 1.0 / (((i + j) * (i + j + 1) >> 1) + i + 1)

def docompute(num):
	for i in range(num):
		sum = 0.0
		j = 0
		while j < num:
			sum += eval_A(i, j)
			# sum += 1.0 / (((i + j) * (i + j + 1) >> 1) + i + 1)
			j += 1

	return sum

for run in range(5):
	docompute(10) #1000


print("Start timing...")
start = time.time()

for run in range(5):
	sum = docompute(100) #10000

print("sum", sum)

duration = "%.3f\n" % (time.time() - start)
print("arith_binop: " + duration)