package com.example.chess.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chess.R;

public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        Button btnStartGame = view.findViewById(R.id.btnStartGame);

        btnStartGame.setOnClickListener(v -> {
            // Crea un "Biglietto" (Intent) per viaggiare da questo Fragment alla MainActivity
            Intent intent = new Intent(requireActivity(), com.example.chess.ui.MainActivity.class);

            // Passare al MainActivity se si vuole o no il timer
            intent.putExtra("EXTRA_TIMER_ENABLED", false);

            // Fai partire il viaggio!
            startActivity(intent);
        });

        return view;
    }
}