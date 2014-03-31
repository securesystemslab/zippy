# basic bitwise ops
print ("----------- << >>")
a = 1 << 3
b = 8 >> 2
c = 1 << 129
d = 8 >> 20
e = -1 << 8
f = -20 >> 12
print (a, b, c, d, e, f)

# more bitwise ops
print ("----------- & | ^")
a = 32 & 8
b = 432 | 9
c = 425 ^ 54
print (a, b, c)

# more bitwise ops
print ("----------- & | ^ w/ BitInteger")
a = 32 & 8484324820482048
b = 432 | 943824320482304820
c = 425 ^ 544382094820482034324242
print (a, b, c)
