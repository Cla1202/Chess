package com.example.chess.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

// Definiamo una chiave primaria composta: la combinazione di levelId e userId
@Entity(tableName = "level_progress", primaryKeys = {"levelId", "userId"})
public class LevelProgress {

    public int levelId;

    @NonNull
    public String userId; // Qui salverai l'ID univoco o l'email dell'account (es. Firebase UID)

    public boolean isCompleted;
    public int mistakesMade;

    public LevelProgress(int levelId, @NonNull String userId, boolean isCompleted, int mistakesMade) {
        this.levelId = levelId;
        this.userId = userId;
        this.isCompleted = isCompleted;
        this.mistakesMade = mistakesMade;
    }
}