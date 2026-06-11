package com.example.chess.source.game;

import com.example.chess.database.LevelProgress;

public interface BaseChessLocalDataSource {
    // (Room)
    void saveProgress(LevelProgress progress);
    LevelProgress getProgress(int levelId, String userId);
    void deleteProgressForUser(String userId);
}