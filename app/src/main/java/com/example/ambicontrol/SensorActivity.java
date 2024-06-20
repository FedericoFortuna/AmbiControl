package com.example.ambicontrol;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private final static float ACC = 30;
    private MediaPlayer mplayer;

    private TextView sensorData;
    private SensorManager mSensorManager;
    private final DecimalFormat dosDecimales = new DecimalFormat("###.###");
    private boolean estaSensando = false;

    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 1;

    public BluetoothDevice myDevice;

    private BluetoothHelper bluetoothHelper;
    private BluetoothDevice connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtener la instancia del Singleton
        bluetoothHelper = BluetoothHelper.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Log.v("hols","hola");
        sensorData = findViewById(R.id.sensor_data);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Button sensarButton = findViewById(R.id.btnSensar);
//        sensarButton.setOnClickListener(v -> {
//            if (!estaSensando) {
//                empezarSensar();
//                estaSensando = true;
//                sensorData.setVisibility(View.VISIBLE);
//            } else {
//                pararSensar();
//                estaSensando = false;
//            }
//        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_BLUETOOTH_PERMISSIONS);
            } else {
                showConnectedDevice();
            }
        } else {
            showConnectedDevice();
        }


    }
    private void empezarSensar() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void pararSensar() {
        mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                txt += "Acelerómetro:\n";
                txt += "x: " + dosDecimales.format(event.values[0]) + " m/s² \n";
                txt += "y: " + dosDecimales.format(event.values[1]) + " m/s² \n";
                txt += "z: " + dosDecimales.format(event.values[2]) + " m/s² \n";
                sensorData.setText(txt);
            }
        }
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
            {
                Log.i("sensor", "running");
                sendData();
                //mplayer.start();
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se usa en este ejemplo
    }



    private void showConnectedDevice() {
        bluetoothHelper.getConnectedDevice(new BluetoothHelper.BluetoothDeviceCallback() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                connectedDevice = device;
                if (device != null) {
                    String deviceInfo = "Connected to: - " + device.getAddress();
                    Log.v("hols", deviceInfo);
                    if (bluetoothHelper.connect(device)) {
                        Log.v("hols", "Connected to device");
                    } else {
                        Log.v("hols", "Failed to connect");
                    }
                } else {
                    Log.v("hols", "No active Bluetooth connection found");
                }
            }

            @Override
            public void onPermissionRequired() {
                ActivityCompat.requestPermissions(SensorActivity.this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_BLUETOOTH_PERMISSIONS);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showConnectedDevice();
            } else {
                Log.v("hols", "no hay permisos");
            }
        }
    }

    private void sendData() {
        String data = "exquismi";
        if (bluetoothHelper.sendData(data)) {
            Log.v("hols", "data enviada");
        } else {
            Log.v("hols", "pincho mandando data");
        }
    }
}
