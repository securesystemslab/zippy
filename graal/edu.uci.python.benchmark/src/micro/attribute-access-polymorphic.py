# zwei 04/24/2014
# micro benchmark: attribute access polymorphic inspired by richards
import time

iteration = 50000 # 50000

class TaskState(object):
	pass

class Task(TaskState):
	def __init__(self, foo):
		self.foo = foo

Task(0)

class DeviceTask(Task):
	def __init__(self, foo):
		Task.__init__(self, foo)

class HandlerTask(Task):
	def __init__(self, foo):
		Task.__init__(self, foo)

class IdleTask(Task):
	def __init__(self, foo):
		Task.__init__(self, foo)

class WorkTask(Task):
	def __init__(self, foo):
		Task.__init__(self, foo)

TASK_LIST = [Task(0), DeviceTask(1), HandlerTask(2), IdleTask(3), WorkTask(4)]

def dostuff():
	task_list = TASK_LIST
	total = 0
	for i in range(iteration):
		for t in task_list:
			total = (total + t.foo) % 7

	return total

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in range(num): # 50000
	  result = dostuff()

	print(result)
	duration = "%.3f\n" % (time.time() - start)
	print("attribute-access-polymorphic: " + duration)

# warm up
for i in range(500):
	dostuff()

measure(1000)