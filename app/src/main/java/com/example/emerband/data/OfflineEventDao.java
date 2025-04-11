package com.example.emerband.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for OfflineEvent entities.
 * Provides methods to interact with the database.
 */
@Dao
public interface OfflineEventDao {
    
    /**
     * Insert a new offline event into the database
     */
    @Insert
    long insert(OfflineEvent event);
    
    /**
     * Update an existing offline event
     */
    @Update
    void update(OfflineEvent event);
    
    /**
     * Delete an offline event from the database
     */
    @Delete
    void delete(OfflineEvent event);
    
    /**
     * Get all offline events ordered by timestamp (oldest first)
     */
    @Query("SELECT * FROM offline_events ORDER BY timestamp ASC")
    List<OfflineEvent> getAllEvents();
    
    /**
     * Get all offline events of a specific type
     */
    @Query("SELECT * FROM offline_events WHERE eventType = :type ORDER BY timestamp ASC")
    List<OfflineEvent> getEventsByType(String type);
    
    /**
     * Get count of offline events
     */
    @Query("SELECT COUNT(*) FROM offline_events")
    int getEventCount();
    
    /**
     * Delete all events
     */
    @Query("DELETE FROM offline_events")
    void deleteAllEvents();
} 