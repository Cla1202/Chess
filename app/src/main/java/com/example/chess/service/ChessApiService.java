package com.example.chess.service;

import com.example.chess.model.MoveRequest;
import com.example.chess.model.GameStatus;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

// Esempio con Retrofit
public interface ChessApiService {

    // Invia una mossa al server
    @POST("game/move")
    Call<GameStatus> sendMove(@Body MoveRequest move);

    // Recupera lo stato della partita online
    @GET("game/status/{gameId}")
    Call<GameStatus> getGameStatus(@Path("gameId") String gameId);
}
