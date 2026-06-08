package com.example.chess.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.chess.database.ChessDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LevelViewModel extends ViewModel {

    private final ChessDatabase database;
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final LiveData<Integer> maxCompletedLevel;
    private final LiveData<java.util.List<com.example.chess.database.LevelProgress>> allCompletedLevels;

    public LevelViewModel(ChessDatabase database) {
        this.database = database;

        // Colleghiamo il LiveData al database filtrando per utente
        this.maxCompletedLevel = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getMaxCompletedLevelLive(userId)
        );

        this.allCompletedLevels = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getAllCompletedLevelsLive(userId)
        );

        // --- AGGIUNTA FONDAMENTALE ---
        // Proviamo a caricare l'utente attuale subito all'avvio
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setUserId(user.getUid());
        } else {
            setUserId("guest_user"); // ID temporaneo se non loggato
        }
    }

    public void setUserId(String userId) {
        currentUserId.setValue(userId);
    }

    public LiveData<Integer> getMaxCompletedLevel() {
        return maxCompletedLevel;
    }

    public LiveData<java.util.List<com.example.chess.database.LevelProgress>> getAllCompletedLevels() {
        return allCompletedLevels;
    }
}