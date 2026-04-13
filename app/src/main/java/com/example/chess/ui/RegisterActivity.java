package com.example.chess.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chess.R;

// Importiamo Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    // 1. Dichiara Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 2. Inizializza Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        EditText nameInput = findViewById(R.id.regNameInput);
        EditText emailInput = findViewById(R.id.regEmailInput);
        EditText passwordInput = findViewById(R.id.regPasswordInput);
        EditText confirmPasswordInput = findViewById(R.id.regConfirmPasswordInput);
        Button registerButton = findViewById(R.id.registerButton);
        TextView backToLogin = findViewById(R.id.backToLoginLink);

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            String confirmPass = confirmPasswordInput.getText().toString().trim();

            // Controlli base
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Le password non coincidono", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.length() < 6) {
                Toast.makeText(this, "La password deve avere almeno 6 caratteri", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Creazione dell'utente su Firebase!
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            // Aggiorniamo il nome del profilo
                            if (user != null) {
                                com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                                    Toast.makeText(RegisterActivity.this, "Account creato con successo!", Toast.LENGTH_LONG).show();
                                    finish(); // Torna al login
                                });
                            }

                        } else {
                            // --- GESTIONE DEGLI ERRORI SPECIFICI (Incluso Email già esistente) ---
                            try {
                                // Lanciamo l'eccezione catturata da Firebase per analizzarla
                                throw task.getException();

                            } catch (com.google.firebase.auth.FirebaseAuthUserCollisionException existEmail) {
                                // L'EMAIL ESISTE GIÀ!
                                Toast.makeText(RegisterActivity.this, "Questa email è già registrata! Vai al Login.", Toast.LENGTH_LONG).show();

                            } catch (com.google.firebase.auth.FirebaseAuthWeakPasswordException weakPassword) {
                                // LA PASSWORD È TROPPO SEMPLICE
                                Toast.makeText(RegisterActivity.this, "La password è troppo debole. Usa almeno 6 caratteri.", Toast.LENGTH_LONG).show();

                            } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException malformedEmail) {
                                // L'EMAIL È SCRITTA MALE (es. manca la @)
                                Toast.makeText(RegisterActivity.this, "Il formato dell'email non è valido.", Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                // ERRORE GENERICO (es. connessione internet assente)
                                Toast.makeText(RegisterActivity.this, "Errore durante la registrazione.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // Tasto per tornare indietro
        backToLogin.setOnClickListener(v -> finish());
    }
}