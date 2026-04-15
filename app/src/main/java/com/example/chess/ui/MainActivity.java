package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.ui.viewmodel.GameViewModel;
import com.example.chess.util.MoveCalculator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // 1. Dichiariamo il nostro ViewModel
    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private Integer selectedPosition = null;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        GridView gridView = findViewById(R.id.chessGrid);

        // CONTROLLO SICUREZZA FIREBASE
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // --- IL CAMBIAMENTO PRINCIPALE ---
        // Colleghiamo l'Activity al suo ViewModel (se stiamo ruotando, ci ricollega a quello esistente)
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // Chiediamo la scacchiera al ViewModel invece di crearne una nuova!
        Board board = viewModel.getBoard();
        // ---------------------------------

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        aggiornaTestoTurno();

        // GESTIONE DEI TOCCHI
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleMove(position);
        });
    }

    private void handleMove(int position) {
        // Recuperiamo sempre la scacchiera dal ViewModel per sicurezza
        Board board = viewModel.getBoard();

        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);

            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else if (p != null) {
                Toast.makeText(this, "Non è il tuo turno!", Toast.LENGTH_SHORT).show();
            }
        } else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);

            if (board.movePiece(startRow, startCol, row, col)) {
                aggiornaTestoTurno();
            } else {
                Toast.makeText(this, "Mossa non valida", Toast.LENGTH_SHORT).show();
            }

            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

    private void aggiornaTestoTurno() {
        if (viewModel.getBoard().isWhiteTurn()) {
            statusText.setText("Turno: Bianco");
        } else {
            statusText.setText("Turno: Nero");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Salviamo la partita quando l'app va in background usando il ViewModel
        if (viewModel != null) {
            viewModel.getRepository().saveCurrentGame(viewModel.getBoard());
        }
    }
}