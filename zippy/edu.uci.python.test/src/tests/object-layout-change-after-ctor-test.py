# zwei 04/15/2015
# bimorphic call to functions that are local variables

class Foo:
	def __init__(self, n):
		self.n = n

f = Foo(42)
print(f.n)
f.y = 43
print(f.y)