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
from abc import ABCMeta, abstractmethod

from pymaging.pixelarray import get_pixel_array

__all__ = ('nearest', 'bilinear', 'Resampler')

from pymaging.affine import AffineTransform
from pymaging.helpers import get_transformed_dimensions
from pymaging.utils import fdiv
import array


class Resampler(object):
    __metaclass__ = ABCMeta

    @abstractmethod
    def _get_value(self, source, source_x, source_y, dx, dy):
        pass

    def affine(self, source, transform, resize_canvas=True):
        if resize_canvas:
            # get image dimensions
            width, height = get_transformed_dimensions(
                transform,
                (0, 0, source.width, source.height)
            )
        else:
            width = source.width
            height = source.height

        pixelsize = source.pixelsize

        # transparent or black background
        background = [0] * pixelsize

        # we want to go from dest coords to src coords:
        transform = transform.inverse()

        # Optimisation:
        # Because affine transforms have no perspective component,
        # the *gradient* of each source row/column must be constant.
        # So, we can calculate the source coordinates for each corner,
        # and then interpolate for each pixel, instead of doing a
        # matrix multiplication for each pixel.

        x_range = range(width)
        y_range = range(height)
        new_array = source.pixels.copy()

        for y in y_range:
            # the 0.5's mean we use the center of each pixel
            row_x0, row_y0 = transform * (0.5, y + 0.5)
            row_x1, row_y1 = transform * (width + 0.5, y + 0.5)

            dx = float(row_x1 - row_x0) / source.width
            dy = float(row_y1 - row_y0) / source.width

            for x in x_range:
                source_x = int(row_x0 + dx * x)
                source_y = int(row_y0 + dy * x)

                new_array.set(x, y,
                    self._get_value(source, source_x, source_y, dx, dy)
                    or background
                )
        return new_array

    def resize(self, source, width, height, resize_canvas=True):
        transform = AffineTransform().scale(
            width / float(source.width),
            height / float(source.height)
        )
        return self.affine(source, transform, resize_canvas=resize_canvas)


class Nearest(Resampler):

    def _get_value(self, source, source_x, source_y, dx, dy):
        if source_x < 0 or source_y < 0 or \
                    source_x >= source.width or source_y >= source.height:
            return None
        else:
            return source.pixels.get(source_y, source_x)

    def resize(self, source, width, height, resize_canvas=True):
        if not resize_canvas:
            # this optimised implementation doesn't deal with this.
            # so delegate to affine()
            return super(Nearest, self).resize(
                source, width, height, resize_canvas=resize_canvas
            )
        pixels = array.array('B')
        pixelsize = source.pixelsize

        x_ratio = fdiv(source.width, width)  # get the x-axis ratio
        y_ratio = fdiv(source.height, height)  # get the y-axis ratio

        y_range = range(height)  # an iterator over the indices of all lines (y-axis)
        x_range = range(width)  # an iterator over the indices of all rows (x-axis)
        for y in y_range:
            y += 0.5  # use the center of each pixel
            source_y = int(y * y_ratio)  # get the source line
            for x in x_range:
                x += 0.5  # use the center of each pixel
                source_x = int(x * x_ratio)  # get the source row
                pixels.extend(source.pixels.get(source_x, source_y))
        return get_pixel_array(pixels, width, height, pixelsize)


