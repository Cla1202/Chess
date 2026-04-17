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
        levels.add(createLevel8());
        levels.add(createLevel9());
        levels.add(createLevel10());
        levels.add(createLevel11());
        levels.add(createLevel12());
        levels.add(createLevel13());
        return levels;
    }

    // --- METODO DI SUPPORTO PER CREARE UNA GRIGLIA VUOTA ---
    private Piece[][] createEmptyBoard() {
        return new Piece[8][8];
    }

    // ==========================================
    // LIVELLO 1: Il Primo Scacco (Matto in 1 - Tutorial)
    // ==========================================
    private QuizLevel createLevel1() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero al sicuro nell'angolo (g8)
        setupLevel[0][5] = new Rook(0, 5, false);   // Torre nera (f8)
        setupLevel[1][5] = new Pawn(1, 5, false);   // Pedone (g7)
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone (h7) - IL BERSAGLIO!

        // --- PEZZI BIANCHI ---
        // Costruiamo una "Batteria" sulla diagonale chiara
        setupLevel[5][3] = new Queen(5, 3, true);   // Regina bianca (d3)
        setupLevel[6][2] = new Bishop(6, 2, true);  // Alfiere bianco (c2) che protegge la Regina da dietro

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU
        // La Regina mangia il pedone in h7, mettendosi in faccia al Re.
        // Il Re non può mangiarla perché è protetta dall'Alfiere: SCACCO MATTO!
        soluzione.add(new MoveRequest(5, 3, 1, 7));

        // Un livello perfetto e veloce per testare lo sblocco del database!
        return new QuizLevel("Livello 1: Benvenuto!", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 2: Batteria Letale (Matto in 1)
    // ==========================================
    private QuizLevel createLevel2() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero (g8)
        setupLevel[1][5] = new Pawn(1, 5, false);   // Pedone (f7)
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone (h7)

        // --- PEZZI BIANCHI ---
        setupLevel[4][3] = new Queen(4, 3, true);   // Regina bianca al centro (d4)
        setupLevel[5][2] = new Bishop(5, 2, true);  // Alfiere bianco (c3) che punta su g7

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (La Regina vola in g7, supportata dall'Alfiere. Matto immediato!)
        soluzione.add(new MoveRequest(4, 3, 1, 6));

        return new QuizLevel("Livello 2: Batteria Letale", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 3: Sacrificio della Regina (Matto in 2)
    // ==========================================
    private QuizLevel createLevel3() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][7] = new King(0, 7, false);   // Re nero nell'angolo (h8)
        setupLevel[0][3] = new Rook(0, 3, false);   // Torre nera lontana in (d8)
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedoni a muro
        setupLevel[1][7] = new Pawn(1, 7, false);

        // --- PEZZI BIANCHI ---
        setupLevel[3][3] = new Queen(3, 3, true);   // Regina bianca (d5)
        setupLevel[2][7] = new Knight(2, 7, true);  // Cavallo bianco letale in (h6)

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Regina si tuffa in g8 per dare scacco sacrificandosi)
        soluzione.add(new MoveRequest(3, 3, 0, 6));

        // MOSSA 2: COMPUTER (Il Re non può mangiare, quindi la Torre è forzata a prendere la Regina in g8)
        soluzione.add(new MoveRequest(0, 3, 0, 6));

        // MOSSA 3: TU (Il Re nero è murato vivo. Il Cavallo salta in f7 e chiude la partita)
        soluzione.add(new MoveRequest(2, 7, 1, 5));

        return new QuizLevel("Livello 3: Sacrificio (in 2)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 4: Matto delle Spalline (Matto in 1)
    // ==========================================
    private QuizLevel createLevel4() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][4] = new King(0, 4, false);   // Re nero al centro del bordo (e8)
        setupLevel[0][3] = new Rook(0, 3, false);   // Le Torri bloccano le vie di fuga laterali (le "Spalline")
        setupLevel[0][5] = new Rook(0, 5, false);

        // --- PEZZI BIANCHI ---
        setupLevel[1][0] = new Queen(1, 0, true);   // Regina bianca (a7)
        setupLevel[2][4] = new King(2, 4, true);    // Re bianco (e6) che farà da scudo

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (La Regina si piazza esattamente di fronte al Re avversario in e7)
        soluzione.add(new MoveRequest(1, 0, 1, 4));

        return new QuizLevel("Livello 4: Le Spalline", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 5: L'Infiltrazione (Matto in 2)
    // ==========================================
    private QuizLevel createLevel5() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero (g8)
        setupLevel[1][5] = new Pawn(1, 5, false);   // Muro pedoni
        setupLevel[1][6] = new Pawn(1, 6, false);
        setupLevel[1][7] = new Pawn(1, 7, false);

        // --- PEZZI BIANCHI ---
        setupLevel[6][3] = new Queen(6, 3, true);   // Regina bianca in basso (d2)
        setupLevel[2][5] = new Pawn(2, 5, true);    // Un pedone bianco insidioso in f6

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Porti la Regina all'attacco in g5)
        soluzione.add(new MoveRequest(6, 3, 3, 6));

        // MOSSA 2: COMPUTER (Il pedone nero in h7 avanza in h6 per cercare di spaventare la Regina)
        soluzione.add(new MoveRequest(1, 7, 2, 7));

        // MOSSA 3: TU (Ignori l'attacco ed entri dritta in g7, protetta dal tuo pedone bianco in f6!)
        soluzione.add(new MoveRequest(3, 6, 1, 6));

        return new QuizLevel("Livello 5: Infiltrazione (in 2)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 6: Matto Arabo (Matto in 1)
    // ==========================================
    private QuizLevel createLevel6() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][7] = new King(0, 7, false);   // Re nero in angolo (h8)
        setupLevel[0][6] = new Rook(0, 6, false);   // Torre nera in g8

        // --- PEZZI BIANCHI ---
        setupLevel[7][7] = new Rook(7, 7, true);    // Torre bianca giù in fondo (h1)
        setupLevel[2][5] = new Knight(2, 5, true);  // Cavallo bianco perfetto in f6

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (La Torre vola fino ad h7. Il Cavallo la protegge e toglie l'unica fuga al Re)
        soluzione.add(new MoveRequest(7, 7, 1, 7));

        return new QuizLevel("Livello 6: Matto Arabo", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 7: Deviazione del Corridoio (Matto in 2)
    // ==========================================
    private QuizLevel createLevel7() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero (g8) chiuso dietro i pedoni
        setupLevel[1][5] = new Pawn(1, 5, false);
        setupLevel[1][6] = new Pawn(1, 6, false);
        setupLevel[1][7] = new Pawn(1, 7, false);
        setupLevel[0][0] = new Rook(0, 0, false);   // Torre nera lontana in a8
        setupLevel[0][3] = new Queen(0, 3, false);  // Regina nera in d8 che "difende" il corridoio

        // --- PEZZI BIANCHI ---
        setupLevel[6][3] = new Queen(6, 3, true);   // Regina bianca (d2)
        setupLevel[7][4] = new Rook(7, 4, true);    // Torre bianca (e1)

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Sacrifichi la Regina andandoti a schiantare in d8)
        soluzione.add(new MoveRequest(6, 3, 0, 3));

        // MOSSA 2: COMPUTER (La Torre nera in a8 è obbligata a mangiare la tua Regina per salvare il proprio Re)
        soluzione.add(new MoveRequest(0, 0, 0, 3));

        // MOSSA 3: TU (Ora che l'ultima riga è sguarnita, la tua Torre sale e dà scacco matto del corridoio!)
        soluzione.add(new MoveRequest(7, 4, 0, 4));

        return new QuizLevel("Livello 7: Deviazione (in 2)", setupLevel, true, soluzione, 3);
    }


    // ==========================================
    // LIVELLO 8: Matto di Anastasia (Sacrificio di Regina)
    // ==========================================
    private QuizLevel createLevel8() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][7] = new King(0, 7, false);   // Re nero all'angolo (h8)
        setupLevel[0][6] = new Rook(0, 6, false);   // Torre nera in g8
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone nero in h7
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedone nero in g7 (blocca la fuga)

        // --- PEZZI BIANCHI ---
        setupLevel[1][4] = new Knight(1, 4, true);  // Cavallo bianco in e7 (LA CHIAVE: controlla g8 e g6)
        setupLevel[3][3] = new Queen(3, 3, true);   // Regina bianca al centro (d5)
        setupLevel[7][7] = new Rook(7, 7, true);    // Torre bianca nascosta (h1)

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Sacrifichi la Regina spaccando la difesa in h7!)
        soluzione.add(new MoveRequest(3, 3, 1, 7));

        // MOSSA 2: COMPUTER (Il Re nero è obbligato a mangiare la tua Regina)
        soluzione.add(new MoveRequest(0, 7, 1, 7));

        // MOSSA 3: TU (La colonna H è libera. La Torre sale e dà un matto letale!)
        soluzione.add(new MoveRequest(7, 7, 5, 7));

        return new QuizLevel("Livello 8: Matto di Anastasia", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 9: Matto di Boden (Le Forbici)
    // ==========================================
    private QuizLevel createLevel9() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][2] = new King(0, 2, false);   // Re nero arroccato lungo (c8)
        setupLevel[0][3] = new Rook(0, 3, false);   // Torre in d8
        setupLevel[1][0] = new Pawn(1, 0, false);   // Pedone a7
        setupLevel[1][1] = new Pawn(1, 1, false);   // Pedone b7
        setupLevel[1][3] = new Pawn(1, 3, false);   // Pedone d7
        setupLevel[2][2] = new Pawn(2, 2, false);   // Pedone c6 (Il bersaglio!)

        // --- PEZZI BIANCHI ---
        setupLevel[3][3] = new Queen(3, 3, true);   // Regina bianca (d5)
        setupLevel[4][5] = new Bishop(4, 5, true);  // Alfiere f4 (Taglia la diagonale scura)
        setupLevel[6][6] = new Bishop(6, 6, true);  // Alfiere g2 (Pronto a colpire)

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Regina mangia il pedone in c6. Boom!)
        soluzione.add(new MoveRequest(3, 3, 2, 2));

        // MOSSA 2: COMPUTER (Il pedone b7 mangia la Regina per forza)
        soluzione.add(new MoveRequest(1, 1, 2, 2));

        // MOSSA 3: TU (La diagonale è aperta: l'Alfiere bianco dà lo scacco matto incrociato!)
        soluzione.add(new MoveRequest(6, 6, 2, 0));

        return new QuizLevel("Livello 9: Matto di Boden", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 10: Matto Affogato (L'incubo del Re)
    // ==========================================
    private QuizLevel createLevel10() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][7] = new King(0, 7, false);   // Re nero in h8
        setupLevel[0][3] = new Rook(0, 3, false);   // Torre nera lontana in d8
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone h7
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedone g7

        // --- PEZZI BIANCHI ---
        setupLevel[3][3] = new Queen(3, 3, true);   // Regina bianca d5
        setupLevel[2][7] = new Knight(2, 7, true);  // Cavallo bianco in h6

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Regina in g8! Ti metti esattamente in faccia al Re!)
        soluzione.add(new MoveRequest(3, 3, 0, 6));

        // MOSSA 2: COMPUTER (Il Re non può mangiare perché c'è il Cavallo in h6, quindi la Torre è obbligata a prendere la Regina)
        soluzione.add(new MoveRequest(0, 3, 0, 6));

        // MOSSA 3: TU (Ora il Re è "affogato" tra pedoni e Torre. Il Cavallo salta e lo finisce!)
        soluzione.add(new MoveRequest(2, 7, 1, 5));

        return new QuizLevel("Livello 10: Matto Affogato", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 11: Matto di Anastasia (Matto in 3)
    // ==========================================
    private QuizLevel createLevel11() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero (g8)
        setupLevel[0][5] = new Rook(0, 5, false);   // Torre nera (f8)
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedone g7
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone h7

        // --- PEZZI BIANCHI ---
        setupLevel[3][3] = new Knight(3, 3, true);  // Cavallo in d5
        setupLevel[5][3] = new Queen(5, 3, true);   // Regina in d3
        setupLevel[5][0] = new Rook(5, 0, true);    // Torre in a3

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Cavallo dà scacco in e7)
        soluzione.add(new MoveRequest(3, 3, 1, 4));
        // MOSSA 2: PC (Re scappa all'angolo)
        soluzione.add(new MoveRequest(0, 6, 0, 7));
        // MOSSA 3: TU (Sacrificio di Regina in h7!)
        soluzione.add(new MoveRequest(5, 3, 1, 7));
        // MOSSA 4: PC (Re obbligato a mangiare la Regina)
        soluzione.add(new MoveRequest(0, 7, 1, 7));
        // MOSSA 5: TU (La Torre sfreccia sulla colonna libera per il Matto in h3)
        soluzione.add(new MoveRequest(5, 0, 5, 7));

        return new QuizLevel("Livello 11: Anastasia (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 12: L'Attacco di Damiano (Matto in 3)
    // ==========================================
    private QuizLevel createLevel12() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re nero (g8)
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedone g7
        setupLevel[1][5] = new Pawn(1, 5, false);   // Pedone f7

        // --- PEZZI BIANCHI ---
        setupLevel[5][3] = new Queen(5, 3, true);   // Regina (d3)
        setupLevel[7][7] = new Rook(7, 7, true);    // Torre (h1)
        setupLevel[2][7] = new Pawn(2, 7, true);    // Pedone (h6) - Il pezzo chiave!
        setupLevel[3][5] = new Pawn(3, 5, true);    // Pedone f5 (Toglie la fuga)

        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Regina in h7 supportata dal pedone!)
        soluzione.add(new MoveRequest(5, 3, 1, 7));
        // MOSSA 2: PC (Il Re mangia la Regina per forza)
        soluzione.add(new MoveRequest(0, 6, 1, 7));
        // MOSSA 3: TU (La Torre sale per dare scacco in h3)
        soluzione.add(new MoveRequest(7, 7, 5, 7));
        // MOSSA 4: PC (Il Re scappa in g8, non può andare altrove)
        soluzione.add(new MoveRequest(1, 7, 0, 6));
        // MOSSA 5: TU (La Torre sale in h8 per il colpo finale)
        soluzione.add(new MoveRequest(5, 7, 0, 7));

        return new QuizLevel("Livello 12: Damiano (Matto in 3)", setupLevel, true, soluzione, 3);
    }

    // ==========================================
    // LIVELLO 13: Il Lascito di Philidor (Matto in 4)
    // ==========================================
    private QuizLevel createLevel13() {
        Piece[][] setupLevel = createEmptyBoard();

        // --- PEZZI NERI ---
        setupLevel[0][6] = new King(0, 6, false);   // Re in g8
        setupLevel[0][5] = new Rook(0, 5, false);   // Torre in f8
        setupLevel[1][6] = new Pawn(1, 6, false);   // Pedone g7
        setupLevel[1][7] = new Pawn(1, 7, false);   // Pedone h7

        // --- PEZZI BIANCHI ---
        setupLevel[4][2] = new Queen(4, 2, true);   // Regina in c4
        setupLevel[3][6] = new Knight(3, 6, true);  // Cavallo in g5

        // Ben 7 mosse totali nella sequenza!
        List<MoveRequest> soluzione = new ArrayList<>();

        // MOSSA 1: TU (Scacco di Cavallo in f7)
        soluzione.add(new MoveRequest(3, 6, 1, 5));
        // MOSSA 2: PC (Re scappa in h8)
        soluzione.add(new MoveRequest(0, 6, 0, 7));
        // MOSSA 3: TU (Cavallo in h6, doppio scacco scoperto con la Regina!)
        soluzione.add(new MoveRequest(1, 5, 2, 7));
        // MOSSA 4: PC (Re torna in g8)
        soluzione.add(new MoveRequest(0, 7, 0, 6));
        // MOSSA 5: TU (Sacrificio folle di Regina in g8!)
        soluzione.add(new MoveRequest(4, 2, 0, 6));
        // MOSSA 6: PC (La Torre nera DEVE mangiare la Regina)
        soluzione.add(new MoveRequest(0, 5, 0, 6));
        // MOSSA 7: TU (Il Re nero è murato vivo, il Cavallo torna in f7 e chiude la partita)
        soluzione.add(new MoveRequest(2, 7, 1, 5));

        return new QuizLevel("Livello 13: Matto di Philidor (in 4)", setupLevel, true, soluzione, 5); // 5 tentativi
    }


}




