# zwei 04/15/2014
# bimorphic call to functions that are local variables

def funcA():
	print("do stuff A")

def funcB():
	print("do stuff B")

def doCall(funcs):
	for t in funcs:
		t()

doCall([funcA, funcB])