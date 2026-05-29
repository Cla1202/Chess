package com.example.chess.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.chess.util.Constants; // Assicurati che l'import sia corretto per il tuo progetto

// Usa la costante per la versione del database
@Database(entities = {LevelProgress.class}, version = Constants.DATABASE_VERSION)
public abstract class ChessDatabase extends RoomDatabase {

    public abstract LevelDao levelDao();

    private static volatile ChessDatabase INSTANCE;

    public static ChessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChessDatabase.class, Constants.DATABASE_NAME) // Usa la costante per il nome
                            // Fondamentale: cancella i vecchi dati incompatibili
                            // e ricrea le tabelle con la nuova struttura
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}