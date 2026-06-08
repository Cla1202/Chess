package com.example.chess.ui.welcome.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.example.chess.repository.user.IChessUserRepository;

public class UserViewModel extends ViewModel {

    private final IChessUserRepository userRepository;
    private boolean authenticationError;

    public UserViewModel(IChessUserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationError = false;
    }

    // 1. Metodo per Login e Registrazione classica (con supporto al parametro Name)
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        return userRepository.getUser(name, email, password, isUserRegistered);
    }

    // 2. Metodo per Login con Google
    public MutableLiveData<Result> getGoogleUser(String idToken) {
        return userRepository.getGoogleUser(idToken);
    }

    // 3. Metodo per il recupero password
    public MutableLiveData<Result> resetPassword(String email) {
        return userRepository.resetPassword(email);
    }

    // 4. Metodo per il Logout
    public MutableLiveData<Result> logout() {
        return userRepository.logout();
    }

    // 5. Metodo per ottenere l'utente connesso dal database locale
    public User getLoggedUser() {
        return userRepository.getLoggedUser();
    }

    // Getter e Setter per gli errori di autenticazione
    public boolean isAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(boolean authenticationError) {
        this.authenticationError = authenticationError;
    }
}