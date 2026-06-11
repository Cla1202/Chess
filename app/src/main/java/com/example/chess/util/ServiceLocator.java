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
import com.example.chess.source.quiz.AssetQuizDataSource;
import com.example.chess.source.quiz.IQuizDataSource;

public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private IChessUserRepository userRepository;
    private ChessRepository chessRepository;
    private QuizRepository quizRepository;

    private ServiceLocator() {}

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

    public IChessUserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new UserRepository();
        }
        return userRepository;
    }

    public ChessDatabase getChessDatabase(Context context) {
        return ChessDatabase.getInstance(context.getApplicationContext());
    }

    /**
     * Provides a singleton instance of QuizRepository.
     * @param context Required to create the AndroidResourceProvider and AssetQuizDataSource.
     */
    public QuizRepository getQuizRepository(Context context) {
        if (quizRepository == null) {
            IResourceProvider resourceProvider = new AndroidResourceProvider(context);
            IQuizDataSource quizDataSource = new AssetQuizDataSource(context);
            quizRepository = new QuizRepository(resourceProvider, quizDataSource);
        }
        return quizRepository;
    }

    private BaseChessRemoteDataSource chessRemoteDataSource;
    private BaseChessLocalDataSource chessLocalDataSource;

    public BaseChessRemoteDataSource getChessRemoteDataSource() {
        if (chessRemoteDataSource == null) {
            chessRemoteDataSource = new ChessRemoteDataSource();
        }
        return chessRemoteDataSource;
    }

    public BaseChessLocalDataSource getChessLocalDataSource(Context context) {
        if (chessLocalDataSource == null) {
            ChessDatabase db = getChessDatabase(context);
            chessLocalDataSource = new ChessRoomDataSource(db.levelDao());
        }
        return chessLocalDataSource;
    }

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
