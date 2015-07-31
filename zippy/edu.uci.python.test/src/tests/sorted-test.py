# zwei 07/03/14
# Test of a sorted() written in Python

def sorted(iterable):
    result = list(iterable)

    for passnum in range(len(result)-1,0,-1):
        for i in range(passnum):
            if result[i] > result[i+1]:
                temp = result[i]
                result[i] = result[i+1]
                result[i+1] = temp

    return result

lst = [2,1,4,3]
print(sorted(lst))
