package com.example.chess.controller;

import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;
import java.util.ArrayList;

public class LocalTwoPlayerController implements GameModeController {
    private Integer selectedPosition = null;
    private Board board;

    @Override
    public void initializeGame(Board board) {
        this.board = board;
        // Usa la scacchiera standard ereditata dal repository o resettata
    }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                callback.refreshUI();
            }
        } else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);
            Piece movingPiece = board.getPiece(startRow, startCol);

            // 1. CONTROLLO PREVENTIVO: L'utente ha cliccato su un altro pezzo dello STESSO colore?
            Piece targetPiece = board.getPiece(row, col);
            if (targetPiece != null && targetPiece.isWhite() == board.isWhiteTurn()) {
                // Cambia istantaneamente la selezione sul nuovo pezzo ed esce dal metodo
                selectedPosition = position;
                callback.refreshUI();
                return;
            }

            // 2. Se non ha cliccato su un pezzo alleato, tenta di eseguire il movimento reale
            if (board.movePiece(startRow, startCol, row, col)) {
                callback.stopTimerView();
                int prevSelected = selectedPosition;
                selectedPosition = null;

                callback.animatePieceMove(prevSelected, position, movingPiece, () -> {
                    updateGameStatusText(callback);
                    callback.refreshUI();
                    if (board.hasAnyLegalMoves(board.isWhiteTurn())) {
                        callback.startTimerView();
                    }
                });
            } else {
                // Se clicca su una casella vuota non valida o su un pezzo avversario ma la mossa non è legale
                callback.showToast("Mossa non valida!");
                selectedPosition = null; // Deseleziona
                callback.refreshUI();    // Pulisce i pallini
            }
        }
    }

    private void updateGameStatusText(GameCallback callback) {
        boolean turnoBianco = board.isWhiteTurn();
        boolean inScacco = board.isKingInCheck(turnoBianco);
        boolean haMosseLegali = board.hasAnyLegalMoves(turnoBianco);

        if (!haMosseLegali) {
            callback.stopTimerView();
            if (inScacco) {
                callback.updateStatusText("🏆 SCACCO MATTO! Vince il " + (turnoBianco ? "NERO" : "BIANCO"), Constants.GOLDENROD);
            } else {
                callback.updateStatusText("🤝 STALLO (Pareggio)", Color.LTGRAY);
            }
        } else {
            String text = "Turno: " + (turnoBianco ? "Bianco" : "Nero") + (inScacco ? " (SCACCO!)" : "");
            callback.updateStatusText(text, inScacco ? Color.RED : Color.WHITE);
        }
    }

    @Override
    public void handleTimeOut(GameCallback callback) {
        // Se scade il tempo del giocatore corrente, vince l'altro
        boolean turnoBianco = board.isWhiteTurn();
        callback.stopTimerView();
        callback.updateStatusText("🏆 TEMPO SCADUTO! Vince il " + (turnoBianco ? "NERO" : "BIANCO"), Constants.GOLDENROD);
        callback.showToast("Tempo scaduto!");
    }

    @Override
    public void onPause(Board board) {}

    public Integer getSelectedPosition() { return selectedPosition; }
    public void clearSelection() { selectedPosition = null; }
}