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
        BottomNavigationView b = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        b.setOnItemSelectedListener(item -> {
            Fragment s = null;
            int id = item.getItemId();
            if (id == R.id.nav_play) s = new PlayFragment();
            else if (id == R.id.nav_quiz) s = new QuizFragment();
            else if (id == R.id.nav_profile) s = new ProfileFragment();
            else if (id == R.id.nav_settings) s = new SettingsFragment();
            if (s != null) getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, s).commit();
            return true;
        });
    }
}