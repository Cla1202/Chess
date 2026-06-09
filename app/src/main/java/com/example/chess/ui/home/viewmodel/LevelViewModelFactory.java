package com.example.chess.ui.home.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.database.ChessDatabase;

public class LevelViewModelFactory implements ViewModelProvider.Factory {

    private final ChessDatabase database;

    public LevelViewModelFactory(ChessDatabase database) {
        this.database = database;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(LevelViewModel.class)) {
            return (T) new LevelViewModel(database);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}