# dictionary
d = {'zero' : 0, 'one' : 1, 'two' : 2, 'three' : 3, 'four' : 4}

# list
l = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

# tuple
t = (22, 44, 66)

# store
d['two'] = 22
l[0] = '00'
l[1:8:2] = (11, 33, 55, 77)
l[8:10] = (88, 99)
l[2:7:2] = t

# load
print d
print d['one']
print l
print l[0]
print l[1:5]
print l[0:10:2]
print t
print t[0:2]
print t[2:4:3]
