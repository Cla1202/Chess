package com.example.chess.util;

public class MoveCalculator {

    // Transforms GridView position (0-63) to row (0-7)
    public static int toRow(int position) {
        return position / 8;
    }

    // Transforms GridView position (0-63) to column (0-7)
    public static int toCol(int position) {
        return position % 8;
    }

    // Inverse operation: from x,y coordinates to flat position for the adapter
    public static int toPosition(int row, int col) {
        return row * 8 + col;
    }
}