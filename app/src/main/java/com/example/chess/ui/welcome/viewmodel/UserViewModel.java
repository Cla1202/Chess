package com.example.chess.ui.welcome.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chess.model.Result;
import com.example.chess.repository.user.IChessUserRepository;

public class UserViewModel extends ViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();

    private final IChessUserRepository userRepository;
    private MutableLiveData<Result> userMutableLiveData;
    private boolean authenticationError;

    public UserViewModel(IChessUserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationError = false;
    }

    // Login classico (Email/Password) o Registrazione
    public MutableLiveData<Result> getUserMutableLiveData(String email, String password, boolean isUserRegistered) {
        if (userMutableLiveData == null) {
            getUserData(email, password, isUserRegistered);
        }
        return userMutableLiveData;
    }

    // Login con Google
    public MutableLiveData<Result> getGoogleUserMutableLiveData(String token) {
        if (userMutableLiveData == null) {
            getGoogleUserData(token);
        }
        return userMutableLiveData;
    }

    // Disconnessione
    public MutableLiveData<Result> logout() {
        if (userMutableLiveData == null) {
            userMutableLiveData = userRepository.logout();
        } else {
            userRepository.logout();
        }
        return userMutableLiveData;
    }

    // Recupero la sessione corrente, se esiste
    public Object getLoggedUser() { // Cambia Object con la tua classe User se ce l'hai
        return userRepository.getLoggedUser();
    }

    public boolean isAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(boolean authenticationError) {
        this.authenticationError = authenticationError;
    }

    private void getUserData(String email, String password, boolean isUserRegistered) {
        userMutableLiveData = userRepository.getUser(email, password, isUserRegistered);
    }

    private void getGoogleUserData(String token) {
        userMutableLiveData = userRepository.getGoogleUser(token);
    }
    // Dentro UserViewModel.java
    public MutableLiveData<Result> resetPassword(String email) {
        return userRepository.resetPassword(email);
    }
}