package com.example.chess.controller;

import android.graphics.Color;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;
import com.example.chess.util.ChessUtil;
import com.example.chess.service.StockfishService;
import com.example.chess.R;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BotGameController implements GameModeController {
    private Integer selectedPosition = null;
    private boolean isBotThinking = false;
    private Board board;

    @Override public void initializeGame(Board board) { this.board = board; }

    @Override
    public void handleSquareClick(int position, GameCallback callback) {
        if (isBotThinking || !board.isWhiteTurn()) return;
        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite()) { selectedPosition = position; callback.refreshUI(); }
        } else {
            int sR = MoveCalculator.toRow(selectedPosition), sC = MoveCalculator.toCol(selectedPosition);
            Piece movingPiece = board.getPiece(sR, sC), target = board.getPiece(row, col);
            if (target != null && target.isWhite()) { selectedPosition = position; callback.refreshUI(); return; }

            if (board.movePiece(sR, sC, row, col)) {
                callback.stopTimerView();
                int prev = selectedPosition; selectedPosition = null;
                callback.animatePieceMove(prev, position, movingPiece, () -> {
                    callback.updateCapturedPieces();
                    callback.refreshUI();
                    if (checkEndGame(callback)) return;
                    playBotMove(callback);
                });
            } else {
                selectedPosition = null; callback.refreshUI();
            }
        }
    }

    private void playBotMove(GameCallback callback) {
        isBotThinking = true;
        callback.updateStatusText(callback.getStr(R.string.bot_pensa), Color.LTGRAY);
        Retrofit r = new Retrofit.Builder().baseUrl("https://stockfish.online/").addConverterFactory(GsonConverterFactory.create()).build();
        StockfishService s = r.create(StockfishService.class);
        s.getBestMove(board.toFen(), 5).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override
            public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String move = response.body().bestmove.replace("bestmove ", "").trim();
                    if (move.length() >= 4) {
                        int start = ChessUtil.algebraicToIndex(move.substring(0, 2));
                        int end = ChessUtil.algebraicToIndex(move.substring(2, 4));
                        char promo = move.length() == 5 ? move.charAt(4) : 'q';
                        Piece p = board.getPiece(start/8, start%8);
                        board.movePiece(start/8, start%8, end/8, end%8, promo);
                        callback.animatePieceMove(start, end, p, () -> {
                            isBotThinking = false;
                            callback.updateCapturedPieces();
                            updateStatus(callback);
                            callback.refreshUI();
                            if (!checkEndGame(callback)) callback.startTimerView();
                        });
                        return;
                    }
                }
                isBotThinking = false; updateStatus(callback); callback.startTimerView();
            }
            @Override public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) { 
                isBotThinking = false; updateStatus(callback); callback.startTimerView(); 
            }
        });
    }

    private void updateStatus(GameCallback callback) {
        boolean white = board.isWhiteTurn(), inCheck = board.isKingInCheck(white);
        if (white) {
            String col = callback.getStr(R.string.bianco);
            String scacco = inCheck ? callback.getStr(R.string.scacco) : "";
            callback.updateStatusText(callback.getStr(R.string.turno_bot, col, scacco), inCheck ? Color.RED : Color.WHITE);
        } else callback.updateStatusText(callback.getStr(R.string.bot_pensa), Color.LTGRAY);
    }

    private boolean checkEndGame(GameCallback callback) {
        boolean t = board.isWhiteTurn();
        if (!board.hasAnyLegalMoves(t)) {
            if (board.isKingInCheck(t)) {
                String winner = callback.getStr(t ? R.string.nero : R.string.bianco).toUpperCase();
                callback.updateStatusText(callback.getStr(R.string.scacco_matto_vince, winner), Constants.GOLDENROD);
            } else callback.updateStatusText(callback.getStr(R.string.stallo), Color.LTGRAY);
            callback.finishGame(); return true;
        }
        return false;
    }

    @Override public void handleTimeOut(GameCallback callback) {}
    @Override public void onPause(Board board) {}
    public Integer getSelectedPosition() { return selectedPosition; }
}