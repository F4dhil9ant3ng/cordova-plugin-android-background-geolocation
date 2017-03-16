package com.geolocation.geolocationplugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class GeoLocationPlugin extends CordovaPlugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isConnected=false, isEnabled=false, stopActivityOnTerminate=false;
    private Intent intent;
    private JSONArray jsonArray;
    private long baseInterval, baseFastInterval;
    private int batteryThresholds[], batteryThrottles[], speedThresholds[], speedThrottles[];


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException{
        Context context = this.cordova.getActivity().getApplicationContext();
        intent = new Intent(context, GeoLocService.class);
        if("getLocation".equals(action)){
            getLocation(args.getString(0), callbackContext);
            return true;
        }
        if("checkPermission".equals(action)){
            checkPermission(args.getString(0), callbackContext);
            return true;
        }
        if("startService".equals(action)){
            startServiceMain(args.getString(0), callbackContext);
            return true;
        }
        if("stopService".equals(action)){
            stopServiceMain(args.getString(0), callbackContext);
            return true;
        }
        if("configure".equals(action)){
            configure(args, callbackContext);
            return true;
        }
        if("showDB".equals(action)){
            showDB(args.getString(0), callbackContext);
        }
        return false;
    }

    private void getLocation(String msg, CallbackContext callbackContext){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.cordova.getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        if(isConnected){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Toast.makeText(webView.getContext(), "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + " Longitude: " + String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
            }           
        }
        
    }

    private void checkPermission(String msg, CallbackContext callbackContext){
         if (ContextCompat.checkSelfPermission(this.cordova.getActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this.cordova.getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        ActivityCompat.requestPermissions(this.cordova.getActivity(),
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                11);
                        callbackContext.success("Permission needed.");

                    } else {
                        ActivityCompat.requestPermissions(this.cordova.getActivity(),
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                11);
                        callbackContext.success("Permission needed.");
                    }
            }
         callbackContext.error("Permission not granted");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(webView.getContext(), "Error connecting to network.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!isConnected){
            isConnected = true;
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Toast.makeText(webView.getContext(), "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + " Longitude: " + String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(webView.getContext(), "Please check internet connection.", Toast.LENGTH_SHORT).show();
    }

    private void startServiceMain(String msg,  CallbackContext callbackContext){

        intent.putExtra("baseInterval", this.baseInterval);
        intent.putExtra("baseFastInterval", this.baseFastInterval);
        Toast.makeText(webView.getContext(), "baseInterval: "+ String.valueOf(this.baseInterval), Toast.LENGTH_SHORT).show();
        intent.putExtra("batteryThresholds", this.batteryThresholds);
        intent.putExtra("batteryThrottles", this.batteryThrottles);
        intent.putExtra("speedThresholds", this.speedThresholds);
        intent.putExtra("speedThrottles", this.speedThrottles);

        isEnabled = true;
        this.cordova.getActivity().startService(intent);
    }

    private void stopServiceMain(String msg, CallbackContext callbackContext){
        this.cordova.getActivity().stopService(intent);
    }

    private void configure(JSONArray jsonArray, CallbackContext callbackContext) throws JSONException{
        try{
            this.baseInterval = Integer.parseInt(jsonArray.getString(0));
            this.baseFastInterval = Integer.parseInt(jsonArray.getString(1));
            JSONArray reusableArray = jsonArray.getJSONArray(2);
            this.batteryThresholds = new int[reusableArray.length()];
            for(int i=0; i<batteryThresholds.length; i++){
                this.batteryThresholds[i] = reusableArray.optInt(i);
            }
            reusableArray = jsonArray.getJSONArray(3);
            this.batteryThrottles = new int[reusableArray.length()];
            for(int i=0; i<batteryThrottles.length; i++){
                this.batteryThrottles[i] = reusableArray.optInt(i);
            }
            reusableArray = jsonArray.getJSONArray(4);
            this.speedThresholds = new int[reusableArray.length()];
            for(int i=0; i<speedThresholds.length; i++){
                this.speedThresholds[i] = reusableArray.optInt(i);
            }
            reusableArray = jsonArray.getJSONArray(5);
            this.speedThrottles = new int[reusableArray.length()];
            for(int i=0; i<speedThrottles.length; i++){
                this.speedThrottles[i] = reusableArray.optInt(i);
            }
            callbackContext.success("configure working.");
        } catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("JSON Exception: " + e.getMessage());
        }
    }

    private void showDB(String msg, CallbackContext callbackContext){
        Context context = this.cordova.getActivity().getApplicationContext();
        Intent intentDB = new Intent(context, DBActivity.class);
        this.cordova.getActivity().startActivity(intentDB);
    }


    public void onDestroy() {
        Activity activity = this.cordova.getActivity();

        if(isEnabled && stopActivityOnTerminate) {
            activity.stopService(intent);
        }
    }

}