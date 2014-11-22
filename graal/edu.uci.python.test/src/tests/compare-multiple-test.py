a = 10

def foo():
	global a
	a = a + 1
	return a
	
if(11 == 12 == foo() == 11):
	print("That's not good");

print("a =", a);
	
