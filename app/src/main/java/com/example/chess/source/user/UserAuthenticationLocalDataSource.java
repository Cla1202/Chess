package com.example.chess.source.user;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.chess.repository.user.UserResponseCallback;

public class UserAuthenticationLocalDataSource extends BaseUserAuthenticationRemoteDataSource {
    private final Context context;

    public UserAuthenticationLocalDataSource(Context context) {
        this.context = context;
    }

    @Override
    public void authenticate(String username, String password, UserResponseCallback callback) {
        // Simuliamo un controllo (es. password > 5 caratteri)
        if (password.length() > 5) {
            saveUserLocally(username);
            callback.onSuccess(username);
        } else {
            callback.onFailure("Password troppo corta!");
        }
    }

    private void saveUserLocally(String username) {
        SharedPreferences prefs = context.getSharedPreferences("ChessPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("logged_user", username).apply();
    }
}