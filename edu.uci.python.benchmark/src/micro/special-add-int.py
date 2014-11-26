# zwei 04/05/14
# special method __add__ dispatch with int operand
import time

class Num:
	def __init__(self, n):
		self.n = n
	def __add__(self, other):
		return Num(self.n + other)
	def __repr__(self):
		return repr(self.n)

def docompute(num):
	for i in range(num):
		one = Num(1)
		j = 0
		while j < num:
			sum = one + j

			if sum.n % 5 == 0:
				one.n = sum.n % 3

			j += 1

	return sum


def measure(num):
	print("Start timing...")
	start = time.time()

	for run in range(num):
		sum = docompute(5000) #10000

	print("sum", sum)

	duration = "%.3f\n" % (time.time() - start)
	print("special-add-int: " + duration)

for run in range(50):
	docompute(500) #1000

measure(5)