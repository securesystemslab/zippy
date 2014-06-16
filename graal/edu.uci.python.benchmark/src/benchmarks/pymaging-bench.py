__author__ = 'zwei'

import sys, os, time

# setup path
path = os.path.join(os.path.dirname(__file__), 'pymaging')
sys.path.append(path)

from pymaging.shapes import Line
from pymaging.webcolors import Black, White, Yellow, SlateBlue
from pymaging.test_utils import image_factory

def create_canvas():
    column = [Black for i in range(1000)]
    blacks = [column for i in range(1000)]
    img = image_factory(blacks)
    return img

def draw_lines(img):
    topleft_bottomright = Line(0, 0, 999, 999)
    bottomright_topleft = Line(999, 999, 0, 0)
    bottomleft_topright = Line(0, 999, 999, 0)
    topright_bottomleft = Line(999, 0, 0, 999)

    img.draw(topleft_bottomright, White)
    img.draw(bottomright_topleft, White)
    img.draw(bottomleft_topright, White)
    img.draw(topright_bottomleft, White)

    slop1 = Line(100, 0, 899, 999)
    slop2 = Line(899, 999, 100, 0)
    slop3 = Line(0, 899, 899, 0)
    slop4 = Line(899, 0, 0, 899)

    img.draw(slop1, Yellow)
    img.draw(slop2, Yellow)
    img.draw(slop3, Yellow)
    img.draw(slop4, Yellow)

    blue1 = Line(10, 30, 500, 600)
    blue2 = Line(700, 900, 100, 20)
    blue3 = Line(0, 300, 666, 33)
    blue4 = Line(876, 0, 20, 717)

    img.draw(blue1, SlateBlue)
    img.draw(blue2, SlateBlue)
    img.draw(blue3, SlateBlue)
    img.draw(blue4, SlateBlue)

    return img


def draw():
    img = create_canvas()
    img = draw_lines(img)
    return img

def main(n):
    for i in range(n):
        img = draw()

    return img

def measure():
    print("Start timing...")
    start = time.time()
    img = main(num)
    duration = "%.3f\n" % (time.time() - start)
    # print(img.pixels())
    print("pymaging-draw: " + duration)

# warm up
num =  int(sys.argv[1]) # 200
for i in range(10):
    main(10)

measure()