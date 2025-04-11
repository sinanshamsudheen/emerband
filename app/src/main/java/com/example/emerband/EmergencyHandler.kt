package com.example.emerband

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class to handle emergency signals received from BLE devices
 * and process associated GPS data to send alerts and make emergency calls.
 */
object EmergencyHandler {

    private const val TAG = "EmergencyHandler"
    private const val PREFS_NAME = "EmerbandPrefs"
    private const val KEY_USER_NAME = "userName"

    // Emergency contact numbers (hardcoded for now)
    // TODO: Allow users to configure emergency contacts via UI
    private val PRIMARY_EMERGENCY_CONTACT = "+1234567890"
    private val SECONDARY_EMERGENCY_CONTACT = "+0987654321"

    /**
     * Handle emergency signal with GPS data from BLE device
     * 
     * @param context Application context
     * @param gpsData String containing GPS coordinates in format "latitude,longitude"
     * @return true if handling was successful, false otherwise
     */
    @JvmStatic
    fun handleEmergencyWithGPS(context: Context, gpsData: String?): Boolean {
        Log.d(TAG, "Handling emergency with GPS data: $gpsData")
        
        // Extract GPS coordinates
        val coordinates = extractGPSCoordinates(gpsData)
        if (coordinates == null) {
            Log.e(TAG, "Failed to extract valid GPS coordinates from data: $gpsData")
            showToast(context, "Emergency alert received but location data is missing or invalid!")
            // Continue with emergency response without GPS data
            return handleEmergencyWithoutGPS(context)
        }
        
        // Get user name from SharedPreferences
        val userName = getUserName(context)
        
        // Generate timestamp
        val timestamp = getCurrentTimestamp()
        
        // Prepare emergency message
        val message = constructEmergencyMessage(userName, coordinates.first, coordinates.second, timestamp)
        
        // Send SMS to emergency contacts
        val smsSent = sendEmergencySMS(context, message)
        
        // Initiate emergency call
        val callInitiated = initiateEmergencyCall(context)
        
        return smsSent || callInitiated
    }
    
    /**
     * Handle emergency without GPS data as fallback
     */
    private fun handleEmergencyWithoutGPS(context: Context): Boolean {
        val userName = getUserName(context)
        val timestamp = getCurrentTimestamp()
        
        val message = """
            🚨 Emergency Alert 🚨
            Name: $userName
            Location: Unknown
            Time: $timestamp
            
            This person needs immediate assistance!
        """.trimIndent()
        
        val smsSent = sendEmergencySMS(context, message)
        val callInitiated = initiateEmergencyCall(context)
        
        return smsSent || callInitiated
    }
    
    /**
     * Extract GPS coordinates from the input string
     * 
     * @param gpsData String in format "latitude,longitude"
     * @return Pair of Double values representing latitude and longitude, or null if invalid
     */
    private fun extractGPSCoordinates(gpsData: String?): Pair<Double, Double>? {
        if (gpsData.isNullOrBlank()) {
            return null
        }
        
        try {
            val parts = gpsData.split(",")
            if (parts.size != 2) {
                return null
            }
            
            val latitude = parts[0].trim().toDoubleOrNull()
            val longitude = parts[1].trim().toDoubleOrNull()
            
            if (latitude == null || longitude == null) {
                return null
            }
            
            // Basic validation of coordinates
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return null
            }
            
            return Pair(latitude, longitude)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing GPS data", e)
            return null
        }
    }
    
    /**
     * Get user name from SharedPreferences
     */
    private fun getUserName(context: Context): String {
        val settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getString(KEY_USER_NAME, "Unknown User") ?: "Unknown User"
    }
    
    /**
     * Get current timestamp in readable format
     */
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * Construct emergency SMS message
     */
    private fun constructEmergencyMessage(
        userName: String,
        latitude: Double,
        longitude: Double,
        timestamp: String
    ): String {
        return """
            🚨 Emergency Alert 🚨
            Name: $userName
            Location: Latitude $latitude, Longitude $longitude
            Time: $timestamp
            
            This person needs immediate assistance!
            
            Map link: https://maps.google.com/maps?q=$latitude,$longitude
        """.trimIndent()
        
        // TODO: Support reverse geocoding to convert coordinates into a human-readable address
    }
    
    /**
     * Send emergency SMS to contacts
     */
    private fun sendEmergencySMS(context: Context, message: String): Boolean {
        // Check permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != 
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS permission not granted")
            showToast(context, "Cannot send emergency SMS: Permission not granted")
            return false
        }
        
        try {
            val smsManager = SmsManager.getDefault()
            
            // Split message if it's too long
            val parts = smsManager.divideMessage(message)
            
            // Send to primary contact
            smsManager.sendMultipartTextMessage(
                PRIMARY_EMERGENCY_CONTACT,
                null,
                parts,
                null,
                null
            )
            
            // Send to secondary contact
            smsManager.sendMultipartTextMessage(
                SECONDARY_EMERGENCY_CONTACT,
                null,
                parts,
                null,
                null
            )
            
            Log.d(TAG, "Emergency SMS sent successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send emergency SMS", e)
            showToast(context, "Failed to send emergency SMS: ${e.message}")
            return false
        }
    }
    
    /**
     * Initiate emergency call
     */
    private fun initiateEmergencyCall(context: Context): Boolean {
        // Check call phone permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != 
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Call phone permission not granted")
            showToast(context, "Cannot make emergency call: Permission not granted")
            return false
        }
        
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$PRIMARY_EMERGENCY_CONTACT")
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(callIntent)
            
            Log.d(TAG, "Emergency call initiated")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate emergency call", e)
            showToast(context, "Failed to initiate emergency call: ${e.message}")
            return false
        }
    }
    
    /**
     * Show toast message
     */
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
} 