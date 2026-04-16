package com.example.chess.model;

import java.util.List;

public class QuizLevel {
    private String title;
    private Piece[][] initialBoardSetup;
    private List<MoveRequest> solutionMoves;
    private boolean whiteTurnToStart;
    private int maxAttempts;

    public QuizLevel(String title, Piece[][] initialBoardSetup, boolean whiteTurnToStart, List<MoveRequest> solutionMoves, int maxAttempts) {
        this.title = title;
        this.initialBoardSetup = initialBoardSetup;
        this.whiteTurnToStart = whiteTurnToStart;
        this.solutionMoves = solutionMoves;
        this.maxAttempts = maxAttempts;
    }

    public String getTitle() { return title; }
    public Piece[][] getInitialBoardSetup() { return initialBoardSetup; }
    public List<MoveRequest> getSolutionMoves() { return solutionMoves; }
    public boolean isWhiteTurnToStart() { return whiteTurnToStart; }
    public int getMaxAttempts() { return maxAttempts; }
}