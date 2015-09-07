# zwei 06/04/2014
# __getitem__ generator
class graph:
    def __init__(self):
        self.neighbors = [1,2,3,4,5]

    def __getitem__(self, node):
        for i in self.neighbors:
            yield i

def callgetitem():
    for i in g[1]:
        result = i
    return result

g = graph()
for n in range(1000):
    result = callgetitem()
print(result)
