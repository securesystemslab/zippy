# zwei 12/09/2013
# bimorphic attribute access inline caching

class Task:
	def __init__(self):
		pass

class TaskA(Task):
	def __init__(self, a):
		self.a = a

class TaskB(Task):
	def __init__(self, a):
		self.a = a

def doGetAttribute(tasks):
	for i in range(2):
		for t in tasks:
			print(t.a)

doGetAttribute([TaskA(42), TaskB(24)])