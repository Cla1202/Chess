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
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.repository.user.UserResponseCallback;
import com.example.chess.source.user.BaseUserAuthenticationRemoteDataSource;
import com.example.chess.source.user.UserAuthenticationLocalDataSource;


public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);


        loginButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            // 1. Creiamo il Data Source e il Repository (seguendo la nuova struttura)
            BaseUserAuthenticationRemoteDataSource dataSource = new UserAuthenticationLocalDataSource(this);
            IChessUserRepository userRepository = new UserRepository(dataSource);

            // 2. Eseguiamo il login con la Callback
            userRepository.login(new LoginRequest(user, pass), new UserResponseCallback() {
                @Override
                public void onSuccess(String username) {
                    Toast.makeText(LoginActivity.this, "Login effettuato!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}