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

        // Il LiveData reagisce e interroga il DAO automaticamente
        // ogni volta che cambia il valore di currentUserId
        this.maxCompletedLevel = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getMaxCompletedLevelLive(userId)
        );

        this.allCompletedLevels = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getAllCompletedLevelsLive(userId)
        );
    }

    // Metodo privato per aggiornare l'ID in modo reattivo
    private void setUserId(String userId) {
        // Aggiorniamo il LiveData solo se l'ID è cambiato, per evitare loop infiniti
        if (currentUserId.getValue() == null || !currentUserId.getValue().equals(userId)) {
            currentUserId.setValue(userId);
        }
    }

    // 1. Recupera il livello massimo superato per questo specifico utente
    public LiveData<Integer> getMaxCompletedLevel(String userId) {
        setUserId(userId);
        return maxCompletedLevel;
    }

    // 2. Recupera tutti i livelli completati per questo specifico utente
    // (Questo è il metodo che ora usa il tuo ProfileFragment!)
    public LiveData<List<LevelProgress>> getAllCompletedLevels(String userId) {
        setUserId(userId);
        return allCompletedLevels;
    }
}