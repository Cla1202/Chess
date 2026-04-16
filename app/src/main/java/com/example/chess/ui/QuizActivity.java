package com.example.chess.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {
    private Board board;
    private ChessAdapter adapter;
    private QuizLevel currentLevel;
    private int currentMoveIndex = 0;
    private Integer selectedPosition = null;

    // --- VARIABILI GLOBALI AGGIORNATE ---
    private TextView levelTitleText;
    private TextView statusText;
    private GridView gridView; // Messa globale per fargliela vedere all'animazione

    private boolean isComputerThinking = false;
    private int errorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        levelTitleText = findViewById(R.id.levelTitleText);
        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid); // Inizializzata qui
        Button hintButton = findViewById(R.id.hintButton);

        com.example.chess.repository.QuizRepository quizRepository = new com.example.chess.repository.QuizRepository();

        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        currentLevel = quizRepository.getAllLevels().get(levelIndex);

        board = new Board(currentLevel.getInitialBoardSetup(), currentLevel.isWhiteTurnToStart());

        if (savedInstanceState != null) {
            currentMoveIndex = savedInstanceState.getInt("CURRENT_MOVE_INDEX", 0);
            errorCount = savedInstanceState.getInt("ERROR_COUNT", 0);

            for (int i = 0; i < currentMoveIndex; i++) {
                MoveRequest move = currentLevel.getSolutionMoves().get(i);
                board.setWhiteTurn(currentLevel.isWhiteTurnToStart() ? (i % 2 == 0) : (i % 2 != 0));
                board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            }

            board.setWhiteTurn(currentLevel.isWhiteTurnToStart() ? (currentMoveIndex % 2 == 0) : (currentMoveIndex % 2 != 0));
        }

        levelTitleText.setText(currentLevel.getTitle());
        statusText.setText("Tocca a te! Trova la mossa vincente.");
        statusText.setTextColor(Color.WHITE);

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleQuizTouch(position);
        });

        hintButton.setOnClickListener(v -> {
            if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                MoveRequest correctMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

                int startPos = correctMove.startRow * 8 + correctMove.startCol;
                int endPos = correctMove.endRow * 8 + correctMove.endCol;

                List<Integer> hints = new ArrayList<>();
                hints.add(startPos);
                hints.add(endPos);

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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_MOVE_INDEX", currentMoveIndex);
        outState.putInt("ERROR_COUNT", errorCount);
    }

    private void handleQuizTouch(int position) {
        Button hintButton = findViewById(R.id.hintButton);

        // Se il computer sta pensando, ignoriamo i tocchi (sicurezza aggiuntiva)
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

            // SE LA MOSSA È QUELLA CORRETTA:
            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                Piece movingPiece = board.getPiece(startRow, startCol);
                board.movePiece(startRow, startCol, row, col);

                int startPos = selectedPosition;

                // --- 1. BLOCCA I TOCCHI SUBITO DOPO LA MOSSA ---
                gridView.setEnabled(false);

                animateMove(startPos, position, movingPiece, () -> {
                    currentMoveIndex++;
                    selectedPosition = null;
                    adapter.setSelectedPosition(null);
                    adapter.setHints(new ArrayList<>());

                    if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                        statusText.setText("Ottimo! Il computer sta rispondendo...");
                        adapter.notifyDataSetChanged();

                        // Chiama la mossa del computer, ma la griglia RESTA BLOCCATA
                        playComputerMove();
                    } else {
                        statusText.setText("Livello Superato! Scacco Matto.");
                        statusText.setTextColor(Color.GREEN);
                        isComputerThinking = true;
                        salvaProgresso();
                        // Anche qui non sblocchiamo, il livello è finito!
                    }
                });

            } else {
                // SE LA MOSSA È SBAGLIATA:
                errorCount++;
                int tentativiRimasti = currentLevel.getMaxAttempts() - errorCount;

                adapter.setHints(new ArrayList<>());
                hintButton.setText("Aiuto");
                hintButton.setEnabled(true);

                if (tentativiRimasti <= 0) {
                    statusText.setText("TENTATIVI ESAURITI! HAI PERSO.");
                    statusText.setTextColor(Color.RED);
                    isComputerThinking = true;
                    // Blocca i tocchi se hai finito le vite
                    gridView.setEnabled(false);
                } else {
                    statusText.setText("Mossa errata! Rimasti: " + tentativiRimasti);
                    statusText.setTextColor(Color.RED);
                }

                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void salvaProgresso() {
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        int currentLevelId = levelIndex + 1;

        // 1. Recuperiamo l'utente attuale da Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Se per qualche motivo non c'è un utente loggato, usiamo un ID di default
        // o blocchiamo il salvataggio per evitare crash
        final String userId = (user != null) ? user.getUid() : "guest_user";

        Executors.newSingleThreadExecutor().execute(() -> {
            ChessDatabase db = ChessDatabase.getInstance(this);

            // 2. Usiamo il nuovo costruttore di LevelProgress che include l'userId
            // Assicurati che la tua classe LevelProgress abbia il campo userId come abbiamo discusso
            LevelProgress progress = new LevelProgress(currentLevelId, userId, true, errorCount);

            db.levelDao().insertProgress(progress);

            runOnUiThread(() -> {
                Toast.makeText(this, "Progresso salvato per: " + ((user != null) ? user.getEmail() : "Ospite"), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 2000);
            });
        });
    }

    private void playComputerMove() {
        isComputerThinking = true;

        new Handler().postDelayed(() -> {
            MoveRequest computerMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            int startPos = computerMove.startRow * 8 + computerMove.startCol;
            int endPos = computerMove.endRow * 8 + computerMove.endCol;
            Piece movingPiece = board.getPiece(computerMove.startRow, computerMove.startCol);

            // Se proviamo a muovere e la mossa è valida (il turno si inverte automaticamente!)
            if (board.movePiece(computerMove.startRow, computerMove.startCol, computerMove.endRow, computerMove.endCol)) {

                // ANIMAZIONE PER IL COMPUTER
                animateMove(startPos, endPos, movingPiece, () -> {
                    currentMoveIndex++;

                    statusText.setText("Il computer si è mosso. Ora tocca a te per il matto!");
                    adapter.notifyDataSetChanged();
                    isComputerThinking = false;

                    // SBLOCCA I TOCCHI ORA CHE IL COMPUTER HA FINITO
                    gridView.setEnabled(true);
                });
            } else {
                // --- PROTEZIONE ANTI-BUG ---
                // Se la mossa del computer fallisce (es. coordinate sbagliate nel QuizRepository)
                isComputerThinking = false;
                gridView.setEnabled(true); // Sblocchiamo per non far bloccare l'app

                Toast.makeText(this, "ERRORE LIVELLO: Mossa computer non valida! Controlla le coordinate nel QuizRepository.", Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    // --- MAGIA DELL'ANIMAZIONE (CON FIX DELLE COORDINATE) ---
    private void animateMove(int startPosition, int endPosition, Piece piece, Runnable onComplete) {
        FrameLayout boardContainer = findViewById(R.id.boardContainer);

        View startView = gridView.getChildAt(startPosition - gridView.getFirstVisiblePosition());
        View endView = gridView.getChildAt(endPosition - gridView.getFirstVisiblePosition());

        if (startView == null || endView == null) {
            onComplete.run();
            return;
        }

        ImageView ghostPiece = new ImageView(this);
        ghostPiece.setImageResource(getResIdForPiece(piece));
        ghostPiece.setLayoutParams(new FrameLayout.LayoutParams(startView.getWidth(), startView.getHeight()));
        ghostPiece.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ghostPiece.setPadding(8, 8, 8, 8);

        ghostPiece.setX(startView.getX() + gridView.getX());
        ghostPiece.setY(startView.getY() + gridView.getY());

        boardContainer.addView(ghostPiece);
        ((ImageView) startView).setImageResource(0);

        ghostPiece.animate()
                .x(endView.getX() + gridView.getX())
                .y(endView.getY() + gridView.getY())
                .setDuration(300)
                .withEndAction(() -> {
                    boardContainer.removeView(ghostPiece);
                    onComplete.run();
                })
                .start();
    }
    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}