package com.example.chess.model;

public class Board {
    private Piece[][] grid;
    private boolean whiteTurn = true;
    private int enPassantColumn = -1;

    public Board() {
        grid = new Piece[8][8];
        setupBoard();
    }

    public Board(Piece[][] customGrid, boolean whiteTurnToStart) {
        this.grid = customGrid;
        this.whiteTurn = whiteTurnToStart;
    }

    // --- GETTER E SETTER (Risolvono gli errori in MainActivity) ---
    public Piece[][] getGrid() { return grid; }
    public boolean isWhiteTurn() { return whiteTurn; }
    public void setWhiteTurn(boolean whiteTurn) { this.whiteTurn = whiteTurn; }
    public int getEnPassantColumn() { return enPassantColumn; }
    public void setEnPassantColumn(int col) { this.enPassantColumn = col; }

    // --- GET PIECE (Risolve l'errore in ChessAdapter) ---
    public Piece getPiece(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return grid[x][y];
    }

    private void setupBoard() {
        // NERI
        grid[0][0] = new Rook(0, 0, false);
        grid[0][1] = new Knight(0, 1, false);
        grid[0][2] = new Bishop(0, 2, false);
        grid[0][3] = new Queen(0, 3, false);
        grid[0][4] = new King(0, 4, false);
        grid[0][5] = new Bishop(0, 5, false);
        grid[0][6] = new Knight(0, 6, false);
        grid[0][7] = new Rook(0, 7, false);
        for (int i = 0; i < 8; i++) grid[1][i] = new Pawn(1, i, false);

        // BIANCHI
        for (int i = 0; i < 8; i++) grid[6][i] = new Pawn(6, i, true);
        grid[7][0] = new Rook(7, 0, true);
        grid[7][1] = new Knight(7, 1, true);
        grid[7][2] = new Bishop(7, 2, true);
        grid[7][3] = new Queen(7, 3, true);
        grid[7][4] = new King(7, 4, true);
        grid[7][5] = new Bishop(7, 5, true);
        grid[7][6] = new Knight(7, 6, true);
        grid[7][7] = new Rook(7, 7, true);
    }

    public boolean isSquareAttacked(int x, int y, boolean attackedByWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece enemy = grid[r][c];
                if (enemy != null && enemy.isWhite() == attackedByWhite) {
                    if (enemy instanceof King) {
                        if (Math.abs(enemy.getX() - x) <= 1 && Math.abs(enemy.getY() - y) <= 1) return true;
                    } else if (enemy instanceof Pawn) {
                        int dir = enemy.isWhite() ? -1 : 1;
                        if (x == enemy.getX() + dir && Math.abs(y - enemy.getY()) == 1) return true;
                    } else {
                        if (enemy.isValidMove(x, y, this)) return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isKingInCheck(boolean isWhite) {
        int kX = -1, kY = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p instanceof King && p.isWhite() == isWhite) { kX = r; kY = c; break; }
            }
        }
        if (kX == -1) return false;
        return isSquareAttacked(kX, kY, !isWhite);
    }

    // --- HAS ANY LEGAL MOVES (Risolve l'errore in MainActivity) ---
    public boolean hasAnyLegalMoves(boolean isWhite) {
        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {
                Piece p = grid[startX][startY];
                if (p != null && p.isWhite() == isWhite) {
                    for (int endX = 0; endX < 8; endX++) {
                        for (int endY = 0; endY < 8; endY++) {
                            if (p.isValidMove(endX, endY, this)) {
                                Piece cap = grid[endX][endY];
                                int oldX = p.getX(), oldY = p.getY();
                                grid[endX][endY] = p; grid[startX][startY] = null;
                                p.setX(endX); p.setY(endY);

                                boolean safe = !isKingInCheck(isWhite);

                                grid[startX][startY] = p; grid[endX][endY] = cap;
                                p.setX(oldX); p.setY(oldY);

                                if (safe) return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean movePiece(int startX, int startY, int endX, int endY) {
        Piece p = grid[startX][startY];
        if (p == null || p.isWhite() != whiteTurn) return false;

        if (p.isValidMove(endX, endY, this)) {
            boolean isCastling = (p instanceof King && Math.abs(endY - startY) == 2);
            boolean isEP = (p instanceof Pawn && startY != endY && grid[endX][endY] == null);

            Piece captured = isEP ? grid[startX][endY] : grid[endX][endY];
            int oldX = p.getX(), oldY = p.getY();

            grid[endX][endY] = p; grid[startX][startY] = null;
            if (isEP) grid[startX][endY] = null;
            p.setX(endX); p.setY(endY);

            Piece rook = null;
            int rSX = -1, rEX = -1;
            if (isCastling) {
                rSX = (endY > startY) ? 7 : 0;
                rEX = (endY > startY) ? endY - 1 : endY + 1;
                rook = grid[startX][rSX];
                if (rook != null) {
                    grid[startX][rEX] = rook; grid[startX][rSX] = null;
                    rook.setX(startX); rook.setY(rEX);
                }
            }

            if (isKingInCheck(whiteTurn)) {
                grid[startX][startY] = p; p.setX(oldX); p.setY(oldY);
                if (isEP) { grid[startX][endY] = captured; grid[endX][endY] = null; }
                else { grid[endX][endY] = captured; }
                if (isCastling && rook != null) {
                    grid[startX][rSX] = rook; grid[startX][rEX] = null;
                    rook.setX(startX); rook.setY(rSX);
                }
                return false;
            }

            if (p instanceof King) ((King) p).setHasMoved(true);
            if (p instanceof Rook) ((Rook) p).setHasMoved(true);
            if (isCastling && rook instanceof Rook) ((Rook) rook).setHasMoved(true);
            if (p instanceof Pawn && (endX == 0 || endX == 7)) grid[endX][endY] = new Queen(endX, endY, p.isWhite());

            enPassantColumn = (p instanceof Pawn && Math.abs(endX - startX) == 2) ? endY : -1;
            whiteTurn = !whiteTurn;
            return true;
        }
        return false;
    }

    public List<Integer> getLegalMovesForPiece(int row, int col) {
        List<Integer> legalMoves = new ArrayList<>();
        Piece piece = getPiece(row, col);

        if (piece == null) return legalMoves;

        // Cicliamo su tutte le 64 caselle della scacchiera
        for (int targetRow = 0; targetRow < 8; targetRow++) {
            for (int targetCol = 0; targetCol < 8; targetCol++) {
                // Verifichiamo se la mossa è valida secondo le regole del pezzo
                // e se non lascia il proprio Re sotto scacco
                if (isValidMove(row, col, targetRow, targetCol)) {
                    // Convertiamo le coordinate row/col in posizione 0-63
                    int position = targetRow * 8 + targetCol;
                    legalMoves.add(position);
                }
            }
        }
        return legalMoves;
    }
}