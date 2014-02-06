# Qunaibit 02/05/2014
# With Statement

a = 5;
 
class Sample:
    def __enter__(self):
        print("In __enter__()")
        return self
 
    def __exit__(self, type, value, trace):
        print("In __exit__()");
        return 5;
 
    def do_something(self, x):
#         raise KeyboardInterrupt
        return "Foo"
 
 
def get_sample():
    return Sample()
 
 
with get_sample() as sample:
    print("sample:", sample.do_something(a))
    a = 1;
print("sample:", sample.do_something(a))

print (a);
                
# class Sample:
#     def __enter__(self):
#         return self
# 
#     def __exit__(self, type, value, trace):
#         print("type:", type)
#         print("value:", value)
#         print("trace:", trace)
# 
#     def do_something(self):
#         bar = 1/0
#         return bar + 10
# 
# with Sample() as sample:
#     sample.do_something()
    
