package com.example.chess.util;

import android.content.Context;

import com.example.chess.database.ChessDatabase;
import com.example.chess.repository.ChessRepository;
import com.example.chess.repository.QuizRepository;
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.source.game.BaseChessLocalDataSource;
import com.example.chess.source.game.BaseChessRemoteDataSource;
import com.example.chess.source.game.ChessRemoteDataSource;
import com.example.chess.source.game.ChessRoomDataSource;

public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private IChessUserRepository userRepository;
    private ChessRepository chessRepository;

    // Private constructor to prevent direct instantiation
    private ServiceLocator() {}

    // Thread-safe Singleton implementation
    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    /**
     * Provides a singleton instance of IChessUserRepository.
     */
    public IChessUserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new UserRepository();
        }
        return userRepository;
    }

    public ChessDatabase getChessDatabase(Context context) {
        return ChessDatabase.getInstance(context.getApplicationContext());
    }

    private QuizRepository quizRepository;

    public QuizRepository getQuizRepository() {
        if (quizRepository == null) {
            quizRepository = new QuizRepository();
        }
        return quizRepository;
    }

    // ... variabili esistenti ...
    private BaseChessRemoteDataSource chessRemoteDataSource;
    private BaseChessLocalDataSource chessLocalDataSource;

    // 1. Fornitore del Remote Data Source
    public BaseChessRemoteDataSource getChessRemoteDataSource() {
        if (chessRemoteDataSource == null) {
            chessRemoteDataSource = new ChessRemoteDataSource();
        }
        return chessRemoteDataSource;
    }

    // 2. Fornitore del Local Data Source (Richiede il DAO di Room)
    public BaseChessLocalDataSource getChessLocalDataSource(Context context) {
        if (chessLocalDataSource == null) {
            ChessDatabase db = getChessDatabase(context);
            chessLocalDataSource = new ChessRoomDataSource(db.levelDao());
        }
        return chessLocalDataSource;
    }

    // 3. AGGIORNA il fornitore della Repository per iniettare i Data Source
    public ChessRepository getChessRepository(Context context) {
        if (chessRepository == null) {
            chessRepository = new ChessRepository(
                    getChessLocalDataSource(context),
                    getChessRemoteDataSource()
            );
        }
        return chessRepository;
    }
}