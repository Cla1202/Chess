package com.example.chess.repository.user;

import com.example.chess.model.LoginRequest;
import com.example.chess.source.user.BaseUserAuthenticationRemoteDataSource;

public class UserRepository implements IChessUserRepository {
    private final BaseUserAuthenticationRemoteDataSource dataSource;

    public UserRepository(BaseUserAuthenticationRemoteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void login(LoginRequest loginRequest, UserResponseCallback callback) {
        dataSource.authenticate(loginRequest.username, loginRequest.password, callback);
    }

    @Override
    public String getLoggedUser() {
        // Qui andrebbe la logica per leggere dalle SharedPreferences
        return "Utente";
    }
}