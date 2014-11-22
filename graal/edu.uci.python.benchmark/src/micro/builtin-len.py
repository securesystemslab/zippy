# zwei 12/02/13
# builtin function len()
import time

def callLen(num, ll):
	length = 0
	for i in range(num):
		ll[i % 5] = i
		length = len(ll)

	return length


def measure():
	print("Start timing...")
	start = time.time()

	ll = [x*2 for x in range(1000)]
	length = callLen(500000000, ll) #1000000000

	print("Final length ", length)

	duration = "%.3f\n" % (time.time() - start)
	print("builtin-len: " + duration)

#warm up
for run in range(5000):
	callLen(10000, [1,2,3,4,5])

measure()