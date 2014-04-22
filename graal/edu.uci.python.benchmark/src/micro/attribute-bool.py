# zwei 04/21/2014
# micro benchmark: boolean attribute access
import time

iteration = 500000

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

def dostuff():
    task = TaskState()
    
    for i in range(iteration):
        task.waiting()
        task.running()

        if task.task_waiting: task.task_holding = False

    return task.task_holding

def measure(num):
    print("Start timing...")
    start = time.time()

    for i in range(num):
      result = dostuff()

    print(result)
    duration = "%.3f\n" % (time.time() - start)
    print("attribute-bool: " + duration)

# warm up
for i in range(2000):
    dostuff()

measure(5000)