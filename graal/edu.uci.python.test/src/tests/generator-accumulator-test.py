# zwei 12/16/2013
# accumulate generator

def group_iter(iterator, n):
        # print(iterator)
        accumulator = []
        for item in iterator:
            accumulator.append(item)
            if len(accumulator) == n:
                yield accumulator
                accumulator = []
        if len(accumulator) != 0:
            yield accumulator

ll = ["w", "c", "g", "h", "z"]
for i in group_iter(ll, 3):
    print(i)