class Bilinear(Resampler):

    def _get_value(self, source, source_x, source_y, dx, dy):
        if source_x < 0 or source_y < 0 or \
                    source_x >= source.width or source_y >= source.height:
            return None

        source_y_i = int(source_y)
        source_x_i = int(source_x)

        weight_y0 = 1 - abs(source_y - source_y_i)
        weight_x0 = 1 - abs(source_x - source_x_i)

        pixelsize = source.pixelsize
        channel_sums = [0.0] * pixelsize
        has_alpha = source.mode.alpha
        color_channels_range = range(pixelsize - 1 if has_alpha else pixelsize)

        # populate <=4 nearest src_pixels, taking care not to go off
        # the edge of the image.
        src_pixels = [source.get_pixel(source_x_i, source_y_i), None, None, None]
        next_x = int(source_x + dx)
        next_y = int(source_x + dy)

        if next_x < source.width and next_x >= 0:
            src_pixels[1] = source.get_pixel(int(next_x), source_y_i)
        else:
            weight_x0 = 1
        if next_y < source.height and next_y >= 0:
            src_pixels[2] = source.get_pixel(source_x_i, next_y)
            if next_x < source.width and next_x >= 0:
                src_pixels[3] = source.get_pixel(next_x, next_y)
        else:
            weight_y0 = 1

        for i, src_pixel in enumerate(src_pixels):
            if src_pixel is None:
                continue
            weight_x = (1 - weight_x0) if (i % 2) else weight_x0
            weight_y = (1 - weight_y0) if (i // 2) else weight_y0
            alpha_weight = weight_x * weight_y
            color_weight = alpha_weight
            alpha = 255
            if has_alpha:
                alpha = src_pixel[-1]
                if not alpha:
                    continue
                color_weight *= (alpha / 255.0)
            for channel_index, channel_value in zip(color_channels_range, src_pixel):
                channel_sums[channel_index] += color_weight * channel_value

            if has_alpha:
                channel_sums[-1] += alpha_weight * alpha
        if has_alpha:
            total_alpha_multiplier = channel_sums[-1] / 255.0
            if total_alpha_multiplier:  # (avoid div/0)
                for channel_index in color_channels_range:
                    channel_sums[channel_index] /= total_alpha_multiplier

        return [int(round(s)) for s in channel_sums]

    def resize(self, source, width, height, resize_canvas=True):
        if not resize_canvas:
            # this optimised implementation doesn't deal with this.
            # so delegate to affine()
            return super(Bilinear, self).resize(
                source, width, height, resize_canvas=resize_canvas
            )
        x_ratio = fdiv(source.width, width)  # get the x-axis ratio
        y_ratio = fdiv(source.height, height)  # get the y-axis ratio
        pixelsize = source.pixelsize
        pixels = array.array('B')

        if source.palette:
            raise NotImplementedError("Resampling of paletted images is not yet supported")

        if x_ratio < 1 and y_ratio < 1:
            if not (width % source.width) and not (height % source.height):
                # optimisation: if doing a perfect upscale,
                # can just use nearest neighbor (it's much faster)
                return nearest.resize(source, width, height)

        has_alpha = source.mode.alpha
        color_channels_range = range(pixelsize - 1 if has_alpha else pixelsize)

        y_range = range(height)  # an iterator over the indices of all lines (y-axis)
        x_range = range(width)  # an iterator over the indices of all rows (x-axis)
        for y in y_range:
            src_y = (y + 0.5) * y_ratio - 0.5  # use the center of each pixel
            src_y_i = int(src_y)

            weight_y0 = 1 - abs(src_y - src_y_i)

            for x in x_range:
                src_x = (x + 0.5) * x_ratio - 0.5
                src_x_i = int(src_x)

                weight_x0 = 1 - abs(src_x - src_x_i)

                channel_sums = [0.0] * pixelsize

                # populate <=4 nearest src_pixels, taking care not to go off
                # the edge of the image.
                src_pixels = [source.get_color(src_y_i, src_x_i), None, None, None]
                if src_x_i + 1 < source.width:
                    src_pixels[1] = source.get_color(src_y_i, src_x_i + 1)
                else:
                    weight_x0 = 1
                if src_y_i + 1 < source.height:
                    src_pixels[2] = source.get_color(src_y_i + 1, src_x_i)
                    if src_x_i + 1 < source.height:
                        src_pixels[3] = source.get_color(src_y_i + 1, src_x_i + 1)
                else:
                    weight_y0 = 1

                for i, src_pixel in enumerate(src_pixels):
                    if src_pixel is None:
                        continue
                    src_pixel = src_pixel.to_pixel(pixelsize)
                    weight_x = (1 - weight_x0) if (i % 2) else weight_x0
                    weight_y = (1 - weight_y0) if (i // 2) else weight_y0
                    alpha_weight = weight_x * weight_y
                    color_weight = alpha_weight
                    alpha = 255
                    if has_alpha:
                        alpha = src_pixel[-1]
                        if not alpha:
                            continue
                        color_weight *= (alpha / 255.0)
                    for channel_index, channel_value in zip(color_channels_range, src_pixel):
                        channel_sums[channel_index] += color_weight * channel_value

                    if has_alpha:
                        channel_sums[-1] += alpha_weight * alpha
                if has_alpha:
                    total_alpha_multiplier = channel_sums[-1] / 255.0
                    if total_alpha_multiplier:  # (avoid div/0)
                        for channel_index in color_channels_range:
                            channel_sums[channel_index] /= total_alpha_multiplier
                pixels.extend([int(round(s)) for s in channel_sums])
        return get_pixel_array(pixels, width, height, pixelsize)


nearest = Nearest()
bilinear = Bilinear()
