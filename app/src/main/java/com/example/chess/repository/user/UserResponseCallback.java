package com.example.chess.repository.user;

public interface UserResponseCallback {
    void onSuccess(String username);
    void onFailure(String errorMessage);
}