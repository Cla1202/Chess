package com.example.chess.repository.user;

import com.example.chess.model.LoginRequest;

public interface IChessUserRepository {
    void login(LoginRequest loginRequest, UserResponseCallback callback);
    String getLoggedUser();
}