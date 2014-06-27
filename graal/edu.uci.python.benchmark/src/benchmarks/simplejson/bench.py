__author__ = 'zwei'

from simplejson.encoder import JSONEncoder

encoder = JSONEncoder(for_json=True)


def encodeList():
    lst = [i for i in range(1000)]
    return encoder.encode(lst)

def encodeObject():
    class Foo:
        def for_json(self):
            return {'a':1, 'b':2, 'c': [i for i in range(1000)]}

    return encoder.encode(Foo())

json = encodeList()
print(json)
json = encodeObject()
print(json)