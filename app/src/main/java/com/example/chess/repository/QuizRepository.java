package com.example.chess.repository;

import android.content.Context;

import com.example.chess.R;
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
    private static final int MIN_RATING = 0;
    private static final int MAX_RATING = 9999;
    private static final int MAX_LEVELS = 30;

    public List<QuizLevel> getLichessLevels(Context context) {
        try {
            LichessPuzzleLoader loader = new LichessPuzzleLoader();
            List<QuizLevel> levels = loader.loadFromCsv(
                    context,
                    context.getAssets().open(PUZZLE_FILE),
                    null,
                    MIN_RATING,
                    MAX_RATING,
                    MAX_LEVELS);

            if (levels.isEmpty()) {
                return getFallbackLevels(context);
            }
            return levels;

        } catch (IOException e) {
            return getFallbackLevels(context);
        }
    }

    public List<QuizLevel> getFallbackLevels(Context context) {
        List<QuizLevel> levels = new ArrayList<>();
        levels.add(createFallbackLevel1(context));
        levels.add(createFallbackLevel2(context));
        return levels;
    }

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

    private QuizLevel createFallbackLevel1(Context context) {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d3")][col("d3")] = new Queen(riga("d3"), col("d3"), true);
        setupLevel[riga("c2")][col("c2")] = new Bishop(riga("c2"), col("c2"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d3", "h7"));

        // Titolo pulito per il fallback
        String title = context.getString(R.string.fallback_welcome);
        return new QuizLevel(1, title, setupLevel, true, soluzione, 3);
    }

    private QuizLevel createFallbackLevel2(Context context) {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("g8")][col("g8")] = new Rook(riga("g8"), col("g8"), false);

        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);
        setupLevel[riga("f6")][col("f6")] = new Knight(riga("f6"), col("f6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h1", "h7"));

        // Titolo pulito per il fallback
        String title = context.getString(R.string.theme_arabian_mate);
        return new QuizLevel(2, title, setupLevel, true, soluzione, 3);
    }
}