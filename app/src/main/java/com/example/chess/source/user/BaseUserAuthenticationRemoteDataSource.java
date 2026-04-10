package com.example.chess.source.user;

import com.example.chess.repository.user.UserResponseCallback;

public abstract class BaseUserAuthenticationRemoteDataSource {
    public abstract void authenticate(String username, String password, UserResponseCallback callback);
}