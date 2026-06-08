package com.example.chess.ui.welcome.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.repository.user.IChessUserRepository;

/**
 * Custom ViewModelProvider per poter passare il Repository
 * nel costruttore di UserViewModel.
 */
public class UserViewModelFactory implements ViewModelProvider.Factory {

    private final IChessUserRepository userRepository;

    public UserViewModelFactory(IChessUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(userRepository);
        }
        throw new IllegalArgumentException("Classe ViewModel sconosciuta");
    }
}