package com.example.chess.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// FIX: Versione aggiornata a 3 per supportare la nuova tabella con userId
@Database(entities = {LevelProgress.class}, version = 3)
public abstract class ChessDatabase extends RoomDatabase {

    public abstract LevelDao levelDao();

    private static volatile ChessDatabase INSTANCE;

    public static ChessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChessDatabase.class, "chess_database")
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