# global var test

x = 10
def foo():
    global x;
    x = 20;
    print (x)

foo();
print(x)
