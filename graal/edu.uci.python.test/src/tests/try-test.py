def divide(x, y):
    try:
        result = x / y
        raise KeyboardInterrupt
    except ZeroDivisionError:
        print("division by zero!")
    except KeyboardInterrupt:
        print("KeyboardInterrupt!")
    else:
        print("result is ", result)
    finally:
        print("executing finally clause")
          
divide(1,1)