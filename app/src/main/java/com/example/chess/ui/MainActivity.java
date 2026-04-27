package com.example.chess.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private Integer selectedPosition = null;
    private TextView statusText;
    private GridView gridView;

    // --- VARIABILI TIMER ---
    private ProgressBar timerBar;
    private TextView timerText;
    private CountDownTimer moveTimer;
    private final long TIME_LIMIT_MS = 30000; // 30 secondi per mossa
    private boolean isTimerEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);

        // Inizializza Timer UI
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);

        isTimerEnabled = getIntent().getBooleanExtra("EXTRA_TIMER_ENABLED", false);
        if (!isTimerEnabled) {
            timerBar.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
        } else {
            startTimer();
        }

        Button btnExit = findViewById(R.id.btnExit);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> finish());
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        Board board = viewModel.getBoard();

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        aggiornaStatoGioco();

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

                // Recupera tutte le mosse legali per il pezzo selezionato
                List<Integer> legalMoves = board.getLegalMovesForPiece(row, col);
                // Passa la lista all'adapter per visualizzare i suggerimenti
                adapter.setHints(legalMoves);

                adapter.notifyDataSetChanged();
            }
        } else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);
            Piece movingPiece = board.getPiece(startRow, startCol);

            if (board.movePiece(startRow, startCol, row, col)) {
                if (isTimerEnabled) stopTimer();

                gridView.setEnabled(false);

                animateMove(selectedPosition, position, movingPiece, () -> {
                    aggiornaStatoGioco();
                    selectedPosition = null;
                    adapter.setSelectedPosition(null);
                    adapter.setHints(new ArrayList<>()); // Svuota i suggerimenti dopo la mossa
                    adapter.notifyDataSetChanged();
                    gridView.setEnabled(true);

                    if (isTimerEnabled) startTimer();
                });

            } else {
                Toast.makeText(this, "Mossa non valida!", Toast.LENGTH_SHORT).show();
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.notifyDataSetChanged();
            }
        }
    }

    // --- LOGICA DEL TIMER ---
    private void startTimer() {
        if (!isTimerEnabled) return;

        if (moveTimer != null) moveTimer.cancel();

        timerBar.setMax((int) TIME_LIMIT_MS);
        timerBar.setProgress((int) TIME_LIMIT_MS);

        moveTimer = new CountDownTimer(TIME_LIMIT_MS, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerBar.setProgress((int) millisUntilFinished);
                int seconds = (int) (millisUntilFinished / 1000);
                timerText.setText(String.format("00:%02d", seconds));

                if (millisUntilFinished < 5000) timerText.setTextColor(Color.RED);
                else timerText.setTextColor(Color.parseColor("#FF5722"));
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
        if (!isTimerEnabled) return;
        if (moveTimer != null) moveTimer.cancel();
    }

    private void handleTimeOut() {
        // Nel gioco libero, se scade il tempo, il turno passa all'altro
        Toast.makeText(this, "Tempo scaduto! Cambio turno.", Toast.LENGTH_SHORT).show();
        viewModel.getBoard().setWhiteTurn(!viewModel.getBoard().isWhiteTurn());
        aggiornaStatoGioco();
        startTimer(); // Riparte per l'altro giocatore
    }

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

        // FIX PER FRAME LAYOUT (Contiene ImageView + TextView coordinate)
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
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void aggiornaStatoGioco() {
        Board board = viewModel.getBoard();
        boolean turnoBianco = board.isWhiteTurn();
        boolean inScacco = board.isKingInCheck(turnoBianco);
        boolean haMosseLegali = board.hasAnyLegalMoves(turnoBianco);

        if (!haMosseLegali) {
            if (isTimerEnabled) stopTimer(); // Ferma tutto se la partita è finita
            if (inScacco) {
                statusText.setText("🏆 SCACCO MATTO! Vince il " + (turnoBianco ? "NERO" : "BIANCO"));
            } else {
                statusText.setText("🤝 STALLO (Pareggio)");
            }
        } else {
            statusText.setText("Turno: " + (turnoBianco ? "Bianco" : "Nero") + (inScacco ? " (SCACCO!)" : ""));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        if (viewModel != null) viewModel.getRepository().saveCurrentGame(viewModel.getBoard());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

}