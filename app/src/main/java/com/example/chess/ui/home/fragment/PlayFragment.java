package com.example.chess.ui.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.chess.R;
import com.example.chess.ui.home.GameActivity;

public class PlayFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play, container, false);
        
        v.findViewById(R.id.btnStartGame).setOnClickListener(view -> {
            Intent i = new Intent(requireActivity(), GameActivity.class);
            i.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_LOCAL_PVP);
            startActivity(i);
        });

        v.findViewById(R.id.btnPlayBot).setOnClickListener(view -> {
            Intent i = new Intent(requireActivity(), GameActivity.class);
            i.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_BOT);
            startActivity(i);
        });
        
        return v;
    }
}