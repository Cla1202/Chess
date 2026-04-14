package com.example.chess.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Diciamo ad Android che questa classe è una tabella del database
@Entity(tableName = "level_progress")
public class LevelProgress {

    @PrimaryKey // Il levelId è la chiave principale, non ci possono essere due livelli uguali
    public int levelId;

    public boolean isCompleted;
    public int mistakesMade; // Possiamo salvare anche gli errori per dare un punteggio!

    // Costruttore
    public LevelProgress(int levelId, boolean isCompleted, int mistakesMade) {
        this.levelId = levelId;
        this.isCompleted = isCompleted;
        this.mistakesMade = mistakesMade;
    }
}