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
    // LEVEL 1: Tutorial (Matto in 1)
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

        return new QuizLevel(1, "Livello 1: Benvenuto!(Matto in 1)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 2: Batteria Letale (Matto in 1)
    // ==========================================
    private QuizLevel createLevel2() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("b7")][col("b7")] = new Pawn(riga("b7"), col("b7"), false);
        setupLevel[riga("a6")][col("a6")] = new King(riga("a6"), col("a6"), false);
        setupLevel[riga("b6")][col("b6")] = new Pawn(riga("b6"), col("b6"), false);
        setupLevel[riga("b5")][col("b5")] = new Pawn(riga("b5"), col("b5"), false);

        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("f1")][col("f1")] = new Rook(riga("f1"), col("f1"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("f1", "a1"));

        return new QuizLevel(2, "Livello 2: Batteria Letale (Matto in 1)", setupLevel, true, soluzione, 1);
    }
    // ==========================================
    // LEVEL 3: Sacrificio di Regina (Matto in 2)
    // ==========================================
    private QuizLevel createLevel3() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("g8")][col("g8")] = new Rook(riga("g8"), col("g8"), false);
        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("g6")][col("g6")] = new Queen(riga("g6"), col("g6"), false);

        setupLevel[riga("g5")][col("g5")] = new Knight(riga("g5"), col("g5"), true);
        setupLevel[riga("h3")][col("h3")] = new Queen(riga("h3"), col("h3"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h3", "h7"));
        soluzione.add(mossa("g6", "h7"));
        soluzione.add(mossa("g5", "f7"));

        return new QuizLevel(3, "Livello 13: Sacrificio di Regina (Matto in 2)", setupLevel, true, soluzione, 2);
    }

    // ==========================================
    // LEVEL 4: Le Spalline (Matto in 1)
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

        return new QuizLevel(4, "Livello 4: Le Spalline (Matto in 1)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 5: Infiltrazione (in 1)
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

        return new QuizLevel(5, "Livello 5: Infiltrazione (Matto in 1)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 6:  Matto Arabo (Matto in 1)
    // ==========================================
    private QuizLevel createLevel6() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("h8")][col("h8")] = new King(riga("h8"), col("h8"), false);

        setupLevel[riga("f7")][col("f7")] = new Rook(riga("f7"), col("f7"), true);
        setupLevel[riga("f6")][col("f6")] = new Knight(riga("f6"), col("f6"), true);
        setupLevel[riga("h1")][col("h1")] = new King(riga("h1"), col("h1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("f7", "h7"));

        return new QuizLevel(6, "Livello 1: Matto Arabo (Matto in 1)", setupLevel, true, soluzione, 1);
    }

    // ==========================================
    // LEVEL 7: Matto dell'Uncino (Matto in 1)
    // ==========================================
    private QuizLevel createLevel7() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("e7")][col("e7")] = new King(riga("e7"), col("e7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);

        setupLevel[riga("a8")][col("a8")] = new Rook(riga("a8"), col("a8"), true);
        setupLevel[riga("f6")][col("f6")] = new Knight(riga("f6"), col("f6"), true);
        setupLevel[riga("e5")][col("e5")] = new Pawn(riga("e5"), col("e5"), true);
        setupLevel[riga("h1")][col("h1")] = new King(riga("h1"), col("h1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("a8", "e8"));


        return new QuizLevel(7, "Livello 7: Matto dell'Uncino (Matto in 1)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 8: Anastasia#1 (Matto in 1)
    // ==========================================
    private QuizLevel createLevel8() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("e8")][col("e8")] = new Rook(riga("e8"), col("e8"), false);
        setupLevel[riga("e7")][col("e7")] = new Knight(riga("e7"), col("e7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new King(riga("h7"), col("h7"), false);
        setupLevel[riga("b7")][col("b7")] = new Bishop(riga("b7"), col("b7"), false);

        setupLevel[riga("c5")][col("c5")] = new Rook(riga("c5"), col("c5"), true);
        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("c5", "h5"));

        return new QuizLevel(8, "Livello 9: Anastasia#1 (Matto in 1)", setupLevel, true, soluzione, 1);
    }

    // ==========================================
    // LEVEL 9: Boden (Matto in 1)
    // ==========================================
    private QuizLevel createLevel9() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("c8")][col("c8")] = new King(riga("c8"), col("c8"), false);
        setupLevel[riga("d8")][col("d8")] = new Rook(riga("d8"), col("d8"), false);
        setupLevel[riga("d7")][col("d7")] = new Pawn(riga("d7"), col("d7"), false);

        setupLevel[riga("f4")][col("f4")] = new Bishop(riga("f4"), col("f4"), true);
        setupLevel[riga("f1")][col("f1")] = new Bishop(riga("f1"), col("f1"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("f1", "a6"));

        return new QuizLevel(9, "Livello 9: Boden (Matto in 1)", setupLevel, true, soluzione, 1);
    }

    // ==========================================
    // LEVEL 10: Matto della Biblioteca (Matto in 3)
    // ==========================================
    private QuizLevel createLevel10() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);

        setupLevel[riga("b7")][col("b7")] = new Rook(riga("b7"), col("b7"), true);
        setupLevel[riga("e7")][col("e7")] = new Rook(riga("e7"), col("e7"), true);
        setupLevel[riga("b1")][col("b1")] = new King(riga("b1"), col("b1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("e7", "g7"));
        soluzione.add(mossa("g8", "h8"));
        soluzione.add(mossa("g7", "h7"));
        soluzione.add(mossa("h8", "g8"));
        soluzione.add(mossa("b7", "g7"));

        return new QuizLevel(10, "Livello 10: Matto della Biblioteca (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 11: Anastasia#2 (Matto in 3)
    // ==========================================
    private QuizLevel createLevel11() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new Pawn(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("b7")][col("b7")] = new Bishop(riga("b7"), col("b7"), false);

        setupLevel[riga("c5")][col("c5")] = new Rook(riga("c5"), col("c5"), true);
        setupLevel[riga("d5")][col("d5")] = new Knight(riga("d5"), col("d5"), true);
        setupLevel[riga("c2")][col("c2")] = new Queen(riga("c2"), col("c2"), true);
        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("d5", "e7"));
        soluzione.add(mossa("g8", "h8"));
        soluzione.add(mossa("c2", "h7"));
        soluzione.add(mossa("h8", "h7"));
        soluzione.add(mossa("c5", "h5"));

        return new QuizLevel(11, "Livello 11: Anastasia#2 (Matto in 3)", setupLevel, true, soluzione, 3);
    }
    // ==========================================
    // LEVEL 12: Damiano (Matto in 2)
    // ==========================================
    private QuizLevel createLevel12() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("e8")][col("e8")] = new Rook(riga("e8"), col("e8"), false);
        setupLevel[riga("f8")][col("f8")] = new King(riga("f8"), col("f8"), false);
        setupLevel[riga("b7")][col("b7")] = new Pawn(riga("b7"), col("b7"), false);
        setupLevel[riga("d7")][col("d7")] = new Queen(riga("d7"), col("d7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("d6")][col("d6")] = new Pawn(riga("d6"), col("d6"), false);
        setupLevel[riga("g6")][col("g6")] = new Knight(riga("g6"), col("g6"), false);
        setupLevel[riga("a5")][col("a5")] = new Pawn(riga("a5"), col("a5"), false);
        setupLevel[riga("c5")][col("c5")] = new Pawn(riga("c5"), col("c5"), false);
        setupLevel[riga("f5")][col("f5")] = new Pawn(riga("f5"), col("f5"), false);

        setupLevel[riga("f6")][col("f6")] = new Bishop(riga("f6"), col("f6"), true);
        setupLevel[riga("d5")][col("d5")] = new Pawn(riga("d5"), col("d5"), true);
        setupLevel[riga("a4")][col("a4")] = new Pawn(riga("a4"), col("a4"), true);
        setupLevel[riga("c4")][col("c4")] = new Pawn(riga("c4"), col("c4"), true);
        setupLevel[riga("b3")][col("b3")] = new Pawn(riga("b3"), col("b3"), true);
        setupLevel[riga("c3")][col("c3")] = new King(riga("c3"), col("c3"), true);
        setupLevel[riga("g3")][col("g3")] = new Queen(riga("g3"), col("g3"), true);
        setupLevel[riga("h1")][col("h1")] = new Rook(riga("h1"), col("h1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h1", "h8"));
        soluzione.add(mossa("g6", "h8"));
        soluzione.add(mossa("g3", "g7"));

        return new QuizLevel(12, "Livello 12: Damiano (Matto in 2)", setupLevel, true, soluzione, 2);
    }

    // ==========================================
    // LEVEL 13:  Lolli (Matto in 3)
    // ==========================================
    private QuizLevel createLevel13() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("a8")][col("a8")] = new Rook(riga("a8"), col("a8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("b7")][col("b7")] = new Queen(riga("b7"), col("b7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("g7")][col("g7")] = new King(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("a6")][col("a6")] = new Pawn(riga("a6"), col("a6"), false);
        setupLevel[riga("c6")][col("c6")] = new Bishop(riga("c6"), col("c6"), false);
        setupLevel[riga("e6")][col("e6")] = new Pawn(riga("e6"), col("e6"), false);
        setupLevel[riga("g6")][col("g6")] = new Knight(riga("g6"), col("g6"), false);
        setupLevel[riga("b5")][col("b5")] = new Pawn(riga("b5"), col("b5"), false);

        setupLevel[riga("g5")][col("g5")] = new Queen(riga("g5"), col("g5"), true);
        setupLevel[riga("h5")][col("h5")] = new Pawn(riga("h5"), col("h5"), true);
        setupLevel[riga("e4")][col("e4")] = new Pawn(riga("e4"), col("e4"), true);
        setupLevel[riga("b3")][col("b3")] = new Bishop(riga("b3"), col("b3"), true);
        setupLevel[riga("c3")][col("c3")] = new Pawn(riga("c3"), col("c3"), true);
        setupLevel[riga("g3")][col("g3")] = new Pawn(riga("g3"), col("g3"), true);
        setupLevel[riga("a2")][col("a2")] = new Pawn(riga("a2"), col("a2"), true);
        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("a1")][col("a1")] = new Rook(riga("a1"), col("a1"), true);
        setupLevel[riga("d1")][col("d1")] = new Rook(riga("d1"), col("d1"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h5", "h6"));
        soluzione.add(mossa("g7", "h8"));
        soluzione.add(mossa("g5", "f6"));
        soluzione.add(mossa("h8", "g8"));
        soluzione.add(mossa("f6", "g7"));

        return new QuizLevel(13, "Livello 13: Lolli (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LEVEL 14: Andersen (Matto in 4)
    // ==========================================
    private QuizLevel createLevel14() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("c8")][col("c8")] = new Rook(riga("c8"), col("c8"), false);
        setupLevel[riga("e8")][col("e8")] = new Knight(riga("e8"), col("e8"), false);
        setupLevel[riga("f8")][col("f8")] = new Rook(riga("f8"), col("f8"), false);
        setupLevel[riga("g8")][col("g8")] = new King(riga("g8"), col("g8"), false);
        setupLevel[riga("a7")][col("a7")] = new Pawn(riga("a7"), col("a7"), false);
        setupLevel[riga("f7")][col("f7")] = new Pawn(riga("f7"), col("f7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("b6")][col("b6")] = new Pawn(riga("b6"), col("b6"), false);
        setupLevel[riga("e6")][col("e6")] = new Pawn(riga("e6"), col("e6"), false);
        setupLevel[riga("g6")][col("g6")] = new Pawn(riga("g6"), col("g6"), false);
        setupLevel[riga("a5")][col("a5")] = new Knight(riga("a5"), col("a5"), false);
        setupLevel[riga("c5")][col("c5")] = new Queen(riga("c5"), col("c5"), false);
        setupLevel[riga("d5")][col("d5")] = new Bishop(riga("d5"), col("d5"), false);

        setupLevel[riga("h6")][col("h6")] = new Queen(riga("h6"), col("h6"), true);
        setupLevel[riga("b5")][col("b5")] = new Pawn(riga("b5"), col("b5"), true);
        setupLevel[riga("e5")][col("e5")] = new Rook(riga("e5"), col("e5"), true);
        setupLevel[riga("f5")][col("f5")] = new Knight(riga("f5"), col("f5"), true);
        setupLevel[riga("a3")][col("a3")] = new Pawn(riga("a3"), col("a3"), true);
        setupLevel[riga("d3")][col("d3")] = new Bishop(riga("d3"), col("d3"), true);
        setupLevel[riga("b2")][col("b2")] = new Bishop(riga("b2"), col("b2"), true);
        setupLevel[riga("c2")][col("c2")] = new Pawn(riga("c2"), col("c2"), true);
        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("d1")][col("d1")] = new Rook(riga("d1"), col("d1"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("f5", "e7"));
        soluzione.add(mossa("c5", "e7"));
        soluzione.add(mossa("h6", "h7"));
        soluzione.add(mossa("g8", "h7"));
        soluzione.add(mossa("e5", "h5"));
        soluzione.add(mossa("h7", "g8"));
        soluzione.add(mossa("h5", "h8"));

        return new QuizLevel(14, "Livello 14: Andersen (Matto in 4)", setupLevel, true, soluzione, 4);
    }

    // ==========================================
    // LEVEL 15: Pedone Principale (Matto in 2)
    // ==========================================
    private QuizLevel createLevel15() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("a8")][col("a8")] = new Rook(riga("a8"), col("a8"), false);
        setupLevel[riga("c8")][col("c8")] = new Bishop(riga("c8"), col("c8"), false);
        setupLevel[riga("g8")][col("g8")] = new Knight(riga("g8"), col("g8"), false);
        setupLevel[riga("h8")][col("h8")] = new Rook(riga("h8"), col("h8"), false);
        setupLevel[riga("a7")][col("a7")] = new Pawn(riga("a7"), col("a7"), false);
        setupLevel[riga("b7")][col("b7")] = new Pawn(riga("b7"), col("b7"), false);
        setupLevel[riga("c7")][col("c7")] = new Pawn(riga("c7"), col("c7"), false);
        setupLevel[riga("g7")][col("g7")] = new Queen(riga("g7"), col("g7"), false);
        setupLevel[riga("h7")][col("h7")] = new Pawn(riga("h7"), col("h7"), false);
        setupLevel[riga("b6")][col("b6")] = new Bishop(riga("b6"), col("b6"), false);
        setupLevel[riga("c6")][col("c6")] = new Knight(riga("c6"), col("c6"), false);
        setupLevel[riga("d6")][col("d6")] = new Pawn(riga("d6"), col("d6"), false);
        setupLevel[riga("e6")][col("e6")] = new King(riga("e6"), col("e6"), false);
        setupLevel[riga("e5")][col("e5")] = new Pawn(riga("e5"), col("e5"), false);

        setupLevel[riga("g5")][col("g5")] = new Bishop(riga("g5"), col("g5"), true);
        setupLevel[riga("h5")][col("h5")] = new Queen(riga("h5"), col("h5"), true);
        setupLevel[riga("d4")][col("d4")] = new Pawn(riga("d4"), col("d4"), true);
        setupLevel[riga("e4")][col("e4")] = new Pawn(riga("e4"), col("e4"), true);
        setupLevel[riga("c3")][col("c3")] = new Pawn(riga("c3"), col("c3"), true);
        setupLevel[riga("a2")][col("a2")] = new Pawn(riga("a2"), col("a2"), true);
        setupLevel[riga("b2")][col("b2")] = new Pawn(riga("b2"), col("b2"), true);
        setupLevel[riga("f2")][col("f2")] = new Pawn(riga("f2"), col("f2"), true);
        setupLevel[riga("g2")][col("g2")] = new Pawn(riga("g2"), col("g2"), true);
        setupLevel[riga("h2")][col("h2")] = new Pawn(riga("h2"), col("h2"), true);
        setupLevel[riga("a1")][col("a1")] = new Rook(riga("a1"), col("a1"), true);
        setupLevel[riga("b1")][col("b1")] = new Knight(riga("b1"), col("b1"), true);
        setupLevel[riga("f1")][col("f1")] = new Rook(riga("f1"), col("f1"), true);
        setupLevel[riga("g1")][col("g1")] = new King(riga("g1"), col("g1"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("h5", "e8"));
        soluzione.add(mossa("g8", "e7"));
        soluzione.add(mossa("d4", "d5"));

        return new QuizLevel(15, "Livello 15: Pedone Principale (Matto in 2)", setupLevel, true, soluzione, 2);
    }

    // ==========================================
    // LEVEL 16: Sacrificio Finale
    // ==========================================
    private QuizLevel createLevel16() {
        Piece[][] setupLevel = createEmptyBoard();

        setupLevel[riga("c8")][col("c8")] = new Rook(riga("c8"), col("c8"), false);
        setupLevel[riga("h5")][col("h5")] = new King(riga("h5"), col("h5"), false);

        setupLevel[riga("f5")][col("f5")] = new King(riga("f5"), col("f5"), true);
        setupLevel[riga("e4")][col("e4")] = new Knight(riga("e4"), col("e4"), true);
        setupLevel[riga("g4")][col("g4")] = new Rook(riga("g4"), col("g4"), true);
        setupLevel[riga("h3")][col("h3")] = new Pawn(riga("h3"), col("h3"), true);

        List<MoveRequest> soluzione = new ArrayList<>();
        soluzione.add(mossa("e4", "f6"));
        soluzione.add(mossa("h5", "h6"));
        soluzione.add(mossa("g4", "g6"));

        return new QuizLevel(16, "Livello 16: Sacrificio Finale", setupLevel, true, soluzione, 2);
    }
}