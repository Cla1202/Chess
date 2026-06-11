package com.example.chess.repository;

import com.example.chess.model.QuizLevel;
import java.util.List;

/**
 * Interface for the Quiz Repository to follow Clean Architecture principles.
 */
public interface IQuizRepository {
    List<QuizLevel> getLichessLevels();
    List<QuizLevel> getFallbackLevels();
}
