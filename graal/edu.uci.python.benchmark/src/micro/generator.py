# zwei 12/17/13
# subscribe simple generator
import time

def gen(n):
	for i in range(n):
		yield i

def call_generator(num, iteration):

	for t in range(iteration):
		for i in gen(num):
			item = i

	return item


def measure():
	print("Start timing...")
	start = time.time()

	num = 1000
	last_item = call_generator(num, 100000) #1000000

	print("Last item ", last_item)

	duration = "%.3f\n" % (time.time() - start)
	print("generator: " + duration)

#warm up
for run in range(1000):
	call_generator(10, 10000)

measure()