package com.example.chess.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockfishService {
    @GET("api/s/v2.php")
    Call<StockfishResponse> getBestMove(
            @Query("fen") String fen,
            @Query("depth") int depth
    );

    class StockfishResponse {
        public boolean success;
        public String bestmove;
        public String mate;
        public String continuation;
    }
}