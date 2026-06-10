package com.example.chess.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chess.model.Board;
import com.example.chess.model.User;
import com.example.chess.util.Constants;

public class ChessLocalDataSource {

    private final SharedPreferences prefs;
    private Board savedBoard; // Remains in memory, ready for Room in the future

    // Context is passed here once and converted to ApplicationContext for safety
    public ChessLocalDataSource(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- GAME MANAGEMENT (CHESSBOARD) ---

    public void saveGame(Board board) {
        this.savedBoard = board;
    }

    public Board loadGame() {
        return savedBoard;
    }

    // --- USER MANAGEMENT USING THE NEW USER MODEL ---

    // Updated to use centralized constants
    public void saveUser(User user) {
        if (user == null) return;

        prefs.edit()
                .putString(Constants.KEY_USER_NAME, user.getName())
                .putString(Constants.KEY_USER_EMAIL, user.getEmail())
                .putString(Constants.KEY_USER_TOKEN, user.getIdToken())
                .apply(); // Saves asynchronously on background threads
    }

    // Retrieves data from SharedPreferences and reconstructs the complete User object
    public User getSavedUser() {
        String email = prefs.getString(Constants.KEY_USER_EMAIL, null);

        // If there is no saved email, it means no user is logged in
        if (email == null) {
            return null;
        }

        String name = prefs.getString(Constants.KEY_USER_NAME, "Guest");
        String token = prefs.getString(Constants.KEY_USER_TOKEN, "");

        return new User(name, email, token);
    }

    // Completely clears user data from local preferences (useful for logout)
    public void clearUser() {
        prefs.edit()
                .remove(Constants.KEY_USER_NAME)
                .remove(Constants.KEY_USER_EMAIL)
                .remove(Constants.KEY_USER_TOKEN)
                .apply();
    }
}
