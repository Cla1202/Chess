package com.example.chess.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.QuizLevel;
import com.example.chess.repository.IQuizRepository;

import java.util.List;

public class LevelViewModel extends ViewModel {

    private final ChessDatabase database;
    private final IQuizRepository quizRepository;
    
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final LiveData<Integer> maxCompletedLevel;
    private final LiveData<List<LevelProgress>> allCompletedLevels;
    
    private final MutableLiveData<List<QuizLevel>> quizLevels = new MutableLiveData<>();

    public LevelViewModel(ChessDatabase database, IQuizRepository quizRepository) {
        this.database = database;
        this.quizRepository = quizRepository;

        this.maxCompletedLevel = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getMaxCompletedLevelLive(userId)
        );

        this.allCompletedLevels = Transformations.switchMap(currentUserId, userId ->
                database.levelDao().getAllCompletedLevelsLive(userId)
        );
    }

    /**
     * Loads the quiz levels using the repository.
     * The repository now handles the data source internally (Clean Architecture).
     */
    public void loadQuizLevels() {
        if (quizLevels.getValue() == null) {
            List<QuizLevel> levels = quizRepository.getLichessLevels();
            quizLevels.setValue(levels);
        }
    }
    
    public LiveData<List<QuizLevel>> getQuizLevels() {
        return quizLevels;
    }

    private void setUserId(String userId) {
        if (currentUserId.getValue() == null || !currentUserId.getValue().equals(userId)) {
            currentUserId.setValue(userId);
        }
    }

    public LiveData<Integer> getMaxCompletedLevel(String userId) {
        setUserId(userId);
        return maxCompletedLevel;
    }

    public LiveData<List<LevelProgress>> getAllCompletedLevels(String userId) {
        setUserId(userId);
        return allCompletedLevels;
    }
}
