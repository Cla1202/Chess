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
import com.example.chess.model.User;
import com.example.chess.ui.home.GameActivity;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.util.Constants;

public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play, container, false);

        // 1. Retrieve the parent Activity and the currently logged-in user
        HomeActivity activity = (HomeActivity) getActivity();
        User currentUser = (activity != null) ? activity.getCurrentUser() : null;

        v.findViewById(R.id.btnStartGame).setOnClickListener(view -> {
            Intent i = new Intent(requireActivity(), GameActivity.class);
            i.putExtra(Constants.EXTRA_MODE, Constants.MODE_LOCAL_PVP);

            // 2. Pass the user to GameActivity
            if (currentUser != null) {
                i.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
            }

            startActivity(i);
        });

        v.findViewById(R.id.btnPlayBot).setOnClickListener(view -> {
            Intent i = new Intent(requireActivity(), GameActivity.class);
            i.putExtra(Constants.EXTRA_MODE, Constants.MODE_BOT);

            // 2. Pass the user to GameActivity
            if (currentUser != null) {
                i.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
            }

            startActivity(i);
        });

        return v;
    }
}