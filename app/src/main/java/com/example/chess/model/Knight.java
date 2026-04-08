package com.example.chess.model;

public class Knight extends Piece {
    public Knight(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
        int dx = Math.abs(targetX - x);
        int dy = Math.abs(targetY - y);
        if (!((dx == 2 && dy == 1) || (dx == 1 && dy == 2))) return false;

        Piece target = board[targetX][targetY];
        return target == null || target.isWhite() != this.isWhite;
    }
}