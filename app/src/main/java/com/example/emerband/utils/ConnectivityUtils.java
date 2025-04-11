package com.example.emerband.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

/**
 * Utility class for checking device connectivity status.
 */
public class ConnectivityUtils {
    
    private static final String TAG = "ConnectivityUtils";
    
    /**
     * Check if the device has internet connectivity
     * @param context Application context
     * @return true if connected to internet, false otherwise
     */
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork());
            
            if (capabilities == null) {
                return false;
            }
            
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }
    }
    
    /**
     * Check if GPS is enabled on the device
     * @param context Application context
     * @return true if GPS is enabled, false otherwise
     */
    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) 
                context.getSystemService(Context.LOCATION_SERVICE);
        
        if (locationManager == null) {
            return false;
        }
        
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    
    /**
     * Get last known location from the device
     * @param context Application context
     * @return Location object or null if unavailable
     */
    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) 
                context.getSystemService(Context.LOCATION_SERVICE);
        
        if (locationManager == null) {
            return null;
        }
        
        try {
            // Try to get location from GPS provider first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            
            // If GPS location is not available, try network provider
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }
        
        return null;
    }
    
    /**
     * Check if any connectivity (GPS or internet) is available
     * @param context Application context
     * @return true if either GPS or internet is available, false otherwise
     */
    public static boolean hasAnyConnectivity(Context context) {
        return isGpsEnabled(context) || isInternetAvailable(context);
    }
    
    /**
     * Register for location updates
     * @param context Application context
     * @param listener LocationListener to receive updates
     */
    public static void requestLocationUpdates(Context context, LocationListener listener) {
        LocationManager locationManager = (LocationManager) 
                context.getSystemService(Context.LOCATION_SERVICE);
        
        if (locationManager == null) {
            return;
        }
        
        try {
            // Request location updates from both GPS and network providers
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000, // minimum time interval (ms)
                        0,    // minimum distance (m)
                        listener,
                        Looper.getMainLooper());
            }
            
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000, // minimum time interval (ms)
                        0,    // minimum distance (m)
                        listener,
                        Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }
    }
} 