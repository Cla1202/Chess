package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // <-- IMPORTANTE: Aggiunto l'import del Button
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

        // --- NUOVO: Colleghiamo il pulsante di uscita ---
        Button btnExit = findViewById(R.id.btnExit);
        // Aggiungiamo un controllo per sicurezza, nel caso l'ID nell'XML non sia ancora btnExit
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                // Chiude questa activity e torna a quella precedente (il menu)
                finish();
            });
        }
        // ------------------------------------------------

        // CONTROLLO SICUREZZA FIREBASE
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        Board board = viewModel.getBoard();

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        // Inizializza lo stato del gioco corretto (Scacco, Turno, ecc.) all'avvio
        aggiornaStatoGioco();

        // GESTIONE DEI TOCCHI
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleMove(position);
        });
    }

    private void handleMove(int position) {
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
                // La mossa è avvenuta con successo, verifichiamo se c'è Scacco o Scacco Matto
                aggiornaStatoGioco();
            } else {
                Toast.makeText(this, "Mossa non valida o Re in pericolo!", Toast.LENGTH_SHORT).show();
            }

            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

    // --- Gestisce tutta la logica dei testi e della fine partita ---
    private void aggiornaStatoGioco() {
        Board board = viewModel.getBoard();
        boolean turnoBianco = board.isWhiteTurn();

        // Chiediamo alla Board la situazione attuale
        boolean inScacco = board.isKingInCheck(turnoBianco);
        boolean haMosseLegali = board.hasAnyLegalMoves(turnoBianco);

        if (!haMosseLegali) {
            if (inScacco) {
                // SCACCO MATTO
                String vincitore = turnoBianco ? "NERO" : "BIANCO";
                statusText.setText("🏆 SCACCO MATTO! Vince il " + vincitore);
                Toast.makeText(this, "Partita finita: Scacco Matto!", Toast.LENGTH_LONG).show();
            } else {
                // STALLO
                statusText.setText("🤝 STALLO (Pareggio)");
                Toast.makeText(this, "La partita finisce in pareggio per stallo.", Toast.LENGTH_LONG).show();
            }
        } else {
            // Partita in corso
            String turnoDi = turnoBianco ? "Bianco" : "Nero";
            if (inScacco) {
                statusText.setText("⚠️ Turno: " + turnoDi + " (Sotto SCACCO!)");
            } else {
                statusText.setText("Turno: " + turnoDi);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.getRepository().saveCurrentGame(viewModel.getBoard());
        }
    }
}