package com.example.chess.model;

public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board board) {
        int curX = getX();
        int curY = getY();

        // 1. SAFETY CHECK: A piece cannot move to the square it is currently on
        if (curX == targetX && curY == targetY) return false;

        // 2. Must move in a straight line (change either only X or only Y)
        if (curX != targetX && curY != targetY) return false;

        // 3. Safe direction calculation
        int dirX = Integer.compare(targetX, curX);
        int dirY = Integer.compare(targetY, curY);

        int x = curX + dirX;
        int y = curY + dirY;

        // Check for obstacles in a straight line
        while (x != targetX || y != targetY) {
            if (board.getPiece(x, y) != null) return false;
            x += dirX;
            y += dirY;
        }

        // 4. Final check
        Piece target = board.getPiece(targetX, targetY);
        return target == null || target.isWhite() != isWhite();
    }
}