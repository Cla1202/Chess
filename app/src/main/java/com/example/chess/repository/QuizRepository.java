package com.example.chess.repository;

import android.content.Context;

import com.example.chess.model.Bishop;
import com.example.chess.model.King;
import com.example.chess.model.Knight;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.Queen;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.Rook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    private static final String PUZZLE_FILE = "puzzles.csv";
    // QuizRepository: allinea le costanti al nuovo file
    private static final int MIN_RATING = 0;      // il filtro è già fatto nel CSV
    private static final int MAX_RATING = 9999;
    private static final int MAX_LEVELS = 30;     // = righe del CSV
    /**
     * Fonte principale dei livelli: il database di puzzle Lichess in assets/puzzles.csv.
     * Se il file manca o non e' leggibile, ricade sui livelli di fallback hardcoded.
     */
    public List<QuizLevel> getLichessLevels(Context context) {
        try {
            LichessPuzzleLoader loader = new LichessPuzzleLoader();
            List<QuizLevel> levels = loader.loadFromCsv(
                    context.getAssets().open(PUZZLE_FILE),
                    null,          // tema: null = tutti quelli presenti nel CSV
                    MIN_RATING,
                    MAX_RATING,
                    MAX_LEVELS);

            // Se il CSV esiste ma non ha prodotto livelli validi, meglio il fallback
            if (levels.isEmpty()) {
                return getFallbackLevels();
            }
            return levels;

        } catch (IOException e) {
            return getFallbackLevels();
        }
    }

    // ==========================================
    // FALLBACK: due livelli minimi, usati SOLO se
    // il CSV non e' disponibile. Garantiscono che
    // il tab Quiz non sia mai vuoto.
    // ==========================================

    public List<QuizLevel> getFallbackLevels() {
        List<QuizLevel> levels = new ArrayList<>();
        levels.add(createFallbackLevel1());
        levels.add(createFallbackLevel2());
        return levels;
    }

    // --- TRADUTTORI DI COORDINATE ---
    private int col(String pos) {
        return pos.toLowerCase().charAt(0) - 'a';
    }

    private int riga(String pos) {
        return 8 - Character.getNumericValue(pos.charAt(1));
    }

    private MoveRequest mossa(String partenza, String arrivo) {
        return new MoveRequest(riga(partenza), col(partenza), riga(arrivo), col(arrivo));
    }

    private Piece[][] createEmptyBoard() {
        return new Piece[8][8];
    }

    // Matto in 1: Dxh7# (ex Livello 1)
    private QuizLevel createFallbackLevel1() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d3")][col("d3")] = new Queen(riga("d3"), col("d3"), true);
        setupLevel[riga("c2")][col("c2")] = new Bishop(riga("c2"), col("c2"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d3", "h7"));

        return new QuizLevel(1, "Livello 1: Benvenuto!", setupLevel, true, soluzione, 3);
    }

    // Matto Arabo: Txh7# (ex Livello 6)
    private QuizLevel createFallbackLevel2() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("g8")][col("g8")] = new Rook(riga("g8"), col("g8"), false);

        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);
        setupLevel[riga("f6")][col("f6")] = new Knight(riga("f6"), col("f6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h1", "h7"));

        return new QuizLevel(2, "Livello 2: Matto Arabo", setupLevel, true, soluzione, 3);
    }
}