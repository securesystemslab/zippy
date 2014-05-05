# zwei 04/05/14
# special method __len__ dispatch
import time

class Num:
	def __init__(self, n):
		self.n = n
	def __len__(self):
		return self.n
	def __repr__(self):
		return repr(self.n)

def docompute(num):
	for i in range(num):
		sum = 0
		one = Num(42)
		j = 0
		while j < num:
			sum = sum + len(one)
			j += 1

	return sum


def measure(num):
	print("Start timing...")
	start = time.time()

	for run in range(num):
		sum = docompute(20000) #10000

	print("sum", sum)

	duration = "%.3f\n" % (time.time() - start)
	print("special-len: " + duration)

for run in range(50):
	docompute(500) #1000

measure(5)