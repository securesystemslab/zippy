# zwei 01/09/2014
# sum a generator expression

genexp = (x*2 for x in range(5))

def _sum(iterable):
    sum = None
    for i in iterable:
        sum += i
    return sum

print(_sum(genexp))
print(_sum(genexp))