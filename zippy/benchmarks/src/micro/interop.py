evalFile("application/x-ruby","./zippy/benchmarks/src/micro/add.ruby")
binadd = importSymbol('binadd')

def add_wrapper() :
  print(binadd (2, 3))

add_wrapper()

