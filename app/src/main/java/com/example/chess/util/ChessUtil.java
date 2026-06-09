package com.example.chess.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class ChessUtil {

    public static void applyLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ChessSettings", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "it");
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        // For Android 7.0+ (API 24)
        context.createConfigurationContext(config);
        // For compatibility and immediate update of current resources
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    public static Context getLocalizedContext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ChessSettings", Context.MODE_PRIVATE);
        String langCode = prefs.getString("language", "it");
        Locale locale = new Locale(langCode);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static int algebraicToIndex(String algebraic) {
        if (algebraic == null || algebraic.length() < 2) return -1;
        char file = algebraic.toLowerCase().charAt(0);
        char rank = algebraic.charAt(1);
        int col = file - 'a';
        int row = 8 - Character.getNumericValue(rank);
        return (row * 8) + col;
    }

    public static String indexToAlgebraic(int index) {
        if (index < 0 || index > 63) return "";
        int row = index / 8;
        int col = index % 8;
        return String.valueOf((char) ('a' + col)) + (8 - row);
    }
}