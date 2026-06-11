package com.example.chess.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.QuizLevel;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Unit test (local) per QuizRepository.
 *
 * Nota: getLichessLevels(Context) dipende dal framework Android (assets),
 * quindi qui testiamo solo i livelli di fallback, che sono Java puro.
 * Il caricamento da CSV è coperto da LichessPuzzleLoaderTest.
 *
 * Oltre alla struttura dei dati, verifichiamo una proprietà di dominio
 * fondamentale: ogni soluzione di fallback deve essere composta da mosse
 * LEGALI e deve portare allo SCACCO MATTO dell'avversario.
 */
public class QuizRepositoryTest {

    private QuizRepository repository;

    @Before
    public void setUp() {
        repository = new QuizRepository();
    }

    @Test
    public void getFallbackLevels_returnsTwoWellFormedLevels() {
        List<QuizLevel> levels = repository.getFallbackLevels();

        assertEquals(2, levels.size());
        int expectedId = 1;
        for (QuizLevel level : levels) {
            assertEquals(expectedId++, level.getId());
            assertNotNull(level.getTitle());
            assertFalse(level.getTitle().isEmpty());
            assertNotNull(level.getInitialBoardSetup());
            assertEquals(8, level.getInitialBoardSetup().length);
            assertNotNull(level.getSolutionMoves());
            assertFalse(level.getSolutionMoves().isEmpty());
            assertTrue(level.getMaxAttempts() > 0);
            assertTrue(level.isWhiteTurnToStart());
        }
    }

    @Test
    public void fallbackLevels_solutionsAreLegalAndDeliverCheckmate() {
        for (QuizLevel level : repository.getFallbackLevels()) {
            Board board = new Board();
            board.setupCustomBoard(level.getInitialBoardSetup(), level.isWhiteTurnToStart());

            // Tutte le mosse della soluzione devono essere accettate dal motore
            for (MoveRequest m : level.getSolutionMoves()) {
                assertTrue("Mossa della soluzione rifiutata nel livello " + level.getId(),
                        board.movePiece(m.startRow, m.startCol, m.endRow, m.endCol));
            }

            // Al termine, il giocatore avversario deve essere in scacco matto
            boolean loserIsWhite = !level.isWhiteTurnToStart();
            assertTrue("Il livello " + level.getId() + " non termina con uno scacco",
                    board.isKingInCheck(loserIsWhite));
            assertFalse("Il livello " + level.getId() + " non termina con un matto",
                    board.hasAnyLegalMoves(loserIsWhite));
        }
    }
}
