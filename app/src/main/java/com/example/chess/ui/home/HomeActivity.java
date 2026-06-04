package com.example.chess.ui.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.chess.R;
import com.example.chess.util.ChessUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Seleziona il primo Fragment all'apertura dell'app
        if (savedInstanceState == null) {
            // Sostituisci "new PlayFragment()" con il tuo Fragment reale
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        }

        // Gestisce i click sulla barra in basso
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment;

            if (itemId == R.id.nav_play) {
                selectedFragment = new PlayFragment(); // La tua vecchia MainActivity
            } else if (itemId == R.id.nav_quiz) {
                selectedFragment = new QuizFragment(); // La tua vecchia LevelSelectionActivity
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment(); // La nuova schermata Profilo
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else {
                return false;
            }

            // Scambia il fragment sul palcoscenico
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });
    }
}