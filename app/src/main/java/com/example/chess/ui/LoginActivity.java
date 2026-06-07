package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.example.chess.R;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.util.ChessUtil;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private static final String TAG = "GOOGLE_LOGIN";
    // ASSICURATI CHE QUESTO SIA IL WEB CLIENT ID
    private static final String WEB_CLIENT_ID = "983665898988-oppci7pehlt92g9uqpjg0j83t40u31rt.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        if (mAuth.getCurrentUser() != null) {
            vaiHome();
            return;
        }

        ImageButton googleButton = findViewById(R.id.googleLoginButton);
        googleButton.setOnClickListener(v -> avviaLoginGoogle());

        configuraLoginEmail();
    }

    private void avviaLoginGoogle() {
        // Creazione opzione Google ID moderna
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Permette di vedere tutti gli account
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false) // Più stabile per il debug
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this, request, new CancellationSignal(), ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential && 
                            credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                GoogleIdTokenCredential tokenCred = GoogleIdTokenCredential.createFrom(credential.getData());
                                firebaseAuthWithGoogle(tokenCred.getIdToken());
                            } catch (Exception e) {
                                Log.e(TAG, "Errore parsing token", e);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Errore Google Login: " + e.getMessage());
                        // Se vedi "not available", controlla SHA-1 e Google Play Services
                        Toast.makeText(LoginActivity.this, "Google Login non disponibile: verifica SHA-1 o Play Store", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) vaiHome();
            else Toast.makeText(this, "Errore Firebase: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void configuraLoginEmail() {
        EditText eI = findViewById(R.id.usernameInput);
        EditText pI = findViewById(R.id.passwordInput);
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            String e = eI.getText().toString().trim(), p = pI.getText().toString().trim();
            if (!e.isEmpty() && !p.isEmpty()) {
                mAuth.signInWithEmailAndPassword(e, p).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) vaiHome();
                    else Toast.makeText(this, "Credenziali errate", Toast.LENGTH_SHORT).show();
                });
            }
        });
        findViewById(R.id.registerLink).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void vaiHome() {
        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}