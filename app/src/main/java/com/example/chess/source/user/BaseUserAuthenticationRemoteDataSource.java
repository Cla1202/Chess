package com.example.chess.source.user;

import androidx.lifecycle.MutableLiveData;
import com.example.chess.model.Result;

public abstract class BaseUserAuthenticationRemoteDataSource {

    // AGGIORNATO: Aggiunto il parametro String name all'inizio
    public abstract MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered);

    public abstract MutableLiveData<Result> getGoogleUser(String idToken);

    public abstract MutableLiveData<Result> resetPassword(String email);

    public abstract MutableLiveData<Result> logout();
}