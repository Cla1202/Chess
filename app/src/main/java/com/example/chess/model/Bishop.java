package com.example.chess.model;

public class Bishop extends Piece {
    public Bishop(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board board) {
        int curX = getX();
        int curY = getY();

        // 1. SALVAVITA: Un pezzo non può muoversi sulla casella in cui si trova già
        if (curX == targetX && curY == targetY) return false;

        // 2. Deve muoversi in diagonale perfetta
        if (Math.abs(targetX - curX) != Math.abs(targetY - curY)) return false;

        // 3. Calcolo direzione sicuro (evita i crash fuori mappa)
        int dirX = Integer.compare(targetX, curX);
        int dirY = Integer.compare(targetY, curY);

        int x = curX + dirX;
        int y = curY + dirY;

        // Controllo ostacoli lungo la diagonale
        while (x != targetX || y != targetY) {
            if (board.getPiece(x, y) != null) return false;
            x += dirX;
            y += dirY;
        }

        // 4. Controllo finale sulla casella di destinazione
        Piece target = board.getPiece(targetX, targetY);
        return target == null || target.isWhite() != isWhite();
    }
}