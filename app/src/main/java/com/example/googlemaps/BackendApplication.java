package com.example.googlemaps;

import android.app.Application;

import com.backendless.Backendless;

public class BackendApplication extends Application {
    public static final String APPLICATION_ID = "DB8DC9D2-C537-6953-FFC6-FC01B6F26100";
    public static final String API_KEY ="2189A186-F49E-426D-B0E6-F3CC7CB0C80F";
    public static final String SERVER_URL = "https:api.backendless.com";
    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(),APPLICATION_ID,API_KEY);
    }
}
