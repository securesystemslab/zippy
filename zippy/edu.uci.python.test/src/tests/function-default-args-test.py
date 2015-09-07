# zwei 05/16/14
# default args

foo = 1
bars = []
for i in range(2):
    def bar(f=foo):
        print(f)
    bars.append(bar)
    foo += 1

bars[0]()
foo = 2
bars[1]()

