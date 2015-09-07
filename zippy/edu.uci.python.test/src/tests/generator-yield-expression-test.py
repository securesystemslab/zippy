# zwei 06/28/2014
# generator that yields a built-in constructor with a generator expression argument

def generator(n):
	for i in range(n):
		x = yield i * 2
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