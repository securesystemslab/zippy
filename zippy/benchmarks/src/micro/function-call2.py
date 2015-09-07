# zwei 10/10/13
# function calls
import time

def emptyFunction(arg):
	return arg

def callFunctions(num):
	count = 0
	for i in xrange(num):
		ret = emptyFunction(i)
		count += 1

	return count


def measure():
	print("Start timing...")
	start = time.time()

	sum = callFunctions(1000000000) #1000000

	print("Number of calls ", sum)

	duration = "%.3f\n" % (time.time() - start)
	print("function-call: " + duration)

#warm up
for run in xrange(10000):
	callFunctions(50000)

measure()