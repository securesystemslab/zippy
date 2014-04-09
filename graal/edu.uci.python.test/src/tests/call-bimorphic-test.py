# zwei 12/09/2013
# bimorphic call to test attribute access inline caching

class Task:
	def doStuff(self):
		pass

class TaskA(Task):
	def doStuff(self):
		print("do stuff A")

class TaskB(Task):
	def doStuff(self):
		print("do stuff B")

def doCall(tasks):
	for t in tasks:
		t.doStuff()

doCall([TaskA(), TaskB()])