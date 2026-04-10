package com.example.chess.model;

public class Board {
    private Piece[][] grid;
    private boolean whiteTurn = true; // Il Bianco inizia sempre
    public boolean isWhiteTurn() { return whiteTurn; }
    public Board() {
        grid = new Piece[8][8];
        setupBoard();
    }

    // Nuovo costruttore per inizializzare la scacchiera a partire da un livello Quiz
    public Board(Piece[][] customGrid, boolean whiteTurnToStart) {
        this.grid = customGrid;
        this.whiteTurn = whiteTurnToStart;
    }

    private void setupBoard() {
        // --- PEZZI NERI (Riga 0 e 1) ---
        grid[0][0] = new Rook(0, 0, false);
        grid[0][1] = new Knight(0, 1, false);
        grid[0][2] = new Bishop(0, 2, false);
        grid[0][3] = new Queen(0, 3, false);
        grid[0][4] = new King(0, 4, false);
        grid[0][5] = new Bishop(0, 5, false);
        grid[0][6] = new Knight(0, 6, false);
        grid[0][7] = new Rook(0, 7, false);

        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn(1, i, false);
        }

        // --- PEZZI BIANCHI (Riga 6 e 7) ---
        for (int i = 0; i < 8; i++) {
            grid[6][i] = new Pawn(6, i, true);
        }

        grid[7][0] = new Rook(7, 0, true);
        grid[7][1] = new Knight(7, 1, true);
        grid[7][2] = new Bishop(7, 2, true);
        grid[7][3] = new Queen(7, 3, true);
        grid[7][4] = new King(7, 4, true);
        grid[7][5] = new Bishop(7, 5, true);
        grid[7][6] = new Knight(7, 6, true);
        grid[7][7] = new Rook(7, 7, true);
    }

    public Piece getPiece(int x, int y) {
        return grid[x][y];
    }

    // Dentro Board.java
    public boolean movePiece(int startX, int startY, int endX, int endY) {
        Piece p = grid[startX][startY];

        // 1. Controllo Turno: Se il pezzo non è del colore di chi deve muovere, annulla
        if (p == null || p.isWhite() != whiteTurn) {
            return false;
        }

        // 2. CHIAMATA ALLE REGOLE: Qui sta il segreto
        // Passiamo la scacchiera attuale (grid) al pezzo per il controllo
        if (p.isValidMove(endX, endY, grid)) {

            // 3. Esegui lo spostamento SOLO se isValidMove è TRUE
            grid[endX][endY] = p;
            grid[startX][startY] = null;

            // Aggiorna le coordinate interne del pezzo
            p.setX(endX);
            p.setY(endY);

            // 3. CAMBIO TURNO: Solo se la mossa è valida
            whiteTurn = !whiteTurn;
            return true; // La mossa è avvenuta con successo
        }

        // 4. Se isValidMove era FALSE, restituiamo false e NON tocchiamo la grid
        return false;
    }
}