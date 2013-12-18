# zwei 12/16/2013
# generator inline

def generator(n):
    for i in range(n):
        yield i

for i in range(500):
    for i in generator(10):
        item = i

print(item)
