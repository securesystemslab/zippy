
"""
# 3x3 matrix
X = [[12,7,3],
    [4 ,5,6],
    [7 ,8,9]]
# 3x4 matrix
Y = [[5,8,1,2],
    [6,7,3,0],
    [4,5,9,1]]
# result is 3x4
result = [[0,0,0,0],
         [0,0,0,0],
         [0,0,0,0]]
"""
import sys, time, random

N = 5

X = [[476, 95, 637, 471, 964], [614, 209, 585, 522, 496], [453, 203, 895, 240, 83], [744, 472, 661, 233, 94], [965, 440, 610, 685, 251]]
Y = [[666, 824, 682, 342, 709], [924, 366, 365, 151, 613], [588, 13, 556, 666, 303], [354, 377, 806, 832, 438], [458, 266, 128, 377, 328]]
result = [[0 for i in range(N)] for j in range(N)]

# iterate through rows of X
for i in range(len(X)):
   # iterate through columns of Y
   for j in range(len(Y[0])):
       # iterate through rows of Y
       for k in range(len(Y)):
           result[i][j] += X[i][k] * Y[k][j]


print("result = ", result)


"""
for r in result:
   print(r)
"""
