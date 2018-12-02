package mupro.hcm.sonification.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Instant;

import mupro.hcm.sonification.MainActivity;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.fragments.MapFragment;
import mupro.hcm.sonification.helpers.FusedLocationProvider;
import mupro.hcm.sonification.helpers.SensorDataHelper;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class UdpService extends IntentService {

    private static final String TAG = "UdpService";
    private static final int PORT = 7777;

    private boolean running = false;

    public UdpService() {
        super("UdpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!running) {
            Log.i(TAG, "Starting UdpService.");
            running = true;
            runServer(intent);
        } else {
            running = false;
        }
    }

    private void runServer(Intent intent) {
        byte[] msg = new byte[4096];
        DatagramPacket dp = new DatagramPacket(msg, msg.length);

        try (DatagramSocket ds = new DatagramSocket(PORT)) {
            Log.i(TAG, "Listening on port " + PORT);
            while (running) {
                ds.receive(dp);
                Log.i(TAG, "Received object.");

                // add location
                FusedLocationProvider.requestSingleUpdate(UdpService.this, (location -> {
                    Log.i(TAG, "Got Location");

                    try {
                        JSONObject data = new JSONObject(new String(msg, 0, dp.getLength()));
                        SensorData sensorData = SensorDataHelper.createSensorDataObjectFromValues(location, data);
                        sensorData.setTimestamp(Instant.now().toString());

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MainActivity.BROADCAST_ACTION);
                        broadcastIntent.putExtra("data", sensorData);

                        sendBroadcast(broadcastIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
