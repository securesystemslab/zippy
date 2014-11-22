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

	return foo.a

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in range(num): # 50000
	  result = dostuff(Foo(42))

	print(result)
	duration = "%.3f\n" % (time.time() - start)
	print("attribute-access: " + duration)

# warm up
for i in range(2000):
	dostuff(Foo(42))

measure(5000)