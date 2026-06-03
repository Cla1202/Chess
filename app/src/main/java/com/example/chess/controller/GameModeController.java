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
    }
}