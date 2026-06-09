package com.example.chess.util;

import android.content.Context;

import com.example.chess.database.ChessDatabase;
import com.example.chess.repository.ChessRepository;
import com.example.chess.repository.QuizRepository;
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.repository.user.UserRepository;

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

    /**
     * Provides a singleton instance of ChessRepository.
     * Uses ApplicationContext to prevent memory leaks.
     */
    public ChessRepository getChessRepository(Context context) {
        if (chessRepository == null) {
            chessRepository = new ChessRepository(context.getApplicationContext());
        }
        return chessRepository;
    }

    /**
     * Provides the local Room Database instance.
     */
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
}