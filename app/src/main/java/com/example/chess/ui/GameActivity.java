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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.ui.viewmodel.GameViewModel;
import com.example.chess.util.ChessUtil;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_LOCAL_PVP = "LOCAL_PVP";
    public static final String MODE_QUIZ = "QUIZ";
    public static final String MODE_BOT = "BOT";

    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private GridView gridView;
    private TextView statusText, levelTitleText, timerText;
    private ProgressBar timerBar;
    private LinearLayout capturedWhiteContainer, capturedBlackContainer;
    private CountDownTimer moveTimer;
    private final long TIME_LIMIT_MS = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);
        levelTitleText = findViewById(R.id.levelTitleText);
        capturedWhiteContainer = findViewById(R.id.capturedWhiteContainer);
        capturedBlackContainer = findViewById(R.id.capturedBlackContainer);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_LOCAL_PVP;
        QuizLevel level = (QuizLevel) getIntent().getSerializableExtra("QUIZ_LEVEL_OBJECT");

        viewModel.initGame(mode, level);

        if (MODE_QUIZ.equals(mode) && level != null) {
            if (levelTitleText != null) levelTitleText.setText(level.getTitle());
        } else {
            if (levelTitleText != null) levelTitleText.setVisibility(View.GONE);
            View timerContainer = findViewById(R.id.timerContainer);
            if (timerContainer != null) timerContainer.setVisibility(View.GONE);
        }

        findViewById(R.id.btnExit).setOnClickListener(v -> finish());
        Button btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null && MODE_QUIZ.equals(mode)) {
            btnHelp.setVisibility(View.VISIBLE);
            btnHelp.setOnClickListener(v -> viewModel.showQuizHint());
        }

        adapter = new ChessAdapter(this, viewModel.getBoard());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((p, v, pos, id) -> viewModel.handleSquareClick(pos));

        observeViewModel();
        updateCapturedPieces();
    }

    private void observeViewModel() {
        viewModel.getSelectedPosition().observe(this, sel -> {
            adapter.setSelectedPosition(sel);
            adapter.notifyDataSetChanged();
        });

        viewModel.getHints().observe(this, hints -> {
            adapter.setHints(hints);
            adapter.notifyDataSetChanged();
        });

        viewModel.getStatus().observe(this, statusInfo -> {
            if (statusInfo != null) {
                statusText.setText(statusInfo.text);
                statusText.setTextColor(statusInfo.color);
            }
        });

        viewModel.getIsThinking().observe(this, thinking -> {
            gridView.setEnabled(!thinking);
        });

        viewModel.getGameEvent().observe(this, event -> {
            if (event == null) return;
            switch (event.type) {
                case REFRESH_UI:
                    adapter.notifyDataSetChanged();
                    break;
                case TOAST:
                    Toast.makeText(this, event.data, Toast.LENGTH_SHORT).show();
                    break;
                case FINISH:
                    gridView.setEnabled(false);
                    break;
                case START_TIMER:
                    startTimerView();
                    break;
                case STOP_TIMER:
                    stopTimerView();
                    break;
                case UPDATE_CAPTURED:
                    updateCapturedPieces();
                    break;
                case ANIMATE_MOVE:
                    animatePieceMove(event.startPos, event.endPos, event.piece, event.onComplete);
                    break;
                case SHOW_PROMOTION:
                    showPromotionDialog(event.isWhite, event.listener);
                    break;
                case LEVEL_COMPLETED:
                    handleLevelCompleted(event.data);
                    break;
            }
        });
    }

    private void startTimerView() {
        if (!MODE_QUIZ.equals(getIntent().getStringExtra(EXTRA_MODE))) return;
        if (moveTimer != null) moveTimer.cancel();
        moveTimer = new CountDownTimer(TIME_LIMIT_MS, 50) {
            @Override public void onTick(long ms) {
                timerBar.setProgress((int) ms);
                timerText.setText(String.format("00:%02d", ms/1000));
            }
            @Override public void onFinish() { viewModel.handleTimeOut(); }
        }.start();
    }

    private void stopTimerView() { if (moveTimer != null) moveTimer.cancel(); }

    private void updateCapturedPieces() {
        if (capturedWhiteContainer == null || capturedBlackContainer == null) return;
        capturedWhiteContainer.removeAllViews();
        capturedBlackContainer.removeAllViews();
        Board b = viewModel.getBoard();
        addCapturedTo(capturedWhiteContainer, b.getCapturedWhite());
        addCapturedTo(capturedBlackContainer, b.getCapturedBlack());
    }

    private void addCapturedTo(LinearLayout container, List<Piece> pieces) {
        if (pieces.isEmpty()) return;
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        java.util.Map<String, Piece> protos = new java.util.HashMap<>();
        String[] order = {"Pawn", "Knight", "Bishop", "Rook", "Queen"};
        for (Piece p : pieces) {
            String t = p.getClass().getSimpleName();
            Integer c = counts.get(t);
            counts.put(t, (c == null ? 0 : c) + 1);
            protos.put(t, p);
        }
        int sz = (int) (24 * getResources().getDisplayMetrics().density);
        for (String t : order) {
            if (counts.containsKey(t)) {
                LinearLayout item = new LinearLayout(this);
                item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                item.setPadding(4, 0, 4, 0);
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
                iv.setImageResource(getResIdForPiece(protos.get(t)));
                item.addView(iv);
                if (counts.get(t) != null && counts.get(t) > 1) {
                    TextView tv = new TextView(this); tv.setText("x" + counts.get(t));
                    tv.setTextColor(Color.WHITE); tv.setTextSize(10); item.addView(tv);
                }
                container.addView(item);
            }
        }
    }

    private void animatePieceMove(int s, int e, Piece piece, Runnable onComplete) {
        FrameLayout container = findViewById(R.id.boardContainer);
        View sV = gridView.getChildAt(s - gridView.getFirstVisiblePosition());
        View eV = gridView.getChildAt(e - gridView.getFirstVisiblePosition());
        if (sV == null || eV == null) { 
            if (onComplete != null) onComplete.run(); 
            return; 
        }
        ImageView ghost = new ImageView(this);
        ghost.setImageResource(getResIdForPiece(piece));
        ghost.setLayoutParams(new FrameLayout.LayoutParams(sV.getWidth(), sV.getHeight()));
        ghost.setX(sV.getX() + gridView.getX()); ghost.setY(sV.getY() + gridView.getY());
        container.addView(ghost);
        if (sV instanceof ViewGroup) ((ImageView)((ViewGroup)sV).getChildAt(0)).setImageResource(0);
        gridView.setEnabled(false);
        ghost.animate().x(eV.getX() + gridView.getX()).y(eV.getY() + gridView.getY()).setDuration(300).withEndAction(() -> {
            container.removeView(ghost); 
            gridView.setEnabled(true); 
            if (onComplete != null) onComplete.run();
        }).start();
    }

    private int getResIdForPiece(Piece p) {
        android.content.SharedPreferences pr = getSharedPreferences("ChessSettings", MODE_PRIVATE);
        String s = pr.getString("piece_style", "Classico"), pref = "";
        switch(s) { case "Neo": pref="neo_"; break; case "Moderno": pref="mod_"; break; case "Alfa": pref="alpha_"; break; }
        String name = pref + (p.isWhite() ? "w_" : "b_") + p.getClass().getSimpleName().toLowerCase();
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        if (id == 0) id = getResources().getIdentifier((p.isWhite()?"w_":"b_") + p.getClass().getSimpleName().toLowerCase(), "drawable", getPackageName());
        return id;
    }

    private void showPromotionDialog(boolean isWhite, GameViewModel.PromotionListener listener) {
        String[] names = {getString(R.string.regina), getString(R.string.torre), getString(R.string.alfiere), getString(R.string.cavallo)};
        char[] codes = {'q', 'r', 'b', 'n'};
        String[] types = {"queen", "rook", "bishop", "knight"};

        android.widget.ListAdapter dialogAdapter = new android.widget.BaseAdapter() {
            @Override public int getCount() { return names.length; }
            @Override public Object getItem(int i) { return names[i]; }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View convertView, ViewGroup parent) {
                if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.item_promotion, parent, false);
                ImageView iv = convertView.findViewById(R.id.promotionIcon);
                TextView tv = convertView.findViewById(R.id.promotionText);
                tv.setText(names[i]);
                iv.setImageResource(getResIdForType(types[i], isWhite));
                return convertView;
            }
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.scegli_promozione)
                .setCancelable(false)
                .setAdapter(dialogAdapter, (dialog, which) -> listener.onPieceSelected(codes[which]))
                .show();
    }

    private int getResIdForType(String type, boolean isWhite) {
        android.content.SharedPreferences pr = getSharedPreferences("ChessSettings", MODE_PRIVATE);
        String s = pr.getString("piece_style", "Classico"), pref = "";
        switch(s) { case "Neo": pref="neo_"; break; case "Moderno": pref="mod_"; break; case "Alfa": pref="alpha_"; break; }
        String name = pref + (isWhite ? "w_" : "b_") + type;
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        if (id == 0) id = getResources().getIdentifier((isWhite?"w_":"b_") + type, "drawable", getPackageName());
        return id;
    }

    private void handleLevelCompleted(String levelIdCompleted) {
        android.content.SharedPreferences prefs = getSharedPreferences("ChessAppPrefs", MODE_PRIVATE);
        int quizGiaFatti = prefs.getInt("quiz_completati", 0);
        prefs.edit().putInt("quiz_completati", quizGiaFatti + 1).apply();

        new Thread(() -> {
            try {
                String numericPart = levelIdCompleted.replaceAll("[^0-9]", "");
                int levelNumber = numericPart.isEmpty() ? 1 : Integer.parseInt(numericPart);
                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                String userId = (user != null) ? user.getUid() : "guest_user";
                com.example.chess.database.LevelProgress progress = new com.example.chess.database.LevelProgress(levelNumber, userId, true, 0);
                com.example.chess.database.ChessDatabase.getInstance(getApplicationContext()).levelDao().insertProgress(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override protected void onPause() { super.onPause(); stopTimerView(); viewModel.onPause(); }
}
