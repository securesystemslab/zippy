# zwei 04/24/2014
# micro benchmark: method call polymorphic inspired by richards
import time

iteration = 50000

class Task(object):
    def __init__(self, p, w, h):
        self.packet_pending = p
        self.task_waiting = w
        self.task_holding = h

    def isTaskHoldingOrWaiting(self):
        return self.task_holding or (not self.packet_pending and self.task_waiting)

def isTaskHoldingOrWaiting(task_holding, packet_pending, task_waiting):
    return task_holding or (not packet_pending and task_waiting)    

TASK_LIST = [Task(False, False, True), 
             Task(False, True, False), 
             Task(True, True, False), 
             Task(True, False, True)]

def dostuff():
    task_list = TASK_LIST
    total = 0
    for i in range(iteration):
        for t in task_list:
            if (t.isTaskHoldingOrWaiting()):
                total += 1

    return total

def noObjectDoStuff():
    p = True
    w = False
    h = True
    total = 0
    for i in range(iteration):
        h = isTaskHoldingOrWaiting(h, p, w)
        if (isTaskHoldingOrWaiting(h, p, w)):
            total += 1

    return total    

def measure(num):
    print("Start timing...")
    start = time.time()

    for i in range(num): # 50000
      result = dostuff()

    print(result)
    duration = "%.3f\n" % (time.time() - start)
    print("boolean-logic: " + duration)

# warm up
for i in range(500):
    dostuff()

measure(1000)