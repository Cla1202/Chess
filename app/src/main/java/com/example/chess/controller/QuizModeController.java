package com.example.chess.controller;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import java.util.ArrayList;
import java.util.List;

public class QuizModeController implements GameModeController {
    private final QuizLevel quizLevel;
    private int currentMoveIndex = 0;
    private int errorCount = 0;
    private Integer selectedPosition = null;
    private boolean isComputerThinking = false;
    private Board board;
    private boolean isHintActive = false;

    public QuizModeController(QuizLevel quizLevel) { this.quizLevel = quizLevel; }

    @Override
    public void initializeGame(Board board) {
        this.board = board;
        board.setupCustomBoard(quizLevel.getInitialBoardSetup(), quizLevel.isWhiteTurnToStart());
    }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        if (isComputerThinking) return;
        int row = position / 8, col = position % 8;
        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) { selectedPosition = position; callback.refreshUI(); }
        } else {
            Piece clicked = board.getPiece(row, col);
            if (clicked != null && clicked.isWhite() == board.isWhiteTurn()) { selectedPosition = position; callback.refreshUI(); return; }
            int sR = selectedPosition / 8, sC = selectedPosition % 8;
            MoveRequest expected = quizLevel.getSolutionMoves().get(currentMoveIndex);
            if (sR == expected.startRow && sC == expected.startCol && row == expected.endRow && col == expected.endCol) {
                callback.stopTimerView();
                isHintActive = false;
                Piece moving = board.getPiece(sR, sC);
                board.movePiece(sR, sC, row, col);
                int prev = selectedPosition; selectedPosition = null;
                callback.animatePieceMove(prev, position, moving, () -> {
                    currentMoveIndex++;
                    callback.updateCapturedPieces();
                    if (currentMoveIndex < quizLevel.getSolutionMoves().size()) {
                        callback.updateStatusText("Ottimo! Risposta del computer...", Color.WHITE);
                        playComputerMove(callback);
                    } else {
                        callback.updateStatusText("Livello Superato!", Color.GREEN);
                        callback.showToast("Progresso salvato!");
                        callback.finishGame();
                    }
                });
            } else {
                errorCount++;
                int rem = quizLevel.getMaxAttempts() - errorCount;
                isHintActive = false;
                if (rem <= 0) {
                    callback.stopTimerView(); callback.updateStatusText("HAI PERSO!", Color.RED); callback.finishGame();
                } else callback.updateStatusText("Mossa errata! Vite: " + rem, Color.RED);
                selectedPosition = null; callback.refreshUI();
            }
        }
    }

    private void playComputerMove(GameCallback callback) {
        isComputerThinking = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest move = quizLevel.getSolutionMoves().get(currentMoveIndex);
            Piece p = board.getPiece(move.startRow, move.startCol);
            board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            callback.animatePieceMove(move.startRow * 8 + move.startCol, move.endRow * 8 + move.endCol, p, () -> {
                currentMoveIndex++;
                callback.updateCapturedPieces();
                callback.updateStatusText("Tocca a te!", Color.WHITE);
                isComputerThinking = false;
                callback.refreshUI();
                callback.startTimerView();
            });
        }, 1000);
    }

    @Override public void handleTimeOut(GameCallback callback) {
        callback.updateStatusText("TEMPO SCADUTO!", Color.RED);
        new Handler(Looper.getMainLooper()).postDelayed(callback::finishGame, 2000);
    }
    @Override public void onPause(Board board) {}
    public Integer getSelectedPosition() { return selectedPosition; }
    public boolean isHintActive() { return isHintActive; }
    public List<Integer> getHintPositions() {
        List<Integer> dests = new ArrayList<>();
        if (currentMoveIndex < quizLevel.getSolutionMoves().size()) dests.add(quizLevel.getSolutionMoves().get(currentMoveIndex).endRow * 8 + quizLevel.getSolutionMoves().get(currentMoveIndex).endCol);
        return dests;
    }
    public void showHint(GameCallback callback) {
        if (isComputerThinking || currentMoveIndex >= quizLevel.getSolutionMoves().size()) return;
        MoveRequest m = quizLevel.getSolutionMoves().get(currentMoveIndex);
        selectedPosition = m.startRow * 8 + m.startCol;
        isHintActive = true;
        callback.showToast("Suggerimento attivato!");
        callback.refreshUI();
    }
}