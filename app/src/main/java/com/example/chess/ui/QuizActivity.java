package com.example.chess.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
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

    private boolean isComputerThinking = false;
    private int errorCount = 0; // Contatore errori


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

        Button hintButton = findViewById(R.id.hintButton);

        // Gestisce il tocco sulla scacchiera
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            handleQuizTouch(position);
        });

        hintButton.setOnClickListener(v -> {
            if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                MoveRequest correctMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

                // Calcolo posizioni
                int startPos = correctMove.startRow * 8 + correctMove.startCol;
                int endPos = correctMove.endRow * 8 + correctMove.endCol;

                List<Integer> hints = new ArrayList<>();
                hints.add(startPos);
                hints.add(endPos);

                // Applica i suggerimenti all'adapter
                adapter.setHints(hints);

                // AGGIORNAMENTO TESTO
                statusText.setText("Suggerimento attivato: osserva le celle gialle!");
                statusText.setTextColor(android.graphics.Color.CYAN);

                // Opzionale: Cambia il testo del bottone stesso
                hintButton.setText("Suggerimento mostrato");
                hintButton.setEnabled(false); // Disabilita per evitare click ripetuti
            }
        });

    }

    private void handleQuizTouch(int position) {
        Button hintButton = findViewById(R.id.hintButton);
        if (isComputerThinking) return;

        if (currentMoveIndex >= currentLevel.getSolutionMoves().size()) {
            Toast.makeText(this, "Hai già superato questo livello!", Toast.LENGTH_SHORT).show();
            return;
        }

        int row = position / 8;
        int col = position % 8;

        if (selectedPosition == null) {
            // --- FASE 1: SELEZIONE ---
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Seleziona un tuo pezzo!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // --- FASE 2: MOVIMENTO ---
            int startRow = selectedPosition / 8;
            int startCol = selectedPosition % 8;

            MoveRequest expectedMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            if (board.movePiece(startRow, startCol, row, col)) {
                currentMoveIndex++; // L'indice passa da 0 (tua mossa) a 1 (mossa computer)

                // Pulisci subito la selezione grafica dell'utente
                selectedPosition = null;
                adapter.setSelectedPosition(null);
                adapter.setHints(new ArrayList<>()); // Rimuovi eventuali aiuti gialli

                // CONTROLLO: C'è una mossa successiva nella lista?
                if (currentMoveIndex < currentLevel.getSolutionMoves().size()) {
                    // Se sì, significa che la prossima mossa (indice 1) è del COMPUTER
                    statusText.setText("Ottimo! Il computer sta rispondendo...");
                    playComputerMove();
                } else {
                    // Se no, il quiz è finito con la tua mossa
                    statusText.setText("Livello Superato! Scacco Matto.");
                    statusText.setTextColor(Color.GREEN);
                }

                adapter.notifyDataSetChanged();
            }
             else {
                // --- GESTIONE ERRORE ---
                errorCount++;
                int tentativiRimasti = currentLevel.getMaxAttempts() - errorCount;

                // Rimuoviamo gli aiuti anche se sbaglia, così deve richiederli
                adapter.setHints(new ArrayList<>());
                hintButton.setText("Aiuto");
                hintButton.setEnabled(true);

                if (tentativiRimasti <= 0) {
                    statusText.setText("TENTATIVI ESAURITI! HAI PERSO.");
                    statusText.setTextColor(android.graphics.Color.RED);
                    isComputerThinking = true;
                } else {
                    statusText.setText("Mossa errata! Rimasti: " + tentativiRimasti);
                    statusText.setTextColor(android.graphics.Color.RED);
                }
            }

            // Reset grafica
            selectedPosition = null;
            adapter.setSelectedPosition(null);
            adapter.notifyDataSetChanged();
        }
    }


    // COMPUTER MOVE
    private void playComputerMove() {
        isComputerThinking = true; // Impedisce all'utente di toccare la scacchiera

        new android.os.Handler().postDelayed(() -> {
            // Prende la mossa all'indice 1 (Re d2 -> e1)
            MoveRequest computerMove = currentLevel.getSolutionMoves().get(currentMoveIndex);

            // FORZA IL TURNO AL NERO (fondamentale!)
            board.setWhiteTurn(false);

            if (board.movePiece(computerMove.startRow, computerMove.startCol,
                    computerMove.endRow, computerMove.endCol)) {

                currentMoveIndex++; // L'indice passa da 1 a 2 (tua mossa finale)

                // RESTITUISCE IL TURNO AL BIANCO
                board.setWhiteTurn(true);

                statusText.setText("Il computer si è mosso. Ora tocca a te per il matto!");
                adapter.notifyDataSetChanged();
            }

            isComputerThinking = false; // L'utente può tornare a giocare
        }, 1000); // 1 secondo di pausa per rendere il gioco naturale
    }

    // TASTO DI SUGGERIMENTO


}