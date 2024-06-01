package com.example.ambicontrol;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

import kotlinx.coroutines.AwaitKt;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView acelerometro;

    private SensorManager mSensorManager;

    DecimalFormat dosDecimales = new DecimalFormat("###.###");



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Defino los TXT para representar los datos de los sensores
        acelerometro = (TextView) findViewById(R.id.acelerometro);

        // Accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    protected void Ini_Sensores() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores() {

        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
    }

    @Override
    protected void onStop() {

        Parar_Sensores();

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Ini_Sensores();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";

        // Cada sensor puede lanzar un thread que pase por aqui
        // Para asegurarnos ante los accesos simult�neos sincronizamos esto

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());
            txt += "Acelerometro:\n";
            txt += "x: " + dosDecimales.format(event.values[0]) + " m/seg2 \n";
            txt += "y: " + dosDecimales.format(event.values[1]) + " m/seg2 \n";
            txt += "z: " + dosDecimales.format(event.values[2]) + " m/seg2 \n";
            acelerometro.setText(txt);
            if ((event.values[0] > 12) || (event.values[1] > 12) || (event.values[2] > 12)) {
                acelerometro.setText("Vibracion Detectada");
            }
        }
    }
}