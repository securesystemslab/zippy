a = [2 * n for n in range(900000)]
b = [3 * n for n in range(900000)]
c = [  0   for n in range(900000)]
for i in range(len(a)):
  c[i] = a[i]*b[i]
# print(c)";
