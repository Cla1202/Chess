package com.example.chess.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test (local) per MoveCalculator: conversioni tra posizione
 * "flat" della GridView (0-63) e coordinate riga/colonna (0-7).
 */
public class MoveCalculatorTest {

    @Test
    public void toRow_convertsFlatPositionToRow() {
        assertEquals(0, MoveCalculator.toRow(0));   // a8
        assertEquals(0, MoveCalculator.toRow(7));   // h8
        assertEquals(1, MoveCalculator.toRow(8));   // a7
        assertEquals(7, MoveCalculator.toRow(63));  // h1
    }

    @Test
    public void toCol_convertsFlatPositionToColumn() {
        assertEquals(0, MoveCalculator.toCol(0));   // a8
        assertEquals(7, MoveCalculator.toCol(7));   // h8
        assertEquals(0, MoveCalculator.toCol(8));   // a7
        assertEquals(7, MoveCalculator.toCol(63));  // h1
    }

    @Test
    public void toPosition_isInverseOfToRowAndToCol() {
        // Round-trip su tutte le 64 case della scacchiera
        for (int pos = 0; pos < 64; pos++) {
            int row = MoveCalculator.toRow(pos);
            int col = MoveCalculator.toCol(pos);
            assertEquals(pos, MoveCalculator.toPosition(row, col));
        }
    }

    @Test
    public void toPosition_cornerCases() {
        assertEquals(0, MoveCalculator.toPosition(0, 0));
        assertEquals(7, MoveCalculator.toPosition(0, 7));
        assertEquals(56, MoveCalculator.toPosition(7, 0));
        assertEquals(63, MoveCalculator.toPosition(7, 7));
    }
}
