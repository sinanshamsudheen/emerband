package com.example.emerband.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class for storing offline emergency events.
 * Used when connectivity is unavailable during an emergency.
 */
@Entity(tableName = "offline_events")
public class OfflineEvent {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String eventType; // "EMERGENCY" or "CYBER"
    private long timestamp;
    private String latitude;
    private String longitude;
    private String additionalData; // For any extra information
    private int retryAttempts; // Track retry attempts
    
    public OfflineEvent(String eventType, long timestamp, String latitude, String longitude, String additionalData) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.additionalData = additionalData;
        this.retryAttempts = 0;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLatitude() {
        return latitude;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public String getLongitude() {
        return longitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    public String getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
    
    public int getRetryAttempts() {
        return retryAttempts;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    public void incrementRetryAttempts() {
        this.retryAttempts++;
    }
    
    // Static constants for event types
    public static final String TYPE_EMERGENCY = "EMERGENCY";
    public static final String TYPE_CYBER = "CYBER";
} 