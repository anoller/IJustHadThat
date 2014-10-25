package com.alecnoller.ijusthadthat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

public class CoordinateFinder implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private static final long FIVE_MINUTES = 5 * 60 * 1000;
	private boolean isPlayServicesAvailable = false;
	private LocationClient locationClient;
	private LocationManager locationManager;
	private Double latitude = null;
	private Double longitude = null;
	
	public CoordinateFinder(Context ctx) {
		// Create as backup
		locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS) {
			isPlayServicesAvailable = true;
			locationClient = new LocationClient(ctx, this, this);
			locationClient.connect();
		} else {
			//useDeviceLocation();
		}	
	}
	
	@Override
	public void onConnected(Bundle connectionHint) {
		/*if (locationClient.getLastLocation().getTime() < FIVE_MINUTES) {
			Location loc = locationClient.getLastLocation();
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
		}*/
		LocationRequest request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(5000);
		locationClient.requestLocationUpdates(request, this);
    }

	@Override
    public void onConnectionFailed(ConnectionResult result) {
    	//useDeviceLocation();
    }
	
	@Override
	public void onDisconnected() {
		//useDeviceLocation();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (location.getAccuracy() < 5) {
	        latitude = location.getLatitude();
	        longitude = location.getLongitude();
		}
	}
	
	public void updateLocation() {
		if (isPlayServicesAvailable) {
			Location loc = locationClient.getLastLocation();
	        latitude = loc.getLatitude();
	        longitude = loc.getLongitude();
		} 
	}

	/*
    public void useDeviceLocation() { 	
		// Get last known location, if it's not too old
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime() < FIVE_MINUTES) {
			Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
		}
		
		// Responds to device location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      latitude = location.getLatitude();
		      longitude = location.getLongitude();
		    }
	
		    public void onStatusChanged(String provider, int status, Bundle extras) {}
	
		    public void onProviderEnabled(String provider) {}
	
		    public void onProviderDisabled(String provider) {}
		  };
	
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000.00, 0, locationListener);
    } */
    
	public Double getCurrentLatitude() {
		return latitude;
	}
	
	public Double getCurrentLongitude() {
		return longitude;
	}

}
