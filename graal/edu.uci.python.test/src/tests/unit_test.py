import unittest

class ArihtmeticTests(unittest.TestCase):

    def testAddition(self):
        self.assertEquals(3 + 4, 7, "3 + 4 not equal to 7")

    def testMultiplication(self):
        self.assertEqual(3 * 4, 12, "3 * 4 not equal to 12")
            
    def testSubtraction(self):
        self.assertEqual(3 - 4, -1, "3 - 4 not equal to -1")
      
    def testSyntax(self):
        self.assertRaises(SyntaxError, compile, "lambda x: x = 2", '<test string>', 'exec')
    
    
class ComparisonTests(unittest.TestCase):
    
    def testLessThan(self):
        self.assertTrue(3 < 4, "3 < 4 is not true")
         
    def testGreaterThan(self):
        self.assertTrue(4 > 3, "4 > 3 is not true")
#             
    def testGreaterThanOrEqual(self):
        self.assertTrue(3 >= 4, "3 >= 4 is not true")
    
      
  
if __name__ == '__main__':
    unittest.main()
    