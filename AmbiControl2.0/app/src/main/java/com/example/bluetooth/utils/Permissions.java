package com.example.bluetooth.utils;

import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Permissions {
    private static Permissions instance;
    private String[] permissions;

    private Permissions()
    {
        permissions = new String[9];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
        {
            configure();
        }
    }

    public static synchronized Permissions getInstance() {
        if (instance == null) 
        {
            instance = new Permissions();
        }
        return instance;
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void configure()
    {
        permissions =
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.POST_NOTIFICATIONS,
                };
    }

    public String[] getValue()
    {
        return permissions;
    }
}
