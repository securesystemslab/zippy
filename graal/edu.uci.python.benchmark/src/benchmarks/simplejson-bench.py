import sys, time, os

# setup path
simplejson_path = os.path.join(os.path.dirname(__file__), 'simplejson')
# python_graph_core_path = os.path.join(python_graph_path, 'core')
sys.path.append(simplejson_path)

# sys.path.append('')

from simplejson.encoder import JSONEncoder

encoder = JSONEncoder()

def produceData():
    lst = [i for i in range(3000)]
    return lst

DATA = produceData()

def encodeList(n):
    for i in range(n):
        json = encoder.refactored_encode(DATA)

    return json

def encodeObject():
    class Foo:
        def for_json(self):
            return {'a':1, 'b':2, 'c': [i for i in range(3000)]}

    return encoder.refactored_encode(Foo())

def measure():
    print("Start timing...")
    start = time.time()
    json = encodeList(num)
    duration = "%.3f\n" % (time.time() - start)
    # print(json)
    print("simplejson-encode: " + duration)

# warm up
num =  int(sys.argv[1]) # 200
for i in range(100):
    json = encodeList(100)

measure()

