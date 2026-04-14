package com.example.chess.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LevelDao {

    // Salva o aggiorna il progresso di un livello
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(LevelProgress progress);

    // Trova le statistiche di un livello specifico
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    LevelProgress getProgressForLevel(int levelId);

    // Trova qual è il livello più alto che hai completato
    @Query("SELECT MAX(levelId) FROM level_progress WHERE isCompleted = 1")
    int getMaxCompletedLevel();
}