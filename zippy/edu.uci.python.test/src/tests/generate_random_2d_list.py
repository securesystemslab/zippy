
import sys, random

if(len(sys.argv) == 1):
    print('<N> <max randint> <var name>')

N = int(sys.argv[1])

X = [[random.randint(0,int(sys.argv[2])) for i in range(N)] for j in range(N)]

f = open(sys.argv[3] + ".py", "w")
f.write(sys.argv[3] + " = ")
strX = str(X)
f.write(strX.replace("], [","],\n ["))
f.close()
