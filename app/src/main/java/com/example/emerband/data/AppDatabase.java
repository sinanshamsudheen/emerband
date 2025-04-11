package com.example.emerband.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Main database class for the application.
 * Provides a singleton instance of the database.
 */
@Database(entities = {OfflineEvent.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // DAOs
    public abstract OfflineEventDao offlineEventDao();
    
    /**
     * Get the database instance
     * @param context Application context
     * @return The singleton database instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "emerband_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 