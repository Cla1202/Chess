package com.example.chess.source.quiz;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Android implementation of IQuizDataSource using Assets.
 */
public class AssetQuizDataSource implements IQuizDataSource {
    private final Context context;
    private static final String FILE_NAME = "puzzles.csv";

    public AssetQuizDataSource(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public InputStream getQuizStream() throws IOException {
        return context.getAssets().open(FILE_NAME);
    }
}
