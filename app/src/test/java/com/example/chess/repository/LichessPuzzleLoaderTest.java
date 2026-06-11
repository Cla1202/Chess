package com.example.chess.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.chess.model.Board;
import com.example.chess.model.King;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.Rook;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Unit test (local) per LichessPuzzleLoader.
 * Il CSV viene simulato con stringhe in memoria (nessun accesso ad assets),
 * così il loader è testato in isolamento dal framework Android.
 *
 * Formato riga:
 * PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
 */
public class LichessPuzzleLoaderTest {

    private static final String HEADER =
            "PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags";

    /**
     * Puzzle di matto del corridoio: nella FEN muove il nero (Rh8-g8),
     * poi il bianco matta con Te1-e8.
     */
    private static final String VALID_ROW =
            "00001,7k/5ppp/8/8/8/8/5PPP/4R1K1 b - - 0 1,h8g8 e1e8,800,75,95,1000,mate mateIn1 backRankMate oneMove,https://lichess.org/abc,";

    private static final String VALID_ROW_2 =
            "00002,7k/5ppp/8/8/8/8/5PPP/4R1K1 b - - 0 1,h8g8 e1e8,900,75,95,1000,mate mateIn1 oneMove,https://lichess.org/def,";

    private LichessPuzzleLoader loader;

    @Before
    public void setUp() {
        loader = new LichessPuzzleLoader();
    }

