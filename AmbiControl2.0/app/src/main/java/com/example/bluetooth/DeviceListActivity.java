package com.example.bluetooth;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetooth.utils.Constants;


public class DeviceListActivity extends Activity
{
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private int positionListBluethoot;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_paired_devices);
        configureComponents();
    }

    private void configureComponents()
    {
        mListView = findViewById(R.id.lv_paired);
        mDeviceList = Objects.requireNonNull(getIntent().getExtras()).getParcelableArrayList("device.list");
        mAdapter = new DeviceListAdapter(this);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(listenerBotonEmparejar);
        mListView.setAdapter(mAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, filter);
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
    }


    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device)
    {
        try
        {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device)
    {
        try
        {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private DeviceListAdapter.OnPairButtonClickListener listenerBotonEmparejar = new DeviceListAdapter.OnPairButtonClickListener()
    {
        @SuppressLint("MissingPermission")
        @Override
        public void onPairButtonClick(int position)
        {

            BluetoothDevice device = mDeviceList.get(position);

            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                unpairDevice(device);
            } else
            {

                showToast(Constants.PAIRING);
                positionListBluethoot = position;
                pairDevice(device);

            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                {
                    showToast(Constants.PAIRING);
                    BluetoothDevice device = (BluetoothDevice) mAdapter.getItem(positionListBluethoot);

                    String bluetoothAddress = device.getAddress();
                    Intent i = new Intent(DeviceListActivity.this, CommunicationActivity.class);
                    i.putExtra(Constants.BT_ADDRESS, bluetoothAddress);

                    startActivity(i);

                }
                else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED)
                {
                    showToast(Constants.NOT_PAIRED);
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };
}


