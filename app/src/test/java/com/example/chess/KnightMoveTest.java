package com.example.chess;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.chess.model.Board;
import com.example.chess.model.Knight;
import com.example.chess.model.Piece;
import java.util.List;

public class KnightMoveTest {

    @Test
    public void knight_ValidMoves_ReturnsCorrectSquares() {
        // Given: Una scacchiera vuota con un cavallo bianco in d4 (riga 4, colonna 3)
        Board board = new Board();      // Scacchiera vuota
        Piece knight = new Knight(true); // true = bianco
        board.setPiece(4, 3, knight);

        // When: Calcoliamo le mosse possibili
        List<String> validMoves = board.getValidMoves(4, 3);

        // Then: Verifichiamo che produca i risultati attesi (es. il cavallo ha 8 mosse possibili)
        assertEquals(8, validMoves.size());
        assertTrue(validMoves.contains("3,1")); // b3
        assertTrue(validMoves.contains("5,5")); // f5
    }

    @Test
    public void piece_MoveOutOfBounds_ReturnsFalseOrEmpty() {
        // Caso limite: Tentativo di mossa fuori dalla scacchiera (es. colonna -1 o 8)
        Board board = new Board();
        assertFalse(board.isValidSquare(-1, 0));
        assertFalse(board.isValidSquare(8, 7));
    }
}