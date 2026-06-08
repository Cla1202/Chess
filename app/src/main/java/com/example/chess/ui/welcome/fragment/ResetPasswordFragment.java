package com.example.chess.ui.welcome.fragment;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.model.Result;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.ui.welcome.viewmodel.UserViewModel;
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;

public class ResetPasswordFragment extends Fragment {

    private EditText etResetEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inizializzazione del ViewModel tramite la Factory
        UserRepository userRepository = new UserRepository();
        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)
        ).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Collegamento dei componenti dell'interfaccia grafica
        etResetEmail = view.findViewById(R.id.resetEmailInput);
        btnResetPassword = view.findViewById(R.id.resetPasswordButton);
        tvBackToLogin = view.findViewById(R.id.backToLoginFromResetLink);

        // Torna indietro al Login rimosso dal BackStack manuale
        tvBackToLogin.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        // UNICO Listener per il pulsante: gestisce Log e chiamata Firebase
        btnResetPassword.setOnClickListener(v -> {
            String email = etResetEmail.getText().toString().trim();

            // Stampa nel Logcat l'esatta stringa che viene spedita a Firebase per debug
            Log.d("FIREBASE_RESET", "Indirizzo inviato: [" + email + "]");

            if (isEmailOk(email)) {
                // Chiamata asincrona al ViewModel
                userViewModel.resetPassword(email).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "Email di ripristino inviata con successo!", Toast.LENGTH_SHORT).show();

                        // Ritorno manuale al LoginFragment
                        getParentFragmentManager().popBackStack();
                    } else {
                        // Estrazione dell'errore restituito da Firebase
                        String errorMessage = ((Result.Error) result).getMessage();
                        Toast.makeText(getContext(), "Errore: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Validazione dell'indirizzo email
    private boolean isEmailOk(String email) {
        if (email.isEmpty()) {
            etResetEmail.setError("L'email non può essere vuota");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etResetEmail.setError("Inserisci un indirizzo email valido");
            return false;
        } else {
            etResetEmail.setError(null);
            return true;
        }
    }
}