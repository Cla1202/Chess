package com.example.chess.source.game;

import com.example.chess.repository.ChessRepository;
import com.example.chess.service.StockfishService;
import com.example.chess.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChessRemoteDataSource implements BaseChessRemoteDataSource {

    private final StockfishService apiService;

    public ChessRemoteDataSource() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.STOCKFISH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(StockfishService.class);
    }

    @Override
    public void getBestMove(String fen, int depth, ChessRepository.BotMoveCallback callback) {
        apiService.getBestMove(fen, depth).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override
            public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().bestmove);
                } else {
                    callback.onError(new Exception("API Error: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
