# basic subscription
print "--------------------- basic laoding subscription with index and slice"
a = [0, 1, 2, 3, 4]
b = 1
print a[b]
print a[1:3:1]


print "--------------------- basic storing subscription with index and slice"
a = [0, 1, 2, 3, 4]
a[1] = 2
a[1:4:2] = (8, 9)
print a
