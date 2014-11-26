import unittest

from pymaging.affine import AffineTransform


class TestAffineTransform(unittest.TestCase):
    ## constructors
    def test_constructor_identity(self):
        a = AffineTransform()
        self.assertEqual(a.matrix, (1, 0, 0, 0, 1, 0, 0, 0, 1))

    def test_constructor_9tuple(self):
        a = AffineTransform((1, 2, 3, 4, 5, 6, 7, 8, 9))
        self.assertEqual(a.matrix, (1, 2, 3, 4, 5, 6, 7, 8, 9))

        self.assertRaises(ValueError, AffineTransform, (1, 2, 3, 4, 5, 6, 7, 8))

    def test_constructor_3x3tuple(self):
        a = AffineTransform(((1, 2, 3), (4, 5, 6), (7, 8, 9)))
        self.assertEqual(a.matrix, (1, 2, 3, 4, 5, 6, 7, 8, 9))

        self.assertRaises(ValueError, AffineTransform, ((1, 2, 3), (4, 5, 6)))
        self.assertRaises(ValueError, AffineTransform, ((1, 2), (3, 4), (5, 6)))

    def test_inverse(self):
        # identity inverse
        self.assertEqual(AffineTransform().inverse(), AffineTransform())

        self.assertEqual(
            AffineTransform((2, 0, 0, 0, 2, 0, 0, 0, 2)).inverse(),
            AffineTransform((0.5, 0, 0, 0, 0.5, 0, 0, 0, 0.5))
        )

        # inverted A*0 = A/0, which is an error
        self.assertRaises(ValueError, (AffineTransform() * 0).inverse)

    # binary operators
    def test_mult_scalar(self):
        a = AffineTransform() * 2
        self.assertEqual(a.matrix, (2, 0, 0, 0, 2, 0, 0, 0, 2))

        a = AffineTransform() * -1
        self.assertEqual(a.matrix, (-1, 0, 0, 0, -1, 0, 0, 0, -1))

    def test_rmult_scalar(self):
        a = 2 * AffineTransform()
        self.assertEqual(a.matrix, (2, 0, 0, 0, 2, 0, 0, 0, 2))

        a = -1 * AffineTransform()
        self.assertEqual(a.matrix, (-1, 0, 0, 0, -1, 0, 0, 0, -1))

    def test_div_scalar(self):
        a = AffineTransform() / 2
        self.assertEqual(a.matrix, (0.5, 0, 0, 0, 0.5, 0, 0, 0, 0.5))

    def test_mult_affine(self):
        a = AffineTransform() * 2
        b = AffineTransform() * 3
        self.assertEqual((a * b).matrix, (6, 0, 0, 0, 6, 0, 0, 0, 6))

    def test_div_affine(self):
        a = AffineTransform() * 8
        b = AffineTransform() * 2
        self.assertEqual((a / b).matrix, (4, 0, 0, 0, 4, 0, 0, 0, 4))

    def test_mult_vector(self):
        a = AffineTransform() * 2
        self.assertEqual(a * (1, 2), (2, 4))
        self.assertEqual(a * (1, 2, 3), (2, 4, 6))

        self.assertRaises(ValueError, lambda: a * (1,))
        self.assertRaises(ValueError, lambda: a * (1, 2, 3, 4))
        self.assertRaises(ValueError, lambda: a * (1, 2, 3, 4))

        self.assertRaises(TypeError, lambda: (1, 2, 3) * a)

    # simple transformations
    def test_translate(self):
        a = AffineTransform()
        t = a.translate(3, 4)
        self.assertEqual(t.matrix, (1, 0, 0, 0, 1, 0, 3, 4, 1))
        self.assertEqual(t.translate(3, 4).matrix, (1, 0, 0, 0, 1, 0, 6, 8, 1))

    def test_rotate(self):
        a = AffineTransform()
        self.assertEqual(a.rotate(0), a)
        self.assertEqual(a.rotate(360), a)

        self.assertEqual(a.rotate(90), a.rotate(270, clockwise=True))

        self.assertEqual(a.rotate(90), AffineTransform((0, -1, 0, 1, 0, 0, 0, 0, 1)))

    # some chain multiplications
    def test_chained_translate_rotate(self):
        a = AffineTransform()
        # translate by (3, 4). rotate by 90 degrees
        t = a.translate(3, 4).rotate(90)
        self.assertEqual(t.matrix, (0, -1, 0, 1, 0, 0, 4, -3, 1))

    def test_chained_translate_rotate_translate(self):
        a = AffineTransform()
        # translate by (3, 4). rotate by 90 degrees. then translate back.
        t = a.translate(3, 4).rotate(90).translate(-3, -4)
        self.assertEqual(t.matrix, (0, -1, 0, 1, 0, 0, 1, -7, 1))
