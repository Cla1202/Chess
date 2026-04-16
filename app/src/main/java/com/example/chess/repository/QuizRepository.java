package com.example.chess.repository;

import com.example.chess.model.Bishop;
import com.example.chess.model.King;
import com.example.chess.model.Knight;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.Queen;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.Rook;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public List<QuizLevel> getAllLevels() {
        List<QuizLevel> levels = new ArrayList<>();

        // Aggiungiamo i livelli alla lista chiamando i metodi dedicati
        levels.add(createLevel1());
        levels.add(createLevel2());
        levels.add(createLevel3());
        levels.add(createLevel4());
        levels.add(createLevel5());
        levels.add(createLevel6());
        levels.add(createLevel7());// Aggiunto un livello 2 di esempio

        return levels;
    }

    // --- METODO DI SUPPORTO PER CREARE UNA GRIGLIA VUOTA ---
    private Piece[][] createEmptyBoard() {
        return new Piece[8][8];
    }

    // ==========================================
    // LIVELLO 1: IL TUO SETUP (Matto in 2)
    // ==========================================
    private QuizLevel createLevel1() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[4][5] = new Queen(4, 5, false);  // Regina in f8
        setupLevel[6][7] = new Pawn(6, 7, false);   // Pedone in a7
        setupLevel[6][5] = new Pawn(6, 5, false);   // Pedone in c7

        // IL RE NERO È IN [6][2]
        setupLevel[6][2] = new King(6, 2, false);   // Re in f7

        setupLevel[5][7] = new Bishop(5, 7, false); // Alfiere in a6
        setupLevel[5][6] = new Pawn(5, 6, false);   // Pedone in b6
        setupLevel[0][7] = new Knight(7, 0, false); // Cavallo in a1

        // --- PEZZI BIANCHI ---
        setupLevel[0][6] = new Knight(0, 6, true);  // Cavallo in f6
        setupLevel[2][2] = new Queen(2, 2, true);   // Regina in f3 (la mossa appena fatta)
        setupLevel[1][7] = new Pawn(1, 7, true);    // Pedone in a2
        setupLevel[1][6] = new Pawn(1, 6, true);    // Pedone in b2
        setupLevel[1][1] = new Pawn(1, 1, true);    // Pedone in g2
        setupLevel[1][0] = new Pawn(1, 0, true);    // Pedone in h2
        setupLevel[5][2] = new Knight(5, 2, true);  // Cavallo in c1
        setupLevel[0][0] = new King(0, 0, true);    // Re in h1

        // Creiamo la sequenza di mosse
        List<MoveRequest> soluzione1 = new ArrayList<>();

        // MOSSA 1: TU (Regina)
        soluzione1.add(new MoveRequest(2, 2, 5, 5));

        // MOSSA 2: COMPUTER (Re) - Risposta forzata
        // IL FIX È QUI: Ora parte da 6, 2 (dove si trova il Re) e va in 7, 2
        soluzione1.add(new MoveRequest(6, 2, 7, 2));

        // MOSSA 3: TU (Regina) - IL MATTO
        soluzione1.add(new MoveRequest(5, 5, 6, 5));

        // Crea il livello e restituiscilo
        return new QuizLevel("Livello 1: Matto in 2", setupLevel, true, soluzione1, 3);
    }

    // ==========================================
    // LIVELLO 2: Sacrificio di Torre (Matto in 2)
    // ==========================================
    private QuizLevel createLevel2() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][0] = new King(0, 0, false);   // Re nero all'angolo in a8
        setupLevel[1][0] = new Pawn(1, 0, false);   // Pedone che blocca la discesa in a7
        setupLevel[1][1] = new Pawn(1, 1, false);   // Pedone che blocca la discesa in b7
        setupLevel[0][2] = new Rook(0, 2, false);   // Torre nera in c8 (che difende il Re)

        // --- PEZZI BIANCHI ---
        setupLevel[7][0] = new Queen(7, 0, true);   // Regina bianca in a1
        setupLevel[7][1] = new Rook(7, 1, true);    // Torre bianca in b1
        setupLevel[7][7] = new King(7, 7, true);    // Re bianco al sicuro in h1

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Sacrifichi la Torre spostandola da b1 a b8 per dare scacco)
        soluzione.add(new MoveRequest(7, 1, 1, 1));

        // MOSSA 2: COMPUTER (La Torre nera è obbligata a mangiare la tua Torre in b8)
        soluzione.add(new MoveRequest(0, 2, 0, 3));

        // MOSSA 3: TU (Ora che la Torre nera si è spostata, la tua Regina sale da a1 ad a8: SCACCO MATTO!)
        soluzione.add(new MoveRequest(7, 0, 1, 0));

        return new QuizLevel("Livello 2: Sacrificio di Torre", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 3: Distrazione Fatale (Matto in 2)
    // ==========================================
    private QuizLevel createLevel3() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][4] = new King(0, 4, false);   // Re nero al centro (e8)
        setupLevel[1][3] = new Pawn(1, 3, false);   // Pedoni che formano un muro davanti al re...
        setupLevel[1][4] = new Pawn(1, 4, false);
        setupLevel[1][5] = new Pawn(1, 5, false);
        setupLevel[0][2] = new Rook(0, 2, false);   // Torre nera di difesa (c8)

        // --- PEZZI BIANCHI ---
        setupLevel[7][0] = new Queen(7, 0, true);   // Regina bianca nell'angolo (a1)
        setupLevel[7][7] = new Rook(7, 7, true);    // Torre bianca nell'altro angolo (h1)
        setupLevel[6][0] = new King(6, 0, true);    // Re bianco al sicuro

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Mandi la Regina come "esca" da a1 ad a8 per dare scacco)
        soluzione.add(new MoveRequest(7, 0, 0, 0));

        // MOSSA 2: COMPUTER (La Torre nera corre in a8 a mangiare la tua Regina)
        soluzione.add(new MoveRequest(0, 2, 0, 0));

        // MOSSA 3: TU (Il Re nero è rimasto senza la sua Torre a fargli da scudo. La tua Torre sale da h1 a h8: SCACCO MATTO!)
        soluzione.add(new MoveRequest(7, 7, 0, 7));

        return new QuizLevel("Livello 3: Distrazione Fatale", setupLevel, true, soluzione, 3);
    }

    private QuizLevel createLevel4() {
        Piece[][] setupLevel = createEmptyBoard();

        // Setup semplice: Matto del corridoio
        setupLevel[0][6] = new King(0, 6, false);
        setupLevel[1][5] = new Pawn(1, 5, false);
        setupLevel[1][6] = new Pawn(1, 6, false);
        setupLevel[1][7] = new Pawn(1, 7, false);

        setupLevel[7][6] = new King(7, 6, true);
        setupLevel[2][0] = new Rook(2, 0, true);

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Torre in fondo) -> Scacco Matto immediato
        soluzione.add(new MoveRequest(2, 0, 0, 0));

        return new QuizLevel("Livello 4: Matto del Corridoio", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 5: Matto dell'Angolo (Regina)
    // ==========================================
    private QuizLevel createLevel5() {
        Piece[][] setupLevel = createEmptyBoard();

        // Re nero incastrato nell'angolo in alto a sinistra
        setupLevel[0][0] = new King(0, 0, false);

        // Il Re bianco gli impedisce di scappare
        setupLevel[2][0] = new King(2, 0, true);

        // La Regina bianca è pronta a dare il colpo di grazia
        setupLevel[2][1] = new Queen(2, 1, true);

        List<MoveRequest> soluzione = new ArrayList<>();
        // MOSSA 1: TU (Regina si sposta in b8 per il matto)
        soluzione.add(new MoveRequest(2, 1, 0, 1));

        return new QuizLevel("Livello 5: Matto dell'Angolo", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 6: Matto di Torre (Opposizione)
    // ==========================================
    private QuizLevel createLevel6() {
        Piece[][] setupLevel = createEmptyBoard();

        // Re nero al centro del bordo superiore
        setupLevel[0][4] = new King(0, 4, false);

        // Re bianco esattamente di fronte (impedisce la fuga in avanti)
        setupLevel[2][4] = new King(2, 4, true);

        // Torre bianca sulla prima colonna
        setupLevel[7][0] = new Rook(7, 0, true);

        List<MoveRequest> soluzione = new ArrayList<>();
        // MOSSA 1: TU (La Torre sale fino in cima e dà matto)
        soluzione.add(new MoveRequest(7, 0, 0, 0));

        return new QuizLevel("Livello 6: Matto di Torre", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 7: Matto del Corridoio (Lato destro)
    // ==========================================
    private QuizLevel createLevel7() {
        Piece[][] setupLevel = createEmptyBoard();

        // Re nero intrappolato dai suoi stessi pedoni a destra
        setupLevel[0][6] = new King(0, 6, false);
        setupLevel[1][5] = new Pawn(1, 5, false);
        setupLevel[1][6] = new Pawn(1, 6, false);
        setupLevel[1][7] = new Pawn(1, 7, false);

        setupLevel[7][0] = new King(7, 0, true);

        // Torre bianca a metà scacchiera
        setupLevel[4][7] = new Rook(4, 7, true);

        List<MoveRequest> soluzione = new ArrayList<>();
        // MOSSA 1: TU (La Torre sale lungo la colonna destra per il matto)
        soluzione.add(new MoveRequest(4, 7, 0, 7));

        return new QuizLevel("Livello 7: Corridoio a Destra", setupLevel, true, soluzione, 3);
    }


}




