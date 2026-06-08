package com.example.chess.repository.user;

import androidx.lifecycle.MutableLiveData;
import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class UserRepository implements IChessUserRepository {

    private final FirebaseAuth mAuth;

    public UserRepository() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public MutableLiveData<Result> getUser(String email, String password, boolean isUserRegistered) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();

        if (isUserRegistered) {
            // L'utente è già registrato -> Fai il LOGIN
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mutableLiveData.postValue(new Result.Success(task.getResult().getUser()));
                        } else {
                            mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                        }
                    });
        } else {
            // L'utente NON è registrato -> Fai la REGISTRAZIONE
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mutableLiveData.postValue(new Result.Success(task.getResult().getUser()));
                        } else {
                            mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                        }
                    });
        }

        return mutableLiveData;
    }

    @Override
    public MutableLiveData<Result> getGoogleUser(String idToken) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mutableLiveData.postValue(new Result.Success(task.getResult().getUser()));
                    } else {
                        mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                    }
                });

        return mutableLiveData;
    }

    @Override
    public MutableLiveData<Result> logout() {
        mAuth.signOut();
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();
        // Segnaliamo che l'operazione ha avuto successo passando null come utente
        mutableLiveData.postValue(new Result.Success(null));
        return mutableLiveData;
    }

    @Override
    public FirebaseUser getLoggedUser() {
        return mAuth.getCurrentUser();
    }

    // Dentro UserRepository.java
    @Override
    public MutableLiveData<Result> resetPassword(String email) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Passiamo null come utente perché l'operazione non restituisce un FirebaseUser
                        mutableLiveData.postValue(new Result.Success(null));
                    } else {
                        mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                    }
                });

        return mutableLiveData;
    }

    // Dentro UserRepository.java, aggiungi questo metodo privato
    private User mapFirebaseUserToCustomUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        // Convertiamo l'utente Firebase nel nostro modello.
        // Usiamo getUid() come token identificativo.
        return new User(
                firebaseUser.getDisplayName(), // Può essere null se registrato con sola email
                firebaseUser.getEmail(),
                firebaseUser.getUid()
        );
    }
}