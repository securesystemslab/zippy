__author__ = 'zwei'


import os, sys, time
chess_path = os.path.join(os.path.dirname(__file__), 'python-chess-0.1.0')
sys.path.append(chess_path)
import chess


def scholar_mate():
    board = chess.Bitboard()
    board.push_san("e4")
    board.push_san("e5")
    board.push_san("Qh5")
    board.push_san("Nc6")
    board.push_san("Bc4")
    board.push_san("Nf6")
    board.push_san("Qxf7")
    assert board.is_checkmate()

def move_from_uci():
    board = chess.Bitboard()
    assert not chess.Move.from_uci("a8a1") in board.legal_moves

for i in range(100):
    scholar_mate()