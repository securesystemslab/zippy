# zwei 10/07/2013
# micro benchmark: simple for range loop
import time

iteration = 1000000

def sumitup(iteration):
	total = 0
	for i in range(iteration):
		total = total + 42

	return total

def measure():
	print("Start timing...")
	start = time.time()

	for i in range(1000):
		sumitup(iteration)

	print(sumitup(iteration))
	duration = "%.3f\n" % (time.time() - start)
	print("for_range: " + duration)

for i in range(1000):
	sumitup(10)

measure()