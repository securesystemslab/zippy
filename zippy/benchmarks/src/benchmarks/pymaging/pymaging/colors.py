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
from __future__ import division
from collections import namedtuple


def _mixin_alpha(colors, alpha):
    ratio = alpha / 255
    return [int(round(color *  ratio)) for color in colors]

class Color(object):
    __slots__ = 'red', 'green', 'blue', 'alpha'

    def __init__(self, red, green, blue, alpha=255):
        self.red = red
        self.green = green
        self.blue = blue
        self.alpha = alpha

    def __str__(self):
        return 'Color: r:%s, g:%s, b:%s, a:%s' % (self.red, self.green, self.blue, self.alpha)

    def __repr__(self):
        return '<%s>' % self

    def __hash__(self):
        return hash((self.red, self.green, self.blue, self.alpha))

    def __eq__(self, other):
        return (
            self.red == other.red and
            self.green == other.green and
            self.blue == other.blue and
            self.alpha == other.alpha
        )

    @classmethod
    def from_pixel(cls, pixel):
        """
        Convert a pixel (list of 3-4 values) to a Color instance.
        """
        assert len(pixel) in (3,4), "Color.from_pixel only supports 3 and 4 value pixels"
        return cls(*map(int, list(pixel)))

    @classmethod
    def from_hexcode(cls, hexcode):
        """
        Convert hexcode to RGB/RGBA.
        """
        hexcode = hexcode.strip('#')
        assert len(hexcode) in (3,4,6,8), "Hex codes must be 3, 4, 6 or 8 characters long"
        if len(hexcode) in (3,4):
            hexcode = ''.join(x*2 for x in hexcode)
        return cls(*[int(''.join(x), 16) for x in zip(hexcode[::2], hexcode[1::2])])

    def get_for_brightness(self, brightness):
        """
        Brightness is a float between 0 and 1
        """
        return Color(self.red, self.green, self.blue, int(round((self.alpha + 1) * brightness)) - 1)

    def cover_with(self, cover_color):
        """
        Mix the two colors respecting their alpha value.

        Puts cover_color over itself compositing the colors using the alpha
        values.
        """
        # fastpath for solid colors
        if cover_color.alpha == 255:
            return Color(cover_color.red, cover_color.green, cover_color.blue, cover_color.alpha)

        srca = cover_color.alpha / 255
        dsta = self.alpha / 255
        outa = srca + dsta * (1 - srca)

        srcr, srcg, srcb = cover_color.red, cover_color.green, cover_color.blue
        dstr, dstg, dstb = self.red, self.green, self.blue

        outr = (srcr * srca + dstr * dsta * (1 - srca)) / outa
        outg = (srcg * srca + dstg * dsta * (1 - srca)) / outa
        outb = (srcb * srca + dstb * dsta * (1 - srca)) / outa

        red = int(round(outr))
        green = int(round(outg))
        blue = int(round(outb))
        alpha = int(round(outa * 255))

        return Color(red, green, blue, alpha)


    def to_pixel(self, pixelsize):
        """
        Convert to pixel (list of 3-4 values)
        """
        assert pixelsize in (3,4), "Color.to_pixel only supports 3 and 4 value pixels"
        if pixelsize == 3:
            return _mixin_alpha([self.red, self.green, self.blue], self.alpha)
        else:
            return [self.red, self.green, self.blue, self.alpha]

    def to_hexcode(self):
        """
        Convert to RGBA hexcode
        """
        return ''.join(hex(x)[2:] for x in (self.red, self.green, self.blue, self.alpha))


ColorType = namedtuple('ColorType', 'length alpha')

RGB = ColorType(3, False)
RGBA = ColorType(4, True)
