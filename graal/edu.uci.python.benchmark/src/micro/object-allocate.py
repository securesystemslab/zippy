# zwei 04/04/2014
# micro benchmark: attribute access
import time

iteration = 50000 # 50000

class Foo():
	def __init__(self, a):
		self.a = a

def dostuff():
	num = 24
	foo = Foo(0)
	for i in range(iteration):
		num += foo.a % 3 
		foo = Foo(num)
		# foo.a = num # replace the line above with this line to remove allocation 

	return num

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in range(num):
	  result = dostuff()

	print(result)
	duration = "%.3f\n" % (time.time() - start)
	print("object-allocate: " + duration)

# warm up
for i in range(2000):
	dostuff()

measure(5000)