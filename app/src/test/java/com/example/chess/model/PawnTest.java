package com.example.chess.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (local) per il movimento del Pedone (Pawn.isValidMove),
 * testato in isolamento su scacchiere costruite ad hoc.
 */
public class PawnTest {

    private Board board;

    private static int col(String pos) { return pos.toLowerCase().charAt(0) - 'a'; }
    private static int row(String pos) { return 8 - Character.getNumericValue(pos.charAt(1)); }

    private static Piece[][] emptyGrid() { return new Piece[8][8]; }

    private Pawn placePawn(Piece[][] g, String pos, boolean isWhite) {
        Pawn p = new Pawn(row(pos), col(pos), isWhite);
        g[row(pos)][col(pos)] = p;
        return p;
    }

    @Before
    public void setUp() {
        board = new Board();
    }

    @Test
    public void whitePawn_singleStepForward_isValid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e2", true);
        board.setupCustomBoard(g, true);
        assertTrue(p.isValidMove(row("e3"), col("e3"), board));
    }

    @Test
    public void whitePawn_doubleStepFromStartingRow_isValid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e2", true);
        board.setupCustomBoard(g, true);
        assertTrue(p.isValidMove(row("e4"), col("e4"), board));
    }

    @Test
    public void whitePawn_doubleStepNotFromStartingRow_isInvalid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e3", true);
        board.setupCustomBoard(g, true);
        assertFalse(p.isValidMove(row("e5"), col("e5"), board));
    }

    @Test
    public void whitePawn_cannotMoveBackwards() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e4", true);
        board.setupCustomBoard(g, true);
        assertFalse(p.isValidMove(row("e3"), col("e3"), board));
    }

    @Test
    public void blackPawn_movesDownTheBoard() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e7", false);
        board.setupCustomBoard(g, false);
        assertTrue(p.isValidMove(row("e6"), col("e6"), board));
        assertTrue(p.isValidMove(row("e5"), col("e5"), board)); // doppio passo
        assertFalse(p.isValidMove(row("e8"), col("e8"), board)); // indietro
    }

    @Test
    public void pawn_forwardMoveBlockedByAnyPiece_isInvalid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e2", true);
        placePawn(g, "e3", false); // ostacolo nemico davanti
        board.setupCustomBoard(g, true);
        // Il pedone non cattura in avanti...
        assertFalse(p.isValidMove(row("e3"), col("e3"), board));
        // ...e il doppio passo è bloccato dal pezzo intermedio
        assertFalse(p.isValidMove(row("e4"), col("e4"), board));
    }

    @Test
    public void pawn_doubleStepBlockedOnDestination_isInvalid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e2", true);
        placePawn(g, "e4", false); // destinazione occupata
        board.setupCustomBoard(g, true);
        assertFalse(p.isValidMove(row("e4"), col("e4"), board));
    }

    @Test
    public void pawn_diagonalCaptureOfEnemy_isValid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e4", true);
        placePawn(g, "d5", false);
        board.setupCustomBoard(g, true);
        assertTrue(p.isValidMove(row("d5"), col("d5"), board));
    }

    @Test
    public void pawn_diagonalCaptureOfOwnPiece_isInvalid() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e4", true);
        placePawn(g, "d5", true); // pezzo amico
        board.setupCustomBoard(g, true);
        assertFalse(p.isValidMove(row("d5"), col("d5"), board));
    }

    @Test
    public void pawn_diagonalMoveToEmptySquare_isInvalidWithoutEnPassant() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e4", true);
        board.setupCustomBoard(g, true); // setupCustomBoard azzera enPassantColumn
        assertFalse(p.isValidMove(row("d5"), col("d5"), board));
    }

    @Test
    public void pawn_enPassant_isValidOnlyOnCorrectRow() {
        Piece[][] g = emptyGrid();
        Pawn p = placePawn(g, "e5", true);
        board.setupCustomBoard(g, true);
        board.setEnPassantColumn(col("d5")); // colonna 'd' disponibile

        // Casa d6 (riga 2): cattura en passant valida per il bianco
        assertTrue(p.isValidMove(row("d6"), col("d6"), board));
    }
}
