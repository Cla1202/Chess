package com.example.chess.ui.home;

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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.User;
import com.example.chess.ui.home.viewmodel.GameViewModel;
import com.example.chess.util.Constants;

import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private GridView gridView;
    private TextView statusText, timerText;
    private ProgressBar timerBar;
    private LinearLayout capturedWhiteContainer, capturedBlackContainer;

    // Variable to store the current user
    private User currentUser;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.chess.util.ChessUtil.getLocalizedContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.chess.util.ChessUtil.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        timerBar = findViewById(R.id.timerBar);
        timerText = findViewById(R.id.timerText);
        capturedWhiteContainer = findViewById(R.id.capturedWhiteContainer);
        capturedBlackContainer = findViewById(R.id.capturedBlackContainer);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // 1. Retrieval of the User object passed via Intent
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_CURRENT_USER)) {
            currentUser = getIntent().getParcelableExtra(Constants.EXTRA_CURRENT_USER);
        }

        String mode = getIntent().getStringExtra(Constants.EXTRA_MODE);
        QuizLevel level = (QuizLevel) getIntent().getSerializableExtra(Constants.EXTRA_QUIZ_LEVEL_OBJECT);

        android.content.SharedPreferences prefs = getSharedPreferences(Constants.SETTINGS_PREFS_NAME, MODE_PRIVATE);
        boolean timerEnabled = prefs.getBoolean(Constants.KEY_TIMER_ENABLED, true);

        // 2. ESSENTIAL: Pass the user ID to the ViewModel before initializing the game
        if (currentUser != null) {
            viewModel.setCurrentUserId(currentUser.getIdToken());
        }

        // 3. Initialize the game
        viewModel.init(mode, level, timerEnabled);

        setupObservers();

        adapter = new ChessAdapter(this, viewModel.getBoard());
        if (Constants.MODE_QUIZ.equals(mode) && level != null) {
            adapter.setFlipped(!level.isWhiteTurnToStart());
        }
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((p, v, pos, id) -> viewModel.handleSquareClick(adapter.mapPosition(pos)));

        findViewById(R.id.btnExit).setOnClickListener(v -> finish());
        View btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            btnHelp.setVisibility(Constants.MODE_QUIZ.equals(mode) ? View.VISIBLE : View.GONE);
            btnHelp.setOnClickListener(v -> viewModel.showQuizHint());
        }

        TextView levelTitle = findViewById(R.id.levelTitleText);
        if (levelTitle != null && level != null && Constants.MODE_QUIZ.equals(mode)) levelTitle.setText(level.getTitle());
    }

    private void setupObservers() {
        viewModel.getSelectedPosition().observe(this, pos -> {
            adapter.setSelectedPosition(pos);
            adapter.notifyDataSetChanged();
        });

        viewModel.getHints().observe(this, h -> {
            adapter.setHints(h);
            adapter.notifyDataSetChanged();
        });

        // UPDATED: Map the StatusColorType enum to actual Android colors
        viewModel.getStatus().observe(this, s -> {
            if (s != null) {
                statusText.setText(s.text);
                switch (s.colorType) {
                    case DANGER:
                        statusText.setTextColor(Color.RED);
                        break;
                    case SUCCESS:
                        statusText.setTextColor(Color.GREEN);
                        break;
                    case WARNING:
                        statusText.setTextColor(ContextCompat.getColor(this, R.color.goldenrod));
                        break;
                    case NORMAL:
                    default:
                        statusText.setTextColor(Color.WHITE);
                        break;
                }
            }
        });

        viewModel.getIsThinking().observe(this, thinking -> gridView.setEnabled(!thinking));

        viewModel.getGameEvent().observe(this, event -> {
            if (event == null) return;
            switch (event.type) {
                case TOAST:
                    Toast.makeText(this, event.data, Toast.LENGTH_SHORT).show();
                    break;
                case FINISH:
                    gridView.setEnabled(false);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::finish, 500);
                    break;
                case UPDATE_CAPTURED:
                    updateCapturedPieces();
                    break;
                case ANIMATE_MOVE:
                    // UPDATED: Passing only data to animate, no Runnable onComplete
                    animatePieceMove(event.startPos, event.endPos, event.piece);
                    break;
                case SHOW_PROMOTION:
                    showPromotionDialog(event.isWhite, event.listener);
                    break;
                case FAIL_RESET:
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::recreate, 2000);
                    break;
            }
        });

        viewModel.getIsTimerVisible().observe(this, visible -> {
            View container = findViewById(R.id.timerContainer);
            if (container != null) container.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE);
        });

        viewModel.getRemainingTime().observe(this, ms -> {
            timerBar.setMax(30000);
            timerBar.setProgress(ms.intValue());
            timerText.setText(String.format(Locale.getDefault(), "00:%02d", ms / 1000));
            timerText.setTextColor(ms < 5000 ? Color.RED : Color.parseColor("#FF5722"));
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

    // UPDATED: Removed Runnable onComplete.
    private void animatePieceMove(int s, int e, Piece piece) {
        s = adapter.mapPosition(s);
        e = adapter.mapPosition(e);
        FrameLayout container = findViewById(R.id.boardContainer);
        View sV = gridView.getChildAt(s - gridView.getFirstVisiblePosition());
        View eV = gridView.getChildAt(e - gridView.getFirstVisiblePosition());

        // If views are not ready or off-screen, skip animation but notify the ViewModel anyway
        if (sV == null || eV == null) {
            viewModel.onAnimationFinished();
            return;
        }

        ImageView ghost = new ImageView(this);
        ghost.setImageResource(getResIdForPiece(piece));
        ghost.setLayoutParams(new FrameLayout.LayoutParams(sV.getWidth(), sV.getHeight()));
        ghost.setX(sV.getX() + gridView.getX());
        ghost.setY(sV.getY() + gridView.getY());
        container.addView(ghost);

        if (sV instanceof ViewGroup) ((ImageView)((ViewGroup)sV).getChildAt(0)).setImageResource(0);
        gridView.setEnabled(false);

        ghost.animate().x(eV.getX() + gridView.getX()).y(eV.getY() + gridView.getY()).setDuration(300).withEndAction(() -> {
            container.removeView(ghost);
            adapter.notifyDataSetChanged();
            gridView.setEnabled(true);

            // UPDATED: Notify the ViewModel that the UI has finished the animation
            viewModel.onAnimationFinished();
        }).start();
    }

    private void showPromotionDialog(boolean isWhite, GameViewModel.PromotionListener listener) {
        String[] names = {getString(R.string.regina), getString(R.string.torre), getString(R.string.alfiere), getString(R.string.cavallo)};
        char[] codes = {'q', 'r', 'b', 'n'};
        String[] types = {"queen", "rook", "bishop", "knight"};
        android.widget.ListAdapter adp = new android.widget.BaseAdapter() {
            @Override public int getCount() { return names.length; }
            @Override public Object getItem(int i) { return names[i]; }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup p) {
                if (v == null) v = getLayoutInflater().inflate(R.layout.item_promotion, p, false);
                ((ImageView)v.findViewById(R.id.promotionIcon)).setImageResource(getResIdForType(types[i], isWhite));
                ((TextView)v.findViewById(R.id.promotionText)).setText(names[i]);
                return v;
            }
        };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.scegli_promozione)
                .setCancelable(false)
                .setAdapter(adp, (d, w) -> listener.onPieceSelected(codes[w]))
                .show();
    }

    private int getResIdForPiece(Piece p) {
        android.content.SharedPreferences pr = getSharedPreferences(Constants.SETTINGS_PREFS_NAME, MODE_PRIVATE);
        String style = pr.getString(Constants.KEY_PIECE_STYLE, "Classico");
        String pref = "";
        switch(style) { case "Neo": pref="neo_"; break; case "Moderno": pref="mod_"; break; case "Alfa": pref="alpha_"; break; }
        String name = pref + (p.isWhite() ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK) + p.getClass().getSimpleName().toLowerCase();
        int id = getResources().getIdentifier(name, Constants.DEF_TYPE_DRAWABLE, getPackageName());
        if (id == 0) id = getResources().getIdentifier((p.isWhite()?Constants.PREFIX_WHITE:Constants.PREFIX_BLACK) + p.getClass().getSimpleName().toLowerCase(), Constants.DEF_TYPE_DRAWABLE, getPackageName());
        return id;
    }

    private int getResIdForType(String type, boolean isWhite) {
        android.content.SharedPreferences pr = getSharedPreferences(Constants.SETTINGS_PREFS_NAME, MODE_PRIVATE);
        String s = pr.getString(Constants.KEY_PIECE_STYLE, "Classico"), pref = "";
        switch(s) { case "Neo": pref="neo_"; break; case "Moderno": pref="mod_"; break; case "Alfa": pref="alpha_"; break; }
        String name = pref + (isWhite ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK) + type;
        int id = getResources().getIdentifier(name, Constants.DEF_TYPE_DRAWABLE, getPackageName());
        if (id == 0) id = getResources().getIdentifier((isWhite?Constants.PREFIX_WHITE:Constants.PREFIX_BLACK) + type, Constants.DEF_TYPE_DRAWABLE, getPackageName());
        return id;
    }
}