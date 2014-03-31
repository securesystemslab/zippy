# simple generator
def gengen(a):
    a = a + a
    if a:
        yield a
    a = a + a
    if a:
        yield a

for i in gengen(3):
    print(i)

# generator with loop
def loopgen(x):
    for i in range(x):
        if i >= x // 2:
            yield i

for i in loopgen(5):
    print(i)
