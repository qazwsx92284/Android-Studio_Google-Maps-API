package com.example.googlemaps;

import android.app.Application;

import com.backendless.Backendless;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.setUrl("https://api.backendless.com");
        Backendless.initApp(getApplicationContext(),
                "DB8DC9D2-C537-6953-FFC6-FC01B6F26100",
                "DB8DC9D2-C537-6953-FFC6-FC01B6F26100");
    }
}
