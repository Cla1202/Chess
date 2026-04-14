package com.example.chess.util;

public class ChessUtil {

    /**
     * Converte una posizione scacchistica (es. "e2") nell'indice della GridView (0-63).
     * Supponendo che l'indice 0 sia "a8" (in alto a sinistra) e l'indice 63 sia "h1" (in basso a destra).
     */
    public static int algebraicToIndex(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            return -1; // Errore
        }

        // Estrai la lettera (colonna a-h) e il numero (riga 1-8)
        char file = algebraic.toLowerCase().charAt(0);
        char rank = algebraic.charAt(1);

        // Calcola la colonna (0 per 'a', 7 per 'h')
        int col = file - 'a';

        // Calcola la riga (0 per la riga '8' in alto, 7 per la riga '1' in basso)
        int row = 8 - Character.getNumericValue(rank);

        // Calcola l'indice finale per un array monodimensionale o una GridView
        return (row * 8) + col;
    }

    /**
     * Fa l'esatto opposto: prende un indice della GridView (es. 52) e restituisce "e2".
     * Molto utile per stampare a schermo le mosse dell'utente!
     */
    public static String indexToAlgebraic(int index) {
        if (index < 0 || index > 63) {
            return "Posizione non valida";
        }

        int row = index / 8;
        int col = index % 8;

        // Trasforma il numero della colonna in lettera
        char file = (char) ('a' + col);

        // Trasforma il numero della riga nel numero scacchistico corretto
        int rank = 8 - row;

        return String.valueOf(file) + rank;
    }
}