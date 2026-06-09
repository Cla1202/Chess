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

    // 1. Method for classic Login and Registration (with support for the Name parameter)
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        return userRepository.getUser(name, email, password, isUserRegistered);
    }

    // 2. Method for Google Login
    public MutableLiveData<Result> getGoogleUser(String idToken) {
        return userRepository.getGoogleUser(idToken);
    }

    // 3. Method for password reset
    public MutableLiveData<Result> resetPassword(String email) {
        return userRepository.resetPassword(email);
    }

    // 4. Method for Logout
    public MutableLiveData<Result> logout() {
        return userRepository.logout();
    }

    // 5. Method to get the connected user from the local database
    public User getLoggedUser() {
        return userRepository.getLoggedUser();
    }

    // Getters and Setters for authentication errors
    public boolean isAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(boolean authenticationError) {
        this.authenticationError = authenticationError;
    }
}