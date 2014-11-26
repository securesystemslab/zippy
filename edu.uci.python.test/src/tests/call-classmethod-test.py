# zwei 06/11/2015
# classmethod calls

class Strength(object):

    def __init__(self, strength, name):
        self.strength = strength
        self.name = name

    @classmethod
    def stronger(cls, s1, s2):
        return s1.strength < s2.strength

    @classmethod
    def weaker(cls, s1, s2):
        return s1.strength > s2.strength

s1 = Strength(1, 'cat')
s2 = Strength(2, 'dog')
stgr = Strength.stronger
print(stgr(s1, s2))
print(Strength.stronger(s1, s2))
print(Strength.weaker(s1, s2))
print(s1.stronger(s1, s2))
print(s1.weaker(s1, s2))