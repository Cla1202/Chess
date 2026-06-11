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
import com.example.chess.repository.IQuizRepository;
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
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        recyclerView = view.findViewById(R.id.levelsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) return view;

        User currentUser = activity.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 1. Get database and repository from ServiceLocator
        ChessDatabase db = ServiceLocator.getInstance().getChessDatabase(requireContext());
        IQuizRepository repository = ServiceLocator.getInstance().getQuizRepository(requireContext());

        // 2. Initialize ViewModel with Factory
        LevelViewModelFactory factory = new LevelViewModelFactory(db, repository);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        // 3. Load levels via ViewModel - Clean Architecture: Fragment doesn't know about assets
        viewModel.loadQuizLevels();

        String userId = currentUser.getIdToken();

        // 4. Observe levels from ViewModel
        viewModel.getQuizLevels().observe(getViewLifecycleOwner(), levels -> {
            if (levels == null) return;

            // 5. Observe progress and update UI
            viewModel.getMaxCompletedLevel(userId).observe(getViewLifecycleOwner(), maxCompleted -> {
                int max = (maxCompleted != null) ? maxCompleted : 0;
                int unlocked = max + 1;

                if (adapter == null) {
                    adapter = new LevelAdapter(levels, unlocked, position -> {
                        QuizLevel selectedLevel = levels.get(position);

                        Intent intent = new Intent(requireActivity(), GameActivity.class);
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
        });

        return view;
    }
}
