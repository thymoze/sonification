package mupro.hcm.sonification.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.location.LocationDataReceiver;

import static mupro.hcm.sonification.MainActivity.ACTION_BROADCAST;
import static mupro.hcm.sonification.MainActivity.CURRENT_DATASET;
import static mupro.hcm.sonification.MainActivity.EXTRA_SENSORDATA;
import static mupro.hcm.sonification.MainActivity.EXTRA_UDPRECEIVER;

public class DataService extends Service {
    private static final String TAG = DataService.class.getName();

    // Update interval for location requests
    private static final long UPDATE_INTERVAL = 3000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    // Channel name and id for the foreground service notification
    private static final String CHANNEL_ID = "channel_01";
    private static final int NOTIFICATION_ID = 1337;


    private NotificationManager mNotificationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private Location mLocation;

    private UdpDataReceiver mUdpDataReceiver;
    private LocationDataReceiver mLocationDataReceiver;

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i(TAG, "Location updated");
                mLocation = locationResult.getLastLocation();
            }
        };

        createLocationRequest();
        getLastLocation();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mChannel);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mUdpDataReceiver = new UdpDataReceiver(new Handler(handlerThread.getLooper()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started.");
        startForeground(NOTIFICATION_ID, getNotification());

        try {
            Log.i(TAG, "Requesting location updates");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }

        startSonification();
        startUdpReceiver();

        IntentFilter intentFilter = new IntentFilter(ACTION_BROADCAST);
        mLocationDataReceiver = new LocationDataReceiver();
        registerReceiver(mLocationDataReceiver, intentFilter);
        Log.i(TAG, "Registered LocationDataReceiver");

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }

        stopUdpReceiver();
        stopSonification();

        if (mLocationDataReceiver != null) {
            unregisterReceiver(mLocationDataReceiver);
        }

        mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
    }

    private void startSonification() {
        Log.i(TAG, "Starting Sonification Service");
        final Intent intent = new Intent(DataService.this, SonificationService.class);
        startService(intent);
    }

    private void stopSonification() {
        Log.i(TAG, "Stopping Sonification Service");
        final Intent intent = new Intent(DataService.this, SonificationService.class);
        stopService(intent);
    }

    private void startUdpReceiver() {
        Log.i(TAG, "Starting Udp Service");
        Intent udpIntent = new Intent(DataService.this, UdpService.class);
        udpIntent.putExtra(EXTRA_UDPRECEIVER, mUdpDataReceiver);
        startService(udpIntent);
    }

    private void stopUdpReceiver() {
        Log.i(TAG, "Stopping Udp Service");
        Intent udpIntent = new Intent(DataService.this, UdpService.class);
        stopService(udpIntent);
    }

    private long saveDataToDatabase(SensorData sensorData) {
        long dataSetId = PreferenceManager.getDefaultSharedPreferences(this).getLong(CURRENT_DATASET, -1);

        Log.i(TAG, "Saving to database...");
        if (dataSetId != -1) {
            if (sensorData != null) {
                sensorData.setDataSetId(dataSetId);
                long sensorId = AppDatabase.getDatabase(getApplicationContext()).sensorDataDao().insert(sensorData);
                Log.i(TAG, "Saved " + sensorId + " to database...");
                return sensorId;
            }
        }
        return -1;
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content))
                .setOngoing(true)
                .setSmallIcon(R.drawable.music_note)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                        } else {
                            Log.w(TAG, "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private class UdpDataReceiver extends ResultReceiver {

        UdpDataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            SensorData data = ((SensorData) resultData.getSerializable("sensorData"));
            Log.i(TAG, "Received: " + data.getTimestamp());

            Toast.makeText(DataService.this, "Data received!", Toast.LENGTH_SHORT).show();

            if (mLocation == null) {
                // just drop the data if there is no location yet...
                return;
            }

            data.setLatitude(mLocation.getLatitude());
            data.setLongitude(mLocation.getLongitude());

            AsyncTask.execute(() -> {
                long id = saveDataToDatabase(data);
                data.setId(id);

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_BROADCAST);
                broadcastIntent.putExtra(EXTRA_SENSORDATA, data);

                Log.i(TAG, "Sending broadcast for " + id);
                sendBroadcast(broadcastIntent);
            });
        }
    }
}
