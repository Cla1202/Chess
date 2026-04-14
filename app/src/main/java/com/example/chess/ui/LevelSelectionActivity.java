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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        RecyclerView recyclerView = findViewById(R.id.levelsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. I dati statici dei livelli
        QuizRepository repository = new QuizRepository();
        List<QuizLevel> levels = repository.getAllLevels();

        // 2. Prepariamo la Factory e il ViewModel
        ChessDatabase db = ChessDatabase.getInstance(this);
        LevelViewModelFactory factory = new LevelViewModelFactory(db);
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        // 3. Osserviamo i cambiamenti!
        // Appena il ViewModel finisce di leggere dal DB, questo blocco scatta in automatico
        viewModel.getMaxUnlockedLevel().observe(this, maxUnlocked -> {

            // Creiamo l'Adapter solo quando abbiamo il dato aggiornato
            LevelAdapter adapter = new LevelAdapter(levels, maxUnlocked, position -> {
                Intent intent = new Intent(LevelSelectionActivity.this, QuizActivity.class);
                intent.putExtra("LEVEL_INDEX", position);
                startActivity(intent);
            });

            recyclerView.setAdapter(adapter);
        });
    }
}