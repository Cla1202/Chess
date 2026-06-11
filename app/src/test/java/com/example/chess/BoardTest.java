package com.example.chess;

import com.example.chess.model.*;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test unitari per la classe Board e la logica di gioco.
 * Copertura: inizializzazione, movimenti, scacco, scaccomatto, en passant, arrocco, promozione.
 */
public class BoardTest {

    private Board board;

    @Before
    public void setUp() {
        board = new Board(); // scacchiera standard
    }

    // -------------------------------------------------------------------------
    // INIZIALIZZAZIONE
    // -------------------------------------------------------------------------

    @Test
    public void boardInit_correctPiecesInStartingPositions() {
        assertTrue(board.getPiece(0, 0) instanceof Rook);
        assertTrue(board.getPiece(0, 4) instanceof King);
        assertTrue(board.getPiece(7, 4) instanceof King);
        assertTrue(board.getPiece(1, 3) instanceof Pawn);
        assertTrue(board.getPiece(6, 3) instanceof Pawn);
    }

    @Test
    public void boardInit_whiteTurnFirst() {
        assertTrue(board.isWhiteTurn());
    }

    @Test
    public void boardInit_emptyBoardConstructor() {
        Board emptyBoard = new Board(true);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                assertNull(emptyBoard.getPiece(r, c));
    }

    @Test
    public void isValidSquare_boundaryCheck() {
        assertTrue(board.isValidSquare(0, 0));
        assertTrue(board.isValidSquare(7, 7));
        assertFalse(board.isValidSquare(-1, 0));
        assertFalse(board.isValidSquare(8, 0));
        assertFalse(board.isValidSquare(0, 8));
    }

    // -------------------------------------------------------------------------
    // MOVIMENTI DI BASE
    // -------------------------------------------------------------------------

    @Test
    public void movePiece_whitePawnAdvancesOneSquare() {
        // pedone bianco da riga 6 a riga 5
        boolean moved = board.movePiece(6, 4, 5, 4);
        assertTrue(moved);
        assertNull(board.getPiece(6, 4));
        assertTrue(board.getPiece(5, 4) instanceof Pawn);
    }

    @Test
    public void movePiece_whitePawnAdvancesTwoSquaresFromStart() {
        boolean moved = board.movePiece(6, 0, 4, 0);
        assertTrue(moved);
        assertTrue(board.getPiece(4, 0) instanceof Pawn);
    }

    @Test
    public void movePiece_pawnCannotMoveTwoSquaresIfNotOnStartRow() {
        board.movePiece(6, 0, 5, 0); // bianco avanza
        board.movePiece(1, 0, 2, 0); // nero avanza (cambio turno)
        boolean moved = board.movePiece(5, 0, 3, 0); // tenta di muovere di 2 di nuovo
        assertFalse(moved);
    }

    @Test
    public void movePiece_wrongTurnReturnsFalse() {
        // è il turno del bianco, tentiamo di muovere un nero
        boolean moved = board.movePiece(1, 0, 2, 0);
        assertFalse(moved);
    }

    @Test
    public void movePiece_turnAlternatesAfterLegalMove() {
        board.movePiece(6, 0, 5, 0);
        assertFalse(board.isWhiteTurn());
        board.movePiece(1, 0, 2, 0);
        assertTrue(board.isWhiteTurn());
    }

    @Test
    public void movePiece_noPieceAtSourceReturnsFalse() {
        boolean moved = board.movePiece(4, 4, 3, 4); // quadrato vuoto
        assertFalse(moved);
    }

    // -------------------------------------------------------------------------
    // CAVALLO
    // -------------------------------------------------------------------------

    @Test
    public void knight_validLShapeMove() {
        Knight knight = new Knight(4, 4, true);
        Board b = new Board(true);
        b.setPiece(4, 4, knight);
        assertTrue(knight.isValidMove(2, 3, b));
        assertTrue(knight.isValidMove(6, 5, b));
        assertTrue(knight.isValidMove(3, 2, b));
    }

    @Test
    public void knight_invalidStraightMove() {
        Knight knight = new Knight(4, 4, true);
        Board b = new Board(true);
        b.setPiece(4, 4, knight);
        assertFalse(knight.isValidMove(4, 5, b));
        assertFalse(knight.isValidMove(5, 5, b));
    }

