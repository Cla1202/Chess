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

        // 1. SALVAVITA: Un pezzo non può muoversi sulla casella in cui si trova già
        if (curX == targetX && curY == targetY) return false;

        // 2. Deve muoversi dritto (o cambia solo X o cambia solo Y)
        if (curX != targetX && curY != targetY) return false;

        // 3. Calcolo direzione sicuro
        int dirX = Integer.compare(targetX, curX);
        int dirY = Integer.compare(targetY, curY);

        int x = curX + dirX;
        int y = curY + dirY;

        // Controllo ostacoli in linea retta
        while (x != targetX || y != targetY) {
            if (board.getPiece(x, y) != null) return false;
            x += dirX;
            y += dirY;
        }

        // 4. Controllo finale
        Piece target = board.getPiece(targetX, targetY);
        return target == null || target.isWhite() != isWhite();
    }
}