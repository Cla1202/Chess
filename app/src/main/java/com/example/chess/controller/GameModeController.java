package com.example.chess.controller;

import com.example.chess.model.Board;
import android.graphics.Color;

public interface GameModeController {
    void initializeGame(Board board);

    void handleSquareClick(int position, GameCallback callback);

    void handleTimeOut(GameCallback callback);

    void onPause(Board board);

    interface GameCallback {
        void refreshUI();
        void showToast(String message);
        void updateStatusText(String text, int color);
        void animatePieceMove(int startPos, int endPos, com.example.chess.model.Piece piece, Runnable onComplete);
        void finishGame();
        void startTimerView();
        void stopTimerView();
        void updateCapturedPieces();
        void showPromotionDialog(boolean isWhite, PromotionListener listener);
        String getStr(int resId);
        String getStr(int resId, Object... args);
        // NUOVA RIGA: Per comunicare all'Activity che il livello è finito
        void onLevelCompleted(String levelId);
    }

    interface PromotionListener {
        void onPieceSelected(char type);
    }
}