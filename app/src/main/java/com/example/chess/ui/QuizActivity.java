package com.example.chess.ui;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.R;
import com.example.chess.adapter.ChessAdapter;
import com.example.chess.model.Bishop;
import com.example.chess.model.Board;
import com.example.chess.model.King;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private Board board;
    private ChessAdapter adapter;
    private QuizLevel currentLevel;
    private int currentMoveIndex = 0;
    private Integer selectedPosition = null;
    private TextView levelTitleText;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        levelTitleText = findViewById(R.id.levelTitleText);
        statusText = findViewById(R.id.statusText);
        GridView gridView = findViewById(R.id.chessGrid);

        // Inizializza il repository e carica il primo livello (indice 0)
        com.example.chess.repository.QuizRepository quizRepository = new com.example.chess.repository.QuizRepository();
        currentLevel = quizRepository.getAllLevels().get(0);

        // Inizializza la scacchiera con il setup del quiz
        board = new Board(currentLevel.getInitialBoardSetup(), currentLevel.isWhiteTurnToStart());

        // Aggiorna la UI con il titolo e lo stato iniziale
        levelTitleText.setText(currentLevel.getTitle());
        statusText.setText("Tocca a te! Trova la mossa vincente.");
        statusText.setTextColor(android.graphics.Color.WHITE);

        // Configura l'adapter per la GridView
        adapter = new ChessAdapter(this, board);
        gridView.setAdapter(adapter);

        // Gestisce il tocco sulla scacchiera
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleQuizTouch(position);
        });
    }

    private void handleQuizTouch(int position) {
        // Se il livello è già completato, non fare nulla
        if (currentMoveIndex >= currentLevel.getSolutionMoves().size()) {
            Toast.makeText(this, "Hai già superato questo livello!", Toast.LENGTH_SHORT).show();
            return;
        }

        int row = position / 8;
        int col = position % 8;

        if (selectedPosition == null) {
            // Fase 1: SELEZIONE di un pezzo
            Piece p = board.getPiece(row, col);
            // Seleziona solo se c'è un pezzo e se è del colore del turno attuale
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Seleziona un tuo pezzo!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Fase 2: MOVIMENTO verso una cella di destinazione
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;

            // Recupera la mossa corretta prevista per questo passaggio del quiz
            MoveRequest expectedMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            // Verifica se la mossa tentata coincide con la soluzione
            if (startRow == expectedMove.startRow && startCol == expectedMove.startCol &&
                    row == expectedMove.endRow && col == expectedMove.endCol) {

                // Mossa CORRETTA!
                if (board.movePiece(startRow, startCol, row, col)) {
                    currentMoveIndex++; // Avanza nella sequenza della soluzione
                    statusText.setText("Ottimo! Mossa corretta.");
                    statusText.setTextColor(android.graphics.Color.GREEN);

                    // Controlla se il livello è finito
                    if (currentMoveIndex >= currentLevel.getSolutionMoves().size()) {
                        statusText.setText("Livello Superato! Congratulazioni.");
                        Toast.makeText(this, "Livello Completato!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // La mossa coincide con la soluzione ma viola le regole di base degli scacchi
                    Toast.makeText(this, "Mossa non consentita dalle regole!", Toast.LENGTH_SHORT).show();
                }

            } else {
                // Mossa ERRATA rispetto alla soluzione
                statusText.setText("Mossa sbagliata. Riprova!");
                statusText.setTextColor(android.graphics.Color.RED);
            }

            // Pulisce la selezione in ogni caso
            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }

}