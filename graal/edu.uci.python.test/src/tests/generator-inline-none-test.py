# zwei 05/16/2014
# generator inline

def call_generator():
    for i in range(5000):
        def localgen(n):
            for i in range(n):
                yield i

        for i in localgen(100):
            item = i

    return item

for i in range(5):
    print(call_generator())
