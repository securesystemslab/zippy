import sys, time, os

# setup path
simplejson_path = os.path.join(os.path.dirname(__file__), 'simplejson')
# python_graph_core_path = os.path.join(python_graph_path, 'core')
sys.path.append(simplejson_path)

# sys.path.append('')

from simplejson.encoder import JSONEncoder

encoder = JSONEncoder()


def encodeList():
    lst = [i for i in range(1000)]
    # return encoder.encode(lst)
    return encoder.refactored_encode(lst)

def encodeObject():
    class Foo:
        def for_json(self):
            return {'a':1, 'b':2, 'c': [i for i in range(1000)]}

    return encoder.refactored_encode(Foo())

json = encodeList()
print(json)
json = encodeObject()
print(json)
