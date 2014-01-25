# zwei 12/17/13
# More work in each generator iteration.
# Hopefully it is easier for parallelization
import time

def do_each_yield(i):
	i = i * 10
	x = 0
	for j in range(i):
		x = x + (j % 5)

	return x

def generator(n):
	for i in range(n):
		# yield i * 2
		yield do_each_yield(i)

def call_generator(num, iteration):
	item = 0	
	for i in generator(num):
		item = i + item % 5
		for t in range(i * 10):
			item += t % 5

	return item


def measure():
	print("Start timing...")
	start = time.time()

	num = 10000
	last_item = call_generator(num, 10) #1000000

	print("Last item ", last_item)

	duration = "%.3f\n" % (time.time() - start)
	print("generator: " + duration)

#warm up
for run in range(200):
	call_generator(200, 10)

measure()