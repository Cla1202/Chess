package com.example.chess.repository.user;

import androidx.lifecycle.MutableLiveData;
import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest; // <-- IMPORT ADDED

public class UserRepository implements IChessUserRepository {

    private final FirebaseAuth mAuth;

    public UserRepository() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    // UPDATED: Accepts the name parameter at the beginning
    @Override
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        MutableLiveData<Result> mutableLiveData = new MutableLiveData<>();

        if (isUserRegistered) {
            // CLASSIC LOGIN (The name is not needed, Firebase already has it on the server)
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
            // NEW USER REGISTRATION
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();

                            // If creation is successful and we have an entered name, save it on Firebase Auth
                            if (firebaseUser != null && name != null && !name.isEmpty()) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            // Once the name is associated on Firebase, create our local User and send Success
                                            User myUser = new User(name, firebaseUser.getEmail(), firebaseUser.getUid());
                                            mutableLiveData.postValue(new Result.Success(myUser));
                                        });
                            } else {
                                // Fallback if the name is empty
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

        // If the displayName on Firebase is null or empty, perform a clean fallback to the first part of the email
        String name = firebaseUser.getDisplayName();
        if (name == null || name.isEmpty()) {
            if (firebaseUser.getEmail() != null) {
                name = firebaseUser.getEmail().split("@")[0];
            } else {
                name = "Player";
            }
        }

        return new User(
                name,
                firebaseUser.getEmail(),
                firebaseUser.getUid()
        );
    }
}