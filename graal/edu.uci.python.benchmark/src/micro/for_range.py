# zwei 10/07/2013
# micro benchmark: simple for range loop

iteration = 1000000

def sumitup(iteration):
	total = 0
	for i in range(iteration):
		total = total + i

	return total

for i in range(1000):
	sumitup(10)

print(sumitup(iteration))