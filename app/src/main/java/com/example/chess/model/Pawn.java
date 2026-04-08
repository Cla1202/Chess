package com.example.chess.model;

public class Pawn extends Piece {
    public Pawn(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
        int direction = isWhite ? -1 : 1; // Bianco sale (-1), Nero scende (+1)
        int startRow = isWhite ? 6 : 1;

        // Movimento dritto di 1
        if (targetY == y && targetX == x + direction && board[targetX][targetY] == null) {
            return true;
        }
        // Movimento iniziale di 2
        if (targetY == y && x == startRow && targetX == x + (2 * direction)
                && board[targetX][targetY] == null && board[x + direction][y] == null) {
            return true;
        }
        // Mangiare in diagonale
        if (Math.abs(targetY - y) == 1 && targetX == x + direction && board[targetX][targetY] != null) {
            return board[targetX][targetY].isWhite() != this.isWhite;
        }
        return false;
    }
}
