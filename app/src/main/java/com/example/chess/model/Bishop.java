package com.example.chess.model;

public class Bishop extends Piece {
    public Bishop(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
        if (Math.abs(targetX - x) != Math.abs(targetY - y)) return false; // Non è diagonale
        if (!isPathClear(x, y, targetX, targetY, board)) return false;

        Piece target = board[targetX][targetY];
        return target == null || target.isWhite() != this.isWhite;
    }

}