package com.example.chess.ui.welcome.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.chess.model.User;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.welcome.viewmodel.UserViewModel;
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;

public class RegisterFragment extends Fragment {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inizializzazione del ViewModel associato all'Activity
        UserRepository userRepository = new UserRepository();
        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)
        ).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.regNameInput);
        etEmail = view.findViewById(R.id.regEmailInput);
        etPassword = view.findViewById(R.id.regPasswordInput);
        etConfirmPassword = view.findViewById(R.id.regConfirmPasswordInput);
        btnRegister = view.findViewById(R.id.registerButton);
        tvBackToLogin = view.findViewById(R.id.backToLoginLink);

        tvBackToLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 1. Validazione dell'input
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Per favore, compila tutti i campi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Le password non coincidono", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "La password deve contenere almeno 6 caratteri", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Chiamata al ViewModel con il NUOVO parametro "name"
            userViewModel.getUser(name, email, password, false).observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    // Recuperiamo l'utente appena creato (che ora ha già il nome integrato da Firebase!)
                    User newUser = ((Result.Success) result).getUser();

                    Toast.makeText(getContext(), "Benvenuto, " + newUser.getName() + "!", Toast.LENGTH_SHORT).show();

                    // Passiamo direttamente alla HomeActivity
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    intent.putExtra("CURRENT_USER", newUser);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    String errorMessage = ((Result.Error) result).getMessage();
                    Toast.makeText(getContext(), "Errore durante la registrazione: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}