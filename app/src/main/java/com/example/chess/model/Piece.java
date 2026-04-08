package com.example.chess.model;

import java.util.List;

public abstract class Piece {
    protected int x, y; // Coordinate 0-7
    protected boolean isWhite;

    public Piece(int x, int y, boolean isWhite) {
        this.x = x;
        this.y = y;
        this.isWhite = isWhite;
    }

    // Metodo astratto: ogni pezzo dirà se può muoversi in una posizione
    public abstract boolean isValidMove(int targetX, int targetY, Piece[][] board);

    // Getter e Setter
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isWhite() { return isWhite; }

    protected boolean isPathClear(int startX, int startY, int targetX, int targetY, Piece[][] board) {
        int dx = Integer.compare(targetX, startX);
        int dy = Integer.compare(targetY, startY);

        int currX = startX + dx;
        int currY = startY + dy;

        while (currX != targetX || currY != targetY) {
            if (board[currX][currY] != null) return false; // Ostacolo trovato
            currX += dx;
            currY += dy;
        }
        return true;
    }
}
