# zwei 04/04/2014
# micro benchmark: attribute access
import time

iteration = 50000 # 50000

class TaskState(object):
    def __init__(self):
        self.packet_pending = True
        self.task_waiting = False
        self.task_holding = False

    def packetPending(self):
        self.packet_pending = True
        self.task_waiting = False
        self.task_holding = False
        return self

    def waiting(self):
        self.packet_pending = False
        self.task_waiting = True
        self.task_holding = False
        return self

    def running(self):
        self.packet_pending = False
        self.task_waiting = False
        self.task_holding = False
        return self

def dostuff(task):
	for i in range(iteration):
		task = TaskState() 
		task.waiting()
		task.running()

	return task.task_holding

def measure(num):
	print("Start timing...")
	start = time.time()

	for i in range(num): # 50000
	  result = dostuff(TaskState())

	print(result)
	duration = "%.3f\n" % (time.time() - start)
	print("attribute-bool: " + duration)

# warm up
for i in range(2000):
	dostuff(TaskState())

measure(5000)