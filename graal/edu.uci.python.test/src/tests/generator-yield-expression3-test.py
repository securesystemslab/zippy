# zwei 06/28/2014
# yield expression in generator functions

def generator(n):
	for i in range(n):
		x = len([]) + (yield i * 2)
		print(x)

gen = generator(5)
it = 0
gen.__next__()

try:
	while True:
		gen.send(it)
		it += 1
except StopIteration:
	pass