package com.example.chess.repository;

import android.content.Context;

import com.example.chess.database.ChessDatabase;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.service.StockfishService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChessRepository {
    private final ChessDatabase database;
    private final StockfishService stockfishService;

    // We use an ExecutorService to handle asynchronous operations in an optimized way
    // and avoid the continuous creation of "new Thread(...)"
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Interface to abstract the Retrofit response from the ViewModel
    public interface BotMoveCallback {
        void onSuccess(String bestMove);
        void onError(Throwable t);
    }

    public ChessRepository(Context context) {
        this.database = ChessDatabase.getInstance(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://stockfish.online/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.stockfishService = retrofit.create(StockfishService.class);
    }

    public Board getNewGame() {
        return new Board();
    }

    public void saveProgress(LevelProgress progress) {
        // Using the executor instead of "new Thread(...).start()"
        executor.execute(() -> database.levelDao().insertProgress(progress));
    }

    public LevelProgress getProgress(int levelId, String userId) {
        // Note: if this is called from the main thread (e.g., via an anonymous Thread from the ViewModel),
        // the architecture could be further improved by returning LiveData or using a callback here as well.
        // For now, we maintain compatibility with your GameViewModel.
        return database.levelDao().getProgressForLevel(levelId, userId);
    }

    /**
     * Requests the best move from the bot.
     * The ViewModel calls this method passing a callback, remaining unaware of Retrofit.
     */
    public void getBestMove(String fen, int depth, BotMoveCallback callback) {
        stockfishService.getBestMove(fen, depth).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override
            public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    callback.onSuccess(res.body().bestmove);
                } else {
                    callback.onError(new Exception("API Error or empty response")); // Tradotto anche il messaggio di errore
                }
            }

            @Override
            public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}