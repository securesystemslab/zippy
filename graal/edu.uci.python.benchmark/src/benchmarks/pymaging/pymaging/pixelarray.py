# -*- coding: utf-8 -*-
import array
# import copy


class GenericPixelArray(object):
    def __init__(self, data, width, height, pixelsize):
        self.data = data
        self.width = width
        self.height = height
        self.pixelsize = pixelsize
        self._precalculate()

    def __repr__(self):
        nice_pixels = '\n'.join([' '.join(['%%%dd' % self.pixelsize % x for x in self.data[i * self.line_length:(i + 1) * self.line_length]]) for i in range(self.height)])
        return '\n%s\n(width=%s, height=%s, pixelsize=%s)' % (nice_pixels, self.width, self.height, self.pixelsize)

    def __eq__(self, other):
        """
        Mostly used for testing
        """
        return self.data == other.data and self.width == other.width and self.height == other.height and self.pixelsize == other.pixelsize

    def _precalculate(self):
        """
        Precalculate some values, this must be called whenever self.width, self.height or self.pixelsize is changed.
        """
        self.line_length = self.width * self.pixelsize
        self.size = self.line_length * self.height

    def _translate(self, x, y):
        """
        Translates a x/y coordinate into the start index.
        """
        return (y * self.line_length) + (x * self.pixelsize)

    def get(self, x, y):
        """
        Returns the pixel (a tuple of length `self.pixelsize`) from the pixel array at x/y
        """
        start = self._translate(x, y)
        return [self.data[start+i] for i in range(self.pixelsize)]

    def set(self, x, y, pixel):
        """
        Sets the pixel (a tuple of length `self.pixelsize`) to the pixel array at x/y
        """
        start = self._translate(x, y)
        for i in range(self.pixelsize):
            self.data[start + i] = pixel[i]

    def copy_flipped_top_bottom(self):
        """
        Flip the lines from top to bottom into a new copy of this pixel array
        """
        newarr = array.array('B', [0]) *  self.size
        for i in range(self.height):
            dst_start = i * self.line_length
            dst_end = dst_start + self.line_length
            src_start = ((self.height - i) * self.line_length) - self.line_length
            src_end = src_start + self.line_length
            newarr[dst_start:dst_end] = self.data[src_start:src_end]
        return get_pixel_array(newarr, self.width, self.height, self.pixelsize)

    def copy_flipped_left_right(self):
        """
        Flip the lines from left to right into a new copy of this pixel array
        """
        new_pixel_array = get_pixel_array(array.array('B', [0]) *  self.size, 
                                          self.width, self.height, self.pixelsize)
        for y in range(self.height):
            for dst_x in range(self.width):
                src_x = self.width - dst_x - 1
                new_pixel_array.set(dst_x, y, self.get(src_x, y))
        return new_pixel_array

    def copy(self):
        return get_pixel_array(self.data, self.width, self.height, self.pixelsize)

    def remove_lines(self, offset, amount):
        """
        Removes `amount` lines from the pixel array starting at line `offset`.
        """
        if not amount:
            return
        start = self.line_length * offset
        end = start + (amount * self.line_length)
        self.height -= amount
        del self.data[start:end]
        self._precalculate()

    def remove_columns(self, offset, amount):
        """
        Removes `amount` columns from the pixel array starting at column `offset`.
        """
        if not amount:
            return
        start = offset * self.pixelsize
        end = start + (amount * self.pixelsize)
        # reversed is used because otherwise line_start would be all messed up.
        for i in reversed(range(self.height)):
            line_start = i * self.line_length
            del self.data[line_start+start:line_start+end]
        self.width -= amount
        self._precalculate()

    def add_lines(self, offset, amount, fill=0):
        """
        Adds `amount` lines to the pixel array starting at `offset` and fills them with `fill`.
        """
        if not amount:
            return
            # special case for adding to the end of the array:
        if offset == self.height:
            self.data.extend(array.array(self.data.typecode, [fill] *  self.line_length * amount))
        else:
            start = offset * self.line_length
            self.data[start:start] = array.array(self.data.typecode, [fill] *  self.line_length * amount)
        self.height += amount
        self._precalculate()

    def add_columns(self, offset, amount, fill=0):
        """
        Adds `amount` columns to the pixel array starting at `offset` and fills them with `fill`.
        """
        if not amount:
            return
        start = offset * self.pixelsize
        for i in reversed(range(self.height)):
            line_start = (i * self.line_length) + start
            self.data[line_start:line_start] = array.array(self.data.typecode, [fill] * self.pixelsize * amount)
        self.width += amount
        self._precalculate()


