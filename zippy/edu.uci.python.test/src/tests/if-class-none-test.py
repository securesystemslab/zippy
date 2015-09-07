
class Foo:
    class_attr = 2
    def __init__(self, num, a=None):
       self.num = num
       self.a = a

class Bar(Foo):
    class_attr = 2
    def __init__(self, num):
       self.num = num


bar = Foo(42)
boo = Foo(41,bar)
foo = boo.a

if foo.a is None:
    print('a is', foo.a)
else:
    print('Error!')

if foo.a is not None:
    print('Error!')
else:
    print('a is', foo.a)

if foo.a:
    print('Error!')
else:
    print('a is', foo.a)

if not foo.a:
    print('a is', foo.a)
else:
    print('Error!')

