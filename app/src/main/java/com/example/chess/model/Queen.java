package com.example.chess.model;

public class Queen extends Piece {
    public Queen(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board board) {
        int curX = getX();
        int curY = getY();

        // Deve essere o linea retta o diagonale
        if (curX != targetX && curY != targetY && Math.abs(targetX - curX) != Math.abs(targetY - curY)) {
            return false;
        }

        // Controllo percorso libero
        int dirX = Integer.compare(targetX, curX);
        int dirY = Integer.compare(targetY, curY);
        int x = curX + dirX;
        int y = curY + dirY;

        while (x != targetX || y != targetY) {
            if (board.getPiece(x, y) != null) return false;
            x += dirX;
            y += dirY;
        }

        Piece target = board.getPiece(targetX, targetY);
        return target == null || target.isWhite() != isWhite();
    }
}