# zwei 01/09/2014
# sum a generator expression

genexp = (x*2 for x in range(5))

def _sum(iterable):
    sum = 0
    for i in iterable:
        sum += i
    return sum

print(_sum(genexp))
print(_sum(genexp))

# test
#def call_sum(num):
#	for i in range(num):
#		total = _sum(genexp)

#	return total

#call_sum(1000000)
