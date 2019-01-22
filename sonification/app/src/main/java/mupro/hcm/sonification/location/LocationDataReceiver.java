package mupro.hcm.sonification.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.function.Function;

import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.sensors.Sensor;

import static mupro.hcm.sonification.MainActivity.EXTRA_SENSORDATA;

public class LocationDataReceiver extends BroadcastReceiver {

    private static final String TAG = LocationDataReceiver.class.getName();
    double lastLatitude = 0;
    double lastLongitude;
    double distance = 0;

    public LocationDataReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorData data = (SensorData) intent.getSerializableExtra(EXTRA_SENSORDATA);
        Log.i(TAG, "Received LocationData");

        if(lastLatitude == 0) {
            lastLatitude = data.getLatitude();
            lastLongitude = data.getLongitude();
        }

        distance = distance + calcDistance(lastLatitude, lastLongitude, data.getLatitude(), data.getLongitude());
        Log.i(TAG, "New distance: " + distance);

        AsyncTask.execute(() -> {
            AppDatabase.getDatabase(context).dataSetDao().setDistanceforId(distance, data.getDataSetId());
            Log.i(TAG, "Saved distance to db");
        });

        lastLatitude = data.getLatitude();
        lastLongitude = data.getLongitude();
    }

    public void calcDistanceDB(Context context, long id) {
        AsyncTask.execute(() -> {
            double distance = 0;
            List<SensorData> sensorData = AppDatabase.getDatabase(context).sensorDataDao().getSensorDataForDataSet(id);

            if (!sensorData.isEmpty()) {
                SensorData a = sensorData.get(0);

                for (SensorData b : sensorData) {
                    distance += calcDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
                    a = b;
                }
            }

            AppDatabase.getDatabase(context).dataSetDao().setDistanceforId(distance, id);
        });
    }

    private double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6371.01;

        double lat = Math.toRadians(lat2 - lat1);
        double lon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lon / 2) * Math.sin(lon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = Math.round((radius * c) * 1000) / 1000.0;

        return Math.abs(d);
    }
}