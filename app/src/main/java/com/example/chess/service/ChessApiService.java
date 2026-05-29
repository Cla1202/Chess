package com.example.chess.service;

import com.example.chess.model.MoveRequest;
import com.example.chess.model.GameStatus;
import com.example.chess.util.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

// Esempio con Retrofit
public interface ChessApiService {

    // Invia una mossa al server
    @POST(Constants.GAME_MOVE)
    Call<GameStatus> sendMove(@Body MoveRequest move);

    // Recupera lo stato della partita online
    @GET(Constants.GAME_STATUS)
    Call<GameStatus> getGameStatus(@Path(Constants.GAME_ID) String gameId);
}
