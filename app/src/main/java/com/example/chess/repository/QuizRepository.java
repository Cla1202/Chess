package com.example.chess.repository;

import com.example.chess.model.Bishop;
import com.example.chess.model.King;
import com.example.chess.model.Knight;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.Queen;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.Rook;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public List<QuizLevel> getAllLevels() {
        List<QuizLevel> levels = new ArrayList<>();

        levels.add(createLevel1());
        levels.add(createLevel2());
        levels.add(createLevel3());
        levels.add(createLevel4());
        levels.add(createLevel5());
        levels.add(createLevel6());
        levels.add(createLevel7());
        levels.add(createLevel8());
        levels.add(createLevel9());
        levels.add(createLevel10());
        levels.add(createLevel11());
        levels.add(createLevel12());
        levels.add(createLevel13());
        levels.add(createLevel14());
        levels.add(createLevel15());
        levels.add(createLevel16());

        return levels;
    }

    // --- COORDINATE TRANSLATORS ---
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

    // ==========================================
    // LEVEL 1: The First Check (Mate in 1 - Tutorial)
    // ==========================================
    private QuizLevel createLevel1() {
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

    // ==========================================
    // LEVEL 2: Lethal Battery (Mate in 1)
    // ==========================================
    private QuizLevel createLevel2() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d4")][col("d4")] = new Queen(riga("d4"), col("d4"), true);
        setupLevel[riga("c3")][col("c3")] = new Bishop(riga("c3"), col("c3"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d4", "g7"));

        return new QuizLevel(2, "Livello 2: Batteria Letale", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 3: Queen Sacrifice (Mate in 2)
    // ==========================================
    private QuizLevel createLevel3() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("d8")][col("d8")] = new Rook(riga("d8"), col("d8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d5")][col("d5")] = new Queen(riga("d5"), col("d5"), true);
        setupLevel[riga("h6")][col("h6")] = new Knight(riga("h6"), col("h6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "g8"));
        soluzione.add(mossa("d8", "g8"));
        soluzione.add(mossa("h6", "f7"));

        return new QuizLevel(3, "Livello 3: Sacrificio (in 2)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 4: Epaulette Mate (Mate in 1)
    // ==========================================
    private QuizLevel createLevel4() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("e8")][col("e8")] = new King(riga("e8"), col("e8"), false);
        setupLevel[riga("d8")][col("d8")] = new Rook(riga("d8"), col("d8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);

        setupLevel[riga("a7")][col("a7")] = new Queen(riga("a7"), col("a7"), true);
        setupLevel[riga("e6")][col("e6")] = new King(riga("e6"), col("e6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("a7", "e7"));

        return new QuizLevel(4, "Livello 4: Le Spalline", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 5: The Infiltration (Mate in 2)
    // ==========================================
    private QuizLevel createLevel5() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d2")][col("d2")] = new Queen(riga("d2"), col("d2"), true);
        setupLevel[riga("f6")][col("f6")] = new Pawn(riga("f6"), col("f6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d2", "d8"));

        return new QuizLevel(5, "Livello 5: Infiltrazione (in 1)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 6: Arabian Mate (Mate in 1)
    // ==========================================
    private QuizLevel createLevel6() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("g8")][col("g8")] = new Rook(riga("g8"), col("g8"), false);

        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);
        setupLevel[riga("f6")][col("f6")] = new Knight(riga("f6"), col("f6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h1", "h7"));

        return new QuizLevel(6, "Livello 6: Matto Arabo", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 7: Back Rank Deviation (Mate in 2)
    // ==========================================
    private QuizLevel createLevel7() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("a8")][col("a8")] = new Rook(riga("a8"), col("a8"), false);
        setupLevel[riga("d8")][col("d8")] = new Queen(riga("d8"), col("d8"), false);

        setupLevel[riga("d2")][col("d2")] = new Queen(riga("d2"), col("d2"), true);
        setupLevel[riga("e1")][col("e1")] = new Rook(riga("e1"), col("e1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d2", "d8"));
        soluzione.add(mossa("a8", "d8"));
        soluzione.add(mossa("e1", "e8"));

        return new QuizLevel(7, "Livello 7: Deviazione (in 2)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 8: Anastasia's Mate (Queen Sacrifice)
    // ==========================================
    private QuizLevel createLevel8() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("g8")][col("g8")] = new Rook(riga("g8"), col("g8"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);

        setupLevel[riga("e7")][col("e7")] = new Knight(riga("e7"), col("e7"), true);
        setupLevel[riga("d5")][col("d5")] = new Queen(riga("d5"), col("d5"), true);
        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "g8"));
        soluzione.add(mossa("h8", "h7"));
        soluzione.add(mossa("h1", "h3"));

        return new QuizLevel(8, "Livello 8: Matto di Anastasia", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 9: Boden's Mate (The Scissors)
    // ==========================================
    private QuizLevel createLevel9() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("c8")][col("c8")] = new King(riga("c8"), col("c8"), false);
        setupLevel[riga("d8")][col("d8")] = new Rook(riga("d8"), col("d8"), false);
        setupLevel[riga("a7")][col("a7")] = new Pawn(riga("a7"), col("a7"), false);
        setupLevel[riga("b7")][col("b7")] = new Pawn(riga("b7"), col("b7"), false);
        setupLevel[riga("d7")][col("d7")] = new Pawn(riga("d7"), col("d7"), false);
        setupLevel[riga("c6")][col("c6")] = new Pawn(riga("c6"), col("c6"), false);

        setupLevel[riga("d5")][col("d5")] = new Queen(riga("d5"), col("d5"), true);
        setupLevel[riga("f4")][col("f4")] = new Bishop(riga("f4"), col("f4"), true);
        setupLevel[riga("g2")][col("g2")] = new Bishop(riga("g2"), col("g2"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "c6"));
        soluzione.add(mossa("b7", "c6"));
        soluzione.add(mossa("g2", "a6"));

        return new QuizLevel(9, "Livello 9: Matto di Boden", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 10: Smothered Mate (The King's Nightmare)
    // ==========================================
    private QuizLevel createLevel10() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("d8")][col("d8")] = new Rook(riga("d8"), col("d8"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);

        setupLevel[riga("d5")][col("d5")] = new Queen(riga("d5"), col("d5"), true);
        setupLevel[riga("h6")][col("h6")] = new Knight(riga("h6"), col("h6"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "g8"));
        soluzione.add(mossa("d8", "g8"));
        soluzione.add(mossa("h6", "f7"));

        return new QuizLevel(10, "Livello 10: Matto Affogato", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 11: Anastasia's Mate (Mate in 3)
    // ==========================================
    private QuizLevel createLevel11() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("d5")][col("d5")] = new Knight(riga("d5"), col("d5"), true);
        setupLevel[riga("d3")][col("d3")] = new Queen(riga("d3"), col("d3"), true);
        setupLevel[riga("a3")][col("a3")] = new Rook(riga("a3"), col("a3"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "e7"));
        soluzione.add(mossa("g8", "h8"));
        soluzione.add(mossa("d3", "h7"));
        soluzione.add(mossa("h8", "h7"));
        soluzione.add(mossa("a3", "h3"));

        return new QuizLevel(11, "Livello 11: Anastasia (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 12: Damiano's Attack (Mate in 3)
    // ==========================================
    private QuizLevel createLevel12() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);

        setupLevel[riga("d3")][col("d3")] = new Queen(riga("d3"), col("d3"), true);
        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);
        setupLevel[riga("h6")][col("h6")] = new Pawn(riga("h6"), col("h6"), true);
        setupLevel[riga("f5")][col("f5")] = new Pawn(riga("f5"), col("f5"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d3", "h7"));
        soluzione.add(mossa("g8", "h7"));
        soluzione.add(mossa("h1", "h3"));
        soluzione.add(mossa("h7", "g8"));
        soluzione.add(mossa("h3", "h8"));

        return new QuizLevel(12, "Livello 12: Damiano (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 13: Philidor's Legacy (Mate in 4)
    // ==========================================
    private QuizLevel createLevel13() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("c4")][col("c4")] = new Queen(riga("c4"), col("c4"), true);
        setupLevel[riga("g5")][col("g5")] = new Knight(riga("g5"), col("g5"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("g5", "f7"));
        soluzione.add(mossa("g8", "h8"));
        soluzione.add(mossa("f7", "h6"));
        soluzione.add(mossa("h8", "g8"));
        soluzione.add(mossa("c4", "g8"));
        soluzione.add(mossa("f8", "g8"));
        soluzione.add(mossa("h6", "f7"));

        return new QuizLevel(13, "Livello 13: Matto di Philidor (in 4)", setupLevel, true, soluzione, 5);
    }

    // ==========================================
    // LEVEL 14: Légal's Mate (Mate in 3)
    // ==========================================
    private QuizLevel createLevel14() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("e1")][col("e1")] = new King(riga("e1"), col("e1"), true);
        setupLevel[riga("c3")][col("c3")] = new Knight(riga("c3"), col("c3"), true);
        setupLevel[riga("e5")][col("e5")] = new Knight(riga("e5"), col("e5"), true);
        setupLevel[riga("c4")][col("c4")] = new Bishop(riga("c4"), col("c4"), true);

        setupLevel[riga("e8")][col("e8")] = new King(riga("e8"), col("e8"), false);
        setupLevel[riga("g4")][col("g4")] = new Bishop(riga("g4"), col("g4"), false);
        setupLevel[riga("d6")][col("d6")] = new Pawn(riga("d6"), col("d6"), false);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("c4", "f7"));
        soluzione.add(mossa("e8", "e7"));
        soluzione.add(mossa("c3", "d5"));

        return new QuizLevel(14, "Livello 14: Matto di Légal", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 15: Morphy's Mate (Mate in 2)
    // ==========================================
    private QuizLevel createLevel15() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);
        setupLevel[riga("d1")][col("d1")] = new Rook(riga("d1"), col("d1"), true);
        setupLevel[riga("f6")][col("f6")] = new Bishop(riga("f6"), col("f6"), true);

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d1", "g7"));
        soluzione.add(mossa("g8", "h8"));
        soluzione.add(mossa("g7", "f7"));

        return new QuizLevel(15, "Livello 15: Matto di Morphy", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 16: Smyslov's Sacrifice (Mate in 3)
    // ==========================================
    private QuizLevel createLevel16() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h1")][col("h1")] = new King(riga("h1"), col("h1"), true);
        setupLevel[riga("g4")][col("g4")] = new Queen(riga("g4"), col("g4"), true);
        setupLevel[riga("b2")][col("b2")] = new Bishop(riga("b2"), col("b2"), true);
        setupLevel[riga("f6")][col("f6")] = new Pawn(riga("f6"), col("f6"), true);

        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("g4", "g7"));
        soluzione.add(mossa("f8", "g7"));
        soluzione.add(mossa("f6", "g7"));
        soluzione.add(mossa("g8", "h8"));

        setupLevel[riga("b2")][col("b2")] = new Bishop(riga("b2"), col("b2"), true);
        soluzione.add(mossa("b2", "f6"));

        return new QuizLevel(16, "Livello 16: Sacrificio Finale", setupLevel, true, soluzione, 3);
    }
}