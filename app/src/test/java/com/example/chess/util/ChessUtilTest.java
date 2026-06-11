package com.example.chess.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test (local) per i metodi di conversione di ChessUtil tra
 * notazione algebrica ("e4") e indice flat 0-63.
 *
 * Nota: i metodi applyLocale/getLocalizedContext dipendono dal framework
 * Android e NON vanno testati qui: andrebbero coperti con Robolectric
 * o con un test instrumented.
 */
public class ChessUtilTest {

    @Test
    public void algebraicToIndex_corners() {
        assertEquals(0, ChessUtil.algebraicToIndex("a8"));
        assertEquals(7, ChessUtil.algebraicToIndex("h8"));
        assertEquals(56, ChessUtil.algebraicToIndex("a1"));
        assertEquals(63, ChessUtil.algebraicToIndex("h1"));
    }

    @Test
    public void algebraicToIndex_centralSquare() {
        // e4 -> riga 4, colonna 4 -> 4*8+4 = 36
        assertEquals(36, ChessUtil.algebraicToIndex("e4"));
    }

    @Test
    public void algebraicToIndex_isCaseInsensitiveOnFile() {
        assertEquals(ChessUtil.algebraicToIndex("e4"), ChessUtil.algebraicToIndex("E4"));
    }

    @Test
    public void algebraicToIndex_invalidInput_returnsMinusOne() {
        // Casi limite: input nullo o troppo corto
        assertEquals(-1, ChessUtil.algebraicToIndex(null));
        assertEquals(-1, ChessUtil.algebraicToIndex(""));
        assertEquals(-1, ChessUtil.algebraicToIndex("e"));
    }

    @Test
    public void indexToAlgebraic_corners() {
        assertEquals("a8", ChessUtil.indexToAlgebraic(0));
        assertEquals("h8", ChessUtil.indexToAlgebraic(7));
        assertEquals("a1", ChessUtil.indexToAlgebraic(56));
        assertEquals("h1", ChessUtil.indexToAlgebraic(63));
    }

    @Test
    public void indexToAlgebraic_outOfRange_returnsEmptyString() {
        assertEquals("", ChessUtil.indexToAlgebraic(-1));
        assertEquals("", ChessUtil.indexToAlgebraic(64));
        assertEquals("", ChessUtil.indexToAlgebraic(100));
    }

    @Test
    public void conversion_roundTrip_onAllSquares() {
        for (int index = 0; index < 64; index++) {
            String algebraic = ChessUtil.indexToAlgebraic(index);
            assertEquals("Round-trip fallito per " + algebraic,
                    index, ChessUtil.algebraicToIndex(algebraic));
        }
    }
}
