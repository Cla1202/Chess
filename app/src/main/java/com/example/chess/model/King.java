package com.example.chess.model;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(int x, int y, boolean isWhite) { super(x, y, isWhite); }
    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    @Override
    public boolean isValidMove(int targetX, int targetY, Board board) {
        int diffX = Math.abs(targetX - getX());
        int diffY = Math.abs(targetY - getY());

        // 1. Base move: If it's a single step, we don't check for check here (the Board class handles that)
        if (diffX <= 1 && diffY <= 1) {
            Piece target = board.getPiece(targetX, targetY);
            return target == null || target.isWhite() != isWhite();
        }

        // 2. Castling: Only if the King moves 2 squares sideways
        if (!hasMoved && diffX == 0 && diffY == 2) {
            return canCastle(targetX, targetY, board);
        }
        return false;
    }

    private boolean canCastle(int targetX, int targetY, Board board) {
        // Initial check for being in check (prevents infinite recursion)
        if (board.isKingInCheck(isWhite)) return false;

        int rookCol = (targetY > getY()) ? 7 : 0;
        Piece rook = board.getPiece(getX(), rookCol);

        if (rook instanceof Rook && !((Rook) rook).hasMoved()) {
            int step = (targetY > getY()) ? 1 : -1;
            for (int c = getY() + step; c != rookCol; c += step) {
                if (board.getPiece(getX(), c) != null) return false;
                // Check transit squares
                if (Math.abs(c - getY()) <= 2) {
                    if (board.isSquareAttacked(getX(), c, !isWhite)) return false;
                }
            }
            return true;
        }
        return false;
    }
}