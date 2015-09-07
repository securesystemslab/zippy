# -*- coding: utf-8 -*-
# Copyright (c) 2012, Jonas Obrist
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#    * Redistributions in binary form must reproduce the above copyright
#      notice, this list of conditions and the following disclaimer in the
#      documentation and/or other materials provided with the distribution.
#    * Neither the name of the Jonas Obrist nor the
#      names of its contributors may be used to endorse or promote products
#      derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL JONAS OBRIST BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
import array
from pymaging.colors import Color
from pymaging.affine import AffineTransform
from pymaging.exceptions import FormatNotSupported, InvalidColor
from pymaging.formats import get_format, get_format_objects
from pymaging.helpers import get_transformed_dimensions
from pymaging.pixelarray import get_pixel_array
from pymaging.resample import nearest
import os



class Image(object):
    def __init__(self, mode, width, height, loader, meta=None):
        self.mode = mode
        self.width = width
        self.height = height
        self.loader = loader
        self.meta = meta
        self._pixelarray = None
        self._palette = None
        self.reverse_palette = None

    # @property
    def pixels(self):
        self.load()
        return self._pixelarray

    # @property
    def pixelsize(self):
        return self.pixels().pixelsize

    @property
    def palette(self):
        self.load()
        return self._palette

    #==========================================================================
    # Constructors
    #==========================================================================

    @classmethod
    def open(cls, fileobj):
        for format in get_format_objects():
            image = format.open(fileobj)
            if image:
                return image
        raise FormatNotSupported()

    @classmethod
    def open_from_path(cls, filepath):
        with open(filepath, 'rb') as fobj:
            return cls.open(fobj)

    @classmethod
    def new(cls, mode, width, height, background_color, palette=None, meta=None):
        color = background_color.to_pixel(mode.length)
        pixel_array = get_pixel_array(array.array('B', color) * width * height, width, height, mode.length)
        return LoadedImage(mode, width, height, pixel_array, palette=palette, meta=meta)

    def load(self):
        if self._pixelarray is not None:
            return
        self._pixelarray, self._palette = self.loader()

    #==========================================================================
    # Saving
    #==========================================================================

    def save(self, fileobj, format):
        format_object = get_format(format)
        if not format_object:
            raise FormatNotSupported(format)
        format_object.save(self, fileobj)

    def save_to_path(self, filepath, format=None):
        if not format:
            format = os.path.splitext(filepath)[1][1:]
        with open(filepath, 'wb') as fobj:
            self.save(fobj, format)

    #==========================================================================
    # Helpers
    #==========================================================================

    def get_reverse_palette(self):
        if self.reverse_palette is None:
            self._fill_reverse_palette()
        return self.reverse_palette

    def _fill_reverse_palette(self):
        self.reverse_palette = {}
        if not self.palette:
            return
        for index, color in enumerate(self.palette):
            color_obj = Color.from_pixel(color)
            color_obj.to_hexcode()
            self.reverse_palette[color_obj] = index

    def _copy(self, pixels, **kwargs):
        defaults = {
            'mode': self.mode,
            'width': self.width,
            'height': self.height,
            'palette': self.palette,
            'meta': self.meta,
        }
        defaults.update(kwargs)
        defaults['pixels'] = pixels
        return LoadedImage(**defaults)

    #==========================================================================
    # Geometry Operations
    #==========================================================================

    def resize(self, width, height, resample_algorithm=nearest, resize_canvas=True):
        pixels = resample_algorithm.resize(
            self, width, height, resize_canvas=resize_canvas
        )
        return self._copy(pixels)

    def affine(self, transform, resample_algorithm=nearest, resize_canvas=True):
        """
        Returns a copy of this image transformed by the given
        AffineTransform.
        """
        pixels = resample_algorithm.affine(
            self,
            transform,
            resize_canvas=resize_canvas,
        )
        return self._copy(pixels)

    def rotate(self, degrees, clockwise=False, resample_algorithm=nearest, resize_canvas=True):
        """
        Returns the image obtained by rotating this image by the
        given number of degrees.
        Anticlockwise unless clockwise=True is given.
        """
        # translate to the origin first, then rotate, then translate back
        transform = AffineTransform()
        transform = transform.translate(self.width * -0.5, self.height * -0.5)
        transform = transform.rotate(degrees, clockwise=clockwise)

        width, height = self.width, self.height
        if resize_canvas:
            # determine new width
            width, height = get_transformed_dimensions(transform, (0, 0, width, height))

        transform = transform.translate(width * 0.5, height * 0.5)

        pixels = resample_algorithm.affine(self, transform, resize_canvas=resize_canvas)

        return self._copy(pixels)

    def get_pixel(self, x, y):
        try:
            raw_pixel = self.pixels.get(x, y)
        except IndexError:
            raise IndexError("Pixel (%d, %d) not in image" % (x, y))
        if self.pixelsize == 1 and self.palette:
            return self.palette[raw_pixel[0]]
        else:
            return raw_pixel

    def get_color(self, x, y):
        return Color.from_pixel(self.get_pixel(x, y))

    def set_color(self, x, y, color):
        if color.alpha != 255:
            base = self.get_color(x, y)
            color = base.cover_with(color)
        if self.reverse_palette and self.pixelsize == 1:
            if color not in self.reverse_palette:
                raise InvalidColor(str(color))
            index = self.reverse_palette[color]
            self.pixels().set(x, y, [index])
        else:
            self.pixels().set(x, y, color.to_pixel(self.pixelsize()))

    def flip_top_bottom(self):
        """
        Vertically flips the pixels of source into target
        """
        pixels = self.pixels.copy_flipped_top_bottom()
        return self._copy(pixels)

    def flip_left_right(self):
        """
        Horizontally flips the pixels of source into target
        """
        return self._copy(pixels=self.pixels.copy_flipped_left_right())

    def crop(self, width, height, padding_top, padding_left):
        new_pixels = self.pixels().copy()
        new_pixels.remove_lines(0, padding_top)
        new_pixels.remove_lines(height, new_pixels.height - height)
        new_pixels.remove_columns(0, padding_left)
        new_pixels.remove_columns(width, new_pixels.width - width)
        return self._copy(new_pixels, width=width, height=height)

    #==========================================================================
    # Manipulation
    #==========================================================================

    def draw(self, shape, color):
        for x, y, pixelcolor in shape.iter_pixels(color):
            self.set_color(x, y, pixelcolor)

    def blit(self, padding_top, padding_left, image):
        """
        Puts the image given on top of this image with the given padding
        """
        # there *must* be a better/faster way to do this:
        # TODO: check that palettes etc match.
        # TODO: fastpath this by copying the array if pixelsize is identical/palette is the same
        for x in range(min([image.width, self.width - padding_left])):
            for y in range(min([image.height, self.height- padding_top])):
                self.set_color(padding_left + x, padding_top + y, image.get_color(x, y))



class LoadedImage(Image):
    def __init__(self, mode, width, height, pixels, palette=None, meta=None):
        self.mode = mode
        self.width = width
        self.height = height
        self.format = format
        self.loader = lambda:None
        self.meta = meta
        self._pixelarray = pixels
        self._palette = palette
        self.reverse_palette = None