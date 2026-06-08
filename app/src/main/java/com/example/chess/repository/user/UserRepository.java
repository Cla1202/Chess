package com.example.chess.repository.user;

import androidx.lifecycle.MutableLiveData;
import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest; // <-- IMPORTAZIONE AGGIUNTA

public class UserRepository implements IChessUserRepository {

    private final FirebaseAuth mAuth;

    public UserRepository() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    // AGGIORNATO: Accetta il parametro name all'inizio
    @Override
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();

        if (isUserRegistered) {
            // LOGIN CLASSICO (Il nome non serve, Firebase lo ha già sul server)
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User myUser = mapFirebaseUserToCustomUser(task.getResult().getUser());
                            mutableLiveData.postValue(new Result.Success(myUser));
                        } else {
                            mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                        }
                    });
        } else {
            // REGISTRAZIONE NUOVO UTENTE
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();

                            // Se la creazione ha successo e abbiamo un nome inserito, lo salviamo su Firebase Auth
                            if (firebaseUser != null && name != null && !name.isEmpty()) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            // Una volta associato il nome su Firebase, creiamo il nostro User locale e inviamo il Success
                                            User myUser = new User(name, firebaseUser.getEmail(), firebaseUser.getUid());
                                            mutableLiveData.postValue(new Result.Success(myUser));
                                        });
                            } else {
                                // Fallback se il nome dovesse essere vuoto
                                User myUser = mapFirebaseUserToCustomUser(firebaseUser);
                                mutableLiveData.postValue(new Result.Success(myUser));
                            }
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
                        User myUser = mapFirebaseUserToCustomUser(task.getResult().getUser());
                        mutableLiveData.postValue(new Result.Success(myUser));
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
        mutableLiveData.postValue(new Result.Success(null));
        return mutableLiveData;
    }

    @Override
    public MutableLiveData<Result> resetPassword(String email) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mutableLiveData.postValue(new Result.Success(null));
                    } else {
                        mutableLiveData.postValue(new Result.Error(task.getException().getMessage()));
                    }
                });
        return mutableLiveData;
    }

    @Override
    public User getLoggedUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return mapFirebaseUserToCustomUser(firebaseUser);
    }

    private User mapFirebaseUserToCustomUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        // Se il displayName su Firebase è nullo o vuoto, facciamo un fallback pulito sulla prima parte dell'email
        String name = firebaseUser.getDisplayName();
        if (name == null || name.isEmpty()) {
            if (firebaseUser.getEmail() != null) {
                name = firebaseUser.getEmail().split("@")[0];
            } else {
                name = "Giocatore";
            }
        }

        return new User(
                name,
                firebaseUser.getEmail(),
                firebaseUser.getUid()
        );
    }
}