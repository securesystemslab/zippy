# test builtin abs
x = abs(10)
print(x)

x = abs(10.25)
print(x)

x = abs(1 + 2j)
print(x)

# test builtin chr
x = chr(65)
print(x)

# test builtin complex
x = complex(2, 3)
print(x)

x = complex(3.4, 4.9)
print(x)

x = complex(2)
print(x)

x = complex()
print(x)

# test builtin float
x = float(2)
print(x)

x = float('+1.23')
print(x)

x = float('   -12345\n')
print(x)

x = float('1e-003')
print(x)

x = float('+1E6')
print(x)

x = float('-Infinity')
print(x)

x = float()
print(x)

# test builtin int
x = int(3)
print(x)

x = int(2.9)
print(x)

x = int("4")
print(x)

x = int(2147483648)
print(x)

x = int()
print(x)

# test builtin len
value = "hello"
print(len(value))

value = (100, 200, 300)
print(len(value))

value = ['a', 'b', 'c', 'd']
print(len(value))

value = {'id' : 17, 'name' : "gulfem"}
print(len(value))


# test builtin range
print(list(range(10))) # [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

print(list(range(1, 11))) # [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

print(list(range(0, 30, 5))) # [0, 5, 10, 15, 20, 25]
     
     
# test builtin iter and next
str = "hello"
it = iter(str)
print it.next()
print it.next()