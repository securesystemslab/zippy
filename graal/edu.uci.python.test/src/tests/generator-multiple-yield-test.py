# zwei 01/17/2014
# generator with multiple yields
def gen():
    a = 1
    b = 4
    yield a
    while(b > a):
        yield b - a
        b -= 1

for i in gen():
    print(i)
