package mupro.hcm.sonification.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.R;

public class DataService extends Service {

    private final String TAG = "DataService";

    // needed for notification
    NotificationManager notificationManager;
    private static String CHANNEL_ID = "1338";
    private static int FOREGROUND_ID = 1337;
    private String notificationTitle = "Sonification";

    private LocationDataReceiver locationDataReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        locationDataReceiver = new LocationDataReceiver(new Handler());
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        Log.i(TAG, "onCreate");
    }

    public void doStuff() {
        Intent intent = new Intent(DataService.this, FusedLocationProviderService.class);
        intent.putExtra("receiver", locationDataReceiver);
        startService(intent);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LocationDataReceiver extends ResultReceiver {

        public LocationDataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case FusedLocationProviderService.LOCATION_ERROR:
                    break;

                case FusedLocationProviderService.LOCATION_SUCCESS:
                    double longitude = resultData.getDouble("longitude");
                    double latitude = resultData.getDouble("latitude");
                    Log.i(TAG, "Long: " + longitude + "; Lat: " + latitude);
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Sonification", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(FOREGROUND_ID, mBuilder.build());
        }

        return (mBuilder.build());
    }
}
