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

        // 1. Movimento dritto (1 o 2 caselle) - Già fatto
        if (targetY == currentY) {
            if (targetX == currentX + direction) return board[targetX][targetY] == null;
            if (currentX == (isWhite() ? 6 : 1) && targetX == currentX + (2 * direction)) {
                return board[targetX][targetY] == null && board[currentX + direction][currentY] == null;
            }
        }

        // 2. Cattura Diagonale Standard
        if (Math.abs(targetY - currentY) == 1 && targetX == currentX + direction) {
            if (board[targetX][targetY] != null) {
                return board[targetX][targetY].isWhite() != this.isWhite();
            }

            // --- LOGICA EN PASSANT ---
            // Se la casella è vuota, controlla se è un En Passant
            if (targetY == boardObject.getEnPassantColumn()) {
                // La riga deve essere quella corretta (riga 2 per il bianco, 5 per il nero)
                int enPassantRow = isWhite() ? 2 : 5;
                return targetX == enPassantRow;
            }
        }

        return false;
    }
}