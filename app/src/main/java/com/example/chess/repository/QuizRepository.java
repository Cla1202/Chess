package com.example.chess.repository;

import com.example.chess.model.Bishop;
import com.example.chess.model.King;
import com.example.chess.model.Knight;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.Queen;
import com.example.chess.model.QuizLevel;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public List<QuizLevel> getAllLevels() {
        List<QuizLevel> levels = new ArrayList<>();

        Piece[][] setupLevel = new Piece[8][8];

// --- PEZZI NERI ---
// Riga 0 (Traversa 8 sulla scacchiera)
        setupLevel[4][5] = new Queen(4, 5, false);  // Regina in f8

// Riga 1 (Traversa 7 sulla scacchiera)
        setupLevel[6][7] = new Pawn(6, 7, false);   // Pedone in a7
        setupLevel[6][5] = new Pawn(6, 5, false);   // Pedone in c7
        setupLevel[6][2] = new King(6, 2, false);   // Re in f7

// Riga 2 (Traversa 6 sulla scacchiera)
        setupLevel[5][7] = new Bishop(5, 7, false); // Alfiere in a6
        setupLevel[5][6] = new Pawn(5, 6, false);   // Pedone in b6

// Riga 7 (Traversa 1 sulla scacchiera)
        setupLevel[0][7] = new Knight(7, 0, false); // Cavallo in a1


// --- PEZZI BIANCHI ---
// Riga 2 (Traversa 6 sulla scacchiera)
        setupLevel[0][6] = new Knight(0, 6, true);  // Cavallo in f6

// Riga 5 (Traversa 3 sulla scacchiera)
        setupLevel[2][2] = new Queen(2, 2, true);   // Regina in f3 (la mossa appena fatta)

// Riga 6 (Traversa 2 sulla scacchiera)
        setupLevel[1][7] = new Pawn(1, 7, true);    // Pedone in a2
        setupLevel[1][6] = new Pawn(1, 6, true);    // Pedone in b2
        setupLevel[1][1] = new Pawn(1, 1, true);    // Pedone in g2
        setupLevel[1][0] = new Pawn(1, 0, true);    // Pedone in h2

// Riga 7 (Traversa 1 sulla scacchiera)
        setupLevel[5][2] = new Knight(5, 2, true);  // Cavallo in c1
        setupLevel[0][0] = new King(0, 0, true);    // Re in h1

// Creazione della soluzione (da compilare con la mossa corretta del Nero)
        List<MoveRequest> solution = new ArrayList<>();
        MoveRequest move = new MoveRequest();
// TODO: Inserisci le coordinate della mossa corretta per risolvere il quiz
// move.startRow = ...; move.startCol = ...;
// move.endRow = ...; move.endCol = ...;
        solution.add(move);

// Creiamo il livello passando "false" perché tocca al NERO iniziare
        QuizLevel myLevel = new QuizLevel(1, "Trova la mossa vincente", setupLevel, false, solution);

        // Creiamo la mossa della soluzione
        List<MoveRequest> soluzione1 = new ArrayList<>();
        MoveRequest mossa1 = new MoveRequest();
        mossa1.startRow = 6; mossa1.startCol = 6;
        mossa1.endRow = 1; mossa1.endCol = 1; // Alfiere si sposta in b7 (1,1) per scacco matto
        soluzione1.add(mossa1);

        QuizLevel livello1 = new QuizLevel(1, "Matto in 1", setupLevel, true, soluzione1);
        levels.add(livello1);


        // === CREAZIONE LIVELLO 2 (Soluzione a più mosse) ===
        // Potrai aggiungere altri pezzi per il setup 2...
        // e inserire più MoveRequest nella lista `soluzione2`

        return levels;
    }
}