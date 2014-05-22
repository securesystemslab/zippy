# zwei 12/16/2013
# generator
def gen(n):
	for i in range(n):
		i += 1
		yield i
		if i % 3 != 0:
			continue
		print('!!')

for i in gen(5):
    print(i)
