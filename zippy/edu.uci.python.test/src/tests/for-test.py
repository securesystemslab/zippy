# ankitv 10/10/13
# Iterating by Sequence Index
fruits = ['banana', 'apple',  'mango']
for index in range(len(fruits)):
    print ('Current fruit :' , fruits[index])

#The else Statement Used with Loops
for num in range(10,15):  #to iterate between 10 to 20
    for i in range(2,num): #to iterate on the factors of the number
        if num%i == 0:      #to determine the first factor
            j=num/i          #to calculate the second factor
            print ('%d = %d * %d' % (num,i,j))
            break #to move to the next number, the #first FOR
    else:                  # else part of the loop
        print (num, 'prime number')

