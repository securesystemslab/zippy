# zwei 06/13/2014
# generator inline


def generator(n):
    for i in range(n):
        yield i

def call_generator():
    gens = [GenA(), GenB()]
    for gen in gens:
        for i in gen.all():
            item = i

    return item

class GenA:
    def __init__(self):
        self.ids = [1,2,3,4,5]
    def all(self):
        for i in self.ids:
            yield i

class GenB(GenA):
    def __init__(self):
        self.ids = [6,7,8,9,10]
    def all(self):
        for i in self.ids:
            yield i

for i in range(5):
    print(call_generator())
