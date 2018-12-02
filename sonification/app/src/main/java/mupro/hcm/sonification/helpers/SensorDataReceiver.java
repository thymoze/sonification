package mupro.hcm.sonification.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

import mupro.hcm.sonification.database.SensorData;

public class SensorDataReceiver extends BroadcastReceiver {

    private static final String TAG = "SensorDataReceiver";

    private Function<SensorData, Void> callback;

    public SensorDataReceiver(Function<SensorData, Void> function) {
        callback = function;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorData data = ((SensorData) intent.getSerializableExtra("data"));
        Log.i(TAG, data.getTimestamp());
        callback.apply(data);
    }
}