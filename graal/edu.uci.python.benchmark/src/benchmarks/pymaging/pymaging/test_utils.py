# -*- coding: utf-8 -*-
import unittest
import array
from pymaging.colors import ColorType
from pymaging.image import Image
from pymaging.pixelarray import get_pixel_array

def pixel_array_factory(colors, alpha=True):
    height = len(colors)
    width = len(colors[0]) if height else 0
    pixel_size = 4 if alpha else 3
    pixel_array = get_pixel_array(array.array('i', [0] * width * height * pixel_size), width, height, pixel_size)
    for y in range(height):
        for x in range(width):
            pixel_array.set(x, y, colors[y][x].to_pixel(pixel_size))
    return pixel_array

def image_factory(colors, alpha=True):
    height = len(colors)
    width = len(colors[0]) if height else 0
    pixel_size = 4 if alpha else 3
    pixel_array = pixel_array_factory(colors, alpha)
    def loader():
        return pixel_array, None
    return Image(ColorType(pixel_size, alpha), width, height, loader)


if hasattr(unittest.TestCase, 'assertIsInstance'):
    class _Compat: pass
else:
    class _Compat:
        def assertIsInstance(self, obj, cls, msg=None):
            if not isinstance(obj, cls):
                standardMsg = '%s is not an instance of %r' % (safe_repr(obj), cls)
                self.fail(self._formatMessage(msg, standardMsg))


class PymagingBaseTestCase(unittest.TestCase, _Compat):
    def assertImage(self, img, colors, alpha=True):
        check = image_factory(colors, alpha)
        self.assertEqual(img.pixels, check.pixels)

