package com.example.chess.model;

public class Pawn extends Piece {
    public Pawn(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board boardObject) {
        Piece[][] board = boardObject.getGrid();
        int direction = isWhite() ? -1 : 1;
        int currentX = getX();
        int currentY = getY();

        // 1. Straight move (1 or 2 squares)
        if (targetY == currentY) {
            if (targetX == currentX + direction) return board[targetX][targetY] == null;
            if (currentX == (isWhite() ? 6 : 1) && targetX == currentX + (2 * direction)) {
                return board[targetX][targetY] == null && board[currentX + direction][currentY] == null;
            }
        }

        // 2. Standard diagonal capture
        if (Math.abs(targetY - currentY) == 1 && targetX == currentX + direction) {
            if (board[targetX][targetY] != null) {
                return board[targetX][targetY].isWhite() != this.isWhite();
            }

            // --- EN PASSANT LOGIC ---
            // If the square is empty, check for En Passant
            if (targetY == boardObject.getEnPassantColumn()) {
                // The row must be the correct one (row 2 for white, 5 for black)
                int enPassantRow = isWhite() ? 2 : 5;
                return targetX == enPassantRow;
            }
        }

        return false;
    }
}