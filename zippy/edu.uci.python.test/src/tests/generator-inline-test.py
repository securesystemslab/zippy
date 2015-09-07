# zwei 12/16/2013
# generator inline

def generator(n):
    for i in range(n):
        yield i

def call_generator():
    for i in range(5000):
        for i in generator(100):
            item = i

    return item

for i in range(5):
    print(call_generator())
