package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.adapter.LevelAdapter;
import com.example.chess.model.QuizLevel;
import com.example.chess.repository.QuizRepository;
import com.example.chess.database.ChessDatabase;
import com.example.chess.ui.viewmodel.LevelViewModel;
import com.example.chess.ui.viewmodel.LevelViewModelFactory;

import java.util.List;

public class LevelSelectionActivity extends AppCompatActivity {

    private LevelViewModel viewModel;
    private LevelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        RecyclerView recyclerView = findViewById(R.id.levelsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        QuizRepository repository = new QuizRepository();
        List<QuizLevel> levels = repository.getAllLevels();

        ChessDatabase db = ChessDatabase.getInstance(this);
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        adapter = new LevelAdapter(levels, 1, position -> {
            Intent intent = new Intent(LevelSelectionActivity.this, QuizActivity.class);
            intent.putExtra("LEVEL_INDEX", position);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // OSSERVIAMO DIRETTAMENTE IL DATABASE
        viewModel.getMaxCompletedLevel().observe(this, maxCompleted -> {

            // Se maxCompleted è null (database vuoto), partiamo da 0
            int max = (maxCompleted != null) ? maxCompleted : 0;

            // Il livello sbloccato è il massimo livello completato + 1
            int unlocked = max + 1;

            // Aggiorniamo la grafica
            adapter.updateMaxUnlocked(unlocked);
        });
    }

    // Abbiamo rimosso l'onResume() perché ora il database lavora in tempo reale!
}