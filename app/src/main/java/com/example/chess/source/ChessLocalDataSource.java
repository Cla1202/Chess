package com.example.chess.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chess.model.Board;
import com.example.chess.model.User; // Import your new User model

public class ChessLocalDataSource {

    private static final String PREFS_NAME = "ChessPrefs";
    private static final String KEY_USER_NAME = "logged_user_name";
    private static final String KEY_USER_EMAIL = "logged_user_email";
    private static final String KEY_USER_TOKEN = "logged_user_token";

    private final SharedPreferences prefs;
    private Board savedBoard; // Remains in memory, ready for Room in the future

    // Context is passed here once and converted to ApplicationContext for safety
    public ChessLocalDataSource(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- GAME MANAGEMENT (CHESSBOARD) ---

    public void saveGame(Board board) {
        this.savedBoard = board;
    }

    public Board loadGame() {
        return savedBoard;
    }

    // --- USER MANAGEMENT USING THE NEW USER MODEL ---

    // Updated to save all fields of your User object
    public void saveUser(User user) {
        if (user == null) return;

        prefs.edit()
                .putString(KEY_USER_NAME, user.getName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_TOKEN, user.getIdToken())
                .apply(); // Saves asynchronously on background threads
    }

    // Retrieves data from SharedPreferences and reconstructs the complete User object
    public User getSavedUser() {
        String email = prefs.getString(KEY_USER_EMAIL, null);

        // If there is no saved email, it means no user is logged in
        if (email == null) {
            return null;
        }

        String name = prefs.getString(KEY_USER_NAME, "Guest");
        String token = prefs.getString(KEY_USER_TOKEN, "");

        return new User(name, email, token);
    }

    // Completely clears user data from local preferences (useful for logout)
    public void clearUser() {
        prefs.edit()
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_TOKEN)
                .apply();
    }
}