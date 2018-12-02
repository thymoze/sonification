package mupro.hcm.sonification.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
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
    private ResultReceiver receiver;

    private final int LOCATION_SUCCESS = 1;
    private final int LOCATION_ERROR = 2;

    private boolean running = false;

    public UdpService() {
        super("UdpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = intent.getParcelableExtra("receiver");

        if (!running) {
            Log.i(TAG, "Starting UdpService.");
            running = true;
            runServer();
        } else {
            running = false;
        }
    }

    private void runServer() {
        byte[] msg = new byte[4096];
        DatagramPacket dp = new DatagramPacket(msg, msg.length);

        try (DatagramSocket ds = new DatagramSocket(PORT)) {
            Log.i(TAG, "Listening on port " + PORT);
            while (running) {
                ds.receive(dp);
                Log.i(TAG, "Received object.");

                try {
                    JSONObject data = new JSONObject(new String(msg, 0, dp.getLength()));
                    SensorData sensorData = SensorDataHelper.createSensorDataObjectFromValues(null, data);
                    sensorData.setTimestamp(Instant.now().toString());

                    returnData(sensorData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void returnData(SensorData data) {
        if (data != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("sensorData", data);
            receiver.send(LOCATION_SUCCESS, bundle);
        } else {
            receiver.send(LOCATION_ERROR, Bundle.EMPTY);
        }
    }
}
