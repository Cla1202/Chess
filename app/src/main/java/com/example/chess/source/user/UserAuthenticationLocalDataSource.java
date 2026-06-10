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
        // Initialize SharedPreferences directly in the constructor
        this.sharedPreferences = context.getSharedPreferences("ChessPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public MutableLiveData<Result> getUser(String name, String email, String password, boolean isUserRegistered) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // Keep your local logic to check password length
        if (password.length() > 5) {
            saveUserLocally(email);

            // If a name is passed (during registration), use it.
            // Otherwise (during login), extract the first part of the email as before.
            String mockName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            User localUser = new User(mockName, email, "local_fake_uid");

            liveData.postValue(new Result.Success(localUser));
        } else {
            // If the password is too short, return the error using the Result class
            liveData.postValue(new Result.Error("error_password_short"));
        }

        return liveData;
    }

    @Override
    public MutableLiveData<Result> getGoogleUser(String idToken) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // Simulate local saving for Google sign-in
        String googleEmail = "google_user@gmail.com";
        saveUserLocally(googleEmail);

        User googleUser = new User("Google User", googleEmail, idToken);
        liveData.postValue(new Result.Success(googleUser));

        return liveData;
    }

    @Override
    public MutableLiveData<Result> resetPassword(String email) {
        MutableLiveData<Result> liveData = new MutableLiveData<>();
        // In the local/mock context, simply simulate that the request was successful
        liveData.postValue(new Result.Success(null));
        return liveData;
    }

    @Override
    public MutableLiveData<Result> logout() {
        MutableLiveData<Result> liveData = new MutableLiveData<>();

        // When the user logs out, clear data from SharedPreferences
        clearLocalUser();

        liveData.postValue(new Result.Success(null));
        return liveData;
    }

    // Helper method to save the user string
    private void saveUserLocally(String username) {
        sharedPreferences.edit().putString(Constants.KEY_LOGGED_USER, username).apply();
    }

    // Helper method to remove the user when they disconnect
    private void clearLocalUser() {
        sharedPreferences.edit().remove("logged_user").apply();
    }
}