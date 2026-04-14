package com.example.chess.ui.home; // <-- Cambialo se il tuo package è diverso!

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chess.R;
import com.example.chess.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Gonfiamo" (rendiamo visibile) il layout XML che abbiamo appena creato
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView nameText = view.findViewById(R.id.profileName);
        TextView emailText = view.findViewById(R.id.profileEmail);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // 1. CHIEDIAMO A FIREBASE CHI È LOGGATO
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Se c'è un utente, scriviamo i suoi dati a schermo
            // Se il nome è nullo (succede raramente), mettiamo un nome di default
            String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Giocatore Sconosciuto";

            nameText.setText(displayName);
            emailText.setText(user.getEmail());
        }

        // 2. GESTIONE DEL TASTO LOGOUT
        logoutButton.setOnClickListener(v -> {
            // Diciamo a Firebase di chiudere la sessione
            FirebaseAuth.getInstance().signOut();

            // Torniamo alla schermata di Login
            // NOTA: Qui usiamo requireActivity() al posto di "this" per ottenere l'Activity che ospita il Fragment!
            Intent intent = new Intent(requireActivity(), LoginActivity.class);

            // Questo flag serve a cancellare tutta la cronologia.
            // Così se l'utente preme "indietro" dopo il logout, non rientra nell'app, ma la chiude.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });

        return view;
    }
}