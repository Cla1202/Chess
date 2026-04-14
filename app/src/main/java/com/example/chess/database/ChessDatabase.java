package com.example.chess.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Dichiariamo quali tabelle ci sono e la versione del database
@Database(entities = {LevelProgress.class}, version = 1)
public abstract class ChessDatabase extends RoomDatabase {

    public abstract LevelDao levelDao();

    // Creiamo un'istanza "Singleton" (significa che ci sarà sempre e solo un database aperto per evitare crash)
    private static volatile ChessDatabase INSTANCE;

    public static ChessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChessDatabase.class, "chess_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}