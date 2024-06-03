package com.example.ambicontrol;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private TextView sensorData;
    private SensorManager mSensorManager;
    private final DecimalFormat dosDecimales = new DecimalFormat("###.###");
    private boolean estaSensando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sensorData = findViewById(R.id.sensor_data);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Button sensarButton = findViewById(R.id.btnSensar);
        sensarButton.setOnClickListener(v -> {
            if (!estaSensando) {
                empezarSensar();
                estaSensando = true;
                sensorData.setVisibility(View.VISIBLE);
            } else {
                pararSensar();
                estaSensando = false;
            }
        });
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
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se usa en este ejemplo
    }
}
