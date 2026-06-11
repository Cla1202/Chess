package com.example.chess.source.quiz;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for quiz data sources (Assets, Database, Network, etc.)
 */
public interface IQuizDataSource {
    InputStream getQuizStream() throws IOException;
}
