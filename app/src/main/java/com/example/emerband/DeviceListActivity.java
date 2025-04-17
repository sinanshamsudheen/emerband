package com.example.emerband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private ListView deviceListView;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;
    private static final String TAG = "DeviceListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        deviceListView = findViewById(R.id.device_list);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<>();

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get paired devices
        getPairedDevices();

        // Set up the list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getDeviceNames());
        deviceListView.setAdapter(adapter);

        // Handle device selection
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = devices.get(position);
            connectToDevice(selectedDevice);
        });
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            devices.addAll(pairedDevices);
        }
    }

    private ArrayList<String> getDeviceNames() {
        ArrayList<String> deviceNames = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            deviceNames.add(device.getName() + "\n" + device.getAddress());
        }
        return deviceNames;
    }

    private void connectToDevice(BluetoothDevice device) {
        BluetoothGatt bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Services discovered.");
                    // Interact with the services and characteristics here
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        });
    }
}
