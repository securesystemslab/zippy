#edited by Ankit Verma

# simple function definition test

def main():
    bit = 128
    byte_acc = 0

    print(bit)
    print(byte_acc)

main()

def add_two(a, b):
    return a + b

temp = 10

while temp > 0:
    temp = add_two(temp, -1)
    print(temp)
