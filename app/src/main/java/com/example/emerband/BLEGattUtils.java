package com.example.emerband;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility class for handling BLE GATT services and characteristics
 */
public class BLEGattUtils {
    
    private static final String TAG = "BLEGattUtils";
    
    // Example UUIDs for GPS service and characteristic
    // In a real application, these would match the Arduino device implementation
    public static final UUID GPS_SERVICE_UUID = UUID.fromString("00001819-0000-1000-8000-00805f9b34fb");
    public static final UUID GPS_CHARACTERISTIC_UUID = UUID.fromString("00002A67-0000-1000-8000-00805f9b34fb");
    
    /**
     * Reads GPS data from the specified characteristic if available
     * 
     * @param gatt The BluetoothGatt connection
     * @return String containing GPS data in format "latitude,longitude" or null if not available
     */
    public static String readGPSData(BluetoothGatt gatt) {
        if (gatt == null) {
            Log.e(TAG, "GATT connection is null");
            return null;
        }
        
        // Get the GPS service
        BluetoothGattService gpsService = gatt.getService(GPS_SERVICE_UUID);
        if (gpsService == null) {
            Log.e(TAG, "GPS service not found");
            return null;
        }
        
        // Get the GPS characteristic
        BluetoothGattCharacteristic gpsCharacteristic = 
                gpsService.getCharacteristic(GPS_CHARACTERISTIC_UUID);
        if (gpsCharacteristic == null) {
            Log.e(TAG, "GPS characteristic not found");
            return null;
        }
        
        // Read the value
        byte[] data = gpsCharacteristic.getValue();
        if (data == null || data.length == 0) {
            Log.e(TAG, "GPS data is empty");
            return null;
        }
        
        // Convert bytes to string
        String gpsString = new String(data, StandardCharsets.UTF_8);
        Log.d(TAG, "Read GPS data: " + gpsString);
        
        return gpsString;
    }
} 