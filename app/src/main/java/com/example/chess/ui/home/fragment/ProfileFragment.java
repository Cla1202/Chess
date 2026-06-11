package com.example.chess.ui.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.User;
import com.example.chess.repository.IQuizRepository;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.home.viewmodel.LevelViewModel;
import com.example.chess.ui.home.viewmodel.LevelViewModelFactory;
import com.example.chess.ui.welcome.LoginActivity;
import com.example.chess.util.ServiceLocator;

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

        // 1. Retrieve the parent Activity (HomeActivity) to access the user and the ViewModel
        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) return view;

        User currentUser = activity.getCurrentUser();

        // 2. Initialize the ViewModel for Levels
        ChessDatabase db = ServiceLocator.getInstance().getChessDatabase(requireContext());
        IQuizRepository repository = ServiceLocator.getInstance().getQuizRepository(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db, repository);
        LevelViewModel levelViewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        if (currentUser != null) {
            // Display custom user data
            String displayName = (currentUser.getName() != null && !currentUser.getName().isEmpty())
                    ? currentUser.getName()
                    : getString(R.string.giocatore_sconosciuto);

            nameText.setText(displayName);
            emailText.setText(currentUser.getEmail());

            // 3. Observe ONLY the progress of this specific user
            levelViewModel.getAllCompletedLevels(currentUser.getIdToken()).observe(getViewLifecycleOwner(), progressList -> {
                if (progressList != null && !progressList.isEmpty()) {
                    calculateAndDisplayStats(progressList, totalQuizzesText, accuracyText, streakText, avgTimeText, currentLevelText);
                } else {
                    resetStatsUI(totalQuizzesText, accuracyText, streakText, avgTimeText, currentLevelText);
                }
            });
        }

        // 4. Clean logout via MVVM
        logoutButton.setOnClickListener(v -> {
            activity.getUserViewModel().logout().observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess()) {
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Error during logout", Toast.LENGTH_SHORT).show();
                }
            });
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
