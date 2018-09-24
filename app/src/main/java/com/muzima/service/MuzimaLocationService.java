package com.muzima.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.List;

public class MuzimaLocationService {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    private Boolean isFineGPSLocationAccessGranted = false;
    private Activity activity;


    @SuppressLint("MissingPermission")
    public MuzimaLocationService(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION);

        locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e(getClass().getSimpleName(), "GPS location enabled");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.e(getClass().getSimpleName(), "GPS location enabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e(getClass().getSimpleName(), "GPS location disabled");
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000,
                10, locationListener);


    }

    @SuppressWarnings("MissingPermission")
    public Location getLastKnownGPS() {
        Location location = null;
        location = getBestGPSLocation();
        Log.e(getClass().getSimpleName(),"Location " +location.toString());
        return location;
    }

    @SuppressLint("MissingPermission")
    private Location getBestGPSLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
             Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("MissingPermission")
    public String getHardGPSData() {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return location.toString();
    }

//    protected void onResume(){ TODO apply on resume to calling activity
//        super.onResume();
//        isLocationEnabled();
//    }

    private void isLocationEnabled() {

        Boolean isEnabled = false;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.context);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Confirm Location");
            alertDialog.setMessage("Your Location is enabled, please enjoy");
            alertDialog.setNegativeButton("Back to interface", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }


}
