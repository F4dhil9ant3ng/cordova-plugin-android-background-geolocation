package com.geolocation.geolocationplugin;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import java.util.ArrayDeque;
import java.util.Deque;

public class GeoLocService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private ServiceHandler mServiceHandler;
    private SQLiteDatabase sqLiteDatabase;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static long newRowId = 0;
    private long baseInterval, baseFastInterval, interval, fastInterval;
    private static int sensorThrottle =0, countMag=0, countDist=0;
    private boolean mRequestingLocationUpdates;
    private LocationsDbHelper locationsDbHelper;
    private Intent batteryStatus;
    private Deque deque;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int batteryThresholds[], batteryThrottles[], speedThresholds[], speedThrottles[];

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addConnectionCallbacks(GeoLocService.this)
                        .addOnConnectionFailedListener(GeoLocService.this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
                mRequestingLocationUpdates = true;
            }
            deque = new ArrayDeque(11);
        }
    }

    @Override
    public void onCreate() {

        locationsDbHelper = new LocationsDbHelper(GeoLocService.this);

//        android.os.Debug.waitForDebugger();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = GeoLocService.this.registerReceiver(null, intentFilter);

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sqLiteDatabase = locationsDbHelper.getWritableDatabase();

        this.baseInterval = this.interval = intent.getLongExtra("baseInterval", 1L);
        this.baseFastInterval = this.fastInterval = intent.getLongExtra("baseFastInterval", 1L);
        this.batteryThresholds = intent.getIntArrayExtra("batteryThresholds");
        this.batteryThrottles = intent.getIntArrayExtra("batteryThrottles");
        this.speedThresholds = intent.getIntArrayExtra("speedThresholds");
        this.speedThrottles = intent.getIntArrayExtra("speedThrottles");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        if(mGoogleApiClient.isConnected()){
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        if(mSensor != null){
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing as of now
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float linear_acceleration[] = new float[2];
        float acc_mag;
        linear_acceleration[0] = event.values[0];
        linear_acceleration[1] = event.values[2];
        acc_mag = (float) Math.sqrt(Math.pow(linear_acceleration[0],2)+Math.pow(linear_acceleration[1],2));
        checkAccData(acc_mag);
    }

    protected void checkAccData(float accMag){
        if((int)accMag > 0){
            sensorThrottle++;
            if(sensorThrottle == 10){
                sensorThrottle=0;
                countMag++;
            }
        } else {
            countMag = 0;
        }
        if(countMag == 5){
            sensorThrottle = 0;
            startLocationUpdates();
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error connecting to network. Check connection and try again.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Please connect to internet and try again!", Toast.LENGTH_LONG).show();
    }

    public static void setNewRowId(long Id){
        newRowId = Id;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Toast.makeText(this, "Location Update Started", Toast.LENGTH_LONG).show();
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        float batteryPct = checkBattery();

        double mCurrentLatitude = location.getLatitude();
        double mCurrentLongitude = location.getLongitude();
        int mDistance;

        DistanceCalculator distanceCalculator = new DistanceCalculator();
        String[] projection = {
                LocationsDbContract.Locations._ID,
                LocationsDbContract.Locations.COLUMN_NAME_LONGITUDE,
                LocationsDbContract.Locations.COLUMN_NAME_LATITUDE,
                LocationsDbContract.Locations.COLUMN_NAME_DISTANCE
        };
        if(newRowId==0){
            mDistance = 0;
            countDist=0;
        } else {
            Cursor cursor = sqLiteDatabase.query(LocationsDbContract.Locations.TABLE_NAME,projection, null,null,null,null,null);
            cursor.moveToPosition((int)newRowId-1);
            mDistance = (int) (distanceCalculator.distance(Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(1)), mCurrentLatitude, mCurrentLongitude, "ft")+0.5);
            int speed = checkSpeed(mDistance);
            // int [] batteryThresholds = {10, 20, 30, 50};
            // int [] speedThresholds = {10, 20, 30, 40};
            // int [] batteryIntFact = {100, 40, 30, 20, 10};
            // int [] speedIntFact = {100, 50, 40, 30, 20};
            setThrottle(speed, (int)batteryPct, this.speedThresholds, this.batteryThresholds, this.speedThrottles, this.batteryThrottles);
            if(mDistance<=50){
                if(deque.size()>=10){
                    deque.pollFirst();
                    deque.addLast(mDistance);
                } else {
                    deque.addLast(mDistance);
                }
                countDist++;
            } else {
                if(deque.size()>=10){
                    int avg=0;
                    deque.pollFirst();
                    deque.addLast(mDistance);
                    for(Object object : deque){
                        avg+=Integer.parseInt(object.toString());
                    }
                    avg/=10;
                    if(avg>50){
                        countDist=0;
                    }
                } else {
                    deque.addLast(mDistance);
                    countDist++;
                }
            }
            if(countDist==10){
                countDist=0;
                mSensorManager.registerListener(this, mSensor, 60000000);
                stopLocationUpdates();
            }
            cursor.close();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationsDbContract.Locations.COLUMN_NAME_LATITUDE, String.valueOf(location.getLatitude()));
        contentValues.put(LocationsDbContract.Locations.COLUMN_NAME_LONGITUDE, String.valueOf(location.getLongitude()));
        contentValues.put(LocationsDbContract.Locations.COLUMN_NAME_DISTANCE, String.valueOf(mDistance));
        newRowId = sqLiteDatabase.insert(LocationsDbContract.Locations.TABLE_NAME, null, contentValues);
        Toast.makeText(this,
                "Latitude: " + String.valueOf(location.getLatitude()) + " Longitude: "
                        + String.valueOf(location.getLongitude()) + "DB Entry: " + newRowId,
                Toast.LENGTH_SHORT).show();
    }

    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        Toast.makeText(this, "Location Update Stopped :)", Toast.LENGTH_SHORT).show();
        mRequestingLocationUpdates = false;
    }

    protected float checkBattery(){
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level / (float) scale)*100;
    }

    protected void setInterval(){
        mLocationRequest.setInterval((long)this.interval);
        mLocationRequest.setInterval((long)this.fastInterval);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        setInterval();
    }

    protected int checkSpeed(float mDistance){
        double timePeriod = this.interval/1000;
        double distance = mDistance/3.28084;
        return (int) (distance/timePeriod);
    }

    protected void setThrottle(int curSpeed, int curBat, int [] speedThresholds, int [] batLevels, int [] spIntPercent, int [] batIntPercent){
        int batIndex=-1, speedIndex = -1, i=0, j=0;
        if(curBat < batLevels[0]){
            batIndex = 0;
        } else {
            for(i=0; i<batLevels.length-1; i++){
                if(curSpeed>=batLevels[i] && curSpeed<batLevels[i+1]){
                    batIndex = i;
                    break;
                }
            }
            if(i == batLevels.length-1){
                batIndex = batLevels.length;
            }
        }
        if(curSpeed < speedThresholds[0]){
            speedIndex = 0;
        }
        else{
            for(j=0; j<speedThresholds.length-1; j++){
                if(curSpeed>=speedThresholds[i] && curSpeed<speedThresholds[i+1]){
                    speedIndex = i;
                    break;
                }
            }
            if(j == speedThresholds.length-1){
                speedIndex = speedThresholds.length;
            }
        }
        float throttle = (float)(spIntPercent[speedIndex] + batIntPercent[batIndex])/(float)200;
        this.interval = (long)((float)this.baseInterval + (float) this.baseInterval * throttle);
        this.fastInterval = (long)((float)this.baseFastInterval + (float)this.baseFastInterval * throttle);
        setInterval();
    }

    @Override
    public boolean stopService(Intent intent) {
        Log.i("GeoLocService", "- Received stop: " + intent);
        return super.stopService(intent);
    }
}
