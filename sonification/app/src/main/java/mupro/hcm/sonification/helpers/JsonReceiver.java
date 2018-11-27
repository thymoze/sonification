package mupro.hcm.sonification.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

public class JsonReceiver extends BroadcastReceiver {

    private static final String TAG = "JsonReceiver";

    private Function<JSONObject, Void> callback;

    public JsonReceiver(Function<JSONObject, Void> function) {
        callback = function;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String dataAsString = intent.getStringExtra("data");
        try {
            Log.i(TAG, dataAsString);
            JSONObject data = new JSONObject(dataAsString);
            callback.apply(data);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}