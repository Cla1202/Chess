package com.example.chess.source.game;

import com.example.chess.repository.ChessRepository;
import com.example.chess.service.StockfishService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ChessRemoteDataSource implements BaseChessRemoteDataSource {

    private final StockfishService apiService;

    public ChessRemoteDataSource() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://stockfish.online/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        apiService = retrofit.create(StockfishService.class);
    }

    @Override
    public void getBestMove(String fen, int depth, ChessRepository.BotMoveCallback callback) {
        apiService.getBestMove(fen, depth).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("API Error: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
