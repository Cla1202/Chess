package com.example.chess.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.repository.ChessRepository;
import com.example.chess.util.MoveCalculator;
import com.example.chess.util.Constants;

public class MainActivity extends AppCompatActivity {
    private Board board;
    private ChessAdapter adapter;
    private ChessRepository repository;
    private Integer selectedPosition = null;
    private TextView statusText;
    private boolean isWhiteTurn = true; // Inizia il bianco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        repository = new ChessRepository();
        board = repository.getNewGame();

        GridView gridView = findViewById(R.id.chessGrid);
        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleTouch(position);
        });
    }

    private void handleTouch(int position) {
        if (selectedPosition == null) {
            // Primo tocco: seleziona un pezzo
            selectedPosition = position;
            // Magari aggiungi un feedback visivo qui
        } else {
            // Secondo tocco: prova a muovere
            int startX = selectedPosition / 8;
            int startY = selectedPosition % 8;
            int endX = position / 8;
            int endY = position % 8;

            if (board.movePiece(startX, startY, endX, endY)) {
                adapter.notifyDataSetChanged();
                isWhiteTurn = !isWhiteTurn;
            } else {
                Toast.makeText(this, "Mossa non valida!", Toast.LENGTH_SHORT).show();
            }
            selectedPosition = null; // Resetta selezione
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        repository.saveCurrentGame(board);
    }

    private void handleMove(int position) {
        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            // SELEZIONE
            Piece p = board.getPiece(row, col);
            // Permetti la selezione SOLO se il pezzo appartiene a chi ha il turno
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Non è il tuo turno!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // MOVIMENTO
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);

            if (board.movePiece(startRow, startCol, row, col)) {
                // 1. Notifica all'adapter di ridisegnare i pezzi mossi
                adapter.notifyDataSetChanged();

                // 2. AGGIORNA IL TESTO (Deve stare DENTRO le graffe del movePiece riuscito)
                if (board.isWhiteTurn()) {
                    statusText.setText("Turno: Bianco");
                } else {
                    statusText.setText("Turno: Nero");
                }
            } else {
                // Se la mossa fallisce (regole violate), avvisa l'utente
                Toast.makeText(this, "Mossa non valida", Toast.LENGTH_SHORT).show();
            }
            // Pulisci sempre la selezione dopo un tentativo di mossa
            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

}