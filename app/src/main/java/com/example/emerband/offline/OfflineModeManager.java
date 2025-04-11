package com.example.emerband.offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.emerband.CyberCellHandler;
import com.example.emerband.EmergencyHandler;
import com.example.emerband.data.AppDatabase;
import com.example.emerband.data.OfflineEvent;
import com.example.emerband.data.OfflineEventDao;
import com.example.emerband.utils.ConnectivityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manager class for handling offline mode functionality.
 * Stores emergency events when offline and processes them when connectivity returns.
 */
public class OfflineModeManager {

    private static final String TAG = "OfflineModeManager";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static OfflineModeManager instance;
    private final Context context;
    private final Executor executor;
    private BroadcastReceiver connectivityReceiver;
    private boolean isReceiverRegistered = false;

    // Private constructor (Singleton pattern)
    private OfflineModeManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.connectivityReceiver = createConnectivityReceiver();
    }

    /**
     * Get the singleton instance of OfflineModeManager
     */
    public static synchronized OfflineModeManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineModeManager(context);
        }
        return instance;
    }

    /**
     * Register the connectivity change receiver to listen for network changes
     */
    public void registerConnectivityReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(connectivityReceiver, filter);
            isReceiverRegistered = true;
            Log.d(TAG, "Connectivity receiver registered");
        }
    }

    /**
     * Unregister the connectivity change receiver
     */
    public void unregisterConnectivityReceiver() {
        if (isReceiverRegistered) {
            context.unregisterReceiver(connectivityReceiver);
            isReceiverRegistered = false;
            Log.d(TAG, "Connectivity receiver unregistered");
        }
    }

    /**
     * Handle an emergency event (button 'E')
     * @param additionalData Additional context for the emergency
     */
    public void handleEmergencyEvent(String additionalData) {
        // Check connectivity first
        if (ConnectivityUtils.isInternetAvailable(context)) {
            // Online - handle normally
            Log.d(TAG, "Online emergency event - handling normally");
            handleOnlineEmergency();
        } else {
            // Offline - store for later processing
            Log.d(TAG, "Offline emergency event - storing for later");
            storeOfflineEmergencyEvent(additionalData);
            
            // Show feedback to the user
            showToast("Emergency alert stored offline. Will be sent when connectivity returns.");
        }
    }

    /**
     * Handle a cyber cell event (button 'C')
     */
    public void handleCyberCellEvent() {
        // Check connectivity first
        if (ConnectivityUtils.isInternetAvailable(context)) {
            // Online - handle normally
            Log.d(TAG, "Online cyber cell event - handling normally");
            handleOnlineCyberCellAlert();
        } else {
            // Offline - store for later processing
            Log.d(TAG, "Offline cyber cell event - storing for later");
            storeOfflineCyberCellEvent();
            
            // Show feedback to the user
            showToast("Cyber alert stored offline. Will be sent when connectivity returns.");
        }
    }

    /**
     * Store an emergency event for offline processing
     */
    private void storeOfflineEmergencyEvent(String additionalData) {
        executor.execute(() -> {
            try {
                // Get last known location (may be null)
                Location lastLocation = ConnectivityUtils.getLastKnownLocation(context);
                String latitude = null;
                String longitude = null;
                
                if (lastLocation != null) {
                    latitude = String.valueOf(lastLocation.getLatitude());
                    longitude = String.valueOf(lastLocation.getLongitude());
                }
                
                // Create and store the offline event
                OfflineEvent event = new OfflineEvent(
                        OfflineEvent.TYPE_EMERGENCY,
                        System.currentTimeMillis(),
                        latitude,
                        longitude,
                        additionalData
                );
                
                AppDatabase.getInstance(context).offlineEventDao().insert(event);
                Log.d(TAG, "Emergency event stored in offline database");
            } catch (Exception e) {
                Log.e(TAG, "Error storing offline emergency event", e);
            }
        });
    }

    /**
     * Store a cyber cell event for offline processing
     */
    private void storeOfflineCyberCellEvent() {
        executor.execute(() -> {
            try {
                // Create and store the offline event
                OfflineEvent event = new OfflineEvent(
                        OfflineEvent.TYPE_CYBER,
                        System.currentTimeMillis(),
                        null,  // No location needed for cyber alerts
                        null,
                        null
                );
                
                AppDatabase.getInstance(context).offlineEventDao().insert(event);
                Log.d(TAG, "Cyber cell event stored in offline database");
            } catch (Exception e) {
                Log.e(TAG, "Error storing offline cyber cell event", e);
            }
        });
    }

    /**
     * Process all stored offline events when connectivity returns
     */
    private void processOfflineEvents() {
        executor.execute(() -> {
            try {
                OfflineEventDao dao = AppDatabase.getInstance(context).offlineEventDao();
                List<OfflineEvent> events = dao.getAllEvents();
                
                Log.d(TAG, "Processing " + events.size() + " offline events");
                
                for (OfflineEvent event : events) {
                    // Only process if retry attempts haven't exceeded max
                    if (event.getRetryAttempts() < MAX_RETRY_ATTEMPTS) {
                        boolean processed = false;
                        
                        // Process based on event type
                        if (OfflineEvent.TYPE_EMERGENCY.equals(event.getEventType())) {
                            processed = processOfflineEmergencyEvent(event);
                        } else if (OfflineEvent.TYPE_CYBER.equals(event.getEventType())) {
                            processed = processOfflineCyberCellEvent(event);
                        }
                        
                        // If successfully processed, delete from database
                        if (processed) {
                            dao.delete(event);
                            Log.d(TAG, "Offline event processed and deleted: " + event.getId());
                        } else {
                            // Increment retry counter and update
                            event.incrementRetryAttempts();
                            dao.update(event);
                            Log.d(TAG, "Offline event processing failed, retry: " + 
                                  event.getRetryAttempts() + "/" + MAX_RETRY_ATTEMPTS);
                        }
                    } else {
                        // Too many retries, log and delete
                        Log.e(TAG, "Max retries exceeded for event: " + event.getId() + ", deleting");
                        dao.delete(event);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing offline events", e);
            }
        });
    }

    /**
     * Process a stored emergency event
     */
    private boolean processOfflineEmergencyEvent(OfflineEvent event) {
        try {
            // Format timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(event.getTimestamp()));
            
            // Prepare location string
            String locationStr;
            if (event.getLatitude() != null && event.getLongitude() != null) {
                locationStr = event.getLatitude() + "," + event.getLongitude();
            } else {
                locationStr = "Location unavailable at time of alert";
            }
            
            // Construct and send SMS
            String message = "🚨 DELAYED EMERGENCY ALERT 🚨\n" +
                    "Alert triggered at: " + timestamp + "\n" +
                    "Location: " + locationStr + "\n" +
                    "This alert was delayed due to connectivity issues.";
            
            // Use emergency handler with constructed GPS data
            boolean handled = EmergencyHandler.handleEmergencyWithGPS(context, locationStr);
            
            return handled;
        } catch (Exception e) {
            Log.e(TAG, "Error processing offline emergency event", e);
            return false;
        }
    }

    /**
     * Process a stored cyber cell event
     */
    private boolean processOfflineCyberCellEvent(OfflineEvent event) {
        try {
            // Format timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(event.getTimestamp()));
            
            // Add timestamp info to the cyber cell alert
            // Use CyberCellHandler
            boolean handled = CyberCellHandler.handleCyberCellAlert(context);
            
            return handled;
        } catch (Exception e) {
            Log.e(TAG, "Error processing offline cyber cell event", e);
            return false;
        }
    }

    /**
     * Create a BroadcastReceiver to listen for connectivity changes
     */
    private BroadcastReceiver createConnectivityReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    boolean isConnected = ConnectivityUtils.isInternetAvailable(context);
                    
                    Log.d(TAG, "Connectivity changed. Internet available: " + isConnected);
                    
                    if (isConnected) {
                        // Process stored events when connectivity returns
                        processOfflineEvents();
                    }
                }
            }
        };
    }
    
    /**
     * Handle online emergency - directly use EmergencyHandler
     */
    private void handleOnlineEmergency() {
        Location location = ConnectivityUtils.getLastKnownLocation(context);
        String locationStr = null;
        
        if (location != null) {
            locationStr = location.getLatitude() + "," + location.getLongitude();
        }
        
        // Use the EmergencyHandler to process the emergency with GPS data
        EmergencyHandler.handleEmergencyWithGPS(context, locationStr);
    }
    
    /**
     * Handle online cyber cell alert - directly use CyberCellHandler
     */
    private void handleOnlineCyberCellAlert() {
        // Use the CyberCellHandler to process the cyber cell alert
        CyberCellHandler.handleCyberCellAlert(context);
    }
    
    /**
     * Show a toast message on the UI thread
     */
    private void showToast(final String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
} 