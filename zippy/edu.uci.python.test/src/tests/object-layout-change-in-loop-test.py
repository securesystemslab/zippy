# zwei 04/18/2015
# bimorphic call to functions that are local variables

class Foo:
	def __init__(self, a):
		self.a = a
	def mod(self, b):
		self.b = b

res = 0
for i in range(10):
	f = Foo(i)
	f.mod(res)
	res += f.a + f.b

print(res)