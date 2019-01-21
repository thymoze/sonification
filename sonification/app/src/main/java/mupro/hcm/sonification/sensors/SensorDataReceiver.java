package mupro.hcm.sonification.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.function.Function;

import mupro.hcm.sonification.database.SensorData;

import static mupro.hcm.sonification.MainActivity.EXTRA_SENSORDATA;

public class SensorDataReceiver extends BroadcastReceiver {

    private static final String TAG = SensorDataReceiver.class.getName();

    private Function<SensorData, Void> callback;

    public SensorDataReceiver(Function<SensorData, Void> function) {
        callback = function;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorData data = (SensorData) intent.getSerializableExtra(EXTRA_SENSORDATA);
        Log.i(TAG, "Received SensorData");
        callback.apply(data);
    }
}