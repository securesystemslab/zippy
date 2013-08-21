# default arguments test
foo = 1

def bar(f=foo):
    print(f)

bar()
foo = 2
bar(4)
