package com.example.chess.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LevelDao {

    // 1. Inserisce o aggiorna il progresso.
    // Grazie alla chiave primaria composta (levelId + userId),
    // aggiornerà solo il livello giusto dell'utente giusto.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(LevelProgress progress);

    // 2. LA PIÙ IMPORTANTE: Fornisce il livello massimo completato per l'utente loggato.
    // Usiamo LiveData così la UI si aggiorna istantaneamente al cambio account.
    @Query("SELECT MAX(levelId) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    LiveData<Integer> getMaxCompletedLevelLive(String userId);

    // 3. Recupera i dettagli di un singolo livello per un utente specifico.
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId AND userId = :userId")
    LevelProgress getProgressForLevel(int levelId, String userId);

    // 4. (Opzionale ma consigliata) Recupera tutti i livelli completati da un utente.
    // Utile se volessi mostrare una lista di "Livelli Superati" nel profilo.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    List<LevelProgress> getAllCompletedLevelsForUser(String userId);
}