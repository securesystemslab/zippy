__author__ = 'zwei'

import sys, os, time

# setup path
path = os.path.join(os.path.dirname(__file__), 'pymaging')
sys.path.append(path)

from pymaging.shapes import Line
from pymaging.webcolors import Black, White
from pymaging.test_utils import image_factory

def create_canvas():
    column = [Black for i in range(500)]
    blacks = [column for i in range(500)]
    img = image_factory(blacks)
    return img

def draw_lines(img):
    topleft_bottomright = Line(0, 0, 499, 499)
    bottomright_topleft = Line(499, 499, 0, 0)
    bottomleft_topright = Line(0, 499, 499, 0)
    topright_bottomleft = Line(499, 0, 0, 499)

    img.draw(topleft_bottomright, White)
    img.draw(bottomright_topleft, White)
    img.draw(bottomleft_topright, White)
    img.draw(topright_bottomleft, White)
    return img


def draw():
    img = create_canvas()
    img = draw_lines(img)
    # print(img.pixels())

draw()