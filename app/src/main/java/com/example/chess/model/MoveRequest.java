package com.example.chess.model;

public class MoveRequest {
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