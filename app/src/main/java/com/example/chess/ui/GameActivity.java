package com.example.chess.ui;

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
import com.example.chess.controller.BotGameController;
import com.example.chess.controller.GameModeController;
import com.example.chess.controller.LocalTwoPlayerController;
import com.example.chess.controller.QuizModeController;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.ui.viewmodel.GameViewModel;
import com.example.chess.util.MoveCalculator;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements GameModeController.GameCallback {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_LOCAL_PVP = "LOCAL_PVP";
    public static final String MODE_QUIZ = "QUIZ";
    public static final String MODE_BOT = "BOT";

    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private GridView gridView;
    private TextView statusText;
    private TextView levelTitleText;

    private ProgressBar timerBar;
    private TextView timerText;
    private CountDownTimer moveTimer;
    private final long TIME_LIMIT_MS = 30000;

    private GameModeController controller;
    private Button btnHelp;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Assicurati che includa tutti gli ID necessari (o usa un layout unificato)

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);
        levelTitleText = findViewById(R.id.levelTitleText); // Se presente nel layout

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        Board board = viewModel.getBoard();

        // Identifica la modalità richiesta tramite l'Intent
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_LOCAL_PVP;

        switch (mode) {
            case MODE_QUIZ:
                // Recupera l'oggetto QuizLevel appropriato passatogli (o tramite indice repository)
                QuizLevel level = (QuizLevel) getIntent().getSerializableExtra("QUIZ_LEVEL_OBJECT");
                controller = new QuizModeController(level);
                if (levelTitleText != null) levelTitleText.setText(level.getTitle());
                break;
            case MODE_BOT:
                controller = new BotGameController();
                if (levelTitleText != null) levelTitleText.setVisibility(View.GONE);
                break;
            case MODE_LOCAL_PVP:
            default:
                controller = new LocalTwoPlayerController();
                if (levelTitleText != null) levelTitleText.setVisibility(View.GONE);
                break;
        }

        // Buttons logics
        btnExit = findViewById(R.id.btnExit);
        if (btnExit != null) btnExit.setOnClickListener(v -> finish());

        btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            if (MODE_QUIZ.equals(mode)) {
                btnHelp.setVisibility(View.VISIBLE);

                btnHelp.setOnClickListener(v -> {
                    if (controller instanceof QuizModeController) {
                        QuizModeController quizController = (QuizModeController) controller;

                        // Chiamiamo il metodo del tuo controller che mostra o suggerisce la mossa
                        // Nota: Assicurati che il metodo si chiami esattamente così nel tuo QuizModeController,
                        // altrimenti usa il nome del metodo che mostra la soluzione (es. mostraSuggerimento, showHint...)
                        quizController.showHint(GameActivity.this);
                    }
                });
            } else {
                // Nascondi negli altri casi (PvP, Bot)
                btnHelp.setVisibility(View.GONE);
            }
        }

        startTimerView();

        controller.initializeGame(board);

        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            controller.handleSquareClick(position, this);
        });

        Button btnExit = findViewById(R.id.btnExit);
        if (btnExit != null) btnExit.setOnClickListener(v -> finish());

        startTimerView();
    }

    // --- CALLBACKS Interface ---

    @Override
    public void refreshUI() {
        Integer sel = null;

        if (controller instanceof LocalTwoPlayerController) {
            sel = ((LocalTwoPlayerController) controller).getSelectedPosition();
        } else if (controller instanceof QuizModeController) {
            sel = ((QuizModeController) controller).getSelectedPosition();
        } else if (controller instanceof BotGameController) {
            // Se nel tuo BotGameController hai un getter simile, usalo qui:
            // sel = ((BotGameController) controller).getSelectedPosition();
        }

        adapter.setSelectedPosition(sel);

        if (sel != null) {
            if (controller instanceof QuizModeController && ((QuizModeController) controller).isHintActive()) {
                adapter.setHints(((QuizModeController) controller).getHintPositions());

                if (btnHelp != null) btnHelp.setEnabled(false);
            } else {
                int row = MoveCalculator.toRow(sel);
                int col = MoveCalculator.toCol(sel);
                adapter.setHints(viewModel.getBoard().getLegalMovesForPiece(row, col));
            }
        } else {
            adapter.setHints(new ArrayList<>());
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateStatusText(String text, int color) {
        statusText.setText(text);
        statusText.setTextColor(color);
    }

    @Override
    public void finishGame() {
        gridView.setEnabled(false);
    }

    @Override
    public void startTimerView() {
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
                controller.handleTimeOut(GameActivity.this);
            }
        }.start();
    }

    @Override
    public void stopTimerView() {
        if (moveTimer != null) moveTimer.cancel();
    }

    @Override
    public void animatePieceMove(int startPosition, int endPosition, Piece piece, Runnable onComplete) {
        FrameLayout boardContainer = findViewById(R.id.boardContainer);
        View startView = gridView.getChildAt(startPosition - gridView.getFirstVisiblePosition());
        View endView = gridView.getChildAt(endPosition - gridView.getFirstVisiblePosition());

        adapter.setHints(new ArrayList<>());
        adapter.setSelectedPosition(null);
        adapter.notifyDataSetChanged();

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

        gridView.setEnabled(false);
        ghostPiece.animate()
                .x(endView.getX() + gridView.getX())
                .y(endView.getY() + gridView.getY())
                .setDuration(300)
                .withEndAction(() -> {
                    boardContainer.removeView(ghostPiece);
                    gridView.setEnabled(true);
                    onComplete.run();
                })
                .start();
    }

    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerView();
        controller.onPause(viewModel.getBoard());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerView();
    }
}