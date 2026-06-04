package com.example.chess.controller;

import android.graphics.Color;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.model.Pawn;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;
import com.example.chess.R;

public class LocalTwoPlayerController implements GameModeController {
    private Integer selectedPosition = null;
    private Board board;

    @Override public void initializeGame(Board board) { this.board = board; }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);
        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position; callback.refreshUI();
            }
        } else {
            int sR = MoveCalculator.toRow(selectedPosition), sC = MoveCalculator.toCol(selectedPosition);
            Piece moving = board.getPiece(sR, sC), target = board.getPiece(row, col);
            
            if (target != null && target.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position; callback.refreshUI(); return;
            }

            // CONTROLLO PROMOZIONE PRIMA DI MUOVERE
            if (moving instanceof Pawn && (row == 0 || row == 7) && board.isValidMove(sR, sC, row, col)) {
                callback.showPromotionDialog(moving.isWhite(), type -> {
                    Piece promoMoving = board.getPiece(sR, sC); // Recupera di nuovo il pezzo
                    board.movePiece(sR, sC, row, col, type);
                    callback.stopTimerView();
                    callback.animatePieceMove(sR * 8 + sC, row * 8 + col, promoMoving, () -> {
                        callback.updateCapturedPieces();
                        updateStatus(callback);
                        callback.refreshUI();
                        if (board.hasAnyLegalMoves(board.isWhiteTurn())) callback.startTimerView();
                    });
                });
                selectedPosition = null;
                return;
            }

            if (board.movePiece(sR, sC, row, col)) {
                callback.stopTimerView();
                int prev = selectedPosition; selectedPosition = null;
                callback.animatePieceMove(prev, position, moving, () -> {
                    callback.updateCapturedPieces();
                    updateStatus(callback);
                    callback.refreshUI();
                    if (board.hasAnyLegalMoves(board.isWhiteTurn())) callback.startTimerView();
                });
            } else {
                selectedPosition = null; callback.refreshUI();
            }
        }
    }

    private void updateStatus(GameCallback callback) {
        boolean white = board.isWhiteTurn(), inCheck = board.isKingInCheck(white);
        if (!board.hasAnyLegalMoves(white)) {
            callback.stopTimerView();
            if (inCheck) {
                String winner = callback.getStr(white ? R.string.nero : R.string.bianco).toUpperCase();
                callback.updateStatusText(callback.getStr(R.string.scacco_matto_vince, winner), Constants.GOLDENROD);
            } else {
                callback.updateStatusText(callback.getStr(R.string.stallo), Color.LTGRAY);
            }
            callback.finishGame();
        } else {
            String colore = callback.getStr(white ? R.string.bianco : R.string.nero);
            String scaccoInfo = inCheck ? callback.getStr(R.string.scacco) : "";
            callback.updateStatusText(callback.getStr(R.string.turno, colore, scaccoInfo), inCheck ? Color.RED : Color.WHITE);
        }
    }

    @Override public void handleTimeOut(GameCallback callback) { 
        callback.updateStatusText(callback.getStr(R.string.tempo_scaduto_msg), Color.RED);
    }
    @Override public void onPause(Board board) {}
    public Integer getSelectedPosition() { return selectedPosition; }
}