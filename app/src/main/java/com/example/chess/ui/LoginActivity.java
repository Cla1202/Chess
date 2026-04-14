package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    // 1. Dichiara il nuovo gestore delle credenziali
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // 2. Inizializza il CredentialManager
        credentialManager = CredentialManager.create(this);

        EditText emailInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);
        ImageButton googleButton = findViewById(R.id.googleLoginButton);

        // --- AZIONE: CLICK SU ACCEDI CON GOOGLE (NUOVO METODO) ---
        googleButton.setOnClickListener(v -> avviaLoginGoogleModerno());

        // --- AZIONE: CLICK SU ACCEDI (EMAIL E PASSWORD) ---
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Inserisci email e password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Accesso effettuato!", Toast.LENGTH_SHORT).show();
                            vaiHome();
                        } else {
                            Toast.makeText(LoginActivity.this, "Errore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // --- AZIONE: CLICK SU REGISTRATI ---
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // --- NUOVO METODO CREDENTIAL MANAGER ---
    private void avviaLoginGoogleModerno() {
        // Costruisce la richiesta con il tuo Client ID
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("983665898988-oppci7pehlt92g9uqpjg0j83t40u31rt.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Esegue la richiesta in background
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();

                        // Controlla se la credenziale è di tipo Google ID Token
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                GoogleIdTokenCredential googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.getData());

                                // Abbiamo il Token! Lo passiamo a Firebase
                                String idToken = googleIdTokenCredential.getIdToken();
                                firebaseAuthWithGoogle(idToken);

                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Errore lettura Token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // Stampa l'errore in rosso nel pannello Logcat di Android Studio (in basso)
                        Log.e("GOOGLE_LOGIN", "Errore CredentialManager: " + e.getMessage());

                        // Mostra l'errore tecnico direttamente sul telefono
                        Toast.makeText(LoginActivity.this, "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // --- METODO: AUTENTICAZIONE GOOGLE CON FIREBASE (Rimane uguale) ---
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Accesso con Google completato", Toast.LENGTH_SHORT).show();
                        vaiHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Errore Firebase Google: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void vaiHome() {
        // Quando il login ha successo (l'utente non è null)
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

// Opzionale ma consigliato: Pulisce la cronologia così l'utente
// non può tornare alla pagina di login premendo "Indietro"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Distrugge definitivamente la LoginActivity
    }


}