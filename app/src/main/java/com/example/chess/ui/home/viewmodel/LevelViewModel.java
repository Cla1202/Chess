package com.example.chess.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;

import java.util.List;

public class LevelViewModel extends ViewModel {

    private final ChessDatabase database;
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final LiveData<Integer> maxCompletedLevel;
    private final LiveData<List<LevelProgress>> allCompletedLevels;

    public LevelViewModel(ChessDatabase database) {
        this.database = database;

        // The LiveData reacts and queries the DAO automatically
        // every time the value of currentUserId changes
        this.maxCompletedLevel = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getMaxCompletedLevelLive(userId)
        );

        this.allCompletedLevels = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getAllCompletedLevelsLive(userId)
        );
    }

    // Private method to update the ID reactively
    private void setUserId(String userId) {
        // We update the LiveData only if the ID has changed, to avoid infinite loops
        if (currentUserId.getValue() == null || !currentUserId.getValue().equals(userId)) {
            currentUserId.setValue(userId);
        }
    }

    // 1. Retrieve the maximum level completed for this specific user
    public LiveData<Integer> getMaxCompletedLevel(String userId) {
        setUserId(userId);
        return maxCompletedLevel;
    }

    // 2. Retrieve all completed levels for this specific user
    // (This is the method your ProfileFragment now uses!)
    public LiveData<List<LevelProgress>> getAllCompletedLevels(String userId) {
        setUserId(userId);
        return allCompletedLevels;
    }
}