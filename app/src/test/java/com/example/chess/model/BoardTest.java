package com.example.chess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (local) per la classe Board.
 * Copre casi normali e casi limite: mosse valide/invalide, gestione del turno,
 * catture, scacco, pezzi inchiodati, arrocco, en passant, promozione,
 * scacco matto e stallo.
 *
 * Convenzione coordinate del progetto:
 *   x = riga (0 = ottava traversa, 7 = prima traversa)
 *   y = colonna (0 = colonna 'a', 7 = colonna 'h')
 */
public class BoardTest {

    private Board board;

    // --- Helper di traduzione coordinate (stessa convenzione di QuizRepository) ---
    private static int col(String pos) { return pos.toLowerCase().charAt(0) - 'a'; }
    private static int row(String pos) { return 8 - Character.getNumericValue(pos.charAt(1)); }

    private static Piece[][] emptyGrid() { return new Piece[8][8]; }

    /** Esegue una mossa in notazione "e2"->"e4" sulla board. */
    private boolean move(String from, String to) {
        return board.movePiece(row(from), col(from), row(to), col(to));
    }

    @Before
    public void setUp() {
        board = new Board();
    }

    // ==========================================================
    // SETUP INIZIALE
    // ==========================================================

    @Test
    public void newBoard_hasStandardSetup() {
        assertTrue(board.isWhiteTurn());
        assertTrue(board.getPiece(row("e1"), col("e1")) instanceof King);
        assertTrue(board.getPiece(row("e1"), col("e1")).isWhite());
        assertTrue(board.getPiece(row("e8"), col("e8")) instanceof King);
        assertFalse(board.getPiece(row("e8"), col("e8")).isWhite());
        assertTrue(board.getPiece(row("d1"), col("d1")) instanceof Queen);
        assertTrue(board.getPiece(row("a1"), col("a1")) instanceof Rook);
        // Tutti i pedoni al loro posto
        for (int c = 0; c < 8; c++) {
            assertTrue(board.getPiece(6, c) instanceof Pawn); // bianchi
            assertTrue(board.getPiece(1, c) instanceof Pawn); // neri
        }
        // Le righe centrali sono vuote
        for (int r = 2; r <= 5; r++) {
            for (int c = 0; c < 8; c++) {
                assertNull(board.getPiece(r, c));
            }
        }
        assertTrue(board.getCapturedWhite().isEmpty());
        assertTrue(board.getCapturedBlack().isEmpty());
    }

    @Test
    public void getPiece_outOfBounds_returnsNullInsteadOfCrashing() {
        // Caso limite: coordinate fuori dalla scacchiera
        assertNull(board.getPiece(-1, 0));
        assertNull(board.getPiece(0, -1));
        assertNull(board.getPiece(8, 0));
        assertNull(board.getPiece(0, 8));
    }

    // ==========================================================
    // GESTIONE DEL TURNO
    // ==========================================================

    @Test
    public void movePiece_validMove_switchesTurn() {
        assertTrue(move("e2", "e4"));
        assertFalse(board.isWhiteTurn());
        assertTrue(move("e7", "e5"));
        assertTrue(board.isWhiteTurn());
    }

    @Test
    public void movePiece_wrongTurn_isRejected() {
        // Il nero non può muovere per primo
        assertFalse(move("e7", "e5"));
        assertTrue(board.isWhiteTurn());
        // Il bianco non può muovere due volte di fila
        assertTrue(move("e2", "e4"));
        assertFalse(move("d2", "d4"));
    }

    @Test
    public void movePiece_emptySquare_isRejected() {
        assertFalse(move("e4", "e5"));
        assertTrue(board.isWhiteTurn());
    }

    @Test
    public void movePiece_invalidGeometry_isRejected() {
        // Una torre non può muoversi in diagonale (e in apertura è anche bloccata)
        assertFalse(move("a1", "c3"));
        // Un alfiere non può saltare il proprio pedone
        assertFalse(move("f1", "c4"));
        assertTrue(board.isWhiteTurn());
    }

    // ==========================================================
    // CATTURE
    // ==========================================================

    @Test
    public void movePiece_capture_addsPieceToCapturedList() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        g[row("d1")][col("d1")] = new Queen(0, 0, true);
        g[row("d7")][col("d7")] = new Pawn(0, 0, false);
        board.setupCustomBoard(g, true);

