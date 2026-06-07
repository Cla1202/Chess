package com.example.chess.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.ui.viewmodel.GameViewModel;
import com.example.chess.util.ChessUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_LOCAL_PVP = "LOCAL_PVP";
    public static final String MODE_QUIZ = "QUIZ";
    public static final String MODE_BOT = "BOT";

    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private GridView gridView;
    private TextView statusText, timerText;
    private ProgressBar timerBar;
    private LinearLayout capturedWhiteContainer, capturedBlackContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);
        capturedWhiteContainer = findViewById(R.id.capturedWhiteContainer);
        capturedBlackContainer = findViewById(R.id.capturedBlackContainer);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        QuizLevel level = (QuizLevel) getIntent().getSerializableExtra("QUIZ_LEVEL_OBJECT");

        android.content.SharedPreferences prefs = getSharedPreferences("ChessSettings", MODE_PRIVATE);
        boolean timerSetting = prefs.getBoolean("timer_enabled", true);
        
        viewModel.init(mode, level, timerSetting);

        setupObservers();

        adapter = new ChessAdapter(this, viewModel.getBoard());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((p, v, pos, id) -> viewModel.handleSquareClick(pos));

        findViewById(R.id.btnExit).setOnClickListener(v -> finish());
        View btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            btnHelp.setVisibility(MODE_QUIZ.equals(mode) ? View.VISIBLE : View.GONE);
            btnHelp.setOnClickListener(v -> viewModel.showHint());
        }
        
        TextView levelTitle = findViewById(R.id.levelTitleText);
        if (levelTitle != null && level != null && MODE_QUIZ.equals(mode)) levelTitle.setText(level.getTitle());
    }

    private void setupObservers() {
        viewModel.getSelectedPosition().observe(this, pos -> { adapter.setSelectedPosition(pos); adapter.notifyDataSetChanged(); });
        viewModel.getHintPositions().observe(this, h -> { adapter.setHints(h); adapter.notifyDataSetChanged(); });
        viewModel.getStatusText().observe(this, t -> statusText.setText(t));
        viewModel.getStatusColor().observe(this, c -> statusText.setTextColor(c));
        viewModel.getIsBotThinking().observe(this, thinking -> gridView.setEnabled(!thinking));
        viewModel.getUpdateCapturedSignal().observe(this, sig -> updateCapturedPieces());
        viewModel.getPromotionRequest().observe(this, req -> { if (req != null) showPromotionDialog(req); });
        viewModel.getToastMessage().observe(this, m -> { if (m != null) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); viewModel.clearToast(); } });

        viewModel.isTimerActive().observe(this, active -> {
            View container = findViewById(R.id.timerContainer);
            if (container != null) container.setVisibility(active ? View.VISIBLE : View.GONE);
        });

        viewModel.getRemainingTime().observe(this, ms -> {
            timerBar.setMax(30000);
            timerBar.setProgress(ms.intValue());
            timerText.setText(String.format(Locale.getDefault(), "00:%02d", ms / 1000));
            timerText.setTextColor(ms < 5000 ? Color.RED : Color.parseColor("#FF5722"));
        });

        viewModel.isGameFinished().observe(this, finished -> {
            if (Boolean.TRUE.equals(finished)) {
                gridView.setEnabled(false);
                if (statusText.getText().toString().contains("PERSO") || statusText.getText().toString().contains("SCADUTO")) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::recreate, 2000);
                }
            }
        });
    }

    private void updateCapturedPieces() {
        capturedWhiteContainer.removeAllViews();
        capturedBlackContainer.removeAllViews();
        addCapturedTo(capturedWhiteContainer, viewModel.getBoard().getCapturedWhite());
        addCapturedTo(capturedBlackContainer, viewModel.getBoard().getCapturedBlack());
    }

    private void addCapturedTo(LinearLayout container, List<Piece> pieces) {
        if (pieces.isEmpty()) return;
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        java.util.Map<String, Piece> protos = new java.util.HashMap<>();
        for (Piece p : pieces) {
            String t = p.getClass().getSimpleName();
            counts.put(t, (counts.get(t) == null ? 0 : counts.get(t)) + 1);
            protos.put(t, p);
        }
        int sz = (int) (24 * getResources().getDisplayMetrics().density);
        for (String t : new String[]{"Pawn", "Knight", "Bishop", "Rook", "Queen"}) {
            if (counts.containsKey(t)) {
                LinearLayout item = new LinearLayout(this);
                item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                item.setPadding(4, 0, 4, 0);
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
                iv.setImageResource(getResIdForPiece(protos.get(t)));
                item.addView(iv);
                if (counts.get(t) > 1) {
                    TextView tv = new TextView(this); tv.setText("x" + counts.get(t));
                    tv.setTextColor(Color.WHITE); tv.setTextSize(10); item.addView(tv);
                }
                container.addView(item);
            }
        }
    }

    private void showPromotionDialog(GameViewModel.PromotionRequest req) {
        String[] names = {getString(R.string.regina), getString(R.string.torre), getString(R.string.alfiere), getString(R.string.cavallo)};
        char[] codes = {'q', 'r', 'b', 'n'};
        String[] types = {"queen", "rook", "bishop", "knight"};
        android.widget.ListAdapter adp = new android.widget.BaseAdapter() {
            @Override public int getCount() { return names.length; }
            @Override public Object getItem(int i) { return names[i]; }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup p) {
                if (v == null) v = getLayoutInflater().inflate(R.layout.item_promotion, p, false);
                ((ImageView)v.findViewById(R.id.promotionIcon)).setImageResource(getResIdForType(types[i], viewModel.getBoard().isWhiteTurn()));
                ((TextView)v.findViewById(R.id.promotionText)).setText(names[i]);
                return v;
            }
        };
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.scegli_promozione).setCancelable(false).setAdapter(adp, (d, w) -> viewModel.completePromotion(req.sR, req.sC, req.eR, req.eC, codes[w])).show();
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

    private int getResIdForType(String type, boolean isWhite) {
        android.content.SharedPreferences pr = getSharedPreferences("ChessSettings", MODE_PRIVATE);
        String s = pr.getString("piece_style", "Classico"), pref = "";
        switch(s) { case "Neo": pref="neo_"; break; case "Moderno": pref="mod_"; break; case "Alfa": pref="alpha_"; break; }
        String name = pref + (isWhite ? "w_" : "b_") + type;
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        if (id == 0) id = getResources().getIdentifier((isWhite?"w_":"b_") + type, "drawable", getPackageName());
        return id;
    }
}