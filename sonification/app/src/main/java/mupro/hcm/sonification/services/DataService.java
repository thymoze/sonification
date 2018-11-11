package mupro.hcm.sonification.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.database.SensorDataHelper;

import static mupro.hcm.sonification.MainActivity.BROADCAST_ACTION;

public class DataService extends Service {

    private final String TAG = "DataService";

    // needed for notification
    NotificationManager notificationManager;
    private static String CHANNEL_ID = "1338";
    private static int FOREGROUND_ID = 1337;
    private String notificationTitle = "Sonification";

    private double longitude = 0;
    private double latitude = 0;
    private JSONObject data = new JSONObject();

    private LocationDataReceiver locationDataReceiver;
    private JsonReceiver jsonReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        locationDataReceiver = new LocationDataReceiver(new Handler());
        jsonReceiver = new JsonReceiver();
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        getCurrentPosition();
        Log.i(TAG, "onCreate");
    }

    public void startReceivingData() {
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(jsonReceiver, intentFilter);
        Intent intent = new Intent(DataService.this, UdpService.class);
        startService(intent);
    }

    private void getCurrentPosition() {
        Intent intent = new Intent(DataService.this, FusedLocationProviderService.class);
        intent.putExtra("receiver", locationDataReceiver);
        startService(intent);
    }

    private void saveToDatabase() {
        SensorData sensorData = SensorDataHelper.createSensorDataObjectFromValues(longitude, latitude, data);
        if (sensorData != null)
            AppDatabase.getDatabase(getApplicationContext()).sensorDataDao().insertAll(sensorData);
    }

    private Notification buildForegroundNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentTitle(notificationTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("The Sonification App is currently running in the background and tracking your position."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);


        notificationManager = getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Sonification", NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(FOREGROUND_ID, mBuilder.build());

        return (mBuilder.build());
    }

    private class JsonReceiver extends BroadcastReceiver {

        private static final String TAG = "JsonReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String dataAsString = intent.getStringExtra("data");
            try {
                Log.i(TAG, dataAsString);
                setData(new JSONObject(dataAsString));
                getCurrentPosition();
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private class LocationDataReceiver extends ResultReceiver {

        private static final String TAG = "LocationReceiver";

        public LocationDataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case FusedLocationProviderService.LOCATION_ERROR:
                    break;

                case FusedLocationProviderService.LOCATION_SUCCESS:
                    setLongitude(resultData.getDouble("longitude"));
                    setLatitude(resultData.getDouble("latitude"));
                    Log.i(TAG, "Long: " + longitude + "; Lat: " + latitude);
                    saveToDatabase();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}

