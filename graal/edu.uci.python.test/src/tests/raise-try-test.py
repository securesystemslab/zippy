# Qunaibit 10/10/2013
# Raise Exceptions
def divide(x, y):
    try:
        result = x / y
        raise KeyboardInterrupt
    except ZeroDivisionError as err:
        print("division by zero!",err)
    except KeyboardInterrupt as k:
        print("KeyboardInterrupt!", k)
    else:
        print("result is ", result)
    finally:
        print("executing finally clause")
          
divide(1,1)
