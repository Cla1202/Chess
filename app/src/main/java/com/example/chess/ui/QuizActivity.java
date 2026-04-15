package com.example.chess.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import per il salvataggio
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
        Button hintButton = findViewById(R.id.hintButton);

        com.example.chess.repository.QuizRepository quizRepository = new com.example.chess.repository.QuizRepository();

        // Recupera l'indice del livello dall'Intent (di default 0 se non lo trova)
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        currentLevel = quizRepository.getAllLevels().get(levelIndex);

        // Inizializza la scacchiera con il setup del quiz
        board = new Board(currentLevel.getInitialBoardSetup(), currentLevel.isWhiteTurnToStart());

        // --- PROTEZIONE DA ROTAZIONE SCHERMO ---
        if (savedInstanceState != null) {
            // Recupera i dati salvati prima della rotazione
            currentMoveIndex = savedInstanceState.getInt("CURRENT_MOVE_INDEX", 0);
            errorCount = savedInstanceState.getInt("ERROR_COUNT", 0);

            // "Manda avanti veloce" la scacchiera eseguendo automaticamente le mosse già fatte
            for (int i = 0; i < currentMoveIndex; i++) {
                MoveRequest move = currentLevel.getSolutionMoves().get(i);
                // Forza il turno corretto in base a chi doveva muovere
                board.setWhiteTurn(currentLevel.isWhiteTurnToStart() ? (i % 2 == 0) : (i % 2 != 0));
                board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            }

            // Ripristina il turno corretto per il giocatore attuale
            board.setWhiteTurn(currentLevel.isWhiteTurnToStart() ? (currentMoveIndex % 2 == 0) : (currentMoveIndex % 2 != 0));
        }

        // Aggiorna la UI con il titolo e lo stato
        levelTitleText.setText(currentLevel.getTitle());
        statusText.setText("Tocca a te! Trova la mossa vincente.");
        statusText.setTextColor(Color.WHITE);

        // Configura l'adapter per la GridView
        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

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

                statusText.setText("Suggerimento attivato: osserva le celle gialle!");
                statusText.setTextColor(Color.CYAN);
                hintButton.setText("Suggerimento mostrato");
                hintButton.setEnabled(false);
            }
        });

        android.widget.ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    // --- NUOVO METODO: SALVA I DATI PRIMA DELLA ROTAZIONE ---
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Mettiamo in cassaforte le nostre variabili chiave
        outState.putInt("CURRENT_MOVE_INDEX", currentMoveIndex);
        outState.putInt("ERROR_COUNT", errorCount);
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
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Seleziona un tuo pezzo!", Toast.LENGTH_SHORT).show();
            }
        } else {
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;

            MoveRequest expectedMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                board.movePiece(startRow, startCol, row, col);
                currentMoveIndex++;

                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.setHints(new ArrayList<>());

                if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                    statusText.setText("Ottimo! Il computer sta rispondendo...");
                    playComputerMove();
                } else {
                    statusText.setText("Livello Superato! Scacco Matto.");
                    statusText.setTextColor(Color.GREEN);
                    isComputerThinking = true;
                    salvaProgresso();
                }

                adapter.notifyDataSetChanged();
            } else {
                errorCount++;
                int tentativiRimasti = currentLevel.getMaxAttempts() - errorCount;

                adapter.setHints(new ArrayList<>());
                hintButton.setText("Aiuto");
                hintButton.setEnabled(true);

                if (tentativiRimasti <= 0) {
                    statusText.setText("TENTATIVI ESAURITI! HAI PERSO.");
                    statusText.setTextColor(Color.RED);
                    isComputerThinking = true;
                } else {
                    statusText.setText("Mossa errata! Rimasti: " + tentativiRimasti);
                    statusText.setTextColor(Color.RED);
                }
            }

            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

    private void salvaProgresso() {
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        int currentLevelId = levelIndex + 1;

        Executors.newSingleThreadExecutor().execute(() -> {
            ChessDatabase db = ChessDatabase.getInstance(this);
            LevelProgress progress = new LevelProgress(currentLevelId, true, errorCount);
            db.levelDao().insertProgress(progress);

            runOnUiThread(() -> {
                Toast.makeText(this, "Progresso salvato nel Database!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 2000);
            });
        });
    }

    private void playComputerMove() {
        isComputerThinking = true;

        new Handler().postDelayed(() -> {
            MoveRequest computerMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            board.setWhiteTurn(false);

            if (board.movePiece(computerMove.startRow, computerMove.startCol,
                    computerMove.endRow, computerMove.endCol)) {

                currentMoveIndex++;
                board.setWhiteTurn(true);

                statusText.setText("Il computer si è mosso. Ora tocca a te per il matto!");
                adapter.notifyDataSetChanged();
            }

            isComputerThinking = false;
        }, 1000);
    }
}