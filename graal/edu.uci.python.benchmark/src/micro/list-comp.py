# zwei 11/11/2013
# micro benchmark: list comprehension
import time

def makeList(size):
	return [i for i in range(size)]

def makeLists(num):
	for i in range(num):
		ll = makeList(100000)

	print(ll[-1])

def measure():
	print("Start timing...")
	start = time.time()

	makeLists(5000) # 50000
		
	duration = "%.3f\n" % (time.time() - start)
	print("list-comp: " + duration)

#warm up
for i in range(12):
	makeLists(500)

measure()