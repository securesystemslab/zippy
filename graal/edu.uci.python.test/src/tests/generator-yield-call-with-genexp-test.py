# zwei 01/20/2014
# generator that yields a built-in constructor with a generator expression argument

def generator(n):
	ll = [j for j in range(n)]
	for i in range(n):
		yield list(ll[x] for x in range(i))

for i in generator(10000):
	result = i

print(i[-1])
