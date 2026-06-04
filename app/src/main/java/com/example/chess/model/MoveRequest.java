package com.example.chess.model;

import java.io.Serializable;

public class MoveRequest implements Serializable {
    public int startRow;
    public int startCol;
    public int endRow;
    public int endCol;

    public MoveRequest(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }
}