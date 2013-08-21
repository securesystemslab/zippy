# multiple assignment
a, b = [3, 4]
a, b = b, a
print(a, b)

# explicit tuple assignment
(a, b) = [1, 2]
print(a, b)

# explicit list assignment
list_l = [7, 8]
[a, b] = list_l
print(a, b)

# nested target list
(a, b), [c, d] = [[1, 2], [3, 4]]
print(a, b, c, d)
