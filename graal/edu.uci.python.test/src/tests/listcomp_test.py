
def basic():
    b = [ 2*i for i in range(5)]
    print(b)

basic()

def nested():
    c = [ (x,y) for x in range(5) for y in range(4)]
    print(c)

nested()

# generator expression
def baz():
    baba = [i for i in (j for j in range(3))]
    print(baba)

baz()

def foo():
    roro = (x + y for x in range(6) for y in range(5))
    return roro

def listcomp_in_listcomp():
    ori = [[1,2,3], [2,3,4], [3,4,5]]
    nest = [[ x // 2 for x in y ] for y in ori ]
    print('nested listcomp', ori)

listcomp_in_listcomp()

def bar(ge):
    nagesha = [i for i in ge]
    print('generator expr', nagesha)

bar(foo())

yoyo = [i for i in range(5) if i % 2 is 0]
print(yoyo)
