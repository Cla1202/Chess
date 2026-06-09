package com.example.chess.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LevelDao {

    // 1. Inserts or updates progress.
    // Thanks to the composite primary key (levelId + userId),
    // it will update only the correct level for the specific user.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(LevelProgress progress);

    // 2. THE MOST IMPORTANT: Provides the maximum level completed for the logged-in user.
    // We use LiveData so the UI updates instantly when the account changes.
    @Query("SELECT MAX(levelId) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    LiveData<Integer> getMaxCompletedLevelLive(String userId);

    // 3. Retrieves the details of a single level for a specific user.
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId AND userId = :userId")
    LevelProgress getProgressForLevel(int levelId, String userId);

    // 4. (Optional but recommended) Retrieves all levels completed by a user.
    // Useful if you want to show a list of "Completed Levels" in the profile.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    List<LevelProgress> getAllCompletedLevelsForUser(String userId);

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isCompleted = 1 ORDER BY completionTimestamp ASC")
    LiveData<List<LevelProgress>> getAllCompletedLevelsLive(String userId);

    @Query("DELETE FROM level_progress WHERE userId = :userId")
    void deleteProgressForUser(String userId);
}