package com.example.chess.repository.user;

import androidx.lifecycle.MutableLiveData;
import com.example.chess.model.Result;
import com.example.chess.model.User; // <-- IMPORTANT

public interface IChessUserRepository {
    MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered);
    MutableLiveData<Result> getGoogleUser(String idToken);
    MutableLiveData<Result> logout();
    MutableLiveData<Result> resetPassword(String email);

    // Must return your custom User model
    User getLoggedUser();
}