    @Test
    public void knight_cannotCaptureOwnPiece() {
        Board b = new Board(true);
        Knight knight = new Knight(4, 4, true);
        Pawn own = new Pawn(2, 3, true);
        b.setPiece(4, 4, knight);
        b.setPiece(2, 3, own);
        assertFalse(knight.isValidMove(2, 3, b));
    }

    @Test
    public void knight_canCaptureEnemyPiece() {
        Board b = new Board(true);
        Knight knight = new Knight(4, 4, true);
        Pawn enemy = new Pawn(2, 3, false);
        b.setPiece(4, 4, knight);
        b.setPiece(2, 3, enemy);
        assertTrue(knight.isValidMove(2, 3, b));
    }

    // -------------------------------------------------------------------------
    // ALFIERE
    // -------------------------------------------------------------------------

    @Test
    public void bishop_validDiagonalMove() {
        Board b = new Board(true);
        Bishop bishop = new Bishop(4, 4, true);
        b.setPiece(4, 4, bishop);
        assertTrue(bishop.isValidMove(6, 6, b));
        assertTrue(bishop.isValidMove(2, 2, b));
        assertTrue(bishop.isValidMove(6, 2, b));
    }

    @Test
    public void bishop_blockedByPieceOnPath() {
        Board b = new Board(true);
        Bishop bishop = new Bishop(4, 4, true);
        Pawn blocker = new Pawn(5, 5, false);
        b.setPiece(4, 4, bishop);
        b.setPiece(5, 5, blocker);
        assertFalse(bishop.isValidMove(6, 6, b));
    }

    @Test
    public void bishop_cannotMoveStraight() {
        Board b = new Board(true);
        Bishop bishop = new Bishop(4, 4, true);
        b.setPiece(4, 4, bishop);
        assertFalse(bishop.isValidMove(4, 6, b));
        assertFalse(bishop.isValidMove(6, 4, b));
    }

    // -------------------------------------------------------------------------
    // TORRE
    // -------------------------------------------------------------------------

    @Test
    public void rook_validStraightMove() {
        Board b = new Board(true);
        Rook rook = new Rook(4, 4, true);
        b.setPiece(4, 4, rook);
        assertTrue(rook.isValidMove(4, 7, b));
        assertTrue(rook.isValidMove(0, 4, b));
    }

    @Test
    public void rook_blockedBySameColorPiece() {
        Board b = new Board(true);
        Rook rook = new Rook(4, 0, true);
        Pawn blocker = new Pawn(4, 3, true);
        b.setPiece(4, 0, rook);
        b.setPiece(4, 3, blocker);
        assertFalse(rook.isValidMove(4, 5, b));
    }

    @Test
    public void rook_cannotMoveDiagonally() {
        Board b = new Board(true);
        Rook rook = new Rook(4, 4, true);
        b.setPiece(4, 4, rook);
        assertFalse(rook.isValidMove(5, 5, b));
    }

    // -------------------------------------------------------------------------
    // REGINA
    // -------------------------------------------------------------------------

    @Test
    public void queen_canMoveStraightAndDiagonal() {
        Board b = new Board(true);
        Queen queen = new Queen(4, 4, true);
        b.setPiece(4, 4, queen);
        assertTrue(queen.isValidMove(4, 7, b)); // dritta
        assertTrue(queen.isValidMove(7, 4, b)); // dritta verticale
        assertTrue(queen.isValidMove(6, 6, b)); // diagonale
    }

    @Test
    public void queen_cannotMoveInLShape() {
        Board b = new Board(true);
        Queen queen = new Queen(4, 4, true);
        b.setPiece(4, 4, queen);
        assertFalse(queen.isValidMove(6, 5, b)); // mossa da cavallo
    }

    // -------------------------------------------------------------------------
    // RE & SCACCO
    // -------------------------------------------------------------------------

    @Test
    public void king_validSingleStepMove() {
        Board b = new Board(true);
        King king = new King(4, 4, true);
        b.setPiece(4, 4, king);
        assertTrue(king.isValidMove(4, 5, b));
        assertTrue(king.isValidMove(5, 5, b));
    }

    @Test
    public void king_cannotMoveTwoSquaresWithoutCastle() {
        Board b = new Board(true);
        King king = new King(4, 4, true);
        b.setPiece(4, 4, king);
        assertFalse(king.isValidMove(4, 6, b)); // hasMoved = false ma non c'è la torre
    }

