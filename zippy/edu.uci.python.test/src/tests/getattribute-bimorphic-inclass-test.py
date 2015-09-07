# zwei 12/09/2013
# bimorphic attribute access inline caching

class Task:
	pass

class TaskA(Task):
	a = 42

class TaskB(Task):
	a = 24

def doGetAttribute(tasks):
	for i in range(2):
		for t in tasks:
			print(t.a)

doGetAttribute([TaskA, TaskB])