# zwei 04/08/2014
# Check get attribute cache with a chain of class checks.

class Task:
	def doStuff(self):
		print("do stuff")

class TaskA(Task):
	pass

class TaskB(TaskA):
	pass

def doCall(tasks):
	for t in tasks:
		t.doStuff()

doCall([TaskB (), TaskB()])