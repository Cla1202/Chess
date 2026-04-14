package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.repository.ChessRepository;
import com.example.chess.util.MoveCalculator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Board board;
    private ChessAdapter adapter;
    private ChessRepository repository;
    private Integer selectedPosition = null;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        GridView gridView = findViewById(R.id.chessGrid);

        // 1. CONTROLLO SICUREZZA FIREBASE
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Se l'utente non è loggato, rimandalo al Login e ferma tutto
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return; // IMPORTANTISSIMO: Ferma il caricamento della schermata!
        }

        // 2. INIZIALIZZAZIONE GIOCO
        repository = new ChessRepository();
        board = repository.getNewGame();

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        // Imposta il testo iniziale del turno
        aggiornaTestoTurno();

        // 3. GESTIONE DEI TOCCHI SULLA SCACCHIERA
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleMove(position); // Ora usiamo solo il metodo corretto!
        });
    }

    // Il metodo definitivo per muovere i pezzi
    private void handleMove(int position) {
        int row = MoveCalculator.toRow(position);
        int col = MoveCalculator.toCol(position);

        if (selectedPosition == null) {
            // --- FASE 1: SELEZIONE ---
            Piece p = board.getPiece(row, col);

            // Permetti la selezione SOLO se c'è un pezzo e appartiene a chi ha il turno
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position); // Evidenzia la casella
                adapter.notifyDataSetChanged();
            } else if (p != null) {
                Toast.makeText(this, "Non è il tuo turno!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // --- FASE 2: MOVIMENTO ---
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);

            // Prova a muovere il pezzo nella logica della scacchiera
            if (board.movePiece(startRow, startCol, row, col)) {
                // Mossa riuscita!
                aggiornaTestoTurno();
            } else {
                // Mossa fallita (es. mossa non permessa dalle regole)
                Toast.makeText(this, "Mossa non valida", Toast.LENGTH_SHORT).show();
            }

            // In ogni caso, pulisci la selezione alla fine
            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

    // Metodo comodità per non ripetere il codice
    private void aggiornaTestoTurno() {
        if (board.isWhiteTurn()) {
            statusText.setText("Turno: Bianco");
        } else {
            statusText.setText("Turno: Nero");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Salviamo la partita quando l'app va in background
        if (repository != null && board != null) {
            repository.saveCurrentGame(board);
        }
    }
}