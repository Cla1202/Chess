package com.example.chess.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.chess.util.Constants; // Ensure the import is correct for your project

@Database(entities = {LevelProgress.class}, version = Constants.DATABASE_VERSION, exportSchema = false)
public abstract class ChessDatabase extends RoomDatabase {

    public abstract LevelDao levelDao();

    private static volatile ChessDatabase INSTANCE;

    public static ChessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChessDatabase.class, Constants.DATABASE_NAME) // Use the constant for the name
                            // Essential: delete old incompatible data
                            // and recreate the tables with the new structure
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}