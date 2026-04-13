package com.example.chess.model;

import java.util.List;

public class QuizLevel {
    private int levelId;
    private String title;

    // Configurazione iniziale: una matrice 8x8 con i pezzi piazzati per il quiz
    private Piece[][] initialBoardSetup;

    // Definisce a chi tocca muovere per primo in questo quiz (es. il Bianco)
    private boolean whiteTurnToStart;

    // Sequenza di mosse che formano l'unica soluzione corretta
    private List<MoveRequest> solutionMoves;

    private int maxAttempts;

    public QuizLevel(int levelId, String title, Piece[][] initialBoardSetup, boolean whiteTurnToStart, List<MoveRequest> solutionMoves, int maxAttempts) {
        this.levelId = levelId;
        this.title = title;
        this.initialBoardSetup = initialBoardSetup;
        this.whiteTurnToStart = whiteTurnToStart;
        this.solutionMoves = solutionMoves;
        this.maxAttempts = maxAttempts;
    }

    public int getLevelId() { return levelId; }
    public String getTitle() { return title; }
    public Piece[][] getInitialBoardSetup() { return initialBoardSetup; }
    public boolean isWhiteTurnToStart() { return whiteTurnToStart; }
    public List<MoveRequest> getSolutionMoves() { return solutionMoves; }

    public int getMaxAttempts() { return maxAttempts; }


}