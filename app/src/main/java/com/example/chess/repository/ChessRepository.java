package com.example.chess.repository;

import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.source.game.BaseChessLocalDataSource;
import com.example.chess.source.game.BaseChessRemoteDataSource;

public class ChessRepository {

    private final BaseChessLocalDataSource localDataSource;
    private final BaseChessRemoteDataSource remoteDataSource;

    // Il costruttore ora accetta le astrazioni dei Data Source
    public ChessRepository(BaseChessLocalDataSource localDataSource, BaseChessRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    public Board getNewGame() {
        return new Board();
    }

    // --- DELEGA AL REMOTE DATA SOURCE ---
    public void getBestMove(String fen, int depth, BotMoveCallback callback) {
        remoteDataSource.getBestMove(fen, depth, callback);
    }

    // --- DELEGA AL LOCAL DATA SOURCE ---
    public void saveProgress(LevelProgress progress) {
        localDataSource.saveProgress(progress);
    }

    public LevelProgress getProgress(int levelId, String userId) {
        return localDataSource.getProgress(levelId, userId);
    }

    public interface BotMoveCallback {
        void onSuccess(String bestMove);
        void onError(Throwable t);
    }
}