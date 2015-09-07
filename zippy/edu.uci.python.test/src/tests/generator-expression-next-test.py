# myq 06/06/2015
# generator expression testing __next__()

genexp = (x*2 for x in range(5))

genexp.__next__()
genexp.__next__()
genexp.__next__()
genexp.__next__()
print(genexp.__next__())
