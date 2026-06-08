package com.example.chess.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chess.model.Board;
import com.example.chess.model.User; // Importiamo il tuo nuovo modello User

public class ChessLocalDataSource {

    private static final String PREFS_NAME = "ChessPrefs";
    private static final String KEY_USER_NAME = "logged_user_name";
    private static final String KEY_USER_EMAIL = "logged_user_email";
    private static final String KEY_USER_TOKEN = "logged_user_token";

    private final SharedPreferences prefs;
    private Board savedBoard; // Rimane in memoria, pronto per Room in futuro

    // Il Context viene passato qui una volta sola, convertito in ApplicationContext per sicurezza
    public ChessLocalDataSource(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- GESTIONE DELLA PARTITA (SCACCHIERA) ---

    public void saveGame(Board board) {
        this.savedBoard = board;
    }

    public Board loadGame() {
        return savedBoard;
    }

    // --- GESTIONE DELL'UTENTE TRAMITE IL NUOVO MODELLO USER ---

    // Aggiornato per salvare tutti i campi del tuo oggetto User
    public void saveUser(User user) {
        if (user == null) return;

        prefs.edit()
                .putString(KEY_USER_NAME, user.getName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_TOKEN, user.getIdToken())
                .apply(); // Salva in modo asincrono nei thread di background
    }

    // Recupera i dati dalle SharedPreferences e ricostruisce l'oggetto User completo
    public User getSavedUser() {
        String email = prefs.getString(KEY_USER_EMAIL, null);

        // Se non c'è l'email salvata, significa che non c'è nessun utente loggato
        if (email == null) {
            return null;
        }

        String name = prefs.getString(KEY_USER_NAME, "Ospite");
        String token = prefs.getString(KEY_USER_TOKEN, "");

        return new User(name, email, token);
    }

    // Cancella completamente i dati dell'utente dalle preferenze locali (utile al logout)
    public void clearUser() {
        prefs.edit()
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_TOKEN)
                .apply();
    }
}