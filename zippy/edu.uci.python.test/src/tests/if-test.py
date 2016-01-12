# zwei 01/02/2014
# if and elif

def test():
	sum = 0
	for i in range(10):
		if i == 0:
			sum += 10
		elif i < 3:
			sum += 2
		elif i < 7:
			sum += 5
		else:
			sum -= 1

	return sum

print(test())