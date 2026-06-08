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

        // 1. Recuperiamo l'Activity madre per prendere l'utente loggato
        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) return view;

        User currentUser = activity.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Errore: Utente non autenticato", Toast.LENGTH_SHORT).show();
            return view;
        }

        ChessDatabase db = ChessDatabase.getInstance(requireContext());
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        // 2. Passiamo il Token ID dell'utente per recuperare i SUOI livelli sbloccati
        String userId = currentUser.getIdToken();

        viewModel.getMaxCompletedLevel(userId).observe(getViewLifecycleOwner(), maxCompleted -> {

            int max = (maxCompleted != null) ? maxCompleted : 0;
            int unlocked = max + 1;

            if (adapter == null) {
                adapter = new LevelAdapter(levels, unlocked, position -> {
                    QuizLevel selectedLevel = levels.get(position);

                    Intent intent = new Intent(requireActivity(), GameActivity.class);
                    intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_QUIZ);
                    intent.putExtra("QUIZ_LEVEL_OBJECT", selectedLevel);

                    // 3. FONDAMENTALE: Passiamo l'utente alla GameActivity così potrà salvare i progressi!
                    intent.putExtra("CURRENT_USER", currentUser);

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