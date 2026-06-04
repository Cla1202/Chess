package com.example.chess.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.chess.service.StockfishService;
import com.example.chess.util.ChessUtil;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private LinearLayout capturedWhiteContainer;
    private LinearLayout capturedBlackContainer;

    private boolean isBotEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);
        capturedWhiteContainer = findViewById(R.id.capturedWhiteContainer);
        capturedBlackContainer = findViewById(R.id.capturedBlackContainer);

        isBotEnabled = getIntent().getBooleanExtra("EXTRA_BOT_ENABLED", false);

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
        aggiornaPezziMangiati();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (isBotEnabled && !viewModel.getBoard().isWhiteTurn()) return;
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

                List<Integer> legalMoves = board.getLegalMovesForPiece(row, col);
                adapter.setHints(legalMoves);
                adapter.notifyDataSetChanged();
            }
        } else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);
            Piece movingPiece = board.getPiece(startRow, startCol);

            if (board.movePiece(startRow, startCol, row, col)) {
                gridView.setEnabled(false);

                animateMove(selectedPosition, position, movingPiece, () -> {
                    aggiornaStatoGioco();
                    aggiornaPezziMangiati();
                    selectedPosition = null;
                    adapter.setSelectedPosition(null);
                    adapter.setHints(new ArrayList<>());
                    adapter.notifyDataSetChanged();
                    
                    if (!isBotEnabled || viewModel.getBoard().isWhiteTurn()) {
                        gridView.setEnabled(true);
                    }
                });

            } else {
                Toast.makeText(this, "Mossa non valida!", Toast.LENGTH_SHORT).show();
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void aggiornaPezziMangiati() {
        capturedWhiteContainer.removeAllViews();
        capturedBlackContainer.removeAllViews();

        Board board = viewModel.getBoard();
        addCapturedPiecesToContainer(capturedWhiteContainer, board.getCapturedWhite());
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
            case "Neo": stylePrefix = "neo_"; break;
            case "Moderno": stylePrefix = "mod_"; break;
            case "Alfa": stylePrefix = "alpha_"; break;
            default: stylePrefix = ""; break;
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

    private void aggiornaStatoGioco() {
        Board board = viewModel.getBoard();
        boolean turnoBianco = board.isWhiteTurn();
        boolean inScacco = board.isKingInCheck(turnoBianco);
        boolean haMosseLegali = board.hasAnyLegalMoves(turnoBianco);

        if (!haMosseLegali) {
            if (inScacco) {
                String vincitore = getString(turnoBianco ? R.string.nero : R.string.bianco).toUpperCase();
                statusText.setText(getString(R.string.scacco_matto_vince, vincitore));
            } else {
                statusText.setText(R.string.stallo);
            }
        } else {
            String colore = getString(turnoBianco ? R.string.bianco : R.string.nero);
            String scaccoInfo = inScacco ? getString(R.string.scacco) : "";
            statusText.setText(getString(R.string.turno, colore, scaccoInfo));
            
            if (isBotEnabled && !turnoBianco) {
                makeBotMove();
            }
        }
    }

    private void makeBotMove() {
        statusText.setText(R.string.bot_pensa);
        gridView.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://stockfish.online/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StockfishService service = retrofit.create(StockfishService.class);
        service.getBestMove(viewModel.getBoard().toFen(), 5).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override
            public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String bestMove = response.body().bestmove.replace("bestmove ", "").trim();
                    if (bestMove.length() >= 4) {
                        int startIdx = ChessUtil.algebraicToIndex(bestMove.substring(0, 2));
                        int endIdx = ChessUtil.algebraicToIndex(bestMove.substring(2, 4));
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            handleMove(startIdx);
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> handleMove(endIdx), 400);
                        }, 600);
                        return;
                    }
                }
                makeRandomMove();
            }
            @Override
            public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) { makeRandomMove(); }
        });
    }

    private void makeRandomMove() {
        Board board = viewModel.getBoard();
        List<Integer> allStartSquares = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            Piece p = board.getPiece(i / 8, i % 8);
            if (p != null && p.isWhite() == board.isWhiteTurn() && !board.getLegalMovesForPiece(i / 8, i % 8).isEmpty()) {
                allStartSquares.add(i);
            }
        }
        if (!allStartSquares.isEmpty()) {
            int startIdx = allStartSquares.get((int) (Math.random() * allStartSquares.size()));
            List<Integer> legalMoves = board.getLegalMovesForPiece(startIdx / 8, startIdx % 8);
            int endIdx = legalMoves.get((int) (Math.random() * legalMoves.size()));
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                handleMove(startIdx);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> handleMove(endIdx), 400);
            }, 600);
        } else { gridView.setEnabled(true); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) viewModel.getRepository().saveCurrentGame(viewModel.getBoard());
    }
}