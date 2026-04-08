package com.example.chess.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chess.model.Board;

public class ChessLocalDataSource {
    // Qui in futuro potresti usare Room o SharedPreferences
    private Board savedBoard;
    private static final String PREFS_NAME = "ChessPrefs";
    private static final String KEY_USER = "logged_user";

    public void saveGame(Board board) {
        this.savedBoard = board;
    }

    public Board loadGame() {
        return savedBoard;
    }

    // Metodo per salvare l'utente
    public void saveUser(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER, username);
        editor.apply(); // Salva in modo asincrono
    }

    // (Opzionale) Metodo per recuperare l'utente salvato
    public String getSavedUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER, "Ospite");
    }


}