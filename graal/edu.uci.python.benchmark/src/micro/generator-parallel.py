# zwei 12/17/13
# More work in each generator iteration.
# Various parameters have been tuned to produce 
# a better balanced workload between caller and the generator

import time

def do_each_yield(i, work):
	x = 0
	for j in range(work):
		x = x + (i % 5)

	return x

def generator(num, work):
	for i in range(num):
		yield do_each_yield(i, work)

def call_generator(num, work):
	item = 0	
	for i in generator(num, work):
		item += do_each_yield(i, work)

	return item


def measure(work_of_each_iteration, num_of_iterations):
	print("Start timing... ", work_of_each_iteration, num_of_iterations)
	start = time.time()

	last_item = call_generator(work_of_each_iteration, num_of_iterations)

	print("Last item ", last_item)

	duration = "%.3f\n" % (time.time() - start)
	print("generator-parallel: " + duration)

#warm up
for run in range(100): # 200
	call_generator(200, 5000)

measure(100000, 20000)
measure(100000, 10000)
measure(100000, 5000)
measure(100000, 2500)
measure(100000, 1000)
measure(100000, 500)

measure(10000000, 500)
measure( 5000000, 500)
measure( 2500000, 500)
measure( 1000000, 500)
measure(  500000, 500)

measure(10000000, 100)
measure( 5000000, 100)
measure( 2500000, 100)
measure( 1000000, 100)
measure(  500000, 100)