package com.example.chess.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {
    private Board board;
    private ChessAdapter adapter;
    private QuizLevel currentLevel;
    private int currentMoveIndex = 0;
    private Integer selectedPosition = null;
    private TextView levelTitleText;
    private TextView statusText;

    private boolean isComputerThinking = false;
    private int errorCount = 0; // Contatore errori

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        levelTitleText = findViewById(R.id.levelTitleText);
        statusText = findViewById(R.id.statusText);
        GridView gridView = findViewById(R.id.chessGrid);

        com.example.chess.repository.QuizRepository quizRepository = new com.example.chess.repository.QuizRepository();

        // Recupera l'indice del livello dall'Intent (di default 0 se non lo trova)
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        currentLevel = quizRepository.getAllLevels().get(levelIndex);

        // Inizializza la scacchiera con il setup del quiz
        board = new Board(currentLevel.getInitialBoardSetup(), currentLevel.isWhiteTurnToStart());

        // Aggiorna la UI con il titolo e lo stato iniziale
        levelTitleText.setText(currentLevel.getTitle());
        statusText.setText("Tocca a te! Trova la mossa vincente.");
        statusText.setTextColor(Color.WHITE);

        // Configura l'adapter per la GridView
        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        Button hintButton = findViewById(R.id.hintButton);

        // Gestisce il tocco sulla scacchiera
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleQuizTouch(position);
        });

        hintButton.setOnClickListener(v -> {
            if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                MoveRequest correctMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

                // Calcolo posizioni
                int startPos = correctMove.startRow * 8 + correctMove.startCol;
                int endPos = correctMove.endRow * 8 + correctMove.endCol;

                List<Integer> hints = new ArrayList<>();
                hints.add(startPos);
                hints.add(endPos);

                // Applica i suggerimenti all'adapter
                adapter.setHints(hints);

                // AGGIORNAMENTO TESTO
                statusText.setText("Suggerimento attivato: osserva le celle gialle!");
                statusText.setTextColor(Color.CYAN);

                // Opzionale: Cambia il testo del bottone stesso
                hintButton.setText("Suggerimento mostrato");
                hintButton.setEnabled(false); // Disabilita per evitare click ripetuti
            }
        });

        // Trova il pulsante indietro
        android.widget.ImageButton backButton = findViewById(R.id.backButton);

// Azione: quando viene cliccato, chiudi questa schermata
        backButton.setOnClickListener(v -> {
            finish(); // Questo comando distrugge la QuizActivity e ti fa scivolare indietro alla schermata precedente!
        });



    }

    private void handleQuizTouch(int position) {
        Button hintButton = findViewById(R.id.hintButton);
        if (isComputerThinking) return;

        if (currentMoveIndex >= currentLevel.getSolutionMoves().size()) {
            Toast.makeText(this, "Hai già superato questo livello!", Toast.LENGTH_SHORT).show();
            return;
        }

        int row = position / 8;
        int col = position % 8;

        if (selectedPosition == null) {
            // --- FASE 1: SELEZIONE ---
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Seleziona un tuo pezzo!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // --- FASE 2: MOVIMENTO ---
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;

            MoveRequest expectedMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            // CONTROLLO: È la mossa corretta? (Confronta con expectedMove)
            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                // Mossa corretta! Esegui il movimento sulla scacchiera
                board.movePiece(startRow, startCol, row, col);
                currentMoveIndex++; // L'indice avanza

                // Pulisci subito la selezione grafica dell'utente
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.setHints(new ArrayList<>()); // Rimuovi eventuali aiuti gialli

                // CONTROLLO: C'è una mossa successiva nella lista?
                if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                    // Se sì, significa che la prossima mossa è del COMPUTER
                    statusText.setText("Ottimo! Il computer sta rispondendo...");
                    playComputerMove();
                } else {
                    // --- VITTORIA E SALVATAGGIO PROGRESSO ---
                    statusText.setText("Livello Superato! Scacco Matto.");
                    statusText.setTextColor(Color.GREEN);
                    isComputerThinking = true; // Blocca la scacchiera a fine partita

                    salvaProgresso();
                }

                adapter.notifyDataSetChanged();
            } else {
                // --- GESTIONE ERRORE (Mossa Sbagliata) ---
                errorCount++;
                int tentativiRimasti = currentLevel.getMaxAttempts() - errorCount;

                // Rimuoviamo gli aiuti anche se sbaglia, così deve richiederli
                adapter.setHints(new ArrayList<>());
                hintButton.setText("Aiuto");
                hintButton.setEnabled(true);

                if (tentativiRimasti <= 0) {
                    statusText.setText("TENTATIVI ESAURITI! HAI PERSO.");
                    statusText.setTextColor(Color.RED);
                    isComputerThinking = true; // Blocca la scacchiera
                } else {
                    statusText.setText("Mossa errata! Rimasti: " + tentativiRimasti);
                    statusText.setTextColor(Color.RED);
                }
            }

            // Reset grafica dopo un tentativo di mossa (sia giusto che sbagliato)
            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

    // --- NUOVO METODO: SALVATAGGIO DEI LIVELLI SBLOCCATI ---
    private void salvaProgresso() {
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        int currentLevelId = levelIndex + 1;

        // Usiamo un Executor per non bloccare l'interfaccia grafica
        Executors.newSingleThreadExecutor().execute(() -> {
            ChessDatabase db = ChessDatabase.getInstance(this);

            // Salviamo il progresso del livello attuale come completato
            LevelProgress progress = new LevelProgress(currentLevelId, true, errorCount);
            db.levelDao().insertProgress(progress);

            // Torniamo sul thread principale per chiudere l'activity e mostrare il brindisi
            runOnUiThread(() -> {
                Toast.makeText(this, "Progresso salvato nel Database!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 2000);
            });
        });
    }

    // COMPUTER MOVE
    private void playComputerMove() {
        isComputerThinking = true; // Impedisce all'utente di toccare la scacchiera

        new Handler().postDelayed(() -> {
            MoveRequest computerMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            // FORZA IL TURNO AL NERO
            board.setWhiteTurn(false);

            if (board.movePiece(computerMove.startRow, computerMove.startCol,
                    computerMove.endRow, computerMove.endCol)) {

                currentMoveIndex++;

                // RESTITUISCE IL TURNO AL BIANCO
                board.setWhiteTurn(true);

                statusText.setText("Il computer si è mosso. Ora tocca a te per il matto!");
                adapter.notifyDataSetChanged();
            }

            isComputerThinking = false;
        }, 1000);
    }
}