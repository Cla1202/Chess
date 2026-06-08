package com.example.chess.repository.user;

import androidx.lifecycle.MutableLiveData;

import com.example.chess.model.Result;
import com.google.firebase.auth.FirebaseUser;

public interface IChessUserRepository {
    // Ritorna LiveData per Login o Registrazione
    MutableLiveData<Result> getUser(String email, String password, boolean isUserRegistered);

    // Ritorna LiveData per l'accesso con Google
    MutableLiveData<Result> getGoogleUser(String idToken);

    // Gestisce la disconnessione
    MutableLiveData<Result> logout();
    // Dentro IChessUserRepository.java
    MutableLiveData<Result> resetPassword(String email);

    // Ritorna l'utente loggato correntemente, se esiste
    FirebaseUser getLoggedUser();
}