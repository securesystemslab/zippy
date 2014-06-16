# -*- coding: utf-8 -*-
#
# This file is part of the python-chess library.
# Copyright (C) 2012-2014 Niklas Fiekas <niklas.fiekas@tu-clausthal.de>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

#import chess
import codecs
import io

def scan_offsets(handle):
    in_comment = False
    last_pos = 0

    for line in handle:
        if not in_comment and line.startswith(bytes("[Event \"", "utf-8")):
            yield last_pos
        else:
            if (not in_comment and "{" in line) or (in_comment and "}" in line):
                if line.rfind("{") < line.rfind("}"):
                    in_comment = False
                else:
                    in_comment = True

        last_pos += len(line)

if __name__ == "__main__":
    games = 0
    for offset in scan_offsets(open("../mega.pgn", "rb")):
        print(games, offset)
        games += 1
    print(games)
