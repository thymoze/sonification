package mupro.hcm.sonification.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.function.Function;

import mupro.hcm.sonification.database.AppDatabase;
import mupro.hcm.sonification.database.DataSet;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.sensors.Sensor;

public class LocationDataReceiver extends BroadcastReceiver {

    private static final String TAG = LocationDataReceiver.class.getName();
    double lastLatitude = 0;
    double lastLongitude;
    double distance = 0;

    private Function<SensorData, Void> callback;

    public LocationDataReceiver(Function<SensorData, Void> function) {
        callback = function;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorData data = (SensorData) intent.getSerializableExtra("sensorData");
        Log.i(TAG, "Received LocationData");

        if(lastLatitude == 0) {
            lastLatitude = data.getLatitude();
            lastLongitude = data.getLongitude();
        }

        distance = distance + calcDistance(lastLatitude, lastLongitude, data.getLatitude(), data.getLongitude());

        AppDatabase.getDatabase(context).dataSetDao().setDistanceforId(distance, data.getId());

        lastLatitude = data.getLatitude();
        lastLongitude = data.getLongitude();

        callback.apply(data);
    }

    private void calcDistanceDB(Context context, int id) {
        double distance = 0;
        SensorData a;
        SensorData b;
        List<SensorData> sensorData = AppDatabase.getDatabase(context).sensorDataDao().getSensorDataForDataSet(id);
        a = sensorData.get(0);

        for (SensorData element : sensorData) {
            b = element;
            distance += calcDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
            a = element;
        }

        AppDatabase.getDatabase(context).dataSetDao().setDistanceforId(distance, id);
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