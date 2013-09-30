# zwei 09/27/2013
# simple class test

class Foo:
    def __init__(self, num):
        self.num = num;

    def showNum(self):
        print(self.num)

foo = Foo(42)
foo.showNum();
