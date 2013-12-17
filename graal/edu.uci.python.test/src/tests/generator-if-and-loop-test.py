# zwei 12/16/2013
# generator
def gen(n):
    if n == 5:
        yield n * 2
    for i in range(n):
        yield i

for i in gen(5):
    print(i)
