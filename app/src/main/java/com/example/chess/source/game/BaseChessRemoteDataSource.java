package com.example.chess.source.game;

import com.example.chess.repository.ChessRepository;

public interface BaseChessRemoteDataSource {
    // For API di Stockfish
    void getBestMove(String fen, int depth, ChessRepository.BotMoveCallback callback);
}