class PixelArray1(GenericPixelArray):
    def __init__(self, data, width, height):
        # super(PixelArray1, self).__init__(data, width, height, 1)
        GenericPixelArray.__init__(self, data, width, height, 1)

    def get(self, x, y):
        """
        Returns the pixel (a tuple of length `self.pixelsize`) from the pixel array at x/y
        """
        start = self._translate(x, y)
        return [self.data[start]]

    def set(self, x, y, pixel):
        """
        Sets the pixel (a tuple of length `self.pixelsize`) to the pixel array at x/y
        """
        start = self._translate(x, y)
        self.data[start] = pixel[0]


class PixelArray2(GenericPixelArray):
    def __init__(self, data, width, height):
        # super(PixelArray2, self).__init__(data, width, height, 2)
        GenericPixelArray.__init__(self, data, width, height, 2)

    def get(self, x, y):
        """
        Returns the pixel (a tuple of length `self.pixelsize`) from the pixel array at x/y
        """
        start = self._translate(x, y)
        return [self.data[start], self.data[start + 1]]

    def set(self, x, y, pixel):
        """
        Sets the pixel (a tuple of length `self.pixelsize`) to the pixel array at x/y
        """
        start = self._translate(x, y)
        self.data[start] = pixel[0]
        self.data[start + 1] = pixel[1]


class PixelArray3(GenericPixelArray):
    def __init__(self, data, width, height):
        # super(PixelArray3, self).__init__(data, width, height, 3)
        GenericPixelArray.__init__(self, data, width, height, 3)

    def get(self, x, y):
        """
        Returns the pixel (a tuple of length `self.pixelsize`) from the pixel array at x/y
        """
        start = self._translate(x, y)
        return [self.data[start], self.data[start + 1], self.data[start + 2]]

    def set(self, x, y, pixel):
        """
        Sets the pixel (a tuple of length `self.pixelsize`) to the pixel array at x/y
        """
        start = self._translate(x, y)
        self.data[start] = pixel[0]
        self.data[start + 1] = pixel[1]
        self.data[start + 2] = pixel[2]


class PixelArray4(GenericPixelArray):
    def __init__(self, data, width, height):
        # super(PixelArray4, self).__init__(data, width, height, 4)
        GenericPixelArray.__init__(self, data, width, height, 4)

    def get(self, x, y):
        """
        Returns the pixel (a tuple of length `self.pixelsize`) from the pixel array at x/y
        """
        start = self._translate(x, y)
        return [self.data[start], self.data[start + 1], self.data[start + 2], self.data[start + 3]]

    def set(self, x, y, pixel):
        """
        Sets the pixel (a tuple of length `self.pixelsize`) to the pixel array at x/y
        """
        start = self._translate(x, y)
        self.data[start] = pixel[0]
        self.data[start + 1] = pixel[1]
        self.data[start + 2] = pixel[2]
        self.data[start + 3] = pixel[3]


def get_pixel_array(data, width, height, pixelsize):
    if pixelsize == 1:
        return PixelArray1(data, width, height)
    elif pixelsize == 2:
        return PixelArray2(data, width, height)
    elif pixelsize == 3:
        return PixelArray3(data, width, height)
    elif pixelsize == 4:
        return PixelArray4(data, width, height)
    else:
        return GenericPixelArray(data, width, height, pixelsize)
