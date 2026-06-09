package com.example.chess.model;

public class Bishop extends Piece {
    public Bishop(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board board) {
        int curX = getX();
        int curY = getY();

        // 1. SAFETY CHECK: A piece cannot move to the square it is currently on
        if (curX == targetX && curY == targetY) return false;

        // 2. Must move in a perfect diagonal
        if (Math.abs(targetX - curX) != Math.abs(targetY - curY)) return false;

        // 3. Safe direction calculation (prevents going off-board)
        int dirX = Integer.compare(targetX, curX);
        int dirY = Integer.compare(targetY, curY);

        int x = curX + dirX;
        int y = curY + dirY;

        // Check for obstacles along the diagonal
        while (x != targetX || y != targetY) {
            if (board.getPiece(x, y) != null) return false;
            x += dirX;
            y += dirY;
        }

        // 4. Final check on the destination square
        Piece target = board.getPiece(targetX, targetY);
        return target == null || target.isWhite() != isWhite();
    }
}