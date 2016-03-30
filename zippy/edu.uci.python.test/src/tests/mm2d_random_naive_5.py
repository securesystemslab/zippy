
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

# N = int(sys.argv[1])
N = 5

X = [[random.randint(0,1000) for i in range(N)] for j in range(N)]
Y = [[random.randint(0,1000) for i in range(N)] for j in range(N)]
result = [[0 for i in range(N)] for j in range(N)]

start = time.time()

# iterate through rows of X
for i in range(len(X)):
   # iterate through columns of Y
   for j in range(len(Y[0])):
       # iterate through rows of Y
       for k in range(len(Y)):
           result[i][j] += X[i][k] * Y[k][j]


duration = "N: %d  Time: %.5f" % (N, (time.time() - start))
# print("Naive " + duration)


"""
for r in result:
   print(r)
"""
