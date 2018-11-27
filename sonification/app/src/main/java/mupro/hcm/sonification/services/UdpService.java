package mupro.hcm.sonification.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import mupro.hcm.sonification.MainActivity;

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

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.BROADCAST_ACTION);
                broadcastIntent.putExtra("data", new String(msg, 0, dp.getLength()));

                sendBroadcast(broadcastIntent);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
