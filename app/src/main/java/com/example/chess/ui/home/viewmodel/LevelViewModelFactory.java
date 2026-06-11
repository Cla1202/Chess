package com.example.chess.ui.home.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.database.ChessDatabase;
import com.example.chess.repository.IQuizRepository;

public class LevelViewModelFactory implements ViewModelProvider.Factory {

    private final ChessDatabase database;
    private final IQuizRepository quizRepository;

    public LevelViewModelFactory(ChessDatabase database, IQuizRepository quizRepository) {
        this.database = database;
        this.quizRepository = quizRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LevelViewModel.class)) {
            return (T) new LevelViewModel(database, quizRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
