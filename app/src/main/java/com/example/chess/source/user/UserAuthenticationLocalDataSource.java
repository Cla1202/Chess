package com.example.chess.source.user;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.MutableLiveData;

import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.example.chess.util.Constants;

public class UserAuthenticationLocalDataSource extends BaseUserAuthenticationRemoteDataSource {
    private final SharedPreferences sharedPreferences;

    public UserAuthenticationLocalDataSource(Context context) {
        // Inizializziamo le SharedPreferences direttamente nel costruttore
        this.sharedPreferences = context.getSharedPreferences("ChessPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // Manteniamo la tua logica di controllo locale sulla lunghezza della password
        if (password.length() > 5) {
            saveUserLocally(email);

            // Se viene passato un nome (in registrazione), usiamo quello.
            // Altrimenti (in login), estraiamo la prima parte dell'email come prima.
            String mockName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            User localUser = new User(mockName, email, "local_fake_uid");

            liveData.postValue(new Result.Success(localUser));
        } else {
            // Se la password è corta, restituiamo l'errore tramite la classe Result
            liveData.postValue(new Result.Error(Constants.PASSWORD_CORTA));
        }

        return liveData;
    }

    @Override
    public MutableLiveData<Result> getGoogleUser(String idToken) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // Simuliamo il salvataggio locale per l'accesso con Google
        String googleEmail = "google_user@gmail.com";
        saveUserLocally(googleEmail);

        User googleUser = new User("Google User", googleEmail, idToken);
        liveData.postValue(new Result.Success(googleUser));

        return liveData;
    }

    @Override
    public MutableLiveData<Result> resetPassword(String email) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();
        // Nel contesto locale/mock, simuliamo semplicemente che l'invio sia riuscito
        liveData.postValue(new Result.Success(null));
        return liveData;
    }

    @Override
    public MutableLiveData<Result> logout() {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // Quando l'utente fa il logout, puliamo i dati dalle SharedPreferences
        clearLocalUser();

        liveData.postValue(new Result.Success(null));
        return liveData;
    }

    // Metodo helper per salvare la stringa dell'utente
    private void saveUserLocally(String username) {
        sharedPreferences.edit().putString("logged_user", username).apply();
    }

    // Metodo helper per rimuovere l'utente quando si disconnette
    private void clearLocalUser() {
        sharedPreferences.edit().remove("logged_user").apply();
    }
}