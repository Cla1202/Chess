package com.example.chess.ui.welcome;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.chess.R;
import com.example.chess.ui.welcome.fragment.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import androidx.credentials.CredentialManager;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.chess.util.ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        if (savedInstanceState == null) {
            loadFragment(new LoginFragment(), false);
        }
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public java.lang.Object getCredentialManagerObject() {
        return credentialManager;
    }

    public CredentialManager getCredentialManager() {
        return credentialManager;
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}