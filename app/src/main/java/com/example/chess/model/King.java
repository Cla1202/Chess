package com.example.chess.model;

public class King extends Piece {
    public King(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
        int diffX = Math.abs(targetX - x);
        int diffY = Math.abs(targetY - y);

        // Massimo 1 casella di distanza in ogni direzione
        return (diffX <= 1 && diffY <= 1);
    }
}