package com.example.chess.source.game;

import com.example.chess.database.LevelDao;
import com.example.chess.database.LevelProgress;

public class ChessRoomDataSource implements BaseChessLocalDataSource {

    private final LevelDao levelDao;

    public ChessRoomDataSource(LevelDao levelDao) {
        this.levelDao = levelDao;
    }

    @Override
    public void saveProgress(LevelProgress progress) {
        levelDao.insertProgress(progress);
    }

    @Override
    public LevelProgress getProgress(int levelId, String userId) {
        return levelDao.getProgressForLevel(levelId, userId);
    }

    @Override
    public void deleteProgressForUser(String userId) {
        levelDao.deleteProgressForUser(userId);
    }
}