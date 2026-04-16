package com.example.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {
    private GameViewModel viewModel;
    private ChessAdapter adapter;
    private Integer selectedPosition = null;
    private TextView statusText;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        gridView = findViewById(R.id.chessGrid);

        Button btnExit = findViewById(R.id.btnExit);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> finish());
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
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

        // PRIMO TOCCO: Seleziona il pezzo
        if (selectedPosition == null) {
            Piece p = board.getPiece(row, col);

            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else if (p != null) {
                Toast.makeText(this, "Non è il tuo turno!", Toast.LENGTH_SHORT).show();
            }
        }
        // SECONDO TOCCO: Scegli destinazione
        else {
            int startRow = MoveCalculator.toRow(selectedPosition);
            int startCol = MoveCalculator.toCol(selectedPosition);

            // Salviamo il pezzo prima che si muova nel modello
            Piece movingPiece = board.getPiece(startRow, startCol);

            // Mossa valida?
            if (board.movePiece(startRow, startCol, row, col)) {
                // Blocchiamo la griglia per evitare tocchi durante l'animazione
                gridView.setEnabled(false);

                // Facciamo partire l'animazione
                animateMove(selectedPosition, position, movingPiece, () -> {
                    // COSA FARE QUANDO L'ANIMAZIONE È FINITA:
                    aggiornaStatoGioco();
                    selectedPosition = null;
                    adapter.setSelectedPosition(null);
                    adapter.notifyDataSetChanged();
                    gridView.setEnabled(true); // Sblocca la griglia
                });

            } else {
                Toast.makeText(this, "Mossa non valida o Re in pericolo!", Toast.LENGTH_SHORT).show();
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.notifyDataSetChanged();
            }
        }
    }

    // --- MAGIA DELL'ANIMAZIONE ---
    private void animateMove(int startPosition, int endPosition, Piece piece, Runnable onComplete) {
        FrameLayout boardContainer = findViewById(R.id.boardContainer);

        // Troviamo le View della casella di partenza e di arrivo
        View startView = gridView.getChildAt(startPosition - gridView.getFirstVisiblePosition());
        View endView = gridView.getChildAt(endPosition - gridView.getFirstVisiblePosition());

        if (startView == null || endView == null) {
            onComplete.run(); // Se le view non si vedono (improbabile negli scacchi), salta l'animazione
            return;
        }

        // 1. Creiamo il "Pezzo Fantasma" per l'animazione
        ImageView ghostPiece = new ImageView(this);
        ghostPiece.setImageResource(getResIdForPiece(piece));
        ghostPiece.setLayoutParams(new FrameLayout.LayoutParams(startView.getWidth(), startView.getHeight()));
        ghostPiece.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ghostPiece.setPadding(8, 8, 8, 8);

        // 2. Posizioniamo il fantasma esattamente sopra il pezzo vero
        ghostPiece.setX(startView.getX());
        ghostPiece.setY(startView.getY());

        // Aggiungiamo il fantasma al layout e nascondiamo temporaneamente quello vero
        boardContainer.addView(ghostPiece);
        ((ImageView) startView).setImageResource(0);

        // 3. Animiamo il fantasma verso la destinazione (Durata: 300ms)
        ghostPiece.animate()
                .x(endView.getX())
                .y(endView.getY())
                .setDuration(300)
                .withEndAction(() -> {
                    // Quando arriva, cancelliamo il fantasma ed eseguiamo l'aggiornamento (onComplete)
                    boardContainer.removeView(ghostPiece);
                    onComplete.run();
                })
                .start();
    }

    // Aiutante per trovare l'immagine del pezzo
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
            if (inScacco) {
                String vincitore = turnoBianco ? "NERO" : "BIANCO";
                statusText.setText("🏆 SCACCO MATTO! Vince il " + vincitore);
            } else {
                statusText.setText("🤝 STALLO (Pareggio)");
            }
        } else {
            String turnoDi = turnoBianco ? "Bianco" : "Nero";
            if (inScacco) {
                statusText.setText("⚠️ Turno: " + turnoDi + " (Sotto SCACCO!)");
            } else {
                statusText.setText("Turno: " + turnoDi);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.getRepository().saveCurrentGame(viewModel.getBoard());
        }
    }
}