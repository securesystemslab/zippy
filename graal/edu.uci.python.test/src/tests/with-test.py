# Qunaibit 02/05/2014
# With Statement

a = 5
              
class Sample:
    def __enter__(self):
        return self
 
    def __exit__(self, type, value, trace):
        print("type:", type)
        print("value:", value)
#         print("trace:", trace) # trace back is not supported yet
        return False
 
    def do_something(self):
        bar = 1/0
        return bar + 10

try:
    with Sample() as sample:
        a = 5
        sample.do_something()
except ZeroDivisionError:
    print("Exception has been thrown correctly")

else:
    print("This is not correct!!")    

finally:
    print("a = ", a)