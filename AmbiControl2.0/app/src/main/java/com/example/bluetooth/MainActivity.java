package com.example.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bluetooth.utils.Constants;
import com.example.bluetooth.utils.Permissions;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends Activity
{
    private TextView txtState;
    private Button btnActivate;
    private Button btnPair;
    private ProgressDialog mProgressDlg;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;

    public static final int MULTIPLE_PERMISSIONS = 10;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent)
        {

            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON)
                {
                    showToast(Constants.ACTIVATE);

                    showEnabled();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                mDeviceList = new ArrayList<>();

                mProgressDlg.show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                mProgressDlg.dismiss();

                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

                startActivity(newIntent);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);
                showToast(Constants.FOUNDED_DEVICE + device.getName());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureComponents();
        Permissions permissions = Permissions.getInstance();
        if (checkPermissions(permissions.getValue()))
        {
            enableComponent();
        }
    }

    private void configureComponents()
    {
        txtState = findViewById(R.id.txtEstado);
        btnActivate = findViewById(R.id.btnActivar);
        btnPair = findViewById(R.id.btnEmparejar);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    protected void enableComponent()
    {
        if (mBluetoothAdapter == null)
        {
            showUnsupported();
        } 
        else
        {
            btnPair.setOnClickListener(btnEmparejarListener);
            btnActivate.setOnClickListener(btnActivarListener);

            if (mBluetoothAdapter.isEnabled())
            {
                showEnabled();
            } else
            {
                showDisabled();
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showEnabled()
    {
        txtState.setText(Constants.ACTIVATED_BT);
        txtState.setTextColor(Color.BLUE);

        btnActivate.setText(Constants.DEACTIVATE);
        btnActivate.setEnabled(true);

        btnPair.setEnabled(true);
    }

    private void showDisabled()
    {
        txtState.setText(Constants.DEACTIVATED_BT);
        txtState.setTextColor(Color.RED);

        btnActivate.setText(Constants.ACTIVATE);
        btnActivate.setEnabled(true);

        btnPair.setEnabled(false);
    }

    private void showUnsupported()
    {
        txtState.setText(Constants.BT_NOT_SUPPORTED);

        btnActivate.setText(Constants.ACTIVATE);
        btnActivate.setEnabled(false);

        btnPair.setEnabled(false);
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private final View.OnClickListener btnEmparejarListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            @SuppressLint("MissingPermission")
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.isEmpty())
            {
                showToast(Constants.NOT_PAIRED_DEVICE_FOUNDED);
            } else
            {
                ArrayList<BluetoothDevice> list = new ArrayList<>(pairedDevices);

                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

                intent.putParcelableArrayListExtra("device.list", list);

                startActivity(intent);
            }
        }
    };


    private final View.OnClickListener btnActivarListener = new View.OnClickListener()
    {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v)
        {
            if (mBluetoothAdapter.isEnabled())
            {
                mBluetoothAdapter.disable();

                showDisabled();
            } else
            {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        }
    };


    private boolean checkPermissions(String[] permissions)
    {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        boolean permissionsEnabled = true;

        for (String p : permissions)
        {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            permissionsEnabled = false;
        }
        return permissionsEnabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == MULTIPLE_PERMISSIONS)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                enableComponent();
            } else
            {
                Toast.makeText(this, "ATENCION: La aplicacion no funcionara " +
                        "correctamente debido a la falta de Permisos", Toast.LENGTH_LONG).show();
            }
        }
    }

}
