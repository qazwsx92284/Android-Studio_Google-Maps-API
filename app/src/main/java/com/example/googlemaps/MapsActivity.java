package com.example.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    double lat = -29.119, lng = 26.3342; //set latitude and longitude
    ImageButton imBtn;
    boolean isExistingPosition = false;
    GeoPoint existingPoint;
    List<GeoPoint> list;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.action_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
        (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else {
            locationManager.requestLocationUpdates(provider,18000,50,this);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        imBtn = (ImageButton)findViewById(R.id.imBtn);
        String type = getIntent().getStringExtra("type"); //get user login type, like if it is susan, nelson..

        if(type.equals("family")){
            imBtn.setVisibility(View.GONE);

            BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
            geoQuery.addCategory("family");
            geoQuery.setIncludeMeta(true);

            Backendless.Geo.getPoints(geoQuery, new AsyncCallback<List<GeoPoint>>() {
                @Override
                public void handleResponse(List<GeoPoint> response) {

                    list = response;

                    if(list.size() !=0) { //if list is not empty
                        for(int i =0; i<list.size(); i++) {
                            LatLng positionMarker = new LatLng(list.get(i).getLatitude()
                            ,list.get(i).getLongitude());

                            mMap.addMarker(new MarkerOptions()
                            .position(positionMarker).snippet(list.get(i).getMetadata("updated").toString())
                            .title(list.get(i).getMetadata("name").toString()));
                        }
                    }
                    else{
                        imBtn.setVisibility(View.GONE);

                        BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
                        geoQuery.addCategory("family");
                        geoQuery.setIncludeMeta(true);

                        Backendless.Geo.getPoints(geoQuery, new AsyncCallback<List<GeoPoint>>() {
                            @Override
                            public void handleResponse(List<GeoPoint> response) {

                                list = response;

                                if(list.size() !=0) {
                                    for(int i = 0; i<list.size(); i++) {
                                        if(list.get(i).getMetadata("name").toString()
                                        .equals(getIntent().getStringExtra("type")));{
                                            isExistingPosition = true;
                                            existingPoint = list.get(i);
                                            break;
                                        }
                                    }
                                }

                                imBtn.setVisibility(View.VISIBLE);

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(MapsActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(MapsActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {

        }

        imBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Busy sending location.. ",
                        Toast.LENGTH_SHORT).show();
                if(!isExistingPosition) {
                    // if there is no geo point online(Backendless), then create and save current geo point.
                    List<String> categories = new ArrayList<>();
                    categories.add("family");

                    Map<String, Object> meta = new HashMap<String, Object>();
                    meta.put("name",getIntent().getStringExtra("type"));
                    meta.put("updated", new Date().toString());

                    Backendless.Geo.savePoint(lat, lng, categories, meta,
                            new AsyncCallback<GeoPoint>() {
                                @Override
                                public void handleResponse(GeoPoint response) {
                                    Toast.makeText(MapsActivity.this, "Successfully saved location", Toast.LENGTH_SHORT).show();
                                    isExistingPosition = true;
                                    existingPoint = response;
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(MapsActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else { //there is already geo point online
                        Backendless.Geo.removePoint(existingPoint, new AsyncCallback<Void>() {
                            @Override
                            public void handleResponse(Void response) {
                                List<String> categories = new ArrayList<>();
                                categories.add("family");

                                Map<String, Object> meta = new HashMap<String, Object>();
                                meta.put("name",getIntent().getStringExtra("type"));
                                meta.put("updated", new Date().toString());

                                Backendless.Geo.savePoint(lat, lng, categories, meta,
                                        new AsyncCallback<GeoPoint>() {
                                            @Override
                                            public void handleResponse(GeoPoint response) {
                                                Toast.makeText(MapsActivity.this, "Successfully saved location", Toast.LENGTH_SHORT).show();
                                                isExistingPosition = true;
                                                existingPoint = response;
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                Toast.makeText(MapsActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(MapsActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                    {Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},0);
        } else{
            Location location = locationManager.getLastKnownLocation(provider);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng position = new LatLng(lat,lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
        mMap.animateCamera(CameraUpdateFactory .zoomTo(10), 2000, null);
        mMap.addMarker(new MarkerOptions().icon(bitmapDescriptor(this,R.drawable.ic_baseline_location_on_24))
                .position(position).title("Marker in Sydney"));

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                    {Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},0);
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));



//        mMap.addMarker((new MarkerOptions().position(AIRPORT).title("Hotel")
//
//        .icon(bitmapDescriptor(this,R.drawable.ic_baseline_airplanemode_active_24))));
//
//        mMap.addMarker(new MarkerOptions().position(HOTEL).anchor(0.0f,0.01f).title("Hotel")
//                .icon(bitmapDescriptor(this,R.drawable.ic_baseline_local_hotel_24)));
//
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(HOTEL).tilt(30).zoom(15).bearing(0).build();
//
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }



    private BitmapDescriptor bitmapDescriptor(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        if(mMap !=null) {
            LatLng position = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().icon(bitmapDescriptor(this,R.drawable.ic_baseline_local_mall_24))
            .anchor(0.0f,1.0f).title("Your last known position").position(position));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}