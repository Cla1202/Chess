package com.example.chess.ui.home;

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
import androidx.lifecycle.ViewModelProvider;
import com.example.chess.R;
import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.ui.LoginActivity;
import com.example.chess.ui.viewmodel.LevelViewModel;
import com.example.chess.ui.viewmodel.LevelViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView nameText = view.findViewById(R.id.profileName);
        TextView emailText = view.findViewById(R.id.profileEmail);
        TextView totalQuizzesText = view.findViewById(R.id.totalQuizzesText);
        TextView accuracyText = view.findViewById(R.id.accuracyText);
        TextView streakText = view.findViewById(R.id.quizStreakText);
        TextView avgTimeText = view.findViewById(R.id.averageTimeText);
        TextView currentLevelText = view.findViewById(R.id.quizLevelText);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName() != null ? user.getDisplayName() : getString(R.string.giocatore_sconosciuto);
            nameText.setText(displayName);
            emailText.setText(user.getEmail());
        }

        ChessDatabase db = ChessDatabase.getInstance(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        LevelViewModel viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        viewModel.getAllCompletedLevels().observe(getViewLifecycleOwner(), progressList -> {
            if (progressList != null && !progressList.isEmpty()) {
                calculateAndDisplayStats(progressList, totalQuizzesText, accuracyText, streakText, avgTimeText, currentLevelText);
            } else {
                resetStatsUI(totalQuizzesText, accuracyText, streakText, avgTimeText, currentLevelText);
            }
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void calculateAndDisplayStats(List<LevelProgress> list, TextView totalTv, TextView accTv, TextView streakTv, TextView timeTv, TextView levelTv) {
        int total = list.size();
        totalTv.setText(String.valueOf(total));

        int perfectSolved = 0;
        long totalTime = 0;
        for (LevelProgress p : list) {
            if (p.mistakesMade == 0) perfectSolved++;
            totalTime += p.timeSpentMillis;
        }
        
        int accuracy = (int) (((double) perfectSolved / total) * 100);
        accTv.setText(String.format(Locale.getDefault(), "%d%%", accuracy));

        long avgMillis = totalTime / total;
        timeTv.setText(String.format(Locale.getDefault(), "%ds", avgMillis / 1000));

        int currentStreak = 0;
        int maxStreak = 0;
        for (LevelProgress p : list) {
            if (p.mistakesMade == 0) {
                currentStreak++;
                if (currentStreak > maxStreak) maxStreak = currentStreak;
            } else {
                currentStreak = 0;
            }
        }
        streakTv.setText(String.format(Locale.getDefault(), "%d 🔥", maxStreak));

        if (total < 5) levelTv.setText(getString(R.string.novizio));
        else if (total < 10) levelTv.setText(getString(R.string.apprendista));
        else if (total < 15) levelTv.setText(getString(R.string.esperto));
        else levelTv.setText(getString(R.string.gran_maestro));
    }

    private void resetStatsUI(TextView totalTv, TextView accTv, TextView streakTv, TextView timeTv, TextView levelTv) {
        totalTv.setText("0");
        accTv.setText("0%");
        streakTv.setText("0 🔥");
        timeTv.setText("0s");
        levelTv.setText(getString(R.string.novizio));
    }
}