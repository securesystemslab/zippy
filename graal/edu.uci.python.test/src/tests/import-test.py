import importme
import sys

def foo():
    print("local foo()")

importme.foo()
foo()

variable = "local variable"
print(variable)
print(importme.variable)