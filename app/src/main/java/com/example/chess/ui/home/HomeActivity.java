package com.example.chess.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.model.User;
import com.example.chess.repository.user.IChessUserRepository;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.ui.home.fragment.PlayFragment;
import com.example.chess.ui.home.fragment.ProfileFragment;
import com.example.chess.ui.home.fragment.QuizFragment;
import com.example.chess.ui.home.fragment.SettingsFragment;
import com.example.chess.ui.welcome.LoginActivity; // Ensure this import is correct
import com.example.chess.ui.welcome.viewmodel.UserViewModel; // Check your ViewModel path
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;
import com.example.chess.util.ChessUtil;
import com.example.chess.util.Constants;
import com.example.chess.util.ServiceLocator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initialize the ViewModel
        IChessUserRepository userRepository = ServiceLocator.getInstance().getUserRepository();
        userViewModel = new ViewModelProvider(this, new UserViewModelFactory(userRepository)).get(UserViewModel.class);

        // 2. Retrieve the user from the Intent (if coming from Login/Register)
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_CURRENT_USER)) {
            currentUser = getIntent().getParcelableExtra(Constants.EXTRA_CURRENT_USER);
        } else {
            // If not in the Intent, try to retrieve the active session from Firebase
            currentUser = userViewModel.getLoggedUser();
        }

        // 3. Security Check: if the user is null, they are not authorized to be here
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Prevent the rest of the code from executing
        }

        // 4. Handle Bottom Navigation
        BottomNavigationView b = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            // Set the correct icon on startup
            b.setSelectedItemId(R.id.nav_profile);
        }

        b.setOnItemSelectedListener(item -> {
            Fragment s = null;
            int id = item.getItemId();
            if (id == R.id.nav_play) s = new PlayFragment();
            else if (id == R.id.nav_quiz) s = new QuizFragment();
            else if (id == R.id.nav_profile) s = new ProfileFragment();
            else if (id == R.id.nav_settings) s = new SettingsFragment();

            if (s != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, s).commit();
            }
            return true;
        });
    }

    // 5. "Bridge" method for Fragments to access user data
    public User getCurrentUser() {
        return currentUser;
    }

    // "Bridge" method for Fragments to use the ViewModel (e.g., for Logout)
    public UserViewModel getUserViewModel() {
        return userViewModel;
    }
}
