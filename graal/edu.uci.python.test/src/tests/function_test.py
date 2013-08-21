# simple function definition test
print("----------------- simple func def")
def main():
    bit = 128
    byte_acc = 0

    print(bit)
    print(byte_acc)

main()

print("----------------- slightly more complicated func def")
def add_two(a, b):
    return a + b

temp = 10

while temp > 0:
    temp = add_two(temp, -1)
    print(temp)
