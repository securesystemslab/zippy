# myq 06/07/2015
# sum a generator expression

genexp = (x*2 for x in range(5))

sum = 0
for i in genexp:
    sum += i
print(sum)

sum = 0
for i in genexp:
    sum += i
print(sum)
