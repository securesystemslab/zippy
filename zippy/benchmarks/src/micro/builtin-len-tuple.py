# zwei 12/13/13
# builtin function len()
import time

def callLen(num, ll):
	length = 0
	for i in range(num):
		length = len(ll)

	return length


def measure():
	print("Start timing...")
	start = time.time()

	ll = tuple(range(1000))
	length = callLen(1000000000, ll) #1000000

	print("Final length ", length)

	duration = "%.3f\n" % (time.time() - start)
	print("builtin-len-tuple: " + duration)

#warm up
for run in range(10000):
	callLen(50000, (1,2,3,4,5))


measure()