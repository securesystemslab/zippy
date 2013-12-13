# test builtin all
x = all([10, 0, 30])
print(x)

# test builtin any
x = any([0, 10, 30])
print(x)

# test builtin abs
x = abs(10)
print(x)

x = abs(10.25)
print(x)

x = abs(1 + 2j)
print(x)

# test builtin bool
x = bool(10)
print(x)

x = bool(0.0)
print(x)

x = bool()
print(x)

# test builtin callable
def foo():
    m = 20;

x = callable(foo)
print(x)

x = callable(int)
print(x)

x = callable(10)
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

# test builtin enumerate
list1 = [1000, 2000, 3000]
for s in enumerate(list1):
    print(s)

# Currently removed because each element should be printed  as
#(0, 'Spring')
#(1, 'Summer')
#(2, 'Fall')
#(3, 'Winter')    
# Ours do not print the quotation marks
#seasons = ['Spring', 'Summer', 'Fall', 'Winter']
#for s in enumerate(seasons):
    #print (s)

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

#x = float('-Infinity')
#print(x)

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

# test builtin max
x = max(10, 20)
print(x)

x = max(20.8, 10.3)
print(x)

#x = max([20, 10, 90])
#print(x)

# test builtin range
print(list(range(10))) # [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

print(list(range(1, 11))) # [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

print(list(range(0, 30, 5))) # [0, 5, 10, 15, 20, 25]
     

# test builtin zip
for s in zip(["ABC", "123"]):
    print(s)     
     
# test builtin iter
for element in iter("hello"):
    print(element)
    
#for element in iter([10, 20, 30]):
#    print(element)
    
#for element in iter({"a", "b", "c"}):
#    print(element)

# test builtin next
x = iter([10, 20, 30])
print(next(x))
print(next(x))
print(next(x))

# test isintance
class Student:
    def __init__(self, id):
        self.id = id

student = Student(12)
x = isinstance(student, Student)
print(x)
