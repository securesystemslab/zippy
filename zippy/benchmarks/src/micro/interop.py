# Ruby
evalFile("application/x-ruby","./zippy/benchmarks/src/micro/add.ruby")
binadd = importSymbol('binadd')

def add_wrapper() :
  print("Result `2 + 3` from Ruby: %d" % binadd (2, 3))

add_wrapper()

# R
evalFile("application/x-r","./zippy/benchmarks/src/micro/add.r")
binadd = importSymbol('binadd')

def add_wrapper() :
  print("Result `2 + 3` from R: %d" % binadd (2, 3))

add_wrapper()

# SL
evalFile("application/x-sl","./zippy/benchmarks/src/micro/add.sl")
binadd = importSymbol('binadd')

def add_wrapper() :
  print("Result `2 + 3` from SL: %d" % binadd (2, 3))

add_wrapper()

