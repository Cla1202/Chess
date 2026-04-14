package com.example.chess.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chess.database.ChessDatabase;
import java.util.concurrent.Executors;

public class LevelViewModel extends ViewModel {

    // LiveData: Contenitore che la UI può "osservare"
    private final MutableLiveData<Integer> maxUnlockedLevel = new MutableLiveData<>();
    private final ChessDatabase database;

    // Il costruttore riceve il database e fa partire subito la ricerca
    public LevelViewModel(ChessDatabase database) {
        this.database = database;
        loadMaxUnlockedLevel();
    }

    // Metodo interno per leggere dal database in background
    private void loadMaxUnlockedLevel() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int maxCompleted = database.levelDao().getMaxCompletedLevel();
            int unlocked = Math.max(1, maxCompleted + 1);

            // postValue serve per aggiornare il LiveData da un thread in background!
            maxUnlockedLevel.postValue(unlocked);
        });
    }

    // Metodo pubblico che l'Activity userà per leggere il dato
    public LiveData<Integer> getMaxUnlockedLevel() {
        return maxUnlockedLevel;
    }
}