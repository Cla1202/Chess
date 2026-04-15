package com.example.chess.model;

public class Knight extends Piece {
    public Knight(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board boardObject) {
        Piece[][] grid = boardObject.getGrid();
        int diffX = Math.abs(targetX - getX());
        int diffY = Math.abs(targetY - getY());

        // Il cavallo si muove a "L": (2,1) o (1,2)
        if ((diffX == 2 && diffY == 1) || (diffX == 1 && diffY == 2)) {
            Piece target = grid[targetX][targetY];
            // Può muoversi se la casella è vuota o ha un nemico
            return target == null || target.isWhite() != isWhite();
        }
        return false;
    }
}