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

        // The Knight moves in an "L" shape: (2,1) or (1,2)
        if ((diffX == 2 && diffY == 1) || (diffX == 1 && diffY == 2)) {
            Piece target = grid[targetX][targetY];
            // Can move if the square is empty or contains an enemy piece
            return target == null || target.isWhite() != isWhite();
        }
        return false;
    }
}