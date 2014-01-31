# zwei 12/17/13
# More work in each generator iteration.
# Various parameters have been tuned to produce 
# a better balanced workload between caller and the generator

import time

heavyness = 20000

def do_each_yield(i):
	x = 0
	for j in range(heavyness):
		x = x + (i % 5)

	return x

def generator(n):
	for i in range(n):
		yield do_each_yield(i)

def call_generator(num, iteration):
	item = 0	
	for i in generator(num):
		item += do_each_yield(i)

	return item


def measure():
	print("Start timing...")
	start = time.time()

	num = 50000
	last_item = call_generator(num, 10)

	print("Last item ", last_item)

	duration = "%.3f\n" % (time.time() - start)
	print("generator: " + duration)

#warm up
for run in range(100): # 200
	call_generator(200, 10)

measure()