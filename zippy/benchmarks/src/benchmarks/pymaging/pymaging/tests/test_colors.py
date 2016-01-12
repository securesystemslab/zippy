import unittest

from pymaging.colors import Color

def ctuple(c):
    return c.red, c.green, c.blue, c.alpha

class TestColor(unittest.TestCase):
    ## constructors
    def test_constructor(self):
        c = Color(10, 20, 30)
        self.assertEqual(c.red, 10)
        self.assertEqual(c.alpha, 255)

    def test_from_pixel(self):
        c = Color.from_pixel([10, 20, 30])
        self.assertEqual(ctuple(c), (10, 20, 30, 255))

        c = Color.from_pixel([10, 20, 30, 40])
        self.assertEqual(ctuple(c), (10, 20, 30, 40))

    def test_from_hexcode(self):
        c = Color.from_hexcode('feef1510')
        self.assertEqual(ctuple(c), (254, 239, 21, 16))

        c = Color.from_hexcode('123')
        self.assertEqual(ctuple(c), (17, 34, 51, 255))
