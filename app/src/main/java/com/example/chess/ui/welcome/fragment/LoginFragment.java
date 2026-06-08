package com.example.chess.ui.welcome.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.chess.R;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.welcome.LoginActivity;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ImageButton btnGoogleLogin;
    private ImageButton btnFacebookLogin;
    private TextView tvRegisterLink;
    private TextView tvForgotPasswordLink;

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
        btnFacebookLogin = view.findViewById(R.id.facebookLoginButton);
        tvRegisterLink = view.findViewById(R.id.registerLink);
        tvForgotPasswordLink = view.findViewById(R.id.forgotPasswordLink);

        tvRegisterLink.setOnClickListener(v ->
                ((LoginActivity) requireActivity()).loadFragment(new RegisterFragment(), true)
        );

        // Dentro il metodo onViewCreated di LoginFragment.java
        tvForgotPasswordLink.setOnClickListener(v -> {
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).loadFragment(new ResetPasswordFragment(), true);
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginActivity activity = (LoginActivity) requireActivity();
            activity.getAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Accesso effettuato!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(activity, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            activity.finish();
                        } else {
                            Toast.makeText(getContext(), "Errore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnGoogleLogin.setOnClickListener(v -> avviaLoginGoogleModerno());

        btnFacebookLogin.setOnClickListener(v ->
                Toast.makeText(getContext(), "Login con Facebook in fase di sviluppo", Toast.LENGTH_SHORT).show()
        );
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
                                firebaseAuthWithGoogle(cred.getIdToken());
                            } catch (Exception e) {
                                Toast.makeText(activity, "Errore lettura Token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e("GOOGLE_LOGIN", "Errore: " + e.getMessage());
                        Toast.makeText(activity, "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        LoginActivity activity = (LoginActivity) requireActivity();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        activity.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(activity, "Accesso con Google completato", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "Errore Firebase Google: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}