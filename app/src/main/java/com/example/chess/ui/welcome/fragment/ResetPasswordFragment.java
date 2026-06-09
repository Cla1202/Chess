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

        // Initialize the ViewModel using the Factory
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

        // Link UI components
        etResetEmail = view.findViewById(R.id.resetEmailInput);
        btnResetPassword = view.findViewById(R.id.resetPasswordButton);
        tvBackToLogin = view.findViewById(R.id.backToLoginFromResetLink);

        // Return to Login fragment by manually popping the BackStack
        tvBackToLogin.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        // Single listener for the button: handles Logging and the Firebase call
        btnResetPassword.setOnClickListener(v -> {
            String email = etResetEmail.getText().toString().trim();

            // Print the exact string sent to Firebase to the Logcat for debugging
            Log.d("FIREBASE_RESET", "Address sent: [" + email + "]");

            if (isEmailOk(email)) {
                // Asynchronous call to the ViewModel
                userViewModel.resetPassword(email).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "Reset email sent successfully!", Toast.LENGTH_SHORT).show();

                        // Manually return to LoginFragment
                        getParentFragmentManager().popBackStack();
                    } else {
                        // Extract the error returned by Firebase
                        String errorMessage = ((Result.Error) result).getMessage();
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Email address validation
    private boolean isEmailOk(String email) {
        if (email.isEmpty()) {
            etResetEmail.setError("Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etResetEmail.setError("Enter a valid email address");
            return false;
        } else {
            etResetEmail.setError(null);
            return true;
        }
    }
}