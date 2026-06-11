package com.example.chess.ui.welcome.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view. View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.model.Result;
import com.example.chess.model.User;
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.welcome.viewmodel.UserViewModel;
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;
import com.example.chess.ui.welcome.LoginActivity;
import com.example.chess.util.Constants;
import com.example.chess.util.NetworkUtil;
import com.example.chess.util.ServiceLocator;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class LoginFragment extends Fragment {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ImageButton btnGoogleLogin;
    private TextView tvRegisterLink;
    private TextView tvForgotPasswordLink;

    private UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UPDATED: Using ServiceLocator instead of "new UserRepository()"
        IChessUserRepository userRepository = ServiceLocator.getInstance().getUserRepository();
        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)
        ).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUsername = view.findViewById(R.id.usernameInput);
        etPassword = view.findViewById(R.id.passwordInput);
        btnLogin = view.findViewById(R.id.loginButton);
        btnGoogleLogin = view.findViewById(R.id.googleLoginButton);
        tvRegisterLink = view.findViewById(R.id.registerLink);
        tvForgotPasswordLink = view.findViewById(R.id.forgotPasswordLink);

        tvRegisterLink.setOnClickListener(v ->
                ((LoginActivity) requireActivity()).loadFragment(new RegisterFragment(), true)
        );

        tvForgotPasswordLink.setOnClickListener(v -> {
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).loadFragment(new ResetPasswordFragment(), true);
            }
        });

        // 1. LOGIN WITH EMAIL AND PASSWORD VIA VIEWMODEL
        btnLogin.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.errore_connessione), Toast.LENGTH_LONG).show();
                return;
            }

            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // ESSENTIAL MODIFICATION: Pass null as the first parameter (Name field)
            userViewModel.getUser(null, email, password, true).observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    User loggedUser = ((Result.Success) result).getUser();

                    // Display custom name in the welcome Toast
                    String welcomeMsg = getString(R.string.welcome_back, loggedUser.getName());
                    Toast.makeText(getContext(), welcomeMsg, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    intent.putExtra(Constants.EXTRA_CURRENT_USER, loggedUser);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    String errorMessage = ((Result.Error) result).getMessage();
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        btnGoogleLogin.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.errore_connessione), Toast.LENGTH_LONG).show();
                return;
            }
            avviaLoginGoogleModerno();
        });
    }

    private void avviaLoginGoogleModerno() {
        LoginActivity activity = (LoginActivity) requireActivity();

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("983665898988-oppci7pehlt92g9uqpjg0j83t40u31rt.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        activity.getCredentialManager().getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                GoogleIdTokenCredential cred = GoogleIdTokenCredential.createFrom(credential.getData());
                                authenticateWithGoogleToken(cred.getIdToken());
                            } catch (Exception e) {
                                Toast.makeText(activity, "Error reading Token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e("GOOGLE_LOGIN", "Error: " + e.getMessage());
                        if (e.getMessage() != null && e.getMessage().contains("No credentials available")) {
                            Toast.makeText(activity, "No Google account found on the device or access not properly configured.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, "Google Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    // 2. LOGIN WITH GOOGLE VIA VIEWMODEL
    private void authenticateWithGoogleToken(String idToken) {
        userViewModel.getGoogleUser(idToken).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                User loggedUser = ((Result.Success) result).getUser();

                Toast.makeText(getContext(), getString(R.string.login_google_success), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.putExtra(Constants.EXTRA_CURRENT_USER, loggedUser);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            } else {
                String errorMessage = ((Result.Error) result).getMessage();
                Toast.makeText(getContext(), "Google Firebase Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
