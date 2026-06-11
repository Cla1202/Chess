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
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.welcome.viewmodel.UserViewModel;
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;
import com.example.chess.util.Constants;
import com.example.chess.util.NetworkUtil;
import com.example.chess.util.ServiceLocator;

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

        IChessUserRepository userRepository = ServiceLocator.getInstance().getUserRepository();
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
            if (!NetworkUtil.isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.errore_connessione), Toast.LENGTH_LONG).show();
                return;
            }

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // validazione input con stringhe tradotte
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.getUser(name, email, password, false).observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    User newUser = ((Result.Success) result).getUser();

                    // Messaggio di benvenuto tradotto
                    String welcomeMsg = getString(R.string.welcome_new_user, newUser.getName());
                    Toast.makeText(getContext(), welcomeMsg, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    // Uso della costante centralizzata
                    intent.putExtra(Constants.EXTRA_CURRENT_USER, newUser);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    String errorMessage = ((Result.Error) result).getMessage();
                    Toast.makeText(getContext(), getString(R.string.error_registration, errorMessage), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
