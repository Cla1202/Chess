package com.example.chess.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.QuizLevel;
import com.example.chess.source.quiz.IQuizDataSource;
import com.example.chess.util.IResourceProvider;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Unit test (local) for QuizRepository.
 * Now it uses a fake IResourceProvider and a fake IQuizDataSource to avoid 
 * dependencies on the Android Context and Assets, making the test pure Java and much faster.
 */
public class QuizRepositoryTest {

    private QuizRepository repository;

    // A simple fake implementation of IResourceProvider for testing purposes
    private final IResourceProvider fakeResourceProvider = new IResourceProvider() {
        @Override
        public String getString(int resId) {
            return "Fake String for ID: " + resId;
        }

        @Override
        public String getString(int resId, Object... formatArgs) {
            return "Fake Formatted String for ID: " + resId;
        }
    };

    // A simple fake implementation of IQuizDataSource for testing purposes.
    // It returns an empty stream so the repository will use fallback levels.
    private final IQuizDataSource fakeDataSource = () -> new ByteArrayInputStream("".getBytes());

    @Before
    public void setUp() {
        // We inject the fake provider and fake data source into the repository
        repository = new QuizRepository(fakeResourceProvider, fakeDataSource);
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
            Board board = new Board(true); // Empty board
            board.setupCustomBoard(level.getInitialBoardSetup(), level.isWhiteTurnToStart());

            // Every move in the solution must be accepted by the engine
            for (MoveRequest m : level.getSolutionMoves()) {
                assertTrue("Solution move rejected in level " + level.getId(),
                        board.movePiece(m.startRow, m.startCol, m.endRow, m.endCol));
            }

            // At the end, the opponent must be in checkmate
            boolean loserIsWhite = !board.isWhiteTurn();
            assertTrue("Level " + level.getId() + " does not end with a check",
                    board.isKingInCheck(loserIsWhite));
            assertFalse("Level " + level.getId() + " does not end with a mate",
                    board.hasAnyLegalMoves(loserIsWhite));
        }
    }
}
