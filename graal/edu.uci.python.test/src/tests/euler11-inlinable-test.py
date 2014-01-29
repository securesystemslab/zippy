# zwei 12/31/13
# simplified euler11 for debugging

NUMS = [
    [ 8, 2,22,97,],
    [49,49,99,40,],
    [81,49,31,73,],
    [52,70,95,23,],
]

def seqs(nums, row, col):
    if row + 4 <= len(nums):                                yield list(nums[i][col] for i in range(row, row+4))
    if col + 4 <= len(nums[row]):                           yield list(nums[row][i] for i in range(col, col+4))
    if row + 4 <= len(nums) and col + 4 <= len(nums[row]):  yield list(nums[row+i][col+i] for i in range(0,4))
    if row + 4 <= len(nums) and col >= 3:                   yield list(nums[row+i][col-i] for i in range(0,4))

def product(seq):
    # print('seq ', seq)
    n = 1
    for x in seq:
        n = n * x
        # print('n', n)
    return n

def list_seqs(nums):
    for row in range(2):
        for col in range(2):
            # print('row ', row, ' col ', col)
            for seq in seqs(nums, row, col):
                # print('list_seqs ', seq)
                yield seq

def solve():
    # ll = [product(seq) for seq in list_seqs(NUMS)]
    # print('solved list ', ll)
    return _max(product(seq) for seq in list_seqs(NUMS))

# test
def _max(iterator):
    max = None
    for i in iterator:
        if max is None:
            max = i
        if i > max:
            max = i
    return max

for i in range(5000):
    solve()

print(solve())

