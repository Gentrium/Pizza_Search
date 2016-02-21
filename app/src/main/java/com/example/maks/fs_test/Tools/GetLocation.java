package com.example.maks.fs_test.Tools;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.maks.fs_test.MainAppActivity;

public class GetLocation {

    private volatile boolean writeToDb;
    private Activity context;
    LocationManager locationManager;
    private volatile boolean gotCoordinates;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {


            if (writeToDb) {
                ChangeLocationListener listener = (MainAppActivity)context;
                listener.locationChanged(location.getLatitude(),location.getLongitude());
                gotCoordinates = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public GetLocation(LocationManager locationManager, Activity context) {
        this.locationManager = locationManager;
        this.context = context;
    }

    public void start() {
        writeToDb = true;
        gotCoordinates = false;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Don't have permissions, sorry", Toast.LENGTH_SHORT)
                    .show();
            ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, locationListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writeToDb = false;
                if(!gotCoordinates){
                    (context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Didn't find GPS coordinates", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        }).start();
    }
}