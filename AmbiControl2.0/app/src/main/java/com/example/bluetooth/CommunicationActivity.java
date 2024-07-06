package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import androidx.annotation.NonNull;

import com.example.bluetooth.utils.Constants;
import com.example.bluetooth.utils.StateMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class CommunicationActivity extends Activity implements SensorEventListener
{

    TextView txtTemperature;
    TextView txtCurrentState;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private final StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString(Constants.MY_UUID);

    private static String address = null;

    private SensorManager mSensorManager;
    private final static float ACC = Constants.THRESHOLD;

    private boolean fanOn;

    private static final String CHANNEL_ID = Constants.CHANNEL_ID;
    private static final int NOTIFICATION_ID = 1;

    private StateMessage sm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);

        txtTemperature = findViewById(R.id.idValorSensorTemperatura);
        txtCurrentState = findViewById(R.id.idEstadoActual);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothIn = handlerMainThread();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        address = extras.getString(Constants.BT_ADDRESS);

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e)
        {
            showToast(Constants.SOCKET_CREATION_FAILED);
        }
        try
        {
            btSocket.connect();
        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                e2.printStackTrace();
            }
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        mConnectedThread.write(Constants.TEXT_TEST_CONNECTION);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
        try
        {
            btSocket.close();
        } catch (IOException e2)
        {
            e2.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private Handler handlerMainThread()
    {
        return new Handler(Looper.getMainLooper())
        {
            public void handleMessage(@NonNull android.os.Message msg)
            {

                sm = StateMessage.getInstance();

                if (msg.what == handlerState)
                {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage).append("\r\n");
                    int endOfLineIndex = recDataString.indexOf("\r\n");
                    if (endOfLineIndex > 0)
                    {
                        String[] parts = readMessage.split("\\|");

                        int numCurrentState = Integer.parseInt(parts[0]);
                        int valSensTemp = Integer.parseInt(parts[parts.length-1]);

                        String valSensTempStr = valSensTemp + " °C";

                        txtTemperature.setText(valSensTempStr);
                        txtCurrentState.setText(sm.getValue(numCurrentState));

                        if (numCurrentState == Constants.CODE_ILUMINANDO_Y_VENTILANDO)
                        {
                            sendNotification();
                        }

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    private void sendNotification()
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = Constants.NOTIFICATION_NAME;
            String description = Constants.NOTIFICATION_DESC;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pixelcut_export)
                .setContentTitle(Constants.HIGH_GAS_ALERT_NOTIFICATION)
                .setContentText(Constants.HIGH_GAS_DETECTED_NOTIFICATION)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
            {

                if (!fanOn)
                {
                    turnOn();
                    fanOn = true;
                } else
                {
                    turnOff();
                    fanOn = false;
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }


    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {

                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            while (true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e)
                {
                    break;
                }
            }
        }


        public void write(String input)
        {
            byte[] msgBuffer = input.getBytes();
            try
            {
                mmOutStream.write(msgBuffer);
            } catch (IOException e)
            {
                showToast(Constants.FAILED_CONNECTION);
                finish();
            }
        }
    }

    private void turnOn()
    {
        mConnectedThread.write(Constants.ENCENDER_VENTILADOR_BT);
        showToast(Constants.FAN_TURN_ON_VIA_SHAKE);
    }

    private void turnOff()
    {
        mConnectedThread.write(Constants.APAGAR_VENTILADOR_BT);
        showToast(Constants.FAN_TURN_OFF_VIA_SHAKE);
    }

}
