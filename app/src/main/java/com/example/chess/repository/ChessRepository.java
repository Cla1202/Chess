package com.example.chess.repository;

import android.content.Context;
import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.service.StockfishService;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChessRepository {
    private final ChessDatabase database;
    private final StockfishService stockfishService;

    public ChessRepository(Context context) {
        this.database = ChessDatabase.getInstance(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://stockfish.online/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.stockfishService = retrofit.create(StockfishService.class);
    }

    public Board getNewGame() { return new Board(); }

    public void saveProgress(LevelProgress progress) {
        new Thread(() -> database.levelDao().insertProgress(progress)).start();
    }

    public LevelProgress getProgress(int levelId, String userId) {
        return database.levelDao().getProgressForLevel(levelId, userId);
    }

    public Call<StockfishService.StockfishResponse> getBestMove(String fen, int depth) {
        return stockfishService.getBestMove(fen, depth);
    }
}