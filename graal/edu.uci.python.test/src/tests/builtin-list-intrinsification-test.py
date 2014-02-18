# zwei 02/11/14
# builtin intrinsification test for list comp

NUMS = [
    [ 8, 2,22,97,],
    [49,49,99,40,],
    [81,49,31,73,],
    [52,70,95,23,],
]

def seqs(num):
    ll = list(i for i in range(num))
    num = len(ll)
    return list(i for i in range(num))

for i in range(5000):
    seqs(10)

print(seqs(10)[-1])

