package com.example.chess.util;

public class MoveCalculator {

    // Trasforma la posizione della GridView (0-63) in riga (0-7)
    public static int toRow(int position) {
        return position / 8;
    }

    // Trasforma la posizione della GridView (0-63) in colonna (0-7)
    public static int toCol(int position) {
        return position % 8;
    }

    // Operazione inversa: da coordinate x,y a posizione piatta per l'adapter
    public static int toPosition(int row, int col) {
        return row * 8 + col;
    }
}