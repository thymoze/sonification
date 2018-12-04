package mupro.hcm.sonification.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.R;
import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.helpers.FusedLocationProvider;

public class DataService extends Service {

    private final String TAG = "DataService";

    // needed for notification
    NotificationManager notificationManager;
    private static String CHANNEL_ID = "1338";
    private static int FOREGROUND_ID = 1337;
    private String notificationTitle = "Sonification";
    private UdpDataReceiver udpDataReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        Log.i(TAG, "onCreate");
        startReceivingData();
    }

    public void startReceivingData() {
        udpDataReceiver = new UdpDataReceiver(new Handler());
        Intent intent = new Intent(DataService.this, UdpService.class);
        intent.putExtra("receiver", udpDataReceiver);
        startService(intent);
    }

    private long saveDataToDatabase(SensorData sensorData) {
        if (sensorData != null)
            return AppDatabase.getDatabase(getApplicationContext()).sensorDataDao().insert(sensorData);
        else
            return -1;
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

    private class UdpDataReceiver extends ResultReceiver {

        private static final String TAG = "LocationReceiver";

        UdpDataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            SensorData data = ((SensorData) resultData.getSerializable("sensorData"));
            Log.i(TAG, "Received: " + data.getTimestamp());

            FusedLocationProvider.requestSingleUpdate(DataService.this, (callback -> {
                Log.i(TAG, "Got location");

                data.setLatitude(callback.latitude);
                data.setLongitude(callback.longitude);

                long id = saveDataToDatabase(data);
                data.setId(id);

                Toast.makeText(DataService.this, "Data received!\n" + data.toString(), Toast.LENGTH_LONG).show();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.BROADCAST_ACTION);
                broadcastIntent.putExtra("sensorData", data);

                sendBroadcast(broadcastIntent);
            }));

            super.onReceiveResult(resultCode, resultData);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}

