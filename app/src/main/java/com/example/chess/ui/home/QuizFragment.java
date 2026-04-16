package com.example.chess.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.chess.repository.QuizRepository;
import com.example.chess.ui.QuizActivity;
import com.example.chess.ui.viewmodel.LevelViewModel;
import com.example.chess.ui.viewmodel.LevelViewModelFactory;
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

        QuizRepository repository = new QuizRepository();
        List<QuizLevel> levels = repository.getAllLevels();

        ChessDatabase db = ChessDatabase.getInstance(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        // OSSERVIAMO IL DATABASE (Usiamo getViewLifecycleOwner)
        viewModel.getMaxCompletedLevel().observe(getViewLifecycleOwner(), maxCompleted -> {

            int max = (maxCompleted != null) ? maxCompleted : 0;
            int unlocked = max + 1;

            if (adapter == null) {
                adapter = new LevelAdapter(levels, unlocked, position -> {
                    Intent intent = new Intent(requireActivity(), QuizActivity.class);
                    intent.putExtra("LEVEL_INDEX", position);
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