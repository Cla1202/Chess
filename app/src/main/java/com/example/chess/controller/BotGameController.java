package com.example.chess.controller;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;

public class BotGameController implements GameModeController {
    private Integer selectedPosition = null;
    private boolean isBotThinking = false;
    private Board board;

    @Override
    public void initializeGame(Board board) {
        this.board = board;
    }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        if (isBotThinking || !board.isWhiteTurn()) return; // Assumiamo il giocatore sia sempre Bianco

        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite()) {
                selectedPosition = position;
                callback.refreshUI();
            }
        } else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);
            Piece movingPiece = board.getPiece(startRow, startCol);

            if (board.movePiece(startRow, startCol, row, col)) {
                callback.stopTimerView();
                int prevSelected = selectedPosition;
                selectedPosition = null;

                callback.animatePieceMove(prevSelected, position, movingPiece, () -> {
                    callback.refreshUI();
                    if (checkEndGame(callback)) return;

                    // Turno del Bot
                    callback.updateStatusText("Il Bot sta pensando...", Color.LTGRAY);
                    playBotMove(callback);
                });
            } else {
                callback.showToast("Mossa non valida!");
                selectedPosition = null;
                callback.refreshUI();
            }
        }
    }

    private void playBotMove(GameCallback callback) {
        isBotThinking = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // --- INSERisci QUI LA LOGICA DI ELABORAZIONE DELLA MOSSA DEL TUO BOT ---
            // Esempio fittizio: prendiamo la prima mossa casuale o calcolata dall'algoritmo
            // com.example.chess.model.MoveRequest botMove = YourBotEngine.calculateBestMove(board);

            // Per ora simuliamo il cambio turno se l'algoritmo non è ancora agganciato:
            isBotThinking = false;

            // Esegui mossa sulla board, triggera l'animazione e aggiorna la UI
            // callback.animatePieceMove(botStartPos, botEndPos, botPiece, () -> { ... });

            callback.startTimerView();
        }, 1200);
    }

    private boolean checkEndGame(GameCallback callback) {
        boolean turno = board.isWhiteTurn();
        if (!board.hasAnyLegalMoves(turno)) {
            if (board.isKingInCheck(turno)) {
                callback.updateStatusText("🏆 SCACCO MATTO! " + (turno ? "Vince il Bot" : "Hai vinto tu!"), Constants.GOLDENROD);
            } else {
                callback.updateStatusText("🤝 STALLO", Color.LTGRAY);
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleTimeOut(GameCallback callback) {
        if (board.isWhiteTurn()) {
            callback.updateStatusText("🏆 TEMPO SCADUTO! Vince il Bot.", Color.RED);
        }
    }

    @Override
    public void onPause(Board board) {}

    public Integer getSelectedPosition() { return selectedPosition; }
    public void clearSelection() { selectedPosition = null; }
}