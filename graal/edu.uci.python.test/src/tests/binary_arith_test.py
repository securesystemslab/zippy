# basic binary ops
print ("----------- basic + - * / //")
a = 1 + 2
b = 4 - 2
c = 6 * 2
d = 7 / 2
e = 7 // 2
print (a, b, c, d, e)

# slightly more complex ones
print ("----------- double/BigInteger + - * / //")
a = 345606 + 364 * 2
print a
b = 1101010101 / 356 - 2002
c = 42 - 99999 * 543858438584385
d = 1 / 356 * 2.0
e = 3 // 5.0
print (a, b, c, d, e)

# more complicated ones
print ("----------- more double/BigInteger / // **")
a = 46372573068954628579 / 432
b = 46372573068954628579 / 43.2
c = (3.56 - 5278948673290672067) // 6427069
d = 2 ** 4
e = 2.5 ** 3.0
print (a, b, c, d, e)

# modulo test
print ("----------- %")
a = 14 % 5
b = 54528840284285205820 % 52
c = 43253252 % 0.7
print (a, b, c)
