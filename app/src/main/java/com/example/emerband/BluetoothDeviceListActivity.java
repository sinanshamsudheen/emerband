package com.example.emerband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothSocket;

public class BluetoothDeviceListActivity extends AppCompatActivity {

    private ListView deviceListView;
    private ArrayList<String> deviceList;
    private ArrayList<BluetoothDevice> devices; // Store BluetoothDevice objects
    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "DeviceListActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        deviceListView = findViewById(R.id.device_list);
        deviceList = new ArrayList<>();
        devices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get paired devices
        getPairedDevices();

        // Start discovery
        startDiscovery();

        // Set item click listener
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = devices.get(position);
            connectToDevice(selectedDevice);
        });
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device); // Add the BluetoothDevice object to the list
                deviceList.add(device.getName() + "\n" + device.getAddress());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);
    }

    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                // Create a BluetoothSocket
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect(); // This will block until the connection is established

                runOnUiThread(() -> Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show());

                // Manage the connection (input/output streams) here
                // For example, you can get the input and output streams:
                // InputStream inputStream = socket.getInputStream();
                // OutputStream outputStream = socket.getOutputStream();

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error connecting to device: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void startDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void stopDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(receiver);
    }

    // BroadcastReceiver to handle found devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !devices.contains(device)) {
                    devices.add(device);
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                    ((ArrayAdapter) deviceListView.getAdapter()).notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscovery();
    }
}
