package com.example.chess.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "level_progress", primaryKeys = {"levelId", "userId"})
public class LevelProgress {
    public int levelId;
    @NonNull
    public String userId;
    public boolean isCompleted;
    public int mistakesMade;
    public long timeSpentMillis;
    public long completionTimestamp;

    public LevelProgress(int levelId, @NonNull String userId, boolean isCompleted, int mistakesMade, long timeSpentMillis, long completionTimestamp) {
        this.levelId = levelId;
        this.userId = userId;
        this.isCompleted = isCompleted;
        this.mistakesMade = mistakesMade;
        this.timeSpentMillis = timeSpentMillis;
        this.completionTimestamp = completionTimestamp;
    }
}