        assertTrue(move("d1", "d7")); // Dxd7
        assertEquals(1, board.getCapturedBlack().size());
        assertTrue(board.getCapturedBlack().get(0) instanceof Pawn);
        assertTrue(board.getCapturedWhite().isEmpty());
        assertTrue(board.getPiece(row("d7"), col("d7")) instanceof Queen);
    }

    @Test
    public void movePiece_cannotCaptureOwnPiece() {
        // La donna bianca non può catturare il proprio pedone in d2
        assertFalse(move("d1", "d2"));
    }

    // ==========================================================
    // SCACCO E PEZZI INCHIODATI
    // ==========================================================

    @Test
    public void isKingInCheck_detectsCheckFromRook() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("e8")][col("e8")] = new Rook(0, 0, false);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertTrue(board.isKingInCheck(true));
        assertFalse(board.isKingInCheck(false));
    }

    @Test
    public void movePiece_pinnedPieceCannotMove_andBoardIsRestored() {
        // Torre bianca in e2 inchiodata dalla torre nera in e8
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("e2")][col("e2")] = new Rook(0, 0, true);
        g[row("e8")][col("e8")] = new Rook(0, 0, false);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertFalse(move("e2", "a2")); // lascerebbe il re in scacco

        // La scacchiera deve essere ripristinata e il turno non deve cambiare
        assertTrue(board.getPiece(row("e2"), col("e2")) instanceof Rook);
        assertNull(board.getPiece(row("a2"), col("a2")));
        assertTrue(board.isWhiteTurn());
        assertTrue(board.getCapturedBlack().isEmpty());
    }

    @Test
    public void movePiece_kingCannotMoveIntoCheck() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("d8")][col("d8")] = new Rook(0, 0, false);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertFalse(move("e1", "d1")); // la colonna 'd' è controllata dalla torre
        assertTrue(move("e1", "f1"));  // la colonna 'f' è libera
    }

    // ==========================================================
    // ARROCCO
    // ==========================================================

    @Test
    public void castling_kingSide_movesKingAndRook() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h1")][col("h1")] = new Rook(0, 0, true);
        g[row("e8")][col("e8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertTrue(move("e1", "g1")); // arrocco corto
        assertTrue(board.getPiece(row("g1"), col("g1")) instanceof King);
        assertTrue(board.getPiece(row("f1"), col("f1")) instanceof Rook);
        assertNull(board.getPiece(row("h1"), col("h1")));
        assertNull(board.getPiece(row("e1"), col("e1")));
    }

    @Test
    public void castling_throughAttackedSquare_isRejected() {
        // La torre nera in f8 controlla f1: l'arrocco corto deve essere vietato
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h1")][col("h1")] = new Rook(0, 0, true);
        g[row("f8")][col("f8")] = new Rook(0, 0, false);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertFalse(move("e1", "g1"));
    }

    @Test
    public void castling_afterKingHasMoved_isRejected() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h1")][col("h1")] = new Rook(0, 0, true);
        g[row("e8")][col("e8")] = new King(0, 0, false);
        board.setupCustomBoard(g, true);

        assertTrue(move("e1", "f1")); // il re si muove...
        assertTrue(move("e8", "d8"));
        assertTrue(move("f1", "e1")); // ...e torna indietro
        assertTrue(move("d8", "e8"));

        assertFalse(move("e1", "g1")); // l'arrocco non è più possibile
    }

    // ==========================================================
    // EN PASSANT
    // ==========================================================

    @Test
    public void enPassant_captureRemovesEnemyPawn() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("e8")][col("e8")] = new King(0, 0, false);
        g[row("e5")][col("e5")] = new Pawn(0, 0, true);
        g[row("d7")][col("d7")] = new Pawn(0, 0, false);
        board.setupCustomBoard(g, false); // muove il nero

        assertTrue(move("d7", "d5"));                       // doppio passo nero
        assertEquals(col("d5"), board.getEnPassantColumn());
        assertTrue(move("e5", "d6"));                       // cattura en passant

        assertTrue(board.getPiece(row("d6"), col("d6")) instanceof Pawn);
        assertNull(board.getPiece(row("d5"), col("d5")));   // il pedone nero è stato rimosso
        assertEquals(1, board.getCapturedBlack().size());
    }

    @Test
    public void enPassant_windowExpiresAfterOneMove() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("e8")][col("e8")] = new King(0, 0, false);
        g[row("e5")][col("e5")] = new Pawn(0, 0, true);
        g[row("d7")][col("d7")] = new Pawn(0, 0, false);
        g[row("a2")][col("a2")] = new Pawn(0, 0, true);
        g[row("h7")][col("h7")] = new Pawn(0, 0, false);
        board.setupCustomBoard(g, false);

        assertTrue(move("d7", "d5")); // doppio passo
        assertTrue(move("a2", "a3")); // il bianco gioca altro
        assertTrue(move("h7", "h6"));

        assertEquals(-1, board.getEnPassantColumn());
        assertFalse(move("e5", "d6")); // en passant non più disponibile
    }

    // ==========================================================
    // PROMOZIONE
    // ==========================================================

    @Test
    public void promotion_defaultsToQueen() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        g[row("a7")][col("a7")] = new Pawn(0, 0, true);
        board.setupCustomBoard(g, true);

        assertTrue(move("a7", "a8"));
        assertTrue(board.getPiece(row("a8"), col("a8")) instanceof Queen);
        assertTrue(board.getPiece(row("a8"), col("a8")).isWhite());
    }

    @Test
    public void promotion_underpromotionToKnight() {
        Piece[][] g = emptyGrid();
        g[row("e1")][col("e1")] = new King(0, 0, true);
        g[row("h8")][col("h8")] = new King(0, 0, false);
        g[row("a7")][col("a7")] = new Pawn(0, 0, true);
        board.setupCustomBoard(g, true);

        assertTrue(board.movePiece(row("a7"), col("a7"), row("a8"), col("a8"), 'n'));
        assertTrue(board.getPiece(row("a8"), col("a8")) instanceof Knight);
    }

    // ==========================================================
    // MATTO E STALLO
    // ==========================================================

    @Test
    public void checkmate_backRankMate_isDetected() {
        // Re nero in h8 chiuso dai propri pedoni; Te1-e8 è matto del corridoio
        Piece[][] g = emptyGrid();
        g[row("h8")][col("h8")] = new King(0, 0, false);
        g[row("g7")][col("g7")] = new Pawn(0, 0, false);
        g[row("h7")][col("h7")] = new Pawn(0, 0, false);
        g[row("e1")][col("e1")] = new Rook(0, 0, true);
        g[row("g1")][col("g1")] = new King(0, 0, true);
        board.setupCustomBoard(g, true);

        assertTrue(move("e1", "e8"));
        assertTrue(board.isKingInCheck(false));
        assertFalse(board.hasAnyLegalMoves(false)); // scacco matto
    }

    @Test
    public void stalemate_isDetected() {
        // Re nero in a8, donna bianca in b6: il nero non è in scacco
        // ma non ha mosse legali -> stallo
        Piece[][] g = emptyGrid();
        g[row("a8")][col("a8")] = new King(0, 0, false);
        g[row("b6")][col("b6")] = new Queen(0, 0, true);
        g[row("g1")][col("g1")] = new King(0, 0, true);
        board.setupCustomBoard(g, false);

        assertFalse(board.isKingInCheck(false));
        assertFalse(board.hasAnyLegalMoves(false)); // stallo
    }

    @Test
    public void hasAnyLegalMoves_startingPosition_isTrueForBothSides() {
        assertTrue(board.hasAnyLegalMoves(true));
        assertTrue(board.hasAnyLegalMoves(false));
    }

    // ==========================================================
    // LEGAL MOVES PER LA UI
    // ==========================================================

    @Test
    public void getLegalMovesForPiece_knightOnStartingSquare_hasTwoMoves() {
        // Il cavallo in b1 può andare solo in a3 e c3
        assertEquals(2, board.getLegalMovesForPiece(row("b1"), col("b1")).size());
    }

    @Test
    public void getLegalMovesForPiece_blockedRook_hasNoMoves() {
        assertTrue(board.getLegalMovesForPiece(row("a1"), col("a1")).isEmpty());
    }

    // ==========================================================
    // EXPORT FEN
    // ==========================================================

    @Test
    public void toFen_startingPosition_isStandardFen() {
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                board.toFen());
    }

    @Test
    public void toFen_afterE4_encodesBoardTurnAndEnPassant() {
        assertTrue(move("e2", "e4"));
        String fen = board.toFen();
        assertTrue(fen.startsWith("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b "));
        assertTrue(fen.contains(" e3 ")); // casa di en passant disponibile
    }
}
