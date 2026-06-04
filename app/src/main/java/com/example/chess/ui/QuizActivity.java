package com.example.chess.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer; // Aggiunto
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

    private TextView levelTitleText;
    private TextView statusText;
    private GridView gridView;
    private LinearLayout capturedWhiteContainer;
    private LinearLayout capturedBlackContainer;

    // --- VARIABILI TIMER ---
    private ProgressBar timerBar;
    private TextView timerText;
    private CountDownTimer moveTimer;
    private final long TIME_LIMIT_MS = 30000; // 30 secondi per mossa

    private boolean isComputerThinking = false;
    private int errorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        levelTitleText = findViewById(R.id.levelTitleText);
        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        capturedWhiteContainer = findViewById(R.id.capturedWhiteContainer);
        capturedBlackContainer = findViewById(R.id.capturedBlackContainer);

        // --- INIZIALIZZA TIMER UI ---
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);

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
        statusText.setText(R.string.trova_mossa_vincente);
        statusText.setTextColor(Color.WHITE);

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);
        aggiornaPezziMangiati();

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
                statusText.setText(R.string.suggerimento_attivato);
                statusText.setTextColor(Color.CYAN);
                hintButton.setEnabled(false);
            }
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // --- AVVIA IL TIMER ALL'INIZIO ---
        startTimer();
    }

    // --- LOGICA DEL TIMER ---
    private void aggiornaPezziMangiati() {
        capturedWhiteContainer.removeAllViews();
        capturedBlackContainer.removeAllViews();

        // Aggiunge i pezzi BIANCHI mangiati in ALTO
        addCapturedPiecesToContainer(capturedWhiteContainer, board.getCapturedWhite());

        // Aggiunge i pezzi NERI mangiati in BASSO
        addCapturedPiecesToContainer(capturedBlackContainer, board.getCapturedBlack());
    }

    private void addCapturedPiecesToContainer(LinearLayout container, List<Piece> pieces) {
        if (pieces.isEmpty()) return;

        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        java.util.Map<String, Piece> prototypes = new java.util.HashMap<>();
        String[] order = {"Pawn", "Knight", "Bishop", "Rook", "Queen"};

        for (Piece p : pieces) {
            String type = p.getClass().getSimpleName();
            Integer currentCount = counts.get(type);
            counts.put(type, (currentCount == null ? 0 : currentCount) + 1);
            prototypes.put(type, p);
        }

        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);

        for (String type : order) {
            if (counts.containsKey(type)) {
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                itemLayout.setPadding(8, 0, 8, 0);

                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
                iv.setImageResource(getResIdForPiece(prototypes.get(type)));
                itemLayout.addView(iv);

                Integer count = counts.get(type);
                if (count != null && count > 1) {
                    TextView tv = new TextView(this);
                    tv.setText("x" + count);
                    tv.setTextColor(Color.WHITE);
                    tv.setTextSize(12);
                    tv.setPadding(4, 0, 0, 0);
                    itemLayout.addView(tv);
                }
                container.addView(itemLayout);
            }
        }
    }

    private void startTimer() {
        if (moveTimer != null) moveTimer.cancel();

        timerBar.setMax((int) TIME_LIMIT_MS);
        timerBar.setProgress((int) TIME_LIMIT_MS);

        moveTimer = new CountDownTimer(TIME_LIMIT_MS, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerBar.setProgress((int) millisUntilFinished);
                int seconds = (int) (millisUntilFinished / 1000);
                timerText.setText(String.format("00:%02d", seconds));

                // Effetto colore: diventa rosso sotto i 5 secondi
                if (millisUntilFinished < 5000) {
                    timerText.setTextColor(Color.RED);
                } else {
                    timerText.setTextColor(Color.parseColor("#FF5722"));
                }
            }

            @Override
            public void onFinish() {
                timerBar.setProgress(0);
                timerText.setText("00:00");
                handleTimeOut();
            }
        }.start();
    }

    private void stopTimer() {
        if (moveTimer != null) moveTimer.cancel();
    }

    private void handleTimeOut() {
        gridView.setEnabled(false);
        statusText.setText(R.string.tempo_scaduto);
        statusText.setTextColor(Color.RED);
        Toast.makeText(this, R.string.troppo_lento, Toast.LENGTH_SHORT).show();

        // Riavvia il livello dopo 2 secondi
        new Handler().postDelayed(() -> {
            int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
            recreate(); // Ricarica l'activity per resettare il livello
        }, 2000);
    }

    private void handleQuizTouch(int position) {
        if (isComputerThinking) return;

        int row = position / 8;
        int col = position % 8;

        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            }
        } else {
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;
            MoveRequest expectedMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                // --- FERMA IL TIMER: MOSSA CORRETTA ---
                stopTimer();

                Piece movingPiece = board.getPiece(startRow, startCol);
                board.movePiece(startRow, startCol, row, col);
                gridView.setEnabled(false);

                animateMove(selectedPosition, position, movingPiece, () -> {
                    currentMoveIndex++;
                    aggiornaPezziMangiati();
                    selectedPosition = null;
                    adapter.setSelectedPosition(null);
                    adapter.setHints(new ArrayList<>());

                    if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                        statusText.setText(R.string.risposta_computer);
                        playComputerMove();
                    } else {
                        statusText.setText(R.string.livello_superato);
                        statusText.setTextColor(Color.GREEN);
                        salvaProgresso();
                    }
                });
            } else {
                // ERRORE
                errorCount++;
                int tentativiRimasti = currentLevel.getMaxAttempts() - errorCount;
                if (tentativiRimasti <= 0) {
                    stopTimer();
                    statusText.setText(R.string.hai_perso);
                    gridView.setEnabled(false);
                } else {
                    statusText.setText(getString(R.string.mossa_errata, tentativiRimasti));
                }
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void playComputerMove() {
        isComputerThinking = true;
        new Handler().postDelayed(() -> {
            MoveRequest computerMove = currentLevel.getSolutionMoves().get(currentMoveIndex);
            int startPos = computerMove.startRow * 8 + computerMove.startCol;
            int endPos = computerMove.endRow * 8 + computerMove.endCol;
            Piece movingPiece = board.getPiece(computerMove.startRow, computerMove.startCol);

            board.movePiece(computerMove.startRow, computerMove.startCol, computerMove.endRow, computerMove.endCol);

            animateMove(startPos, endPos, movingPiece, () -> {
                currentMoveIndex++;
                aggiornaPezziMangiati();
                statusText.setText(R.string.tocca_a_te);
                isComputerThinking = false;
                gridView.setEnabled(true);

                // --- LA RIGA MAGICA CHE MANCAVA ---
                // Diciamo all'Adapter di ridisegnare la griglia con i pezzi aggiornati!
                adapter.notifyDataSetChanged();
                // --- FAI RIPARTIRE IL TIMER PER LA NUOVA MOSSA ---
                startTimer();
            });
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer(); // Evita memory leak
    }

    // --- ANIMAZIONE E METODI UTILI (Invariati o con FIX) ---
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

        if (startView instanceof ViewGroup) {
            ImageView realPieceImage = (ImageView) ((ViewGroup) startView).getChildAt(0);
            realPieceImage.setImageResource(0);
        }

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
        android.content.SharedPreferences prefs = getSharedPreferences("ChessSettings", MODE_PRIVATE);
        String style = prefs.getString("piece_style", "Classico");
        String stylePrefix;

        switch (style) {
            case "Neo":
                stylePrefix = "neo_";
                break;
            case "Moderno":
                stylePrefix = "mod_";
                break;
            case "Alfa":
                stylePrefix = "alpha_";
                break;
            default:
                stylePrefix = "";
                break;
        }

        String colorPrefix = piece.isWhite() ? "w_" : "b_";
        String pieceName = piece.getClass().getSimpleName().toLowerCase();

        String fullName = stylePrefix + colorPrefix + pieceName;
        int resId = getResources().getIdentifier(fullName, "drawable", getPackageName());

        if (resId == 0) {
            fullName = colorPrefix + pieceName;
            resId = getResources().getIdentifier(fullName, "drawable", getPackageName());
        }

        return resId;
    }

    private void salvaProgresso() {
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);
        int currentLevelId = levelIndex + 1;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = (user != null) ? user.getUid() : "guest_user";

        Executors.newSingleThreadExecutor().execute(() -> {
            ChessDatabase db = ChessDatabase.getInstance(this);
            LevelProgress progress = new LevelProgress(currentLevelId, userId, true, errorCount);
            db.levelDao().insertProgress(progress);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.progresso_salvato, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 2000);
            });
        });
    }
}