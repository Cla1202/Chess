package com.example.chess.model;

 public class Rook extends Piece {
    public Rook(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

     @Override
     public boolean isValidMove(int targetX, int targetY, Piece[][] board) {
         if (x != targetX && y != targetY) return false; // Non è una linea retta
         if (!isPathClear(x, y, targetX, targetY, board)) return false;

         Piece target = board[targetX][targetY];
         return target == null || target.isWhite() != this.isWhite; // Vuota o nemico
     }
    private boolean isPathClear(int targetX, int targetY, Piece[][] board) {
        // Logica per ciclare tra (x,y) e (targetX, targetY)
        // e restituire false se incontra un pezzo
        return true; // Semplificato per ora
    }


}
