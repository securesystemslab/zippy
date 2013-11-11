# zwei 10/09/13
# arithmetic ops (partially extracted from spectralnorm)
import time

def docompute(num):
	for i in range(num):
		sum = 0.0
		j = 0
		while j < num:
			sum += 1.0 / (((i + j) * (i + j + 1) >> 1) + i + 1)
			j += 1

	return sum


def measure(num):
	print("Start timing...")
	start = time.time()

	for run in range(num):
		sum = docompute(10000) #10000

	print("sum", sum)

	duration = "%.3f\n" % (time.time() - start)
	print("arith-binop: " + duration)

for run in range(50):
	docompute(1000) #1000

measure(5)