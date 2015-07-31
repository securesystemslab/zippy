class A:
    def __init__(self, x = 0, y = 0, z = 0):
        self.x = x
        self.y = y
        self.z = z
    
    def dist(self, other):
        return other.x
        
class B:
    def __init__(self, A_points):
        self.A_points = A_points
        
    def __call__(self, u):
        return self.A_points[0]

c = [B([A(7,8,3), A(4,2,6)]), B([A(1,11,9), A(10,5,12)])]

minx = min([a.x for b in c for a in b.A_points])
miny = min([a.y for b in c for a in b.A_points])
maxx = max([a.x for b in c for a in b.A_points])
maxy = max([a.y for b in c for a in b.A_points])

print(minx)
print(miny)
print(maxx)
print(maxy)

# for b in c:
#     length = 0
#     curr = b(0)
#     for i in range(1, 1000):
#         last = curr
#         t = 1 / 999 * i
#         curr = b(t)
#         length += curr.dist(last)