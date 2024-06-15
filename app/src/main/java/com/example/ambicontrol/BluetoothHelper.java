package com.example.ambicontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private BluetoothSocket bluetoothSocket;
    private static final String TAG = "BluetoothHelper";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID for SPP

    public BluetoothHelper(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void getConnectedDevice(BluetoothDeviceCallback callback) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionRequired();
            return;
        }

        if (bluetoothAdapter == null) {
            callback.onDeviceFound(null);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (isConnected(device)) {
                callback.onDeviceFound(device);
                return;
            }
        }

        callback.onDeviceFound(null);
    }

    public interface BluetoothDeviceCallback {
        void onDeviceFound(BluetoothDevice device);
        void onPermissionRequired();
    }

    private boolean isConnected(BluetoothDevice device) {
        try {
            // Usar reflexión para acceder a un método oculto en la API de BluetoothDevice
            java.lang.reflect.Method method = device.getClass().getMethod("isConnected");
            return (boolean) method.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendData(String data) {
        Log.v("hols", "data enviada bt helper");
        if (bluetoothSocket == null) {
            return false;
        }
        Log.v("hols", "data enviada bt helper2");
        try {
            byte[] msgBuffer = data.getBytes();
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(msgBuffer);
            return true;
        } catch (IOException e) {
            Log.v("ASDASDASDASD", "Error while sending data");
            return false;
        }
    }




    public boolean connect(BluetoothDevice device) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }


        try {
            Log.d(TAG, "Creating socket with UUID: " + MY_UUID.toString());
            bluetoothAdapter.cancelDiscovery();
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            //bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            Log.d(TAG, "Connecting to socket...");

            bluetoothSocket.connect();
            Log.d(TAG, "Connection successful");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error while connecting", e);
            closeSocket();
            try {
                Log.d(TAG, "Trying fallback...");
                bluetoothSocket = createRfcommSocket(device);
                bluetoothSocket.connect();
                Log.d(TAG, "Fallback connection successful");
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Fallback connection failed", e2);
                closeSocket();
                return false;
            }
        }
    }

    private void closeSocket() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Socket closed successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error while closing socket", e);
            }
        }
    }
    public void closeConnection() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.v("ASDASDASDASD", "Error while closing connection");
        }
    }

    /*private BluetoothSocket createRfcommSocket(BluetoothDevice device) throws Exception {
        Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
        return (BluetoothSocket) m.invoke(device, 1);
    }*/

    private BluetoothSocket createRfcommSocket(BluetoothDevice device) throws IOException {
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
            return (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e) {
            Log.e(TAG, "Could not create fallback socket", e);
            throw new IOException(e);
        }
    }
}
