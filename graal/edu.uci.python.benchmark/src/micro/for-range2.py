# zwei 10/07/2013
# micro benchmark: simple for range loop
import time

iteration = 50000 # 50000

def add(left, right):
    return left + right

def sumitup(iteration):
	total = 0
	for i in xrange(iteration):
		total = add(total, i)

	return total

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in xrange(num): # 50000
		sumitup(iteration)

	print(sumitup(iteration))
	duration = "%.3f\n" % (time.time() - start)
	print("for-range: " + duration)

for i in xrange(10000): # 5000
	sumitup(6000) # 1000

#add("a", "b")
measure(50000)