package com.example.chess.ui.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.adapter.LevelAdapter;
import com.example.chess.database.ChessDatabase;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.User;
import com.example.chess.repository.QuizRepository;
import com.example.chess.ui.home.GameActivity;
import com.example.chess.ui.home.HomeActivity;
import com.example.chess.ui.home.viewmodel.LevelViewModel;
import com.example.chess.ui.home.viewmodel.LevelViewModelFactory;
import com.example.chess.util.Constants;
import com.example.chess.util.ServiceLocator;

import java.util.List;

public class QuizFragment extends Fragment {

    private LevelViewModel viewModel;
    private LevelAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.levelsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 1. USE OF SERVICE LOCATOR INSTEAD OF "new QuizRepository()"
        QuizRepository repository = ServiceLocator.getInstance().getQuizRepository();
        List<QuizLevel> levels = repository.getAllLevels();

        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) return view;

        User currentUser = activity.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 2. USE OF SERVICE LOCATOR FOR THE DATABASE
        ChessDatabase db = ServiceLocator.getInstance().getChessDatabase(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        String userId = currentUser.getIdToken();

        viewModel.getMaxCompletedLevel(userId).observe(getViewLifecycleOwner(), maxCompleted -> {

            int max = (maxCompleted != null) ? maxCompleted : 0;
            int unlocked = max + 1;

            if (adapter == null) {
                adapter = new LevelAdapter(levels, unlocked, position -> {
                    QuizLevel selectedLevel = levels.get(position);

                    Intent intent = new Intent(requireActivity(), GameActivity.class);
                    // Fixed using Constants
                    intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_QUIZ);
                    intent.putExtra(Constants.EXTRA_QUIZ_LEVEL_OBJECT, selectedLevel);
                    intent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);

                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateMaxUnlocked(unlocked);
            }
        });

        return view;
    }
}
