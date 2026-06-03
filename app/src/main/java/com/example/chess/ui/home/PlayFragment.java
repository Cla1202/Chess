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
import com.example.chess.ui.GameActivity;

public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        Button btnStartGame = view.findViewById(R.id.btnStartGame);

        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_LOCAL_PVP);
            startActivity(intent);
        });

        return view;
    }
}