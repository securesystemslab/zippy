# zwei 01/03/14
# generator expression inlining

def call_generator_builtin(num, iteration):
    item = 0
    for t in range(iteration):
        num += t % 5
        ll = list(x * 2 for x in range(num))
        item = ll[-1] + item % 5

    return item

for i in range(1000):
    result = call_generator_builtin(10, 100)
print(result)
