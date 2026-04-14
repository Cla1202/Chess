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
import com.example.chess.ui.QuizActivity; // Attenzione: questa rimane un'Activity a parte!
import com.example.chess.ui.viewmodel.LevelViewModel;
import com.example.chess.ui.viewmodel.LevelViewModelFactory;

import java.util.List;

public class QuizFragment extends Fragment {

    private LevelViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.levelsRecyclerView);

        // REGOLA D'ORO: Usiamo requireContext() invece di "this"
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        QuizRepository repository = new QuizRepository();
        List<QuizLevel> levels = repository.getAllLevels();

        ChessDatabase db = ChessDatabase.getInstance(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db);

        // Usiamo "this" qui perché stiamo attaccando il ViewModel a questo specifico Fragment
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        // REGOLA D'ORO 2: Nei Fragment, per osservare i dati in modo sicuro usiamo getViewLifecycleOwner()
        viewModel.getMaxUnlockedLevel().observe(getViewLifecycleOwner(), maxUnlocked -> {

            LevelAdapter adapter = new LevelAdapter(levels, maxUnlocked, position -> {
                // REGOLA D'ORO 3: Per lanciare un'Activity (il Quiz vero e proprio), chiediamo l'Activity madre
                Intent intent = new Intent(requireActivity(), QuizActivity.class);
                intent.putExtra("LEVEL_INDEX", position);
                startActivity(intent);
            });

            recyclerView.setAdapter(adapter);
        });

        return view;
    }
}