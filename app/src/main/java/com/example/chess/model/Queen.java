package com.example.chess.model;

public class Queen extends Piece {
    public Queen(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
        boolean straight = (x == targetX || y == targetY);
        boolean diagonal = Math.abs(targetX - x) == Math.abs(targetY - y);

        if (!(straight || diagonal)) return false;
        if (!isPathClear(x, y, targetX, targetY, board)) return false;

        Piece target = board[targetX][targetY];
        return target == null || target.isWhite() != this.isWhite;
    }

}