    @Test
    public void isKingInCheck_detectsCheck() {
        Board b = new Board(true);
        King whiteKing = new King(4, 4, true);
        Rook blackRook = new Rook(0, 4, false);
        b.setPiece(4, 4, whiteKing);
        b.setPiece(0, 4, blackRook);
        assertTrue(b.isKingInCheck(true));
    }

    @Test
    public void isKingInCheck_noCheckOnEmptyPath() {
        Board b = new Board(true);
        King whiteKing = new King(4, 4, true);
        Rook blackRook = new Rook(0, 4, false);
        Pawn blocker = new Pawn(2, 4, true); // blocca la torre
        b.setPiece(4, 4, whiteKing);
        b.setPiece(0, 4, blackRook);
        b.setPiece(2, 4, blocker);
        assertFalse(b.isKingInCheck(true));
    }

    @Test
    public void movePiece_cannotMoveIntoCheck() {
        // Re bianco in (7,4). Torre nera in (5,3). Muoviamo il re in (6,4) che sarebbe sotto scacco
        Board b = new Board(true);
        King whiteKing = new King(7, 4, true);
        Rook blackRook = new Rook(6, 3, false);
        b.setPiece(7, 4, whiteKing);
        b.setPiece(6, 3, blackRook);
        b.setWhiteTurn(true);
        boolean moved = b.movePiece(7, 4, 6, 4); // re si muove su colonna attaccata dalla torre
        assertFalse(moved);
    }

    // -------------------------------------------------------------------------
    // SCACCOMATTO & STALLO
    // -------------------------------------------------------------------------

    @Test
    public void hasAnyLegalMoves_checkmate_returnsFalse() {
        // Fool's mate setup: scaccomatto del barbiere al bianco
        Board b = new Board();
        b.movePiece(6, 5, 5, 5); // f3 (pedone bianco)
        b.movePiece(1, 4, 3, 4); // e5 (pedone nero)
        b.movePiece(6, 6, 4, 6); // g4 (pedone bianco - errore!)
        b.movePiece(0, 3, 4, 7); // Qh4# (regina nera)
        assertFalse(b.hasAnyLegalMoves(true)); // il bianco non ha mosse legali
    }

    @Test
    public void hasAnyLegalMoves_normalGame_returnsTrue() {
        assertTrue(board.hasAnyLegalMoves(true));
        assertTrue(board.hasAnyLegalMoves(false));
    }

    // -------------------------------------------------------------------------
    // EN PASSANT
    // -------------------------------------------------------------------------

    @Test
    public void enPassant_columnSetAfterDoublePawnPush() {
        board.movePiece(6, 4, 4, 4); // pedone bianco e4
        board.movePiece(1, 0, 2, 0); // pedone nero muove
        board.movePiece(4, 4, 3, 4); // pedone bianco avanza
        board.movePiece(1, 3, 3, 3); // pedone nero d5 (doppio avanzamento)
        assertEquals(3, board.getEnPassantColumn()); // colonna d
    }

    @Test
    public void enPassant_captureIsLegal() {
        board.movePiece(6, 4, 4, 4); // e4
        board.movePiece(1, 0, 2, 0); // a6 (nero)
        board.movePiece(4, 4, 3, 4); // e5
        board.movePiece(1, 3, 3, 3); // d5 (doppio, setta en passant)
        boolean captured = board.movePiece(3, 4, 2, 3); // exd6 en passant
        assertTrue(captured);
        assertNull(board.getPiece(3, 3)); // pedone nero catturato
    }

    // -------------------------------------------------------------------------
    // ARROCCO
    // -------------------------------------------------------------------------

    @Test
    public void castling_kingsideAllowed_whenPathClear() {
        // Liberiamo il cammino per l'arrocco corto bianco
        Board b = new Board(true);
        King king = new King(7, 4, true);
        Rook rook = new Rook(7, 7, true);
        b.setPiece(7, 4, king);
        b.setPiece(7, 7, rook);
        b.setWhiteTurn(true);
        boolean moved = b.movePiece(7, 4, 7, 6);
        assertTrue(moved);
        assertTrue(b.getPiece(7, 6) instanceof King);
        assertTrue(b.getPiece(7, 5) instanceof Rook);
    }

