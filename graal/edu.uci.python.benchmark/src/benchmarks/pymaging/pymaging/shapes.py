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

from pymaging.utils import fdiv
import math


class BaseShape(object):
    def iter_pixels(self, color):
        raise StopIteration()


class Pixel(BaseShape):
    def __init__(self, x, y):
        self.x = x
        self.y = y
        
    def iter_pixels(self, color):
        yield self.x, self.y, color


class Line(Pixel):
    """
    Use Bresenham Line Algorithm (http://en.wikipedia.org/wiki/Bresenham's_line_algorithm):
    
    function line(x0, x1, y0, y1)
        boolean steep := abs(y1 - y0) > abs(x1 - x0)
        if steep then
            swap(x0, y0)
            swap(x1, y1)
        if x0 > x1 then
            swap(x0, x1)
            swap(y0, y1)
        int deltax := x1 - x0
        int deltay := abs(y1 - y0)
        real error := 0
        real deltaerr := deltay / deltax
        int ystep
        int y := y0
        if y0 < y1 then ystep := 1 else ystep := -1
        for x from x0 to x1
            if steep then plot(y,x) else plot(x,y)
            error := error + deltaerr
            if error ≥ 0.5 then
                y := y + ystep
                error := error - 1.0
    """
    def __init__(self, start_x, start_y, end_x, end_y):
        self.start_x = start_x
        self.start_y = start_y
        self.end_x = end_x
        self.end_y = end_y
        steep = abs(self.end_y - self.start_y) > abs(self.end_x - self.start_x)
        if steep:
            x0, y0 = self.start_y, self.start_x
            x1, y1 = self.end_y, self.end_x
        else:
            x0, y0 = self.start_x, self.start_y
            x1, y1 = self.end_x, self.end_y
        if x0 > x1:
            x0, x1 = x1, x0
            y0, y1 = y1, y0
        
        self.x0, self.x1, self.y0, self.y1 = x0, x1, y0, y1
        
        delta_x = x1 - x0
        delta_y = abs(y1 - y0)
        self.error = 0.0
        self.delta_error = fdiv(delta_y, delta_x)
        if y0 < y1:
            self.ystep = 1
        else:
            self.ystep = -1
        
        self.y = y0
        
        self.iterator = self.steep_iterator if steep else self.normal_iterator
        
    def steep_iterator(self):
        for x in range(self.x0, self.x1):
            yield self.y, x
            self.shift()
        yield self.y1, self.x1
        
    def normal_iterator(self):
        for x in range(self.x0, self.x1):
            yield x, self.y
            self.shift()
        yield self.x1, self.y1
    
    def shift(self):
        self.error += self.delta_error
        if self.error >= 0.5:
            self.y += self.ystep
            self.error -= 1.0
    
    def iter_pixels(self, color):
        for x, y in self.iterator():
            yield x, y, color


# For AntiAliasedLin
_round = lambda x: int(round(x))
_ipart = int # integer part of x
_fpart = lambda x: x - math.floor(x) # fractional part of x
_rfpart = lambda x: 1 - _fpart(x)

class AntiAliasedLine(Line):
    def __init__(self, start_x, start_y, end_x, end_y):
        self.start_x = start_x
        self.start_y = start_y
        self.end_x = end_x
        self.end_y = end_y

    def iter_pixels(self, color):
        """
        Use Xiaolin Wu's line algorithm: http://en.wikipedia.org/wiki/Xiaolin_Wu%27s_line_algorithm
        
        function plot(x, y, c) is
            plot the pixel at (x, y) with brightness c (where 0 ≤ c ≤ 1)
        
        function ipart(x) is
            return integer part of x
        
        function round(x) is
            return ipart(x + 0.5)
        
        function fpart(x) is
            return fractional part of x
        
        function rfpart(x) is
            return 1 - fpart(x)
        
        function drawLine(x1,y1,x2,y2) is
            dx = x2 - x1
            dy = y2 - y1
            if abs(dx) < abs(dy) then                 
              swap x1, y1
              swap x2, y2
              swap dx, dy
            end if
            if x2 < x1
              swap x1, x2
              swap y1, y2
            end if
            gradient = dy / dx
            
            // handle first endpoint
            xend = round(x1)
            yend = y1 + gradient * (xend - x1)
            xgap = rfpart(x1 + 0.5)
            xpxl1 = xend  // this will be used in the main loop
            ypxl1 = ipart(yend)
            plot(xpxl1, ypxl1, rfpart(yend) * xgap)
            plot(xpxl1, ypxl1 + 1, fpart(yend) * xgap)
            intery = yend + gradient // first y-intersection for the main loop
            
            // handle second endpoint
            xend = round (x2)
            yend = y2 + gradient * (xend - x2)
            xgap = fpart(x2 + 0.5)
            xpxl2 = xend  // this will be used in the main loop
            ypxl2 = ipart (yend)
            plot (xpxl2, ypxl2, rfpart (yend) * xgap)
            plot (xpxl2, ypxl2 + 1, fpart (yend) * xgap)
            
            // main loop
            for x from xpxl1 + 1 to xpxl2 - 1 do
                plot (x, ipart (intery), rfpart (intery))
                plot (x, ipart (intery) + 1, fpart (intery))
                intery = intery + gradient
        end function
        """
        def _plot(x, y, c):
            """
            plot the pixel at (x, y) with brightness c (where 0 ≤ c ≤ 1)
            """
            return int(x), int(y), color.get_for_brightness(c)
        dx = self.end_x - self.start_x
        dy = self.end_y - self.start_y
        x1, x2, y1, y2 = self.start_x, self.end_x, self.start_y, self.end_y
        if abs(dx) > abs(dy):
            x1, y1 = y1, x1
            x2, y2 = y2, x2
            dx, dy = dy, dx
        if x2 < x1:
            x1, x2 = x2, x1
            y1, y2 = y2, y1
        
        gradient = fdiv(dy, dx)
        
        xend = round(x1)
        yend = y1 + gradient * (xend - x1)
        xgap = _rfpart(x1 + 0.5)
        xpxl1 = xend 
        ypxl1 = _ipart(yend)
        yield _plot(xpxl1, ypxl1, _rfpart(yend) * xgap)
        yield _plot(xpxl1, ypxl1 + 1, _fpart(yend) * xgap)
        
        intery = yend + gradient
        
        xend = _round(x2)
        yend = y2 + gradient *  (xend - x2)
        xgap = _fpart(x2 + 0.5)
        xpxl2 = xend
        ypxl2 = _ipart(yend)
        yield _plot(xpxl2, ypxl2, _rfpart(yend) * xgap)
        yield _plot(xpxl2, ypxl2 + 1, _fpart(yend) * xgap)
        
        for x in range (xpxl1 + 1, xpxl2 - 1):
            yield _plot(x, _ipart(intery), _rfpart(intery))
            yield _plot(x, _ipart(intery) + 1, _fpart(intery))
            intery += gradient
