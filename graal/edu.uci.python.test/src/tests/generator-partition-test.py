# zwei 06/16/2014
# inspired by sympy.utilities.iterables.kbins.partition()

def partition(lista, bins):
    #  EnricoGiampieri's partition generator from
    #  http://stackoverflow.com/questions/13131491/
    #  partition-n-items-into-k-bins-in-python-lazily
    if len(lista) == 1 or bins == 1:
        yield [lista]
    elif len(lista) > 1 and bins > 1:
        for i in range(1, len(lista)):
            for part in partition(lista[i:], bins - 1):
                if len([lista[:i]] + part) == bins:
                    yield [lista[:i]] + part

for i in partition(range(3), 3):
    print(i)
