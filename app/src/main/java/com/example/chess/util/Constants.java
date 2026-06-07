package com.example.chess.util;

import android.graphics.Color;

public class Constants {
    // COLORI TEMI SCACCHIERA
    public static final String THEME_CLASSIC_LIGHT = "#EEEED2";
    public static final String THEME_CLASSIC_DARK = "#769656";
    public static final String THEME_WOOD_LIGHT = "#F0D9B5";
    public static final String THEME_WOOD_DARK = "#B58863";
    public static final String THEME_OCEAN_LIGHT = "#DEE3E6";
    public static final String THEME_OCEAN_DARK = "#8CA2AD";
    public static final String THEME_GREY_LIGHT = "#E1E1E1";
    public static final String THEME_GREY_DARK = "#707070";

    // COLORI
    public static final String COLOR_LIGHT = "#EEEED2";
    public static final String COLOR_DARK = "#769656";
    public static final String COLOR_SELECTED = "#F5F682"; 
    public static final String COLOR_SQUARE_LIGHT = "#EEEEEE";

    public static final int GOLDENROD = Color.parseColor("#DAA520");

    // MESSAGGI DI STATO
    public static final String STATUS_WHITE_TURN = "Turno: Bianco";
    public static final String STATUS_BLACK_TURN = "Turno: Nero";
    public static final String MSG_INVALID_MOVE = "Mossa non valida!";
    public static final String STEEL_BLUE = "#81B3D2";
    
    // RISORSE E PREFISSI GRAFICI
    public static final String PREFIX_WHITE = "w_";
    public static final String PREFIX_BLACK = "b_";
    public static final String DEF_TYPE_DRAWABLE = "drawable";

    // --- COLORI UI: CARD LIVELLI ---
    public static final String COLOR_CARD_UNLOCKED = "#2C2C2C"; 
    public static final String COLOR_CARD_LOCKED = "#151515";   
    public static final String COLOR_TEXT_LOCKED = "#555555";   

    // --- MESSAGGI E TESTI UI ---
    public static final String PREFIX_LEVEL = "Livello ";
    public static final String MSG_LEVEL_LOCKED = "Devi superare il livello precedente per sbloccare questo!";

    // --- DATABASE ROOM ---
    public static final String DATABASE_NAME = "chess_database";
    public static final int DATABASE_VERSION = 4;
    public static final String UTENTE = "Utente";

    // SERVICE
    public static final String GAME_MOVE = "game'/move";
    public static final String GAME_STATUS = "game/status/{gameId}";
    public static final String GAME_ID = "gameId";

    // USER AUTHEN
    public static final String PASSWORD_CORTA = "Password troppo corta!";
}