package com.example.chess.repository;

import android.content.Context;

import com.example.chess.model.Board;
import com.example.chess.model.LoginRequest;
import com.example.chess.source.ChessLocalDataSource;

public class ChessRepository {
    private ChessLocalDataSource localDataSource;

    public ChessRepository() {
        this.localDataSource = new ChessLocalDataSource();
    }

    public Board getNewGame() {
        return new Board(); // Crea una scacchiera nuova
    }

    public Board getSavedGame() {
        Board b = localDataSource.loadGame();
        return (b != null) ? b : new Board();
    }

    public void saveCurrentGame(Board board) {
        localDataSource.saveGame(board);
    }
}