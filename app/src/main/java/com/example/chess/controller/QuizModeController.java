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

    public QuizModeController(QuizLevel quizLevel) {
        this.quizLevel = quizLevel;
    }

    @Override
    public void initializeGame(Board board) {
        this.board = board;
        board.setupCustomBoard(quizLevel.getInitialBoardSetup(), quizLevel.isWhiteTurnToStart());
    }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        if (isComputerThinking) return;

        int row = position / 8;
        int col = position % 8;

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                callback.refreshUI();
            }
        } else {
            Piece clickedPiece = board.getPiece(row, col);
            if (clickedPiece != null && clickedPiece.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                callback.refreshUI();
                return;
            }
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;
            MoveRequest expectedMove = quizLevel.getSolutionMoves().get(currentMoveIndex);

            //Checking if it's the right move
            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                callback.stopTimerView();
                isHintActive = false;

                Piece movingPiece = board.getPiece(startRow, startCol);
                board.movePiece(startRow, startCol, row, col);

                int prevSelected = selectedPosition;
                selectedPosition = null;

                callback.animatePieceMove(prevSelected, position, movingPiece, () -> {
                    currentMoveIndex++;
                    if (currentMoveIndex < quizLevel.getSolutionMoves().size()) {
                        callback.updateStatusText("Ottimo! Risposta del computer...", Color.WHITE);
                        playComputerMove(callback);
                    } else {
                        callback.updateStatusText("Livello Superato!", Color.GREEN);
                        callback.showToast("Progresso salvato!");
                        // Qui andrebbe inserito il salvataggio su DB Room asincrono se necessario
                        callback.finishGame();
                    }
                });
            } else {
                errorCount++;
                int remainingTries = quizLevel.getMaxAttempts() - errorCount;
                isHintActive = false;
                if (remainingTries <= 0) {
                    callback.stopTimerView();
                    callback.updateStatusText("HAI PERSO!", Color.RED);
                    callback.finishGame();
                } else {
                    callback.updateStatusText("Mossa errata! Vite: " + remainingTries, Color.RED);
                }
                selectedPosition = null;
                callback.refreshUI();
            }
        }
    }

    private void playComputerMove(GameCallback callback) {
        isComputerThinking = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest computerMove = quizLevel.getSolutionMoves().get(currentMoveIndex);
            int startPos = computerMove.startRow * 8 + computerMove.startCol;
            int endPos = computerMove.endRow * 8 + computerMove.endCol;
            Piece movingPiece = board.getPiece(computerMove.startRow, computerMove.startCol);

            board.movePiece(computerMove.startRow, computerMove.startCol, computerMove.endRow, computerMove.endCol);

            callback.animatePieceMove(startPos, endPos, movingPiece, () -> {
                currentMoveIndex++;
                callback.updateStatusText("Tocca a te!", Color.WHITE);
                isComputerThinking = false;
                callback.refreshUI();
                callback.startTimerView();
            });
        }, 1000);
    }

    @Override
    public void handleTimeOut(GameCallback callback) {
        callback.updateStatusText("TEMPO SCADUTO!", Color.RED);
        callback.showToast("Sei stato troppo lento!");
        new Handler(Looper.getMainLooper()).postDelayed(callback::finishGame, 2000);
    }

    @Override
    public void onPause(Board board) {}

    public Integer getSelectedPosition() { return selectedPosition; }
    public void clearSelection() { selectedPosition = null; }
    // METODO UTILE PER L'ADAPTER: Permette a GameActivity di sapere se deve filtrare le mosse legali
    public boolean isHintActive() { return isHintActive; }

    // Ritorna solo la casella finale esatta del quiz
    public List<Integer> getHintPositions() {
        List<Integer> hintDest = new ArrayList<>();
        if (currentMoveIndex < quizLevel.getSolutionMoves().size()) {
            MoveRequest expectedMove = quizLevel.getSolutionMoves().get(currentMoveIndex);
            hintDest.add(expectedMove.endRow * 8 + expectedMove.endCol);
        }
        return hintDest;
    }
    public void showHint(GameCallback callback) {
        if (isComputerThinking || currentMoveIndex >= quizLevel.getSolutionMoves().size()) {
            return;
        }

        MoveRequest expectedMove = quizLevel.getSolutionMoves().get(currentMoveIndex);
        int startPosition = expectedMove.startRow * 8 + expectedMove.startCol;

        selectedPosition = startPosition;
        isHintActive = true;

        int endPosition = expectedMove.endRow * 8 + expectedMove.endCol;
        ArrayList<Integer> exactDest = new ArrayList<>();
        exactDest.add(endPosition);

        callback.showToast("Suggerimento attivato! Guarda la scacchiera.");

        Piece p = board.getPiece(expectedMove.startRow, expectedMove.startCol);
        if (p != null) {
            String pieceName = p.getClass().getSimpleName().toLowerCase();
            callback.updateStatusText("Suggerimento: Muovi " + pieceName, Color.parseColor("#FF9800"));
        }

        callback.refreshUI();
    }
}