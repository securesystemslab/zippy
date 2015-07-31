# zwei 01/03/14
# generator expression inlining

def call_generator_localvar(num, iteration):
	item = 0
	for t in range(iteration):
		num += t % 5
		ge = (x * 2 for x in range(num))
		for i in ge:
			item = i + item % 5

	return item

for i in range(1000):
	result = call_generator_localvar(10, 100)
print(result)
