package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chess.R;
import com.example.chess.model.LoginRequest;
import com.example.chess.repository.ChessRepository;



public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private ChessRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        repository = new ChessRepository();

        loginButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            LoginRequest request = new LoginRequest(user, pass);

            if (repository.performLogin(this,request)) {
                // Login riuscito: Vai alla scacchiera
                Toast.makeText(this, "Login effettuato!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                // Puoi passare l'username alla MainActivity
                intent.putExtra("USERNAME", user);
                startActivity(intent);
                finish(); // Chiudi LoginActivity
            } else {
                Toast.makeText(this, "Credenziali non valide", Toast.LENGTH_LONG).show();
            }
        });
    }
}