package com.example.chess.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (local) per il movimento dei pezzi maggiori e del re,
 * testati in isolamento su scacchiere costruite ad hoc.
 */
public class PieceMovementTest {

    private Board board;

    private static int col(String pos) { return pos.toLowerCase().charAt(0) - 'a'; }
    private static int row(String pos) { return 8 - Character.getNumericValue(pos.charAt(1)); }

    private static Piece[][] emptyGrid() { return new Piece[8][8]; }

    private <T extends Piece> T place(Piece[][] g, T piece, String pos) {
        piece.setX(row(pos));
        piece.setY(col(pos));
        g[row(pos)][col(pos)] = piece;
        return piece;
    }

    private boolean valid(Piece p, String target) {
        return p.isValidMove(row(target), col(target), board);
    }

    @Before
    public void setUp() {
        board = new Board();
    }

    // ==================== CAVALLO ====================

    @Test
    public void knight_movesInLShape() {
        Piece[][] g = emptyGrid();
        Knight n = place(g, new Knight(0, 0, true), "d4");
        board.setupCustomBoard(g, true);

        // Tutte le 8 destinazioni a "L"
        String[] targets = {"b3", "b5", "c2", "c6", "e2", "e6", "f3", "f5"};
        for (String t : targets) assertTrue("Cavallo d4 -> " + t, valid(n, t));

        // Mosse non a "L"
        assertFalse(valid(n, "d5"));
        assertFalse(valid(n, "e5"));
        assertFalse(valid(n, "d4")); // ferma sul posto
    }

    @Test
    public void knight_canJumpOverPieces() {
        Piece[][] g = emptyGrid();
        Knight n = place(g, new Knight(0, 0, true), "d4");
        // Lo circondiamo completamente di pedoni
        place(g, new Pawn(0, 0, true), "c3");
        place(g, new Pawn(0, 0, true), "c4");
        place(g, new Pawn(0, 0, true), "c5");
        place(g, new Pawn(0, 0, true), "d3");
        place(g, new Pawn(0, 0, true), "d5");
        place(g, new Pawn(0, 0, true), "e3");
        place(g, new Pawn(0, 0, true), "e4");
        place(g, new Pawn(0, 0, true), "e5");
        board.setupCustomBoard(g, true);

        assertTrue(valid(n, "f5")); // salta gli ostacoli
    }

    @Test
    public void knight_cannotLandOnOwnPiece_butCanCaptureEnemy() {
        Piece[][] g = emptyGrid();
        Knight n = place(g, new Knight(0, 0, true), "d4");
        place(g, new Pawn(0, 0, true), "f5");  // amico
        place(g, new Pawn(0, 0, false), "f3"); // nemico
        board.setupCustomBoard(g, true);

        assertFalse(valid(n, "f5"));
        assertTrue(valid(n, "f3"));
    }

    // ==================== ALFIERE ====================

    @Test
    public void bishop_movesDiagonally_notStraight() {
        Piece[][] g = emptyGrid();
        Bishop b = place(g, new Bishop(0, 0, true), "d4");
        board.setupCustomBoard(g, true);

        assertTrue(valid(b, "a1"));
        assertTrue(valid(b, "h8"));
        assertTrue(valid(b, "a7"));
        assertTrue(valid(b, "g1"));
        assertFalse(valid(b, "d8")); // verticale
        assertFalse(valid(b, "h4")); // orizzontale
        assertFalse(valid(b, "d4")); // ferma sul posto
    }

    @Test
    public void bishop_isBlockedByPieceOnPath() {
        Piece[][] g = emptyGrid();
        Bishop b = place(g, new Bishop(0, 0, true), "d4");
        place(g, new Pawn(0, 0, false), "f6"); // ostacolo sulla diagonale
        board.setupCustomBoard(g, true);

        assertTrue(valid(b, "e5"));
        assertTrue(valid(b, "f6"));   // può catturare l'ostacolo nemico
        assertFalse(valid(b, "g7"));  // ma non oltrepassarlo
        assertFalse(valid(b, "h8"));
    }

    // ==================== TORRE ====================

    @Test
    public void rook_movesStraight_notDiagonally() {
        Piece[][] g = emptyGrid();
        Rook r = place(g, new Rook(0, 0, true), "d4");
        board.setupCustomBoard(g, true);

        assertTrue(valid(r, "d8"));
        assertTrue(valid(r, "d1"));
        assertTrue(valid(r, "a4"));
        assertTrue(valid(r, "h4"));
        assertFalse(valid(r, "e5")); // diagonale
        assertFalse(valid(r, "d4")); // ferma sul posto
    }

    @Test
    public void rook_isBlockedByPieceOnPath() {
        Piece[][] g = emptyGrid();
        Rook r = place(g, new Rook(0, 0, true), "d4");
        place(g, new Pawn(0, 0, true), "d6"); // ostacolo amico
        board.setupCustomBoard(g, true);

        assertTrue(valid(r, "d5"));
        assertFalse(valid(r, "d6")); // casa occupata da un amico
        assertFalse(valid(r, "d7")); // oltre l'ostacolo
    }

    // ==================== DONNA ====================

    @Test
    public void queen_movesStraightAndDiagonally() {
        Piece[][] g = emptyGrid();
        Queen q = place(g, new Queen(0, 0, true), "d4");
        board.setupCustomBoard(g, true);

        assertTrue(valid(q, "d8")); // verticale
        assertTrue(valid(q, "a4")); // orizzontale
        assertTrue(valid(q, "h8")); // diagonale
        assertTrue(valid(q, "a1")); // diagonale
        assertFalse(valid(q, "e6")); // mossa da cavallo
        assertFalse(valid(q, "f5")); // né linea né diagonale
    }

    @Test
    public void queen_isBlockedByPieceOnPath() {
        Piece[][] g = emptyGrid();
        Queen q = place(g, new Queen(0, 0, true), "d4");
        place(g, new Pawn(0, 0, false), "d6");
        board.setupCustomBoard(g, true);

        assertTrue(valid(q, "d6"));  // cattura
        assertFalse(valid(q, "d7")); // oltre l'ostacolo
    }

    // ==================== RE ====================

    @Test
    public void king_movesOneSquareInAnyDirection() {
        Piece[][] g = emptyGrid();
        King k = place(g, new King(0, 0, true), "d4");
        board.setupCustomBoard(g, true);

        String[] targets = {"c3", "c4", "c5", "d3", "d5", "e3", "e4", "e5"};
        for (String t : targets) assertTrue("Re d4 -> " + t, valid(k, t));

        assertFalse(valid(k, "d6")); // due case (non è arrocco: colonna invariata)
        assertFalse(valid(k, "f6")); // due case in diagonale
    }

    @Test
    public void king_cannotLandOnOwnPiece_butCanCaptureEnemy() {
        Piece[][] g = emptyGrid();
        King k = place(g, new King(0, 0, true), "d4");
        place(g, new Pawn(0, 0, true), "d5");  // amico
        place(g, new Pawn(0, 0, false), "e5"); // nemico
        board.setupCustomBoard(g, true);

        assertFalse(valid(k, "d5"));
        assertTrue(valid(k, "e5"));
    }
}
