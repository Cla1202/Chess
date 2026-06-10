package com.example.chess.util;

public class Constants {
    // --- CHESSBOARD THEME COLORS (Hex strings for ChessAdapter) ---
    public static final String THEME_CLASSIC_LIGHT = "#EEEED2";
    public static final String THEME_CLASSIC_DARK = "#769656";
    public static final String THEME_WOOD_LIGHT = "#F0D9B5";
    public static final String THEME_WOOD_DARK = "#B58863";
    public static final String THEME_OCEAN_LIGHT = "#DEE3E6";
    public static final String THEME_OCEAN_DARK = "#8CA2AD";
    public static final String THEME_GREY_LIGHT = "#E1E1E1";
    public static final String THEME_GREY_DARK = "#707070";

    // --- RESOURCES AND GRAPHIC PREFIXES ---
    public static final String PREFIX_WHITE = "w_";
    public static final String PREFIX_BLACK = "b_";
    public static final String DEF_TYPE_DRAWABLE = "drawable";

    // --- ROOM DATABASE ---
    public static final String DATABASE_NAME = "chess_database";
    public static final int DATABASE_VERSION = 6;
    public static final String TABLE_USER = "Utente";

    // --- RETROFIT & APIS ---
    public static final String STOCKFISH_BASE_URL = "https://stockfish.online/";

    // --- SHARED PREFERENCES KEYS ---
    public static final String PREFS_NAME = "ChessPrefs";
    public static final String SETTINGS_PREFS_NAME = "ChessSettings";
    public static final String KEY_LOGGED_USER = "logged_user";
    public static final String KEY_USER_NAME = "logged_user_name";
    public static final String KEY_USER_EMAIL = "logged_user_email";
    public static final String KEY_USER_TOKEN = "logged_user_token";
    public static final String KEY_TIMER_ENABLED = "timer_enabled";
    public static final String KEY_PIECE_STYLE = "piece_style";

    // --- INTENT EXTRAS ---
    public static final String EXTRA_CURRENT_USER = "CURRENT_USER";
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_QUIZ_LEVEL_OBJECT = "QUIZ_LEVEL_OBJECT";

    // --- GAME MODES ---
    public static final String MODE_LOCAL_PVP = "LOCAL_PVP";
    public static final String MODE_QUIZ = "QUIZ";
    public static final String MODE_BOT = "BOT";
}