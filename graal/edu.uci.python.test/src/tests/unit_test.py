import unittest

class MyTest(unittest.TestCase):

    def testAddition(self):
        self.assertEqual(3 + 4, 7, "3 + 4 not equal to 7")
        
    def testMultiplication(self):
        self.assertEqual(3 * 4, 12, "3 * 4 not equal to 12")
    

if __name__ == '__main__':
    unittest.main()
