package com.example.chess;

import com.example.chess.util.ChessUtil;
import com.example.chess.util.MoveCalculator;

import org.junit.Test;

import static org.junit.Assert.*;

//  Test unitari per MoveCalculator e ChessUtil (solo metodi puri, senza Context Android).

public class UtilTest {

    // -------------------------------------------------------------------------
    // MoveCalculator
    // -------------------------------------------------------------------------

    @Test
    public void toRow_firstSquare_returnsZero() {
        assertEquals(0, MoveCalculator.toRow(0));
    }

    @Test
    public void toRow_position8_returnsRow1() {
        assertEquals(1, MoveCalculator.toRow(8));
    }

    @Test
    public void toRow_position63_returnsRow7() {
        assertEquals(7, MoveCalculator.toRow(63));
    }

    @Test
    public void toRow_position36_returnsRow4() {
        assertEquals(4, MoveCalculator.toRow(36));
    }

    @Test
    public void toCol_firstSquare_returnsZero() {
        assertEquals(0, MoveCalculator.toCol(0));
    }

    @Test
    public void toCol_position7_returnsCol7() {
        assertEquals(7, MoveCalculator.toCol(7));
    }

    @Test
    public void toCol_position9_returnsCol1() {
        assertEquals(1, MoveCalculator.toCol(9));
    }

    @Test
    public void toCol_position63_returnsCol7() {
        assertEquals(7, MoveCalculator.toCol(63));
    }

    @Test
    public void toPosition_row0col0_returns0() {
        assertEquals(0, MoveCalculator.toPosition(0, 0));
    }

    @Test
    public void toPosition_row7col7_returns63() {
        assertEquals(63, MoveCalculator.toPosition(7, 7));
    }

    @Test
    public void toPosition_row4col4_returns36() {
        assertEquals(36, MoveCalculator.toPosition(4, 4));
    }

    @Test
    public void roundTrip_toPosition_toRow_toCol() {
        // Verifica che la conversione position → (row, col) → position sia l'identità
        for (int pos = 0; pos < 64; pos++) {
            int row = MoveCalculator.toRow(pos);
            int col = MoveCalculator.toCol(pos);
            assertEquals(pos, MoveCalculator.toPosition(row, col));
        }
    }

    // -------------------------------------------------------------------------
    // ChessUtil – algebraicToIndex
    // -------------------------------------------------------------------------

    @Test
    public void algebraicToIndex_a8_returns0() {
        assertEquals(0, ChessUtil.algebraicToIndex("a8"));
    }

    @Test
    public void algebraicToIndex_h1_returns63() {
        assertEquals(63, ChessUtil.algebraicToIndex("h1"));
    }

    @Test
    public void algebraicToIndex_e4_correctIndex() {
        // e4 → col = 4 (e-a), row = 8-4 = 4  → index = 4*8+4 = 36
        assertEquals(36, ChessUtil.algebraicToIndex("e4"));
    }

    @Test
    public void algebraicToIndex_nullInput_returnsMinusOne() {
        assertEquals(-1, ChessUtil.algebraicToIndex(null));
    }

    @Test
    public void algebraicToIndex_emptyString_returnsMinusOne() {
        assertEquals(-1, ChessUtil.algebraicToIndex(""));
    }

    @Test
    public void algebraicToIndex_singleChar_returnsMinusOne() {
        assertEquals(-1, ChessUtil.algebraicToIndex("e"));
    }

    // -------------------------------------------------------------------------
    // ChessUtil – indexToAlgebraic
    // -------------------------------------------------------------------------

    @Test
    public void indexToAlgebraic_0_returnsA8() {
        assertEquals("a8", ChessUtil.indexToAlgebraic(0));
    }

    @Test
    public void indexToAlgebraic_63_returnsH1() {
        assertEquals("h1", ChessUtil.indexToAlgebraic(63));
    }

    @Test
    public void indexToAlgebraic_36_returnsE4() {
        assertEquals("e4", ChessUtil.indexToAlgebraic(36));
    }

    @Test
    public void indexToAlgebraic_negativeIndex_returnsEmptyString() {
        assertEquals("", ChessUtil.indexToAlgebraic(-1));
    }

    @Test
    public void indexToAlgebraic_outOfBounds_returnsEmptyString() {
        assertEquals("", ChessUtil.indexToAlgebraic(64));
    }

    @Test
    public void roundTrip_algebraic_index() {
        // Verifica che algebraicToIndex(indexToAlgebraic(i)) == i per ogni casella
        for (int i = 0; i < 64; i++) {
            String algebraic = ChessUtil.indexToAlgebraic(i);
            assertFalse("indexToAlgebraic(" + i + ") restituisce stringa vuota", algebraic.isEmpty());
            int backToIndex = ChessUtil.algebraicToIndex(algebraic);
            assertEquals("Round-trip fallito per indice " + i, i, backToIndex);
        }
    }
}