    private static InputStream csv(String... rows) {
        StringBuilder sb = new StringBuilder(HEADER);
        for (String row : rows) sb.append('\n').append(row);
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // ==========================================================
    // CASO NORMALE
    // ==========================================================

    @Test
    public void loadFromCsv_validRow_buildsOneLevel() throws IOException {
        List<QuizLevel> levels = loader.loadFromCsv(csv(VALID_ROW), null, 0, 9999, 10);

        assertEquals(1, levels.size());
        QuizLevel level = levels.get(0);
        assertEquals(1, level.getId());
        assertTrue(level.getTitle().startsWith("Livello 1"));
        assertEquals(3, level.getMaxAttempts());

        // Nella FEN muove il nero -> il giocatore del puzzle è il bianco
        assertTrue(level.isWhiteTurnToStart());

        // La prima mossa UCI (h8g8, dell'avversario) deve essere già applicata
        Piece[][] setup = level.getInitialBoardSetup();
        assertTrue(setup[0][6] instanceof King);   // re nero spostato in g8
        assertFalse(setup[0][6].isWhite());
        assertNull(setup[0][7]);                   // h8 ora vuota
        assertTrue(setup[7][4] instanceof Rook);   // torre bianca ferma in e1
        assertTrue(setup[1][5] instanceof Pawn);   // pedone nero f7 al suo posto

        // La soluzione è composta dalle mosse rimanenti: solo e1e8
        assertEquals(1, level.getSolutionMoves().size());
        MoveRequest m = level.getSolutionMoves().get(0);
        assertEquals(7, m.startRow);
        assertEquals(4, m.startCol);
        assertEquals(0, m.endRow);
        assertEquals(4, m.endCol);
    }

    @Test
    public void loadFromCsv_solutionIsLegalAndDeliversCheckmate() throws IOException {
        // Test di coerenza con il motore di gioco: la soluzione caricata
        // dal CSV deve essere legale per Board e produrre scacco matto
        List<QuizLevel> levels = loader.loadFromCsv(csv(VALID_ROW), null, 0, 9999, 10);
        QuizLevel level = levels.get(0);

        Board board = new Board();
        board.setupCustomBoard(level.getInitialBoardSetup(), level.isWhiteTurnToStart());
        for (MoveRequest m : level.getSolutionMoves()) {
            assertTrue(board.movePiece(m.startRow, m.startCol, m.endRow, m.endCol));
        }
        assertTrue(board.isKingInCheck(false));
        assertFalse(board.hasAnyLegalMoves(false));
    }

    // ==========================================================
    // FILTRI
    // ==========================================================

    @Test
    public void loadFromCsv_ratingOutOfRange_isExcluded() throws IOException {
        // Rating 800: fuori dall'intervallo [1000, 2000]
        List<QuizLevel> levels = loader.loadFromCsv(csv(VALID_ROW), null, 1000, 2000, 10);
        assertTrue(levels.isEmpty());
    }

    @Test
    public void loadFromCsv_themeFilter_excludesNonMatchingPuzzles() throws IOException {
        List<QuizLevel> levels = loader.loadFromCsv(csv(VALID_ROW), "mateIn2", 0, 9999, 10);
        assertTrue(levels.isEmpty());

        List<QuizLevel> matching = loader.loadFromCsv(csv(VALID_ROW), "mateIn1", 0, 9999, 10);
        assertEquals(1, matching.size());
    }

    @Test
    public void loadFromCsv_respectsMaxLevels() throws IOException {
        List<QuizLevel> levels = loader.loadFromCsv(csv(VALID_ROW, VALID_ROW_2), null, 0, 9999, 1);
        assertEquals(1, levels.size());
    }

    // ==========================================================
    // CASI LIMITE / DATI CORROTTI
    // ==========================================================

    @Test
    public void loadFromCsv_emptyFile_returnsEmptyList() throws IOException {
        List<QuizLevel> levels = loader.loadFromCsv(
                new ByteArrayInputStream(new byte[0]), null, 0, 9999, 10);
        assertNotNull(levels);
        assertTrue(levels.isEmpty());
    }

    @Test
    public void loadFromCsv_headerOnly_returnsEmptyList() throws IOException {
        List<QuizLevel> levels = loader.loadFromCsv(csv(), null, 0, 9999, 10);
        assertTrue(levels.isEmpty());
    }

    @Test
    public void loadFromCsv_malformedRows_areSkippedWithoutCrashing() throws IOException {
        String tooFewFields = "abc,def";
        String nonNumericRating = "00003,7k/5ppp/8/8/8/8/5PPP/4R1K1 b - - 0 1,h8g8 e1e8,notanumber,75,95,1000,mateIn1,url,";
        List<QuizLevel> levels = loader.loadFromCsv(
                csv(tooFewFields, nonNumericRating, VALID_ROW), null, 0, 9999, 10);

        // Le righe corrotte vengono ignorate, quella valida viene caricata
        assertEquals(1, levels.size());
    }

    @Test
    public void loadFromCsv_puzzleWithPromotion_isSkipped() throws IOException {
        // Mossa UCI a 5 caratteri (promozione e7e8q): il puzzle va scartato
        String promotionRow =
                "00004,4k3/4P3/8/8/8/8/8/4K3 w - - 0 1,e7e8q e8d7,800,75,95,1000,promotion,url,";
        List<QuizLevel> levels = loader.loadFromCsv(csv(promotionRow), null, 0, 9999, 10);
        assertTrue(levels.isEmpty());
    }

    @Test
    public void loadFromCsv_puzzleWithSingleMove_isSkipped() throws IOException {
        // Servono almeno 2 mosse: una dell'avversario + almeno una di soluzione
        String singleMoveRow =
                "00005,7k/5ppp/8/8/8/8/5PPP/4R1K1 b - - 0 1,h8g8,800,75,95,1000,mateIn1,url,";
        List<QuizLevel> levels = loader.loadFromCsv(csv(singleMoveRow), null, 0, 9999, 10);
        assertTrue(levels.isEmpty());
    }

    // ==========================================================
    // METODI PUBBLICI DI SUPPORTO
    // ==========================================================

    @Test
    public void parseFen_startingPosition_isParsedCorrectly() {
        Piece[][] board = loader.parseFen(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertNotNull(board);
        assertTrue(board[0][4] instanceof King);
        assertFalse(board[0][4].isWhite());
        assertTrue(board[7][4] instanceof King);
        assertTrue(board[7][4].isWhite());
        for (int c = 0; c < 8; c++) {
            assertTrue(board[1][c] instanceof Pawn);
            assertTrue(board[6][c] instanceof Pawn);
        }
        // Le coordinate interne dei pezzi devono coincidere con l'array
        assertEquals(0, board[0][4].getX());
        assertEquals(4, board[0][4].getY());
    }

    @Test
    public void parseFen_invalidFen_returnsNull() {
        assertNull(loader.parseFen("8/8/8 w - - 0 1"));          // ranghi mancanti
        assertNull(loader.parseFen("9/8/8/8/8/8/8/8 w - - 0 1")); // rango troppo lungo
        assertNull(loader.parseFen("x7/8/8/8/8/8/8/8 w - - 0 1")); // carattere sconosciuto
    }

    @Test
    public void uciToMove_convertsCoordinates() {
        MoveRequest m = loader.uciToMove("e2e4");
        assertEquals(6, m.startRow);
        assertEquals(4, m.startCol);
        assertEquals(4, m.endRow);
        assertEquals(4, m.endCol);
    }
}
