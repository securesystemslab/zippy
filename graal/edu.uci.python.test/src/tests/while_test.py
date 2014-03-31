# basic while

a = 1
b = 5

while a:
  print("a is true" , a)
  a = a - 1

while a < b:
  print(a, " < ", b)
  b = b - 1

a = 1
while not a:
  print(a, " is not true")

b = 5
while not a > b:
  print(a, " < ", b)
  b = b - 1
