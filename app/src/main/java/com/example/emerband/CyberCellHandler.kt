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

/**
 * Utility class that handles reporting digital threats to a cyber cell
 * by initiating calls and sending alert SMS messages.
 */
object CyberCellHandler {

    private const val TAG = "CyberCellHandler"
    private const val PREFS_NAME = "EmerbandPrefs"
    
    // Keys for SharedPreferences
    private const val KEY_CYBER_CELL_NUMBER = "cyberCellNumber"
    private const val KEY_CYBER_ALERT_MESSAGE = "cyberAlertMessage"
    
    // Default values
    private const val DEFAULT_CYBER_CELL_NUMBER = "1122334455"
    private const val DEFAULT_CYBER_ALERT_MESSAGE = "I'm under digital threat. Please help."
    
    /**
     * Handles the cyber cell alert by initiating a call and sending an SMS
     * 
     * @param context Application context
     * @return true if at least one action (call or SMS) was successful
     */
    @JvmStatic
    fun handleCyberCellAlert(context: Context): Boolean {
        Log.d(TAG, "Handling cyber cell alert")
        
        // Attempt both actions, even if one fails
        val callInitiated = initiateCall(context)
        val smsSent = sendAlertSms(context)
        
        // Return true if at least one action succeeded
        return callInitiated || smsSent
    }
    
    /**
     * Get the cyber cell phone number from SharedPreferences or use default
     */
    @JvmStatic
    fun getCyberCellNumber(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CYBER_CELL_NUMBER, DEFAULT_CYBER_CELL_NUMBER) 
            ?: DEFAULT_CYBER_CELL_NUMBER
    }
    
    /**
     * Get the cyber alert message from SharedPreferences or use default
     */
    @JvmStatic
    fun getCyberAlertMessage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CYBER_ALERT_MESSAGE, DEFAULT_CYBER_ALERT_MESSAGE)
            ?: DEFAULT_CYBER_ALERT_MESSAGE
    }
    
    /**
     * Save the cyber cell phone number to SharedPreferences
     */
    @JvmStatic
    fun saveCyberCellNumber(context: Context, number: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CYBER_CELL_NUMBER, number).apply()
    }
    
    /**
     * Save the cyber alert message to SharedPreferences
     */
    @JvmStatic
    fun saveCyberAlertMessage(context: Context, message: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CYBER_ALERT_MESSAGE, message).apply()
    }
    
    /**
     * Initiates a call to the cyber cell helpline
     */
    private fun initiateCall(context: Context): Boolean {
        // Check call phone permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != 
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Call phone permission not granted")
            showToast(context, "Cannot make cyber cell call: Permission not granted")
            return false
        }
        
        try {
            val cyberCellNumber = getCyberCellNumber(context)
            
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$cyberCellNumber")
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(callIntent)
            
            Log.d(TAG, "Cyber cell call initiated to $cyberCellNumber")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate cyber cell call", e)
            showToast(context, "Failed to call cyber cell: ${e.message}")
            return false
        }
    }
    
    /**
     * Sends an alert SMS to the cyber cell helpline
     */
    private fun sendAlertSms(context: Context): Boolean {
        // Check SMS permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != 
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS permission not granted")
            showToast(context, "Cannot send cyber cell SMS: Permission not granted")
            return false
        }
        
        try {
            val cyberCellNumber = getCyberCellNumber(context)
            val alertMessage = getCyberAlertMessage(context)
            
            val smsManager = SmsManager.getDefault()
            
            // Split message if it's too long
            val parts = smsManager.divideMessage(alertMessage)
            
            // Send the message
            smsManager.sendMultipartTextMessage(
                cyberCellNumber,
                null,
                parts,
                null,
                null
            )
            
            Log.d(TAG, "Cyber cell SMS sent to $cyberCellNumber")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send cyber cell SMS", e)
            showToast(context, "Failed to send cyber cell SMS: ${e.message}")
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