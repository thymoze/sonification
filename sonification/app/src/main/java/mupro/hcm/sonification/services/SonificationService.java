package mupro.hcm.sonification.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import mupro.hcm.sonification.database.SensorData;
import mupro.hcm.sonification.sensors.Sensor;
import mupro.hcm.sonification.sensors.SensorDataReceiver;
import mupro.hcm.sonification.utils.Direction;
import mupro.hcm.sonification.utils.SoundQueue;

import static mupro.hcm.sonification.MainActivity.ACTION_BROADCAST;

public class SonificationService extends Service {

    private final String TAG = getClass().getName();
    private int counter;
    private SensorDataReceiver mSensorDataReceiver;
    private ArrayList<List<Double>> medianLists;
    private double oldMedians[];
    private double currentMedians[];
    private SoundQueue soundQueue;
    private final int PERCENTAGE = 10;
    private final int NUM_SAMPLES = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        counter = 0;
        medianLists = new ArrayList<>();
        oldMedians = new double[Sensor.values().length];
        currentMedians = new double[Sensor.values().length];
        soundQueue = new SoundQueue(getBaseContext());

        // add a list for each sensor
        Arrays.stream(Sensor.values()).forEach(e -> medianLists.add(new ArrayList<>()));

        registerReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerReceiver() {
        mSensorDataReceiver = new SensorDataReceiver(this::receivedData);
        IntentFilter intentFilter = new IntentFilter(ACTION_BROADCAST);
        registerReceiver(mSensorDataReceiver, intentFilter);
        Log.i(TAG, "Registered SensorDataReceiver");
    }

    private Void receivedData(SensorData sensorData) {
        handleSonification(sensorData);
        return null;
    }

    private void handleSonification(SensorData sensorData) {
        counter++;
        Log.i(TAG, "Counter: " + counter);
        for (Sensor sensor : Sensor.values()) {
            handleSonificationForSingleSensor(sensor, sensorData);
        }

        // reset counter
        if (counter > NUM_SAMPLES && counter % NUM_SAMPLES == 0) {
            counter = NUM_SAMPLES;
        }
    }

    private void handleSonificationForSingleSensor(Sensor sensor, SensorData sensorData) {
        int sensorIndex = sensor.ordinal();

        // add sensorData to the median list
        List<Double> mediansOfCurrentSensor = medianLists.get(sensorIndex);
        mediansOfCurrentSensor.add(sensorData.get(sensor));

        // Log.i(TAG, "Medians size: " + mediansOfCurrentSensor.size());

        if (counter == NUM_SAMPLES) { // took first NUM_SAMPLES samples to calculate first median
            Collections.sort(mediansOfCurrentSensor);
            oldMedians[sensorIndex] = mediansOfCurrentSensor.get(NUM_SAMPLES / 2);
            mediansOfCurrentSensor.clear();
        } else if (counter > NUM_SAMPLES && counter % NUM_SAMPLES == 0) { // took NUM_SAMPLES samples for comparison with old median
            Collections.sort(mediansOfCurrentSensor);
            currentMedians[sensorIndex] = mediansOfCurrentSensor.get(NUM_SAMPLES / 2);

            // Log.i(TAG, "Old median: " + oldMedians[sensorIndex]);
            // Log.i(TAG, "Current median:" + currentMedians[sensorIndex]);
            // Log.i(TAG, "Difference current / old: " + ((currentMedians[sensorIndex] / oldMedians[sensorIndex]) - 1f));

            // play sound if median increases by PERCENTAGE
            if (currentMedians[sensorIndex] / oldMedians[sensorIndex] > (1f + PERCENTAGE / 100f)) {
                // play up sound
                oldMedians[sensorIndex] = currentMedians[sensorIndex];
                soundQueue.playSoundForSensorWithDirection(sensor, Direction.UP);
            } else if (oldMedians[sensorIndex] / currentMedians[sensorIndex] > (1f + PERCENTAGE / 100f)) {
                // play down sound
                oldMedians[sensorIndex] = currentMedians[sensorIndex];
                soundQueue.playSoundForSensorWithDirection(sensor, Direction.DOWN);
            }

            // clear median list for next sample set
            mediansOfCurrentSensor.clear();

            // set the new median values
            oldMedians[sensorIndex] = currentMedians[sensorIndex];
        }
    }
}
