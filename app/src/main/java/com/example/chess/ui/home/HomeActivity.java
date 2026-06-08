package com.example.chess.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.model.User;
import com.example.chess.repository.user.UserRepository;
import com.example.chess.ui.home.fragment.PlayFragment;
import com.example.chess.ui.home.fragment.ProfileFragment;
import com.example.chess.ui.home.fragment.QuizFragment;
import com.example.chess.ui.home.fragment.SettingsFragment;
import com.example.chess.ui.welcome.LoginActivity; // Assicurati che questo import sia corretto
import com.example.chess.ui.welcome.viewmodel.UserViewModel; // Controlla il percorso del tuo ViewModel
import com.example.chess.ui.welcome.viewmodel.UserViewModelFactory;
import com.example.chess.util.ChessUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Inizializziamo il ViewModel
        UserRepository userRepository = new UserRepository();
        userViewModel = new ViewModelProvider(this, new UserViewModelFactory(userRepository)).get(UserViewModel.class);

        // 2. Recuperiamo l'utente dall'Intent (se veniamo dal Login/Register)
        if (getIntent() != null && getIntent().hasExtra("CURRENT_USER")) {
            currentUser = getIntent().getParcelableExtra("CURRENT_USER");
        } else {
            // Se non c'è nell'Intent, proviamo a recuperare la sessione attiva da Firebase
            currentUser = userViewModel.getLoggedUser();
        }

        // 3. Controllo di Sicurezza: se l'utente è null, non è autorizzato a stare qui.
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Blocchiamo l'esecuzione del resto del codice
        }

        // 4. Gestione della Bottom Navigation
        BottomNavigationView b = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            // Impostiamo l'icona corretta all'avvio
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

    // 5. Metodo "Ponte" per far accedere i Fragment ai dati dell'utente
    public User getCurrentUser() {
        return currentUser;
    }

    // Metodo "Ponte" per permettere ai Fragment di usare il ViewModel (es. per il Logout)
    public UserViewModel getUserViewModel() {
        return userViewModel;
    }
}