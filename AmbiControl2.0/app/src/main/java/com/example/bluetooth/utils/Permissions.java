package com.example.bluetooth.utils;

import android.Manifest;

public class Permissions {
    private static Permissions instance;
    private String[] permissions;

    private Permissions()
    {
        permissions = new String[9];
        configure();
    }

    public static synchronized Permissions getInstance() {
        if (instance == null) {
            instance = new Permissions();
        }
        return instance;
    }


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
