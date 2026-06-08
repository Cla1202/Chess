package com.example.chess.model;

import java.io.Serializable;
import java.util.List;

public abstract class Piece implements Serializable {
    protected int x, y; // Coordinates 0-7
    protected boolean isWhite;

    public Piece(int x, int y, boolean isWhite) {
        this.x = x;
        this.y = y;
        this.isWhite = isWhite;
    }

    // Abstract method: each piece will define its valid moves
    // Changes from (int endX, int endY, Piece[][] grid)
    // to this:
    public abstract boolean isValidMove(int endX, int endY, Board board);

    // Getters and Setters
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
            if (board[currX][currY] != null) return false; // Obstacle found
            currX += dx;
            currY += dy;
        }
        return true;
    }
}
