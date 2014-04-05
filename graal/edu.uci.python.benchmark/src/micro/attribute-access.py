# zwei 04/04/2014
# micro benchmark: attribute access
import time

iteration = 50000 # 50000

class Foo():
	def __init__(self, a):
		self.a = a

def dostuff(foo):
	for i in range(iteration):
		local_a = foo.a + 1 
		foo.a = local_a % 5

	return local_a

# def add(left, right):
#     return left + right

# def sumitup(iteration):
# 	total = 0
# 	for i in range(iteration):
# 		total = add(total, i)

# 	return total

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in range(num): # 50000
	  result = dostuff(Foo(42))

	print(result)
	duration = "%.3f\n" % (time.time() - start)
	print("attribute-access: " + duration)

# for i in range(10000): # 5000
	# sumitup(6000) # 1000

#add("a", "b")
measure(500)