    @Test
    public void castling_queensideAllowed_whenPathClear() {
        Board b = new Board(true);
        King king = new King(7, 4, true);
        Rook rook = new Rook(7, 0, true);
        b.setPiece(7, 4, king);
        b.setPiece(7, 0, rook);
        b.setWhiteTurn(true);
        boolean moved = b.movePiece(7, 4, 7, 2);
        assertTrue(moved);
        assertTrue(b.getPiece(7, 2) instanceof King);
        assertTrue(b.getPiece(7, 3) instanceof Rook);
    }

    @Test
    public void castling_notAllowed_whenKingHasMoved() {
        Board b = new Board(true);
        King king = new King(7, 4, true);
        king.setHasMoved(true);
        Rook rook = new Rook(7, 7, true);
        b.setPiece(7, 4, king);
        b.setPiece(7, 7, rook);
        b.setWhiteTurn(true);
        boolean moved = b.movePiece(7, 4, 7, 6);
        assertFalse(moved);
    }

    @Test
    public void castling_notAllowed_throughCheck() {
        Board b = new Board(true);
        King king = new King(7, 4, true);
        Rook rook = new Rook(7, 7, true);
        Rook enemyRook = new Rook(0, 5, false); // attacca f1
        b.setPiece(7, 4, king);
        b.setPiece(7, 7, rook);
        b.setPiece(0, 5, enemyRook);
        b.setWhiteTurn(true);
        boolean moved = b.movePiece(7, 4, 7, 6);
        assertFalse(moved);
    }

    // -------------------------------------------------------------------------
    // PROMOZIONE
    // -------------------------------------------------------------------------

    @Test
    public void promotion_pawnBecomesQueenByDefault() {
        Board b = new Board(true);
        Pawn pawn = new Pawn(1, 0, true); // pedone bianco quasi promosso
        b.setPiece(1, 0, pawn);
        King blackKing = new King(0, 7, false);
        King whiteKing = new King(7, 7, true);
        b.setPiece(0, 7, blackKing);
        b.setPiece(7, 7, whiteKing);
        b.setWhiteTurn(true);
        b.movePiece(1, 0, 0, 0); // avanza alla promozione (default 'q')
        assertTrue(b.getPiece(0, 0) instanceof Queen);
    }

    @Test
    public void promotion_pawnBecomesKnight() {
        Board b = new Board(true);
        Pawn pawn = new Pawn(1, 0, true);
        b.setPiece(1, 0, pawn);
        King blackKing = new King(0, 7, false);
        King whiteKing = new King(7, 7, true);
        b.setPiece(0, 7, blackKing);
        b.setPiece(7, 7, whiteKing);
        b.setWhiteTurn(true);
        b.movePiece(1, 0, 0, 0, 'n');
        assertTrue(b.getPiece(0, 0) instanceof Knight);
    }

    // -------------------------------------------------------------------------
    // FEN
    // -------------------------------------------------------------------------

    @Test
    public void toFen_startingPositionContainsExpectedTokens() {
        String fen = board.toFen();
        assertNotNull(fen);
        assertTrue(fen.contains(" w ")); // turno bianco
        assertTrue(fen.contains("KQkq")); // tutti gli arrocchi disponibili
        assertTrue(fen.startsWith("r")); // torre nera in a8
    }

    // -------------------------------------------------------------------------
    // getLegalMovesForPiece
    // -------------------------------------------------------------------------

    @Test
    public void getLegalMovesForPiece_pawnStartHasTwoMoves() {
        List<Integer> moves = board.getLegalMovesForPiece(6, 4); // pedone bianco e2
        assertEquals(2, moves.size());
    }

    @Test
    public void getLegalMovesForPiece_blockedPawnHasNoMoves() {
        Board b = new Board(true);
        Pawn white = new Pawn(3, 3, true);
        Pawn black = new Pawn(2, 3, false); // blocca
        King wk = new King(7, 4, true);
        King bk = new King(0, 4, false);
        b.setPiece(3, 3, white);
        b.setPiece(2, 3, black);
        b.setPiece(7, 4, wk);
        b.setPiece(0, 4, bk);
        b.setWhiteTurn(true);
        List<Integer> moves = b.getLegalMovesForPiece(3, 3);
        assertEquals(0, moves.size());
    